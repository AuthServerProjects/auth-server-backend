package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.RoleDto;
import com.behpardakht.oauth_server.authorization.service.RoleService;
import com.behpardakht.oauth_server.authorization.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/role/")
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "save")
    public ResponseEntity<ResponseDto<String>> save(@RequestBody RoleDto request) {
        roleService.save(request);
        String response = MessageResolver.getMessage(
                Messages.ROLE_ADDED_SUCCESS.getMessage(), new Object[]{request.getName()});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') || hasAuthority('user:manage_roles')")
    @GetMapping(path = "findAll")
    public ResponseEntity<ResponseDto<List<RoleDto>>> findAll() {
        List<RoleDto> response = roleService.findAll();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "find/{id}")
    public ResponseEntity<ResponseDto<RoleDto>> findById(@PathVariable Long id) {
        RoleDto response = roleService.findDtoById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(path = "toggleStatus/{id}")
    public ResponseEntity<ResponseDto<Boolean>> toggleStatus(@PathVariable Long id) {
        Boolean response = roleService.toggleStatus(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping(path = "delete/{id}")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        roleService.delete(id);
        String response = MessageResolver.getMessage(
                Messages.ROLE_DELETED_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok().body(ResponseDto.success(response));
    }

    @PreAuthorize("hasAuthority('MANAGE_ROLE_PERMISSIONS')")
    @PostMapping(path = "{roleId}/addPermission/{permissionId}")
    public ResponseEntity<ResponseDto<String>> addPermission(@PathVariable Long roleId,
                                                             @PathVariable Long permissionId) {
        roleService.addPermission(roleId, permissionId);
        String response = MessageResolver.getMessage(
                Messages.PERMISSION_ADDED_TO_ROLE.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasAuthority('MANAGE_ROLE_PERMISSIONS')")
    @DeleteMapping(path = "{roleId}/removePermission/{permissionId}")
    public ResponseEntity<ResponseDto<String>> removePermission(@PathVariable Long roleId,
                                                                @PathVariable Long permissionId) {
        roleService.removePermission(roleId, permissionId);
        String response = MessageResolver.getMessage(
                Messages.PERMISSION_REMOVED_FROM_ROLE.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}