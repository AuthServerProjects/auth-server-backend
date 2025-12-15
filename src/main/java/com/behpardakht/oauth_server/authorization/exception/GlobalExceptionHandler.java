package com.behpardakht.oauth_server.authorization.exception;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.base.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseDto<?>> handleNotFoundException(NotFoundException exception) {
        ResponseDto<?> responseDto = getResponseDto(exception, HttpStatus.NOT_FOUND);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ResponseDto<?>> handleAlreadyExistException(AlreadyExistException exception) {
        ResponseDto<?> responseDto = getResponseDto(exception, HttpStatus.BAD_REQUEST);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<?>> handleCustomException(CustomException exception) {
        ResponseDto<?> responseDto = getResponseDto(exception, HttpStatus.BAD_REQUEST);
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }

    private ResponseDto<?> getResponseDto(CustomException exception, HttpStatus error) {
        log.error("thrown exception with message: {}",
                exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage());
        String message = MessageResolver.getMessage(exception.getExceptionMessage().getMessage(), exception.getParams());
        return ResponseDto.failed(error.name(), message, null);
    }

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ResponseDto<?>> handleAccessDeniedException(Exception exception) {
        log.warn("Access denied: {}", exception.getMessage());
        String exMessage = ExceptionMessage.ACCESS_DENIED.getMessage();
        String message = MessageResolver.getMessage(exMessage);
        ResponseDto<?> responseDto = ResponseDto.failed(exMessage, message, null);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }

    @ExceptionHandler({
            AuthenticationException.class,
            BadCredentialsException.class,
            InsufficientAuthenticationException.class,
            AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<ResponseDto<?>> handleAuthenticationException(Exception exception) {
        log.warn("Authentication failed: {}", exception.getMessage());
        String message = MessageResolver.getMessage(ExceptionMessage.AUTHENTICATION_FAILED_CREDENTIALS.getMessage());
        ResponseDto<?> responseDto = ResponseDto.failed(
                ExceptionMessage.AUTHENTICATION_FAILED_CREDENTIALS.getMessage(), message, null);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handleGeneralException(Exception exception) {
        String message = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();
        log.error("thrown exception with message: {}", message);
        String localizedMessage = MessageResolver.getMessage(ExceptionMessage.GENERAL_ERROR.getMessage());
        ResponseDto<?> responseDto = ResponseDto.failed(
                HttpStatus.INTERNAL_SERVER_ERROR.name(), localizedMessage, null);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<?>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation exception: {}", ex.getMessage());

        List<String> errorMessages = ex.getBindingResult().getAllErrors().stream().filter(Objects::nonNull)
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        ResponseDto<?> responseDto =
                ResponseDto.failed(
                        ExceptionMessage.INPUTS_ARE_NOT_VALID.name(),
                        MessageResolver.getMessage(ExceptionMessage.INPUTS_ARE_NOT_VALID.getMessage()),
                        errorMessages);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseDto);
    }
}