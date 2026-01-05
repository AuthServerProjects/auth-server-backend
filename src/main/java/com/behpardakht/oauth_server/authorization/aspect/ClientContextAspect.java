package com.behpardakht.oauth_server.authorization.aspect;

import com.behpardakht.oauth_server.authorization.exception.ExceptionMessage;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.model.dto.base.BaseFilterDto;
import com.behpardakht.oauth_server.authorization.model.dto.base.PageableRequestDto;
import com.behpardakht.oauth_server.authorization.security.ClientContextHolder;
import com.behpardakht.oauth_server.authorization.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
@Aspect
@Component
@Order(0)
public class ClientContextAspect {

    @Before("execution(* com.behpardakht.oauth_server.authorization.service..*.findAll(..)) && args(request,..)")
    public void setClientContext(JoinPoint joinPoint, PageableRequestDto<BaseFilterDto> request) {
        if (request == null) {
            return;
        }

        try {
            if (request.getFilters() == null) {
                BaseFilterDto filter = createFilterInstance(joinPoint);
                if (filter != null) {
                    request.setFilters(filter);
                }
            }

            if (request.getFilters() != null && request.getFilters().getClientId() == null) {
                Long clientDbId = ClientContextHolder.getClientDbId();
                if (clientDbId == null && !SecurityUtils.isSuperAdmin()) {
                    throw new CustomException(ExceptionMessage.CLIENT_CONTEXT_REQUIRED);
                }
                request.getFilters().setClientId(clientDbId);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to set client context: {}", e.getMessage());
        }
    }

    private BaseFilterDto createFilterInstance(JoinPoint joinPoint) {
        try {
            // Get the method parameter types
            Type[] genericParameterTypes = joinPoint.getSignature().getDeclaringType()
                    .getDeclaredMethod(
                            joinPoint.getSignature().getName(),
                            getParameterTypes(joinPoint)
                    ).getGenericParameterTypes();

            // Find the PageableRequestDto parameter
            for (Type paramType : genericParameterTypes) {
                if (paramType instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) paramType;
                    if (pType.getRawType().equals(PageableRequestDto.class)) {
                        // Get the generic type (the filter DTO)
                        Type filterType = pType.getActualTypeArguments()[0];
                        if (filterType instanceof Class) {
                            Class<?> filterClass = (Class<?>) filterType;
                            if (BaseFilterDto.class.isAssignableFrom(filterClass)) {
                                return (BaseFilterDto) filterClass.getDeclaredConstructor().newInstance();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not create filter instance: {}", e.getMessage());
        }
        return null;
    }

    private Class<?>[] getParameterTypes(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        return types;
    }
}
