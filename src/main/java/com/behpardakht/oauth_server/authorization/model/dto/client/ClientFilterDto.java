package com.behpardakht.oauth_server.authorization.model.dto.client;

import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClientFilterDto extends BaseFilterDto {

    private Boolean isEnabled;
}