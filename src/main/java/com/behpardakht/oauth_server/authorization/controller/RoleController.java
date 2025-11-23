package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessages;
import com.behpardakht.oauth_server.authorization.model.entity.Role;
import com.behpardakht.oauth_server.authorization.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/role/")
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "save")
    public ResponseEntity<String> save(@RequestBody Role role) {
        roleService.save(role);
        String message = MessageResolver.getMessage(ExceptionMessages.ROLE_ADDED_SUCCESS.getMessage());
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "findAll")
    public List<Role> findAllRoles() {
        return roleService.findAllRoles();
    }
}