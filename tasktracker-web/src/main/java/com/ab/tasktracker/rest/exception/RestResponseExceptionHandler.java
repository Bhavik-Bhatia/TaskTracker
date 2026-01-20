package com.ab.tasktracker.rest.exception;


import com.ab.tasktracker.exception.AppException;
import com.ab.tasktracker.exception.ErrorCode;
import com.ab.tasktracker.exception.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class RestResponseExceptionHandler {


    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ExceptionResponse> methodArgumentNotValidExceptionExceptionHandler(MethodArgumentNotValidException exception){
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        StringBuilder errors = new StringBuilder();
        for (FieldError fieldError:fieldErrors){
            errors.append(fieldError.getDefaultMessage());
            errors.append("; ");
        }
        ExceptionResponse errorDetails = new ExceptionResponse();
        errorDetails.setErrorCode(ErrorCode.METHOD_ARGUMENT_NOT_VALID.name());
        errorDetails.setMessage(errors.toString());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ExceptionResponse> genericAppExceptionHandler(AppException e) {
        ExceptionResponse errorDetails = new ExceptionResponse();
        errorDetails.setErrorCode(e.getErrorCode());
        errorDetails.setTraceId(e.getTraceID());
        errorDetails.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionResponse> genericExceptionHandler(Exception e) {
        ExceptionResponse errorDetails = new ExceptionResponse();
        errorDetails.setErrorCode(ErrorCode.INTERNAL_ERROR.name());
        errorDetails.setTraceId("generic-exception");
        errorDetails.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
