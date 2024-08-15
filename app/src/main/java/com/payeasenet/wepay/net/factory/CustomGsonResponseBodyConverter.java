package com.payeasenet.wepay.net.factory;


import android.text.TextUtils;

import com.ehking.sdk.wepay.net.exception.ResultException;
import com.ehking.sdk.wepay.utlis.LogUtil;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * /**
 * 类 CustomGsonResponseBodyConverter 简介
 * <p>
 * 描述：自定义的网络响应体适配器
 * </p>
 * 创建日期：2017年04月10日
 *
 * @author zhaoyong.chen@ehking.com
 * @version 1.0
 */

//CustomGsonResponseBodyConverter.java
final class CustomGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final Gson gson;
    private final TypeAdapter<T> adapter;

    CustomGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;

    }

    @Override
    public T convert(ResponseBody value) throws IOException {

        String code = "200";
        String msg = "";

        String response = value.string();
        LogUtil.d("msg:" + response);

        try {
            if(response.contains("{") && response.contains("}") ){
                response = response.substring(response.indexOf("{"),response.lastIndexOf("}")+1);
            }
            JSONObject decodeObj = new JSONObject(response);



            if (decodeObj.has("status")) {
                if ("SUCCESS".equals(decodeObj.getString("status"))) {
                    code = "200";
                    if(decodeObj.has("code") && "EJ0000117".equals(decodeObj.getString("code"))){
                        code = decodeObj.getString("code");
                    }
                    if(decodeObj.has("code") && "EJ0000214".equals(decodeObj.getString("code"))){
                        code = decodeObj.getString("code");
                    }

                }else{
                    if (decodeObj.has("code")) {

                        code = decodeObj.getString("code");
                    }else if (decodeObj.has("errorCode")) {
                        code = decodeObj.getString("errorCode");
                    } else{
                        code = "10000";
                    }
                }

            }
            if (decodeObj.has("message")) {
                msg = decodeObj.getString("message");
            }
            if (decodeObj.has("error")) {
                msg = decodeObj.getString("error");
            }
            if (decodeObj.has("errorMessage")) {
                msg = decodeObj.getString("errorMessage");
            }
            if (decodeObj.has("cause")) {
                msg = decodeObj.getString("cause");
            }


            if (!TextUtils.isEmpty(response) && code == "200") {
                MediaType contentType = value.contentType();
                Charset charset = contentType != null ? contentType.charset(Charset.forName("UTF-8")) : Charset.forName("UTF-8");
                InputStream inputStream = new ByteArrayInputStream(response.getBytes());
                Reader reader = new InputStreamReader(inputStream, charset);
                JsonReader jsonReader = gson.newJsonReader(reader);
                return adapter.read(jsonReader);
            } else {
                throw new ResultException(code, msg);
            }
        } catch (JSONException e) {
            throw new ResultException(code, e.getMessage());
        } catch (MalformedJsonException e) {
            return (T) response;
        } finally {
            value.close();
        }

    }
}
