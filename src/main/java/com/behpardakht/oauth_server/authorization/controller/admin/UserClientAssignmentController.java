package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.CreateUserAssignmentDto;
import com.behpardakht.oauth_server.authorization.model.dto.user.UserClientAssignmentDto;
import com.behpardakht.oauth_server.authorization.service.user.UserClientAssignmentService;
import com.behpardakht.oauth_server.authorization.util.Messages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(ADMIN_PREFIX + "/user-assignment/")
public class UserClientAssignmentController {

    private final UserClientAssignmentService userClientAssignmentService;

    @GetMapping("findAll")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_assignment:read')")
    public ResponseEntity<ResponseDto<List<UserClientAssignmentDto>>> findAll() {
        List<UserClientAssignmentDto> response = userClientAssignmentService.findAllByCurrentClient();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @GetMapping("find/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_assignment:read')")
    public ResponseEntity<ResponseDto<UserClientAssignmentDto>> findById(@PathVariable Long id) {
        UserClientAssignmentDto response = userClientAssignmentService.findDtoById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PostMapping("save")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_assignment:create')")
    public ResponseEntity<ResponseDto<UserClientAssignmentDto>> save(@RequestBody @Valid CreateUserAssignmentDto request) {
        UserClientAssignmentDto response = userClientAssignmentService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.success(response));
    }

    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_assignment:update')")
    public ResponseEntity<ResponseDto<String>> update(@PathVariable Long id,
                                                      @RequestBody @Valid UserClientAssignmentDto request) {
        userClientAssignmentService.update(id, request);
        String response = MessageResolver.getMessage(
                Messages.USER_UPDATED_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PostMapping("resetPassword/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_assignment:reset_password')")
    public ResponseEntity<ResponseDto<String>> resetPassword(@PathVariable Long id) {
        userClientAssignmentService.resetPassword(id);
        String response = MessageResolver.getMessage(
                Messages.PASSWORD_SENT_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PatchMapping("ban/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_assignment:ban')")
    public ResponseEntity<ResponseDto<String>> banUser(@PathVariable Long id) {
        userClientAssignmentService.banUser(id);
        String response = MessageResolver.getMessage(
                Messages.USER_BANNED_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PatchMapping("unban/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_assignment:unban')")
    public ResponseEntity<ResponseDto<String>> unbanUser(@PathVariable Long id) {
        userClientAssignmentService.unbanUser(id);
        String response = MessageResolver.getMessage(
                Messages.USER_UNBANNED_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'user_assignment:delete')")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        userClientAssignmentService.delete(id);
        String response = MessageResolver.getMessage(
                Messages.USER_ASSIGNMENT_DELETED_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}