package com.ab.tasktracker.constants;

public interface TaskTrackerURI {
    String TASK_URI = "task";
    String TEST_URI = "test";
    String ADD_TASK_URI = "/addtask";
    String GET_TASK_URI = "/gettask";
    String GET_ALL_ME_TASK_URI = "/getAllTasks";
    String REMOVE_TASK_URI = "/removetask";
    String TRANS_TEST_GET_API_URI = "/test-get-api";
    String CACHE_TEST_API_URI = "/test-cache-api";
    String TYPESENSE_TEST_API_URI = "/test-typesense-api";
    String TEST_API_PATH_VAR = "/test-path-variable/{value}";
    String TEST_API_REQ_BODY = "/test-request-body";
    String TEST_API_REQ_BODY_HEADERS = "/test-request-body-headers";
    String TEST_API_REQ_BODY_PARAMETER = "/test-request-body-paramters";
}
