package com.behpardakht.oauth_server.authorization.controller.admin;

import com.behpardakht.oauth_server.authorization.model.dto.audit.AuditLogDto;
import com.behpardakht.oauth_server.authorization.model.dto.audit.AuditLogFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import com.behpardakht.oauth_server.authorization.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.behpardakht.oauth_server.authorization.util.GeneralUtil.ADMIN_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = ADMIN_PREFIX + "/audit/")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("findAll")
    public ResponseEntity<ResponseDto<PageableResponseDto<AuditLogDto>>> findAll(@RequestBody
                                                                                 PageableRequestDto<AuditLogFilterDto> request) {
        PageableResponseDto<AuditLogDto> response = auditLogService.findAll(request);
        return ResponseEntity.ok(ResponseDto.success(response));
    }
}