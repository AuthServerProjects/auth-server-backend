package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.UserRoleFilterDto;
import com.behpardakht.oauth_server.authorization.service.user.UserRoleService;
import com.behpardakht.oauth_server.authorization.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/user-role-/")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:assign')")
    @PostMapping("assign")
    public ResponseEntity<ResponseDto<UserRoleDto>> assign(@RequestParam Long userClientId,
                                                           @RequestParam Long roleId) {
        UserRoleDto response = userRoleService.assign(userClientId, roleId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:unassign')")
    @DeleteMapping("unassign")
    public ResponseEntity<ResponseDto<String>> unassign(@RequestParam Long userClientId,
                                                        @RequestParam Long roleId) {
        userRoleService.unassign(userClientId, roleId);
        String response = MessageResolver.getMessage(Messages.ROLE_UNASSIGNED_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:read')")
    @GetMapping("findByUserClientId/{userClientId}")
    public ResponseEntity<ResponseDto<List<UserRoleDto>>> findByUserClientId(@PathVariable
                                                                             Long userClientId) {
        List<UserRoleDto> response =
                userRoleService.findByUserClientId(userClientId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:read')")
    @GetMapping("findByRoleId/{roleId}")
    public ResponseEntity<ResponseDto<List<UserRoleDto>>> findByRoleId(@PathVariable Long roleId) {
        List<UserRoleDto> response = userRoleService.findByRoleId(roleId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:read')")
    @PostMapping("findAll")
    public ResponseEntity<ResponseDto<PageableResponseDto<UserRoleDto>>> findAll(@RequestBody
                                                                                           PageableRequestDto
                                                                                                   <UserRoleFilterDto>
                                                                                                   request) {
        PageableResponseDto<UserRoleDto> response = userRoleService.findAll(request);
        return ResponseEntity.ok(ResponseDto.success(response));
    }


    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:read')")
    @GetMapping("find/{id}")
    public ResponseEntity<ResponseDto<UserRoleDto>> findById(@PathVariable Long id) {
        UserRoleDto response = userRoleService.findDtoById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_role:unassign')")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        userRoleService.delete(id);
        String response = MessageResolver.getMessage(Messages.ROLE_REMOVED_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}