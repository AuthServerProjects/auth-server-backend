package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.RoleAssignmentDto;
import com.behpardakht.oauth_server.authorization.service.RoleAssignmentService;
import com.behpardakht.oauth_server.authorization.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/role-assignment/")
public class RoleAssignmentController {

    private final RoleAssignmentService roleAssignmentService;

    @PreAuthorize("hasAuthority('user:manage_roles')")
    @PostMapping("assign")
    public ResponseEntity<ResponseDto<RoleAssignmentDto>> assign(@RequestParam Long userId,
                                                                 @RequestParam Long roleId) {
        RoleAssignmentDto response = roleAssignmentService.assign(userId, roleId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasAuthority('user:manage_roles')")
    @DeleteMapping("unassign")
    public ResponseEntity<ResponseDto<String>> unassign(@RequestParam Long userId,
                                                        @RequestParam Long roleId) {
        roleAssignmentService.unassign(userId, roleId);
        String response = MessageResolver.getMessage(Messages.ROLE_UNASSIGNED_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("findByUserId/{userId}")
    public ResponseEntity<ResponseDto<List<RoleAssignmentDto>>> findByUserId(@PathVariable Long userId) {
        List<RoleAssignmentDto> response = roleAssignmentService.findByUserId(userId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("findByUsername/{username}")
    public ResponseEntity<ResponseDto<List<RoleAssignmentDto>>> findByUsername(@PathVariable String username) {
        List<RoleAssignmentDto> response = roleAssignmentService.findDtoByUsername(username);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("findByRoleId/{roleId}")
    public ResponseEntity<ResponseDto<List<RoleAssignmentDto>>> findByRoleId(@PathVariable Long roleId) {
        List<RoleAssignmentDto> response = roleAssignmentService.findByRoleId(roleId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("findAll")
    public ResponseEntity<ResponseDto<List<RoleAssignmentDto>>> findAll() {
        List<RoleAssignmentDto> response = roleAssignmentService.findAll();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasAuthority('user:manage_roles')")
    @GetMapping("find/{id}")
    public ResponseEntity<ResponseDto<RoleAssignmentDto>> findById(@PathVariable Long id) {
        RoleAssignmentDto response = roleAssignmentService.findDtoById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasAuthority('user:manage_roles')")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        roleAssignmentService.delete(id);
        String response = MessageResolver.getMessage(Messages.ROLE_REMOVED_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}