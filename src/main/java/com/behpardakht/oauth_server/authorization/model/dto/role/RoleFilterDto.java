package com.behpardakht.oauth_server.authorization.model.dto.role;

import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleFilterDto extends BaseFilterDto {
    private String name;
}