package com.payeasenet.wepay.net.factory;



import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okio.Buffer;
import retrofit2.Converter;

/**
 /**
 * 类 CustomGsonRequestBodyConverter 简介
 * <p>
 * 描述：自定义的网络请求体适配器
 * </p>
 * 创建日期：2017年04月10日
 *
 * @author zhaoyong.chen@ehking.com
 * @version 1.0
 */


public final class CustomGsonRequestBodyConverter<T> implements Converter<T, okhttp3.RequestBody> {

    /*public static final MediaType MEDIA_TYPE = MediaType.parse(String.format(
            "application/vnd.ehking.%s-v%s+json", RetrofitClient.Companion.getAPPNAME(), RetrofitClient.Companion.getAPI_VERSION()));*/
    public static final MediaType MEDIA_TYPE=MediaType.parse("application/vnd.ehking-v1.0+json");
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final Gson gson;
    private final TypeAdapter<T> adapter;

    CustomGsonRequestBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public okhttp3.RequestBody convert(T value) throws IOException {

        if (value instanceof String) {
             String str = URLEncoder.encode((String) value, "UTF-8");
            return RequestBody.Companion.create(MEDIA_TYPE, str);
        } else {
            Buffer buffer = new Buffer();
            java.io.Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
            JsonWriter jsonWriter = gson.newJsonWriter(writer);
            adapter.write(jsonWriter, value);
            jsonWriter.close();
            return RequestBody.Companion.create(MEDIA_TYPE, buffer.readByteString());
        }
    }
}