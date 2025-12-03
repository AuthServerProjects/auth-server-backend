package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/user/")
public class UserController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "findAll")
    public ResponseEntity<ResponseDto<PageableResponseDto<UsersDto>>> findAll(@RequestBody
                                                                                  PageableRequestDto<UserFilterDto>
                                                                                          request) {
        PageableResponseDto<UsersDto> response = adminUserService.findAll(request);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "find/{id}")
    public ResponseEntity<ResponseDto<UsersDto>> findById(@PathVariable Long id) {
        UsersDto response = adminUserService.findById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or #username == authentication.principal.username")
    @GetMapping(path = "findByUsername")
    public ResponseEntity<ResponseDto<UsersDto>> findByUsername(@RequestParam String username) {
        UsersDto response = adminUserService.findUserByUsername(username);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "existUsername")
    public ResponseEntity<ResponseDto<Boolean>> existUsername(@RequestParam String username) {
        boolean response = adminUserService.existUserWithUsername(username);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "existPhoneNumber")
    public ResponseEntity<ResponseDto<Boolean>> existPhoneNumber(@RequestParam String phoneNumber) {
        boolean response = adminUserService.existUserWithPhoneNumber(phoneNumber);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "save")
    public ResponseEntity<ResponseDto<String>> save(@RequestBody UsersDto request) {
        adminUserService.save(request);
        return ResponseEntity.ok(ResponseDto.success("User registered successfully"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping(path = "update/{id}")
    public ResponseEntity<ResponseDto<String>> update(@PathVariable Long id, @RequestBody UsersDto request) {
        adminUserService.update(id, request);
        return ResponseEntity.ok(ResponseDto.success("User updated successfully"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "resetPassword/{id}")
    public ResponseEntity<ResponseDto<String>> resetPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
        return ResponseEntity.ok(ResponseDto.success("Password sent to your mobile"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "addRoleToUser")
    public ResponseEntity<ResponseDto<?>> addRoleToUser(@RequestParam String username,
                                                        @RequestParam String roleName) {
        adminUserService.addRoleToUser(username, roleName);
        return ResponseEntity.ok(ResponseDto.success("Role added successfully"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "removeRoleFromUser")
    public ResponseEntity<ResponseDto<String>> removeRoleFromUser(@RequestParam String username,
                                                                  @RequestParam String roleName) {
        adminUserService.removeRoleFromUser(username, roleName);
        return ResponseEntity.ok(ResponseDto.success("Role removed successfully"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(path = "toggleStatus/{id}")
    public ResponseEntity<ResponseDto<Boolean>> toggleStatus(@PathVariable Long id) {
        Boolean response = adminUserService.toggleStatus(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}