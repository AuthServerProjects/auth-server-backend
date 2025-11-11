package com.behpardakht.oauth_server.authorization.exception;

import com.behpardakht.oauth_server.authorization.config.bundle.MessageResolver;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.CustomException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import com.behpardakht.oauth_server.authorization.model.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<?>> handleCustomException(CustomException exception) {
        ResponseDto<?> responseDto = getResponseDto(exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseDto<?>> handleNotFoundException(NotFoundException exception) {
        ResponseDto<?> responseDto = getResponseDto(exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ResponseDto<?>> handleAlreadyExistException(AlreadyExistException exception) {
        ResponseDto<?> responseDto = getResponseDto(exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    private ResponseDto<?> getResponseDto(CustomException exception) {
        log.error("thrown exception with message: {}",
                exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage());
        String message = MessageResolver.getMessage(exception.getExceptionMessage().getMessage(), exception.getParams());
        return ResponseDto.failed(message, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handleGeneralException(Exception exception) {
        String message = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();
        log.error("thrown exception with message: {}", message);
        ResponseDto<?> responseDto = ResponseDto.failed("An error occurred. Please try again.", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<?>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation exception: {}", ex.getMessage());

        List<String> errorMessages = ex.getBindingResult().getAllErrors().stream().filter(Objects::nonNull)
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        ResponseDto<?> responseDto =
                ResponseDto.failed(
                        MessageResolver.getMessage(ExceptionMessages.INPUTS_ARE_NOT_VALID.getMessage()),
                        errorMessages);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }
}