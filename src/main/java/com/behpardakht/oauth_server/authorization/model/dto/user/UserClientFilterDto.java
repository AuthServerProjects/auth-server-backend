package com.behpardakht.oauth_server.authorization.model.dto.user;

import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserClientFilterDto extends BaseFilterDto {
    private String username;
    private String phoneNumber;
    private Boolean isEnabled;
    private Boolean isAccountNonLocked;
    private Boolean isAccountNonExpired;
    private Boolean isCredentialsNonExpired;
}