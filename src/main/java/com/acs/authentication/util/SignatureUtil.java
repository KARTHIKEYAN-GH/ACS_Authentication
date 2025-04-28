package com.acs.authentication.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignatureUtil {

	public static String generateSignature(Map<String, String> params, String secretKey)
			throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
		String endpoint = "http://10.30.11.31:8080/client/api?";

		// Step 1: Sort parameters case-insensitively
		SortedMap<String, String> sortedParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		sortedParams.putAll(params);

		System.out.println("Sorted params: " + sortedParams);

		// Step 2: Build the sorted and lowercased string to sign
		StringBuilder query = new StringBuilder();
		for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
			query.append(entry.getKey().toLowerCase()).append("=").append(entry.getValue().toLowerCase()).append("&");
		}
		// Remove trailing "&"
		query.deleteCharAt(query.length() - 1);

		String stringToSign = query.toString();
		System.out.println("String to sign: " + stringToSign);

		// Step 3: Sign using HMAC SHA-1
		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
		mac.init(keySpec);
		byte[] rawHmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

		// Step 4: Encode the signature in Base64
		String signature = Base64.getEncoder().encodeToString(rawHmac);

		// Step 5: URL encode the signature
		String encodedSignature = URLEncoder.encode(signature, "UTF-8");

		// Step 6: Build the final URL with signature
		StringBuilder finalUrl = new StringBuilder(endpoint);
		for (Map.Entry<String, String> entry : params.entrySet()) {
			finalUrl.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"))
					.append("&");
		}
		// Append the signature
		finalUrl.append("signature=").append(encodedSignature);

		System.out.println("\nFinal signed URL:");
		System.out.println(finalUrl);
		return finalUrl.toString();
	}

	public static void main(String args[])
			throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		SignatureUtil sg = new SignatureUtil();

		// Prepare parameters
		HashMap<String, String> param = new HashMap<>();
		param.put("apiKey", "M3_9MJvYWqpGPxt0pgYYyhnz7NgTaBIUsh9pICv-TZ0-daZGlSUSY5O597ypgsMii3QtyFlrqAxUzjtQGT43bQ");
		param.put("domainId", "f885e154-e0bf-4f25-92ef-6c5e446dca11");
		param.put("command", "listNetworks");

		// Generate final signed URL
		String finalUrl = sg.generateSignature(param,
				"jHSYxkimKbnPiAdKobaCqH5P6QMH-tOAi0UbLUzXgipmZPRL5iyX8UdE-5qdCph17bISqfIERjr1_-VXkm9r4Q");

		// Output the final URL
		System.out.println("Final URL: " + finalUrl);
	}
}
