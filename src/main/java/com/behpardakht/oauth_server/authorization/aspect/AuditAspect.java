package com.behpardakht.oauth_server.authorization.aspect;

import com.behpardakht.oauth_server.authorization.model.entity.AuditLog;
import com.behpardakht.oauth_server.authorization.model.entity.Client;
import com.behpardakht.oauth_server.authorization.repository.AuditLogRepository;
import com.behpardakht.oauth_server.authorization.service.ClientService;
import com.behpardakht.oauth_server.authorization.util.GeneralUtil;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final ClientService clientService;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

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
            EvaluationContext context = createEvaluationContext(joinPoint);

            String username = parseSpel(auditable.username(), context);
            String details = parseSpel(auditable.details(), context);

            Client client = resolveClient(auditable.clientId(), context);

            if (!success && errorDetails != null) {
                details = details != null ? details + " | Error: " + errorDetails : errorDetails;
            }

            HttpServletRequest request = getCurrentRequest();

            AuditLog auditLog = AuditLog.builder()
                    .action(auditable.action())
                    .username(username)
                    .client(client)  // Changed from clientId
                    .ipAddress(GeneralUtil.getClientIpAddress(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .details(details)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    private Client resolveClient(String clientIdExpression, EvaluationContext context) {
        try {
            if (clientIdExpression != null && !clientIdExpression.isEmpty()) {
                Object value = parser.parseExpression(clientIdExpression).getValue(context);
                if (value instanceof Long clientDbId) {
                    return clientService.findById(clientDbId);
                } else if (value instanceof String clientId) {
                    return clientService.findByClientId(clientId);
                }
            }
            Long clientDbId = SecurityUtils.getCurrentClientId();
            if (clientDbId != null) {
                return clientService.findById(clientDbId);
            }
        } catch (Exception e) {
            log.warn("Failed to resolve client: {}", e.getMessage());
        }
        return null;
    }

    private EvaluationContext createEvaluationContext(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        return new MethodBasedEvaluationContext(null, method, args, parameterNameDiscoverer);
    }

    private String parseSpel(String expression, EvaluationContext context) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        try {
            Object value = parser.parseExpression(expression).getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to parse SpEL expression: {}", expression);
            return null;
        }
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