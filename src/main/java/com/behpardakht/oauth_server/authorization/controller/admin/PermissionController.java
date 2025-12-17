package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.PermissionDto;
import com.behpardakht.oauth_server.authorization.model.dto.role.PermissionFilterDto;
import com.behpardakht.oauth_server.authorization.service.PermissionService;
import com.behpardakht.oauth_server.authorization.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/permission/")
public class PermissionController {

    private final PermissionService permissionService;

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'permission:create')")
    @PostMapping(path = "save")
    public ResponseEntity<ResponseDto<String>> save(@RequestBody PermissionDto request) {
        permissionService.save(request);
        String response = MessageResolver.getMessage(
                Messages.PERMISSION_ADDED_SUCCESS.getMessage(), new Object[]{request.getName()});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'permission:update')")
    @PutMapping(path = "update/{id}")
    public ResponseEntity<ResponseDto<String>> update(@PathVariable Long id,
                                                      @RequestBody PermissionDto request) {
        permissionService.update(id, request);
        String response = MessageResolver.getMessage(
                Messages.PERMISSION_UPDATED_SUCCESS.getMessage(), new Object[]{request.getName()});
        return ResponseEntity.ok(ResponseDto.success(response));
    }


    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'permission:read')")
    @PostMapping(path = "findAll")
    public ResponseEntity<ResponseDto<PageableResponseDto<PermissionDto>>> findAll(@RequestBody
                                                                                   PageableRequestDto
                                                                                           <PermissionFilterDto> request) {
        PageableResponseDto<PermissionDto> response = permissionService.findAll(request);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'permission:read')")
    @GetMapping(path = "find/{id}")
    public ResponseEntity<ResponseDto<PermissionDto>> findById(@PathVariable Long id) {
        PermissionDto response = permissionService.findDtoById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'permission:update')")
    @PatchMapping(path = "toggleStatus/{id}")
    public ResponseEntity<ResponseDto<Boolean>> toggleStatus(@PathVariable Long id) {
        Boolean response = permissionService.toggleStatus(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'permission:delete')")
    @DeleteMapping(path = "delete/{id}")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        permissionService.delete(id);
        String response = MessageResolver.getMessage(
                Messages.PERMISSION_DELETED_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok().body(ResponseDto.success(response));
    }
}