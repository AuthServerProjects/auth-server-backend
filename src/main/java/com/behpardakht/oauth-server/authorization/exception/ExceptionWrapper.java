package com.behpardakht.side_pay.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ExceptionWrapper {

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String resourceName, String fieldName, String fieldValue) {
            super(String.format("%s not found with %s : %s", resourceName, fieldName, fieldValue));
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class AlreadyExistException extends RuntimeException {
        public AlreadyExistException(String resourceName, String fieldValue) {
            super(String.format("%s is already exist : %s", resourceName, fieldValue));
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class IncorrectException extends RuntimeException {
        public IncorrectException(String resourceName) {
            super(String.format("%s is Incorrect", resourceName));
        }
    }

    @ResponseStatus(value = HttpStatus.REQUEST_TIMEOUT)
    public static class ExpireException extends RuntimeException {
        public ExpireException(String resourceName) {
            super(String.format("%s is Expired", resourceName));
        }
    }
}