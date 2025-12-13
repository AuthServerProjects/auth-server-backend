package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UsersDto;
import com.behpardakht.oauth_server.authorization.service.user.AdminUserService;
import com.behpardakht.oauth_server.authorization.util.Messages;
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
                                                                              PageableRequestDto<UserFilterDto> request) {
        PageableResponseDto<UsersDto> response = adminUserService.findAll(request);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(path = "find/{id}")
    public ResponseEntity<ResponseDto<UsersDto>> findById(@PathVariable Long id) {
        UsersDto response = adminUserService.findById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
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
        String response = MessageResolver.getMessage(
                Messages.USER_REGISTERED_SUCCESS.getMessage(), new Object[]{request.getUsername()});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping(path = "update/{id}")
    public ResponseEntity<ResponseDto<String>> update(@PathVariable Long id, @RequestBody UsersDto request) {
        adminUserService.update(id, request);
        String response = MessageResolver.getMessage(
                Messages.USER_UPDATED_SUCCESS.getMessage(), new Object[]{request.getUsername()});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping(path = "resetPassword/{id}")
    public ResponseEntity<ResponseDto<String>> resetPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
        String response = MessageResolver.getMessage(Messages.PASSWORD_SENT_SUCCESS.getMessage());
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping(path = "toggleStatus/{id}")
    public ResponseEntity<ResponseDto<Boolean>> toggleStatus(@PathVariable Long id) {
        Boolean response = adminUserService.toggleStatus(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}