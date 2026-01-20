package com.ab.tasktracker.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ExceptionResponse {
    private String errorCode;
    private String traceId;
    private String message;
}
