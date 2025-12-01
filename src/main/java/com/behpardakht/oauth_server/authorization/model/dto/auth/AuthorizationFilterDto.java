package com.behpardakht.oauth_server.authorization.model.dto.auth;

import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthorizationFilterDto extends BaseFilterDto {

    private String principalName;
    private String registeredClientId;
}