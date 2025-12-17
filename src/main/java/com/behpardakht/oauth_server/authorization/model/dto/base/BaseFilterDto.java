package com.behpardakht.oauth_server.authorization.model.dto.base;

import lombok.Data;

@Data
public class BaseFilterDto {
    private Long clientId;
    private String client;
    private Boolean isEnabled;
}