package com.behpardakht.oauth_server.authorization.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserAssignmentDto {

    private Long userId;
    private UsersDto newUser;
}