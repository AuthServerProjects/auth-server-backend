package com.behpardakht.oauth_server.authorization.service;

import com.behpardakht.oauth_server.authorization.model.dto.audit.AuditLogDto;
import com.behpardakht.oauth_server.authorization.model.dto.audit.AuditLogFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableResponseDto;
import com.behpardakht.oauth_server.authorization.model.entity.AuditLog;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.model.enums.AuditAction;
import com.behpardakht.oauth_server.authorization.model.mapper.AuditLogMapper;
import com.behpardakht.oauth_server.authorization.repository.AuditLogRepository;
import com.behpardakht.oauth_server.authorization.repository.filter.AuditLogFilterSpecification;
import com.behpardakht.oauth_server.authorization.service.otp.OtpStorageService;
import com.behpardakht.oauth_server.authorization.util.GeneralUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final ClientService clientService;
    private final AuditLogFilterSpecification auditLogFilterSpecification;
    private final OtpStorageService otpStorageService;
    private final AuditLogMapper auditLogMapper;
    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(AuditAction action, String username, Long clientDbId,
                    HttpServletRequest request, String details, Boolean success) {
        Client client = clientDbId != null ? clientService.findById(clientDbId) : null;
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .username(username)
                .client(client)
                .ipAddress(GeneralUtil.getClientIpAddress(request))
                .userAgent(request.getHeader("User-Agent"))
                .details(details)
                .success(success)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Async
    public void log(AuditAction action, String state, String details, Boolean success) {
        String phoneNumber = otpStorageService.getSessionDto(state).phoneNumber();
        String clientId = otpStorageService.getSessionDto(state).clientId();
        Client client = clientService.findByClientId(clientId);

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .username(phoneNumber)
                .client(client)
                .details(details)
                .success(success)
                .build();
        auditLogRepository.save(auditLog);
    }

    public PageableResponseDto<AuditLogDto> findAll(PageableRequestDto<AuditLogFilterDto> request) {
        Specification<AuditLog> spec = auditLogFilterSpecification.toSpecification(request.getFilters());
        Page<AuditLog> page = auditLogRepository.findAll(spec, request.toPageable());
        return PageableResponseDto.build(auditLogMapper.toDtoList(page.getContent()), page);
    }
}