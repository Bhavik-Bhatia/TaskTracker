package com.ab.tasktracker.constants;

public interface TaskTrackerConstants {
    String INVALID_EMAIL_MESSAGE = "Invalid email format!";
    String EMAIL_REQUIRED_MESSAGE = "email is required!";
    String PASSWORD_REQUIRED_MESSAGE = "Password is required!";
    String DEVICE_ID_REQUIRED_MESSAGE = "Device identifier is required!";
    String PASSWORD_VALIDATION_MESSAGE = "Password must be between 8 and 20 characters long";
    String USERNAME_REQUIRED_MESSAGE = "Username is required!";
    String TASK_NAME_REQUIRED_MESSAGE = "Task name is required!";
    String TASK_STATUS_NAME_REQUIRED_MESSAGE = "Task status name is required!";
    String PARENT_TASK_NAME_REQUIRED_MESSAGE = "Parent task id is required!";
    String TASK_PRIORITY_REQUIRED_MESSAGE = "Task priority is required!";
    String TASK_COMPLETION_DATE_REQUIRED_MESSAGE = "Task completion date is required!";
    String TASK_START_DATE_REQUIRED_MESSAGE = "Task start date is required!";
    String TASK_DUE_DATE_REQUIRED_MESSAGE = "Task due date is required!";
    String TASK_DATE_FORMAT_INVALID = "Task date format required is yyyy-MM-dd HH:mm:ss.SSSSSSZ!";
    String TASK_DATE_PRESENT_AND_FUTURE = "Task date should be in present and future!";
    String TASK_NOT_EXISTS = "Task with this Id does not exist for this user";
    String MOBILE_NUMBER_REQUIRED_MESSAGE = "Mobile Number is required!";
    String USER_ALREADY_EXISTS_MESSAGE = "User already exists!";
    String USER_DOES_NOT_EXIST_MESSAGE = "User does not exist!";
    String INVALID_PASSWORD_MESSAGE = "Password is invalid!";
    String TOKEN_INVALID = "Invalid authorization header!";
    String TOKEN_EXPIRED = "User token expired!";
    String AUTHENTICATION_REQUIRED = "User needs to be authenticated!";
    String UNIDENTIFIED_DEVICE = "User device is not recognizable!";
    String CACHE_USER_DETAILS = "#usd";
    String CACHE_TASK_DETAILS = "#td";
    String CACHE_DEVICE_DETAILS = "#udd";
    String TASKTRACKER_SERVICE_NAME = "task-tracker";
}
