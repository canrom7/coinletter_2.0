package com.payeasenet.wepay.net.factory;



import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;


/**
* 类 CustomGsonConverterFactory 简介
* <p>
* 描述：Gson解析工厂
* </p>
* 创建日期：2017年04月10日
*
* @author zhaoyong.chen@ehking.com
* @version 1.0
*/

public class CustomGsonConverterFactory extends Converter.Factory {

   public  Gson gson;

   protected CustomGsonConverterFactory(Gson gson) {
       if (gson == null) throw new NullPointerException("gson == null");
       this.gson = gson;
   }
   public static CustomGsonConverterFactory create() {
       return create(new Gson());
   }

   public static CustomGsonConverterFactory create(Gson gson) {
       return new CustomGsonConverterFactory(gson);
   }

   @Override
   public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
       TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
       return new CustomGsonResponseBodyConverter<>(gson, adapter);
   }

   @Override
   public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
       TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
       return new CustomGsonRequestBodyConverter<>(gson, adapter);
   }

}
