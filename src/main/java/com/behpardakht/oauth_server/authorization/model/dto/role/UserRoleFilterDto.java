package com.behpardakht.oauth_server.authorization.model.dto.role;

import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRoleFilterDto extends BaseFilterDto {
    private String username;
    private String roleName;
    private Long roleId;
    private Long userClientId;
}