package com.behpardakht.oauth_server.authorization.aspect;

import com.behpardakht.oauth_server.authorization.model.entity.AuditLog;
import com.behpardakht.oauth_server.authorization.repository.AuditLogRepository;
import com.behpardakht.oauth_server.authorization.util.GeneralUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning(value = "@annotation(auditable)", returning = "result")
    public void auditSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
        saveAuditLog(joinPoint, auditable, true, null);
    }

    @AfterThrowing(value = "@annotation(auditable)", throwing = "ex")
    public void auditFailure(JoinPoint joinPoint, Auditable auditable, Exception ex) {
        saveAuditLog(joinPoint, auditable, false, ex.getMessage());
    }

    @Async
    protected void saveAuditLog(JoinPoint joinPoint, Auditable auditable, boolean success, String errorDetails) {
        try {
            String username = extractParam(joinPoint, auditable.usernameParam());
            String clientId = extractParam(joinPoint, auditable.clientIdParam());
            String details = extractParam(joinPoint, auditable.detailsParam());
            if (!success && errorDetails != null) {
                details = details != null ? details + " | Error: " + errorDetails : errorDetails;
            }
            HttpServletRequest request = getCurrentRequest();
            AuditLog auditLog = AuditLog.builder()
                    .action(auditable.action())
                    .username(username)
                    .clientId(clientId)
                    .ipAddress(GeneralUtil.getClientIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .details(details)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    private String extractParam(JoinPoint joinPoint, String paramName) {
        if (paramName == null || paramName.isEmpty()) {
            return null;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(paramName) && args[i] != null) {
                return args[i].toString();
            }
        }
        return extractFromDto(args, paramName);
    }

    private String extractFromDto(Object[] args, String paramName) {
        for (Object arg : args) {
            if (arg == null) continue;
            try {
                Method getter = arg.getClass().getMethod(
                        "get" + paramName.substring(0, 1).toUpperCase() + paramName.substring(1));
                Object value = getter.invoke(arg);
                return value != null ? value.toString() : null;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}