package com.berkeh.connector;
import android.security.keystore.*;
import android.util.Base64;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
public final class Crypto {
 static synchronized javax.crypto.SecretKey key() throws Exception {
  KeyStore s=KeyStore.getInstance("AndroidKeyStore");s.load(null);
  if(!s.containsAlias("berkeh-local-v1")){KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder("berkeh-local-v1",KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();}
  return (javax.crypto.SecretKey)s.getKey("berkeh-local-v1",null);
 }
 static String encrypt(String text) throws Exception {Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());return Base64.encodeToString(c.getIV(),2)+":"+Base64.encodeToString(c.doFinal(text.getBytes(StandardCharsets.UTF_8)),2);}
 static String decrypt(String text) throws Exception {String[] p=text.split(":",2);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(p[0],2)));return new String(c.doFinal(Base64.decode(p[1],2)),StandardCharsets.UTF_8);}
 static String hex(byte[] a){StringBuilder b=new StringBuilder();for(byte v:a)b.append(String.format(java.util.Locale.ROOT,"%02x",v&255));return b.toString();}
 static String hash(String s) throws Exception{return hex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}
 static String sign(String secret,String time,String body)throws Exception{Mac m=Mac.getInstance("HmacSHA256");m.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return hex(m.doFinal((time+"\n"+body).getBytes(StandardCharsets.UTF_8)));}
}
