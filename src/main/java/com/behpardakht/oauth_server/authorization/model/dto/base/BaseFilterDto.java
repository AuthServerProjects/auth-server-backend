package com.behpardakht.oauth_server.authorization.model.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class BaseFilterDto {
    @JsonIgnore
    private Long clientId;
    @JsonIgnore
    private String client;
    private Boolean isEnabled;
}