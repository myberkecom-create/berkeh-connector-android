package com.berkeh.connector;
public class ProtocolTest {
 public static void main(String[] args)throws Exception {
  if(!Config.allowed("+981234", "+981234\nBankX"))throw new AssertionError("exact sender");
  if(Config.allowed("+9812345", "+981234"))throw new AssertionError("partial sender accepted");
  if(Config.allowed("Bank", ""))throw new AssertionError("empty whitelist");
  if(!Config.otp("رمز پویا ۱۲۳۴۵۶")||!Config.otp("Your OTP is 123"))throw new AssertionError("OTP filter");
  if(Config.otp("واریز 100,001 ریال"))throw new AssertionError("deposit filtered");
  if(!Config.normalize("https://example.com/shop/").equals("https://example.com/shop"))throw new AssertionError("subdirectory");
  for(String s:new String[]{"http://example.com", "https://user:pass@example.com", "https://example.com/?token=x", "https://example.com/wp-json"}) {boolean rejected=false;try{Config.normalize(s);}catch(Exception e){rejected=true;}if(!rejected)throw new AssertionError("unsafe URL");}
  String body="{\"sender\":\"Bank\",\"body\":\"واریز ۱۰۰۰۰ ریال\"}";
  System.out.println(Crypto.sign("test-secret", "1790246400", body));
  System.out.println("Protocol/filter checks passed");
 }
}
