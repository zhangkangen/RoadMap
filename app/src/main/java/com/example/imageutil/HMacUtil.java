package com.example.imageutil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Administrator on 2014/12/8.
 */
public class HMacUtil {
    public static String hmac_sha1(String value, String key) {
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes());
            return new String(Base64Util.base64(rawHmac)).trim();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
