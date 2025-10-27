package com.behpardakht.side_pay.auth.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;

public record ExceptionDetailsDto(HttpStatus error, int errorCode, LocalDate timestamp, String message, String path) {
}