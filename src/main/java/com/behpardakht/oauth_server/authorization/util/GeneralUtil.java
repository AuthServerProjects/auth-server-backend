package com.behpardakht.oauth_server.authorization.util;

public class GeneralUtil {

    public static final String API_PREFIX = "/auth-api/v1";
    public static final String ADMIN_PREFIX = "/auth-admin/v1";
    public static final String URL_PREFIX = "/auth/v1";

    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "*".repeat(phoneNumber.length() - 4) + phoneNumber.substring(phoneNumber.length() - 4);
    }
}