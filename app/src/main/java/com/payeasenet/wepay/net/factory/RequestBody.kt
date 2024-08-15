package com.payeasenet.wepay.net.factory

/**
 * Created by chenchaoyong on 2017/4/12.
 */

import java.io.File
import java.io.IOException
import java.nio.charset.Charset

import okhttp3.MediaType
import okhttp3.internal.Util
import okio.BufferedSink
import okio.ByteString
import okio.Okio
import okio.Source

abstract class RequestBody {
    /** Returns the Content-Type header for this body.  */
    abstract fun contentType(): MediaType

    /**
     * Returns the number of bytes that will be written to `out` in a call to [.writeTo],
     * or -1 if that count is unknown.
     */
    @Throws(IOException::class)
    fun contentLength(): Long {
        return -1
    }

    /** Writes the content of this request to `out`.  */
    @Throws(IOException::class)
    abstract fun writeTo(sink: BufferedSink)

    companion object {

        /**
         * Returns a new request body that transmits `content`. If `contentType` is non-null
         * and lacks a charset, this will use UTF-8.
         */
        fun create(contentType: MediaType?, content: String): okhttp3.RequestBody {
            var contentType1 = contentType
            var charset: Charset? = Util.UTF_8
            if (contentType != null) {
                charset = contentType.charset()
                if (charset == null) {
                    charset = Util.UTF_8
                    contentType1 = MediaType.parse(contentType.toString() + "")
                }
            }
            val bytes = content.toByteArray(charset!!)
            return create(contentType1, bytes)
        }

        /** Returns a new request body that transmits `content`.  */
        fun create(contentType: MediaType, content: ByteString): okhttp3.RequestBody {
            return object : okhttp3.RequestBody() {
                override fun contentType(): MediaType? {
                    return contentType
                }

                @Throws(IOException::class)
                override fun contentLength(): Long {
                    return content.size().toLong()
                }

                @Throws(IOException::class)
                override fun writeTo(sink: BufferedSink) {
                    sink.write(content)
                }
            }
        }

        /** Returns a new request body that transmits `content`.  */
        @JvmOverloads
        fun create(contentType: MediaType?, content: ByteArray?,
                   offset: Int = 0, byteCount: Int = content!!.size): okhttp3.RequestBody {
            if (content == null) throw NullPointerException("content == null")
            Util.checkOffsetAndCount(content.size.toLong(), offset.toLong(), byteCount.toLong())
            return object : okhttp3.RequestBody() {
                override fun contentType(): MediaType? {
                    return contentType
                }

                override fun contentLength(): Long {
                    return byteCount.toLong()
                }

                @Throws(IOException::class)
                override fun writeTo(sink: BufferedSink) {
                    sink.write(content, offset, byteCount)
                }
            }
        }

        /** Returns a new request body that transmits the content of `file`.  */
        fun create(contentType: MediaType, file: File?): okhttp3.RequestBody {
            if (file == null) throw NullPointerException("content == null")

            return object : okhttp3.RequestBody() {
                override fun contentType(): MediaType? {
                    return contentType
                }

                override fun contentLength(): Long {
                    return file.length()
                }

                @Throws(IOException::class)
                override fun writeTo(sink: BufferedSink) {
                    var source: Source? = null
                    try {
                        source = Okio.source(file)
                        sink.writeAll(source!!)
                    } finally {
                        Util.closeQuietly(source)
                    }
                }
            }
        }
    }
}
