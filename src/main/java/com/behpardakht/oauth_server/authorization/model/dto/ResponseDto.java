package com.behpardakht.oauth_server.authorization.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseDto<T> {
    private Boolean success;
    private String message;
    private T responses;

    public static <T> ResponseDto<T> success(T response) {
        return ResponseDto.<T>builder()
                .success(true)
                .message("OK")
                .responses(response)
                .build();
    }

    public static <T> ResponseDto<T> failed(String message, T response) {
        return ResponseDto.<T>builder()
                .success(false)
                .message(message)
                .responses(response)
                .build();
    }
}