package br.com.provas.security;
import java.nio.file.*;
import java.security.*;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class TokenCipherTest {
 @TempDir Path directory;
 @Test void tokenRoundTripsWithoutStoringPlainTextAndRejectsTampering()throws Exception{
  var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);var keys=generator.generateKeyPair();var base64=Base64.getMimeEncoder(64,new byte[]{10});
  Files.writeString(directory.resolve("publica.pem"),"-----BEGIN PUBLIC KEY-----\n"+base64.encodeToString(keys.getPublic().getEncoded())+"\n-----END PUBLIC KEY-----");
  Files.writeString(directory.resolve("privada.pem"),"-----BEGIN PRIVATE KEY-----\n"+base64.encodeToString(keys.getPrivate().getEncoded())+"\n-----END PRIVATE KEY-----");
  var cipher=new TokenCipher(directory.toString());String token="TokenVisível-2026";var encrypted=cipher.encrypt(token);assertFalse(encrypted.contains(token));assertEquals(token,cipher.decrypt(encrypted));assertNotEquals(encrypted,cipher.encrypt(token));
  var parts=encrypted.split(":");var bytes=Base64.getDecoder().decode(parts[3]);bytes[0]^=1;parts[3]=Base64.getEncoder().encodeToString(bytes);assertThrows(IllegalStateException.class,()->cipher.decrypt(String.join(":",parts)));
 }
}
