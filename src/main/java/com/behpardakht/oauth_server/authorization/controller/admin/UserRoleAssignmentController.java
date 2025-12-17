package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleAssignmentDto;
import com.behpardakht.oauth_server.authorization.service.user.UserRoleAssignmentService;
import com.behpardakht.oauth_server.authorization.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/user-role-assignment/")
public class UserRoleAssignmentController {

    private final UserRoleAssignmentService userRoleAssignmentService;

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:assign')")
    @PostMapping("assign")
    public ResponseEntity<ResponseDto<UserRoleAssignmentDto>> assign(@RequestParam Long userClientAssignmentId,
                                                                     @RequestParam Long roleId) {
        UserRoleAssignmentDto response = userRoleAssignmentService.assign(userClientAssignmentId, roleId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:unassign')")
    @DeleteMapping("unassign")
    public ResponseEntity<ResponseDto<String>> unassign(@RequestParam Long userClientAssignmentId,
                                                        @RequestParam Long roleId) {
        userRoleAssignmentService.unassign(userClientAssignmentId, roleId);
        String response = MessageResolver.getMessage(Messages.ROLE_UNASSIGNED_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:read')")
    @GetMapping("findByUserClientAssignmentId/{userClientAssignmentId}")
    public ResponseEntity<ResponseDto<List<UserRoleAssignmentDto>>> findByUserClientAssignmentId(@PathVariable
                                                                                                 Long userClientAssignmentId) {
        List<UserRoleAssignmentDto> response =
                userRoleAssignmentService.findByUserClientAssignmentId(userClientAssignmentId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:read')")
    @GetMapping("findByRoleId/{roleId}")
    public ResponseEntity<ResponseDto<List<UserRoleAssignmentDto>>> findByRoleId(@PathVariable Long roleId) {
        List<UserRoleAssignmentDto> response = userRoleAssignmentService.findByRoleId(roleId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:read')")
    @GetMapping("findAll")
    public ResponseEntity<ResponseDto<List<UserRoleAssignmentDto>>> findAll() {
        List<UserRoleAssignmentDto> response = userRoleAssignmentService.findAll();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:read')")
    @GetMapping("find/{id}")
    public ResponseEntity<ResponseDto<UserRoleAssignmentDto>> findById(@PathVariable Long id) {
        UserRoleAssignmentDto response = userRoleAssignmentService.findDtoById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:unassign')")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        userRoleAssignmentService.delete(id);
        String response = MessageResolver.getMessage(Messages.ROLE_REMOVED_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}