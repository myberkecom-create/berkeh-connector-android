package com.berkeh.connector;
import android.content.*;import org.json.*;import javax.net.ssl.HttpsURLConnection;import java.net.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;
public final class Api {
 static JSONObject post(Context ctx,String route,JSONObject data)throws Exception {
  String body=data.toString(),time=Long.toString(System.currentTimeMillis()/1000);
  HttpsURLConnection c=(HttpsURLConnection)new URL(Config.get(ctx,"site")+"/wp-json/berkeh-c2c/v1/"+route).openConnection();
  try{c.setConnectTimeout(3500);c.setReadTimeout(3500);c.setInstanceFollowRedirects(false);c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");c.setRequestProperty("X-BCC2C-Timestamp",time);c.setRequestProperty("X-BCC2C-Signature",Crypto.sign(Config.secret(ctx),time,body));c.setRequestProperty("X-BCC2C-Nonce",UUID.randomUUID().toString());
   byte[] bytes=body.getBytes(StandardCharsets.UTF_8);c.setFixedLengthStreamingMode(bytes.length);try(OutputStream o=c.getOutputStream()){o.write(bytes);}int code=c.getResponseCode();
   if(code<200||code>=300)throw new HttpError(code);
   try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[2048];int n;while((n=in.read(buf))!=-1){out.write(buf,0,n);if(out.size()>262144)throw new IOException("response too large");}JSONObject r=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));if(!r.optBoolean("ok"))throw new IOException("invalid response");return r;}
  }finally{c.disconnect();}
 }
 static JSONObject identity(Context c)throws Exception{return new JSONObject().put("device_id",Config.device(c)).put("app_version","1.0.0");}
 static String explain(Exception e){if(e instanceof HttpError){int n=((HttpError)e).code;if(n==401||n==403)return "خطای دسترسی "+n+"؛ کلید API، ساعت گوشی و فایروال سایت را بررسی کنید.";if(n>=300&&n<400)return "آدرس تغییر مسیر دارد؛ آدرس نهایی HTTPS سایت را ثبت کنید.";if(n==404)return "API پیدا نشد؛ فعال بودن افزونه و آدرس سایت را بررسی کنید.";return "پاسخ سایت: HTTP "+n;}if(e instanceof javax.net.ssl.SSLException)return "گواهی HTTPS سایت معتبر نیست.";return "اتصال برقرار نشد؛ اینترنت، سایت و تنظیمات را بررسی کنید.";}
 static class HttpError extends IOException {final int code;HttpError(int c){super("HTTP "+c);code=c;}}
}
