package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.ExceptionMessages;
import com.behpardakht.oauth_server.authorization.model.dto.RoleDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = API_PREFIX + "/role/")
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "save")
    public ResponseEntity<String> save(@RequestBody RoleDto roleDto) {
        roleService.save(roleDto);
        String message = MessageResolver.getMessage(ExceptionMessages.ROLE_ADDED_SUCCESS.getMessage());
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "findAll")
    public ResponseEntity<Set<RoleDto>> findAllRoles() {
        Set<RoleDto> roles = roleService.findAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "{id}")
    public ResponseEntity<RoleDto> findById(@PathVariable Long id) {
        RoleDto role = roleService.findDtoById(id);
        return ResponseEntity.ok(role);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(path = "toggleStatus/{roleId}")
    public ResponseEntity<ResponseDto<Boolean>> toggleStatus(@PathVariable Long roleId) {
        Boolean newStatus = roleService.toggleStatus(roleId);
        return ResponseEntity.ok(ResponseDto.success(newStatus));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        roleService.delete(id);
        String message = MessageResolver.getMessage(ExceptionMessages.ROLE_DELETED_SUCCESS.getMessage());
        return ResponseEntity.ok().body(message);
    }
}