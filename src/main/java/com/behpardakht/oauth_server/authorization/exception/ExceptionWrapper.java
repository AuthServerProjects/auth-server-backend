package com.behpardakht.oauth_server.authorization.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ExceptionWrapper {

    @Getter
    public static class CustomException extends RuntimeException {
        private final ExceptionMessage exceptionMessage;
        private final Object[] params;

        public CustomException(ExceptionMessage exceptionMessage, Throwable cause, Object... params) {
            super(cause);
            this.exceptionMessage = exceptionMessage;
            this.params = params;
        }

        public CustomException(ExceptionMessage exceptionMessage, Object... params) {
            this.exceptionMessage = exceptionMessage;
            this.params = params;
        }

        public CustomException(ExceptionMessage exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
            this.params = new Object[0];
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public static class NotFoundException extends CustomException {
        public NotFoundException(String resourceName, String fieldName, String fieldValue) {
            super(ExceptionMessage.NOT_FOUND_WITH, resourceName, fieldName, fieldValue);
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class AlreadyExistException extends CustomException {
        public AlreadyExistException(String resourceName, String fieldValue) {
            super(ExceptionMessage.IS_ALREADY_EXIST, resourceName, fieldValue);
        }
    }
}