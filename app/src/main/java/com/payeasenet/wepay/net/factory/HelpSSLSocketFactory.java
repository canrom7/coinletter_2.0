package com.payeasenet.wepay.net.factory;

import android.content.Context;

import com.alibaba.fastjson.util.IOUtils;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * 类 <code>${CLASS_NAME}</code>
 * <p>
 * 描述：
 * </p>
 * 创建日期：2018年10月18日
 *
 * @author zhaoyong.chen@ehking.com
 * @version 1.0
 */
public class HelpSSLSocketFactory {

    public static SSLSocketFactory getSSLSocketFactory(Context context, int cer) {
        try {
            // 服务器端需要验证的客户端证书，其实就是客户端的keystore
            KeyStore keyStore = KeyStore.getInstance("BKS");
            // 客户端信任的服务器端证书
            KeyStore trustStore = KeyStore.getInstance("BKS");

            //读取证书
            InputStream ksIn = context.getResources().openRawResource(cer);
            InputStream tsIn =  context.getResources().openRawResource(cer);

            //加载证书
            keyStore.load(ksIn, "1234qwer".toCharArray());
            trustStore.load(tsIn, "1234qwer".toCharArray());
            IOUtils.close(ksIn);
            IOUtils.close(tsIn);

            //初始化SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            trustManagerFactory.init(trustStore);
            keyManagerFactory.init(keyStore, "1234qwer".toCharArray());
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            //通过HttpsURLConnection设置链接
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            return socketFactory;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static SSLSocketFactory getSSLSocketFactory(Context context, int[] certificates) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }

        //CertificateFactory用来证书生成
        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
            //Create a KeyStore containing our trusted CAs
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            for (int i = 0; i < certificates.length; i++) {
                //读取本地证书
                InputStream is = context.getResources().openRawResource(certificates[i]);
                keyStore.setCertificateEntry(String.valueOf(i), certificateFactory.generateCertificate(is));

                if (is != null) {
                    is.close();
                }
            }
            //Create a TrustManager that trusts the CAs in our keyStore
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            //Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();

        } catch (Exception e) {

        }
        return null;
    }


}