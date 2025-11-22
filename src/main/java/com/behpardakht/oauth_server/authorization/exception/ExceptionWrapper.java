package com.behpardakht.oauth_server.authorization.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ExceptionWrapper {

    @Getter
    @Setter
    public static class CustomException extends RuntimeException {
        private final ExceptionMessages exceptionMessage;
        private Object[] params;
        private Exception cause;

        public CustomException(ExceptionMessages exceptionMessage, Exception e, Object... params) {
            this.exceptionMessage = exceptionMessage;
            this.params = params;
            this.cause = e;
        }

        public CustomException(ExceptionMessages exceptionMessage, Object... params) {
            this.exceptionMessage = exceptionMessage;
            this.params = params;
        }

        public CustomException(ExceptionMessages exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public static class NotFoundException extends CustomException {
        public NotFoundException(String resourceName, String fieldName, String fieldValue) {
            super(ExceptionMessages.NOT_FOUND_WITH, resourceName, fieldName, fieldValue);
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class AlreadyExistException extends CustomException {
        public AlreadyExistException(String resourceName, String fieldValue) {
            super(ExceptionMessages.IS_ALREADY_EXIST, resourceName, fieldValue);
        }
    }
}