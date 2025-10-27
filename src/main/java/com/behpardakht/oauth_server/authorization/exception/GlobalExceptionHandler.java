package com.behpardakht.oauth_server.authorization.exception;

import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.AlreadyExistException;
import com.behpardakht.oauth_server.authorization.exception.ExceptionWrapper.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionDetailsDto> handleNotFoundException(NotFoundException notFoundException,
                                                                       WebRequest webRequest) {
        ExceptionDetailsDto errorDetails = new ExceptionDetailsDto(
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value(),
                LocalDate.now(),
                notFoundException.getMessage(),
                webRequest.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ExceptionDetailsDto> handleAlreadyExistException(AlreadyExistException alreadyExistException,
                                                                           WebRequest webRequest) {
        ExceptionDetailsDto errorDetails = new ExceptionDetailsDto(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                LocalDate.now(),
                alreadyExistException.getMessage(),
                webRequest.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        List<ObjectError> errorList = ex.getBindingResult().getAllErrors();
        errorList.forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}