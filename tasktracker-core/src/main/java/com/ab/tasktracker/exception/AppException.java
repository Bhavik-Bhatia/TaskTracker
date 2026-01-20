package com.ab.tasktracker.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class AppException extends Exception {

    private String traceID;

    private String errorCode;

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode.name();
        this.traceID = UUID.randomUUID().toString();
    }
}
