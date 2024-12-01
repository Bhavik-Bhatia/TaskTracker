package com.ab.tasktracker.exception;

public class InvalidPasswordException extends RuntimeException{
    public InvalidPasswordException(String exceptionMessage){
        super(exceptionMessage);
    }
}
