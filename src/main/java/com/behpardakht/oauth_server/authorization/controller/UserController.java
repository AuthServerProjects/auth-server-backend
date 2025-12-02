package com.behpardakht.oauth_server.authorization.controller;

import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/user/")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "findAll")
    public ResponseEntity<ResponseDto<PageableResponseDto<UsersDto>>> findAll(@RequestBody
                                                                                  PageableRequestDto<UserFilterDto>
                                                                                          request) {
        PageableResponseDto<UsersDto> response = userService.findAll(request);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "find/{id}")
    public ResponseEntity<ResponseDto<UsersDto>> findById(@PathVariable Long id) {
        UsersDto response = userService.findById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or #username == authentication.principal.username")
    @GetMapping(path = "findByUsername")
    public ResponseEntity<ResponseDto<UsersDto>> findByUsername(@RequestParam String username) {
        UsersDto response = userService.findUserByUsername(username);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "existUsername")
    public ResponseEntity<ResponseDto<Boolean>> existUsername(@RequestParam String username) {
        boolean response = userService.existUserWithUsername(username);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "existPhoneNumber")
    public ResponseEntity<ResponseDto<Boolean>> existPhoneNumber(@RequestParam String phoneNumber) {
        boolean response = userService.existUserWithPhoneNumber(phoneNumber);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "save")
    public ResponseEntity<ResponseDto<String>> save(@RequestBody UsersDto request) {
        userService.save(request);
        return ResponseEntity.ok(ResponseDto.success("User registered successfully"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping(path = "update/{id}")
    public ResponseEntity<ResponseDto<String>> update(@PathVariable Long id, @RequestBody UsersDto request) {
        userService.update(id, request);
        return ResponseEntity.ok(ResponseDto.success("User updated successfully"));
    }

    @PreAuthorize("#oldUsername == authentication.principal.username")
    @PostMapping(path = "changeUsername")
    public ResponseEntity<ResponseDto<?>> changeUsername(@RequestParam String oldUsername,
                                                         @RequestParam String newUsername) {
        userService.changeUsername(oldUsername, newUsername);
        return ResponseEntity.ok(ResponseDto.success("User changed successfully"));
    }

    @PreAuthorize("#username == authentication.principal.username")
    @PostMapping(path = "changePassword")
    public ResponseEntity<ResponseDto<?>> changePassword(@RequestParam String username,
                                                         @RequestParam String oldPassword,
                                                         @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok(ResponseDto.success("Password changed successfully"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "resetPassword/{id}")
    public ResponseEntity<ResponseDto<String>> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return ResponseEntity.ok(ResponseDto.success("Password sent to your mobile"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "addRoleToUser")
    public ResponseEntity<ResponseDto<?>> addRoleToUser(@RequestParam String username,
                                                        @RequestParam String roleName) {
        userService.addRoleToUser(username, roleName);
        return ResponseEntity.ok(ResponseDto.success("Role added successfully"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "removeRoleFromUser")
    public ResponseEntity<ResponseDto<String>> removeRoleFromUser(@RequestParam String username,
                                                                  @RequestParam String roleName) {
        userService.removeRoleFromUser(username, roleName);
        return ResponseEntity.ok(ResponseDto.success("Role removed successfully"));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(path = "toggleStatus/{id}")
    public ResponseEntity<ResponseDto<Boolean>> toggleStatus(@PathVariable Long id) {
        Boolean response = userService.toggleStatus(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}