package com.behpardakht.oauth_server.authorization.security;

public class ClientContextHolder {
    private static final ThreadLocal<Long> clientIdHolder = new ThreadLocal<>();

    public static void setClientId(Long clientId) {
        clientIdHolder.set(clientId);
    }

    public static Long getClientId() {
        return clientIdHolder.get();
    }

    public static void clear() {
        clientIdHolder.remove();
    }
}