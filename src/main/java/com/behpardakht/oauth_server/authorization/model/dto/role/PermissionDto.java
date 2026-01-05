package com.behpardakht.oauth_server.authorization.model.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {
    private Long id;
    private String name;
    private String description;
    private Long clientId;
    private Boolean isEnabled;
}
