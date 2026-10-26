package br.com.provas.security;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/** Cifra tokens consultáveis pelo professor usando AES-GCM e a chave RSA local fora do Git. */
@Component
public class TokenCipher {
 private final PublicKey publicKey;private final PrivateKey privateKey;private final SecureRandom random=new SecureRandom();
 public TokenCipher(@Value("${app.security.keys-directory:keys}") String directory){try{
  var factory=KeyFactory.getInstance("RSA");publicKey=factory.generatePublic(new X509EncodedKeySpec(pem(Path.of(directory,"publica.pem"))));
  privateKey=factory.generatePrivate(new PKCS8EncodedKeySpec(pem(Path.of(directory,"privada.pem"))));
 }catch(Exception e){throw new IllegalStateException("Não foi possível carregar as chaves de proteção dos tokens.",e);}}
 private byte[] pem(Path path)throws java.io.IOException{return Base64.getMimeDecoder().decode(Files.readString(path).replaceAll("-----[^-]+-----",""));}
 private Cipher rsa(int mode,Key key)throws GeneralSecurityException{var cipher=Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");cipher.init(mode,key,new OAEPParameterSpec("SHA-256","MGF1",MGF1ParameterSpec.SHA256,PSource.PSpecified.DEFAULT));return cipher;}
 public String encrypt(String token){try{var bytes=new byte[32];random.nextBytes(bytes);var iv=new byte[12];random.nextBytes(iv);var aes=Cipher.getInstance("AES/GCM/NoPadding");aes.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(bytes,"AES"),new GCMParameterSpec(128,iv));var encoder=Base64.getEncoder();return "v1:"+encoder.encodeToString(rsa(Cipher.ENCRYPT_MODE,publicKey).doFinal(bytes))+":"+encoder.encodeToString(iv)+":"+encoder.encodeToString(aes.doFinal(token.getBytes(StandardCharsets.UTF_8)));}catch(GeneralSecurityException e){throw new IllegalStateException("Não foi possível proteger o token.",e);}}
 public String decrypt(String encrypted){try{var parts=encrypted.split(":");if(parts.length!=4||!parts[0].equals("v1"))throw new GeneralSecurityException("Formato inválido");var decoder=Base64.getDecoder();var key=rsa(Cipher.DECRYPT_MODE,privateKey).doFinal(decoder.decode(parts[1]));var aes=Cipher.getInstance("AES/GCM/NoPadding");aes.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,decoder.decode(parts[2])));return new String(aes.doFinal(decoder.decode(parts[3])),StandardCharsets.UTF_8);}catch(GeneralSecurityException|IllegalArgumentException e){throw new IllegalStateException("Não foi possível recuperar o token desta liberação.",e);}}
}
