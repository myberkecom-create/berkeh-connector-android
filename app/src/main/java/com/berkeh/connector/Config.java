package com.berkeh.connector;
import android.content.*;
import java.net.URI;
import java.util.*;
public final class Config {
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("berkeh",0);}
 static String get(Context c,String k){return prefs(c).getString(k,"");}
 static boolean enabled(Context c){return prefs(c).getBoolean("enabled",false);}
 static String secret(Context c)throws Exception{return Crypto.decrypt(get(c,"secret"));}
 static String device(Context c){String id=get(c,"device");if(id.isEmpty()){id=UUID.randomUUID().toString();prefs(c).edit().putString("device",id).commit();}return id;}
 static String normalize(String value)throws Exception{URI u=new URI(value.trim());if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null||u.getQuery()!=null||u.getFragment()!=null)throw new Exception("آدرس کامل HTTPS سایت را بدون پارامتر وارد کنید.");String s=u.toASCIIString();while(s.endsWith("/"))s=s.substring(0,s.length()-1);if(s.contains("/wp-json"))throw new Exception("فقط آدرس اصلی سایت را وارد کنید، نه آدرس API.");return s;}
 static boolean allowed(String sender,String list){if(sender==null)return false;for(String s:list.split("[\n,;،]+"))if(!s.trim().isEmpty()&&sender.trim().equalsIgnoreCase(s.trim()))return true;return false;}
 static boolean otp(String text){String s=text.toLowerCase(Locale.ROOT);return s.contains("رمز")||s.contains("otp")||s.contains("password")||s.contains("verification code")||s.contains("کد ورود");}
}
