package com.ab.tasktracker.exception;


import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.ab.tasktracker.dto.ExceptionResponse;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.validation.constraints.NotBlank;
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
        errorDetails.setCode(HttpStatus.BAD_REQUEST.toString());
        errorDetails.setMessage(errors.toString());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = MalformedJwtException.class)
    public ResponseEntity<ExceptionResponse> malformedJwtExceptionHandler(Exception e){
        ExceptionResponse errorDetails = new ExceptionResponse();
        errorDetails.setCode(HttpStatus.UNAUTHORIZED.toString());
        errorDetails.setMessage(TaskTrackerConstants.TOKEN_INVALID);
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionResponse> genericExceptionHandler(Exception e){
        ExceptionResponse errorDetails = new ExceptionResponse();
        errorDetails.setCode(HttpStatus.BAD_REQUEST.toString());
        errorDetails.setMessage(e.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

}
