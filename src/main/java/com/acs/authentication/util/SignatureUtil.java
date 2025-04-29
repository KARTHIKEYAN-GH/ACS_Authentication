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

	public static String generateSignature(Map<String, String> params, String secretKey) {
		try {
			String endpoint = "http://10.30.11.31:8080/client/api?";

			// Step 1: Sort parameters case-insensitively
			SortedMap<String, String> sortedParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			sortedParams.putAll(params);

			System.out.println("Sorted params: " + sortedParams);

			// Step 2: Build the sorted and lowercased string to sign
			StringBuilder query = new StringBuilder();
			for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
				if ("userId".equalsIgnoreCase(entry.getKey())) {
					continue; // Skip userId while signing
				}
				query.append(entry.getKey().toLowerCase()).append("=").append(entry.getValue().toLowerCase())
						.append("&");
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
			for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
				if ("userId".equalsIgnoreCase(entry.getKey())) {
					continue; // Skip userId while forming URL also
				}
				finalUrl.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"))
						.append("&");
			}
			// Append the signature
			finalUrl.append("signature=").append(encodedSignature);

			return finalUrl.toString();

		} catch (Exception e) {
			throw new RuntimeException("Failed to generate signature", e);
		}
	}

}
