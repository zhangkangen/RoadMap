package com.example.imageutil;


import org.apache.commons.codec.binary.Base64;

/**
 * Created by Administrator on 2014/12/8.
 */
public class Base64Util {
    public static String base64(byte[] target) {
        return new Base64().encodeToString(target).replace('+', '-').replace('/', '_');
    }
}
