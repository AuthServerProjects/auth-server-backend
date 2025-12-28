package com.behpardakht.oauth_server.authorization.security;

public class ClientContextHolder {
    private static final ThreadLocal<Long> clientDbIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> clientIdHolder = new ThreadLocal<>();

    public static void setClientDbId(Long clientDbId) {
        clientDbIdHolder.set(clientDbId);
    }

    public static Long getClientDbId() {
        return clientDbIdHolder.get();
    }

    public static void setClientId(String clientId) {
        clientIdHolder.set(clientId);
    }

    public static String getClientId() {
        return clientIdHolder.get();
    }

    public static void clear() {
        clientDbIdHolder.remove();
        clientIdHolder.remove();
    }
}