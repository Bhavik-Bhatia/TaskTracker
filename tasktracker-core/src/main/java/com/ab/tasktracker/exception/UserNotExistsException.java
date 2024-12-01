package com.ab.tasktracker.exception;


public class UserNotExistsException extends RuntimeException{
    public UserNotExistsException(String exceptionMessage){
        super(exceptionMessage);
    }
}
