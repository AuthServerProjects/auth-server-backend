package com.behpardakht.oauth_server.authorization.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;

public record ExceptionDetailsDto(HttpStatus error, int errorCode, LocalDate timestamp, String message, String path) {
}