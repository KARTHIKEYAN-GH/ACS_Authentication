package com.acs.authentication.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

@Component
public class PasswordCryptoUtil {

	private static final String SECRET_KEY = "1234567890abcdef"; // Must be 16 chars (128-bit key)

	// Encrypt plain password
	public String encrypt(String plainText) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	// Decrypt encrypted password
	public String decrypt(String encryptedText) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
		byte[] decryptedBytes = cipher.doFinal(decodedBytes);
		return new String(decryptedBytes);
	}

    public static void main (String args[]) throws Exception {
//   	PasswordCryptoUtil pc=new PasswordCryptoUtil();
//    	String encryptedPassword=pc.encrypt("@Password17");
//    	System.out.println("encryptedPassword :"+encryptedPassword);
//    	String decryptedPasswrd = pc.decrypt(encryptedPassword);
//    	System.out.println("decryptedPasswrd :"+decryptedPasswrd);
//    	
   }
}
