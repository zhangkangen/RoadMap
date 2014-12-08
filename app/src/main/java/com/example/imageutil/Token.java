package com.example.imageutil;

/**
 * Created by Administrator on 2014/12/8.
 */
public class Token {
    public static String createToken(long deadlineTime,long albumId){
        String accesskey = "";
        String secretKey = "";
        String json = "{\"deadline\":%s ,\"album\":\"%s\",\"returnBody\":\"\"}";
        String base64param = Base64Util.base64(json.getBytes()).trim();
        String sign = HMacUtil.hmac_sha1(base64param , secretKey);
        String token = accesskey+":"+sign+":"+base64param;
        return token;
    }
}
