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
@RequestMapping(ADMIN_PREFIX + "/user-assignments/")
public class UserClientAssignmentController {

    private final UserClientAssignmentService userClientAssignmentService;

    @GetMapping("findAll")
    @PreAuthorize("hasAuthority('READ_USER_ASSIGNMENT')")
    public ResponseEntity<ResponseDto<List<UserClientAssignmentDto>>> findAll() {
        List<UserClientAssignmentDto> response = userClientAssignmentService.findAllByCurrentClient();
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @GetMapping("find/{id}")
    @PreAuthorize("hasAuthority('READ_USER_ASSIGNMENT')")
    public ResponseEntity<ResponseDto<UserClientAssignmentDto>> findById(@PathVariable Long id) {
        UserClientAssignmentDto response = userClientAssignmentService.findDtoById(id);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @GetMapping("findByUserId/{userId}")
    @PreAuthorize("hasAuthority('READ_USER_ASSIGNMENT')")
    public ResponseEntity<ResponseDto<List<UserClientAssignmentDto>>> findByUserId(@PathVariable Long userId) {
        List<UserClientAssignmentDto> response = userClientAssignmentService.findByUserId(userId);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    @PostMapping("save")
    @PreAuthorize("hasAuthority('CREATE_USER_ASSIGNMENT')")
    public ResponseEntity<ResponseDto<UserClientAssignmentDto>> save(@RequestBody @Valid CreateUserAssignmentDto request) {
        UserClientAssignmentDto response = userClientAssignmentService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.success(response));
    }

    @PatchMapping("banUser/{id}")
    @PreAuthorize("hasAuthority('BAN_USER')")
    public ResponseEntity<ResponseDto<String>> banUser(@PathVariable Long id) {
        userClientAssignmentService.banUser(id);
        String response = MessageResolver.getMessage(
                Messages.USER_BANNED_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok().body(ResponseDto.success(response));
    }

    @PatchMapping("unbanUser/{id}")
    @PreAuthorize("hasAuthority('UNBAN_USER')")
    public ResponseEntity<ResponseDto<String>> unbanUser(@PathVariable Long id) {
        userClientAssignmentService.unbanUser(id);
        String response = MessageResolver.getMessage(
                Messages.USER_UNBANNED_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok().body(ResponseDto.success(response));
    }

    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER_ASSIGNMENT')")
    public ResponseEntity<ResponseDto<String>> delete(@PathVariable Long id) {
        userClientAssignmentService.delete(id);
        String response = MessageResolver.getMessage(
                Messages.USER_ASSIGNMENT_DELETED_SUCCESS.getMessage(), new Object[]{id});
        return ResponseEntity.ok().body(ResponseDto.success(response));
    }
}