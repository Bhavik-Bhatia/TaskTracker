package com.ab.tasktracker.rest;

import com.ab.tasktracker.constants.TaskTrackerURI;
import com.ab.tasktracker.service.APITestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(TaskTrackerURI.TEST_URI)
public class APITestResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(APITestResource.class);

    @Autowired
    private APITestService testService;

    @GetMapping(value = TaskTrackerURI.TEST_API_PATH_VAR, produces = MediaType.APPLICATION_JSON_VALUE)
    public String testPathVariable(@PathVariable int value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key",value);
        LOGGER.debug("value received from client: {}", value);
        return jsonObject.toString();
    }

    @PostMapping(value = TaskTrackerURI.TEST_API_REQ_BODY, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String testReqBody(@RequestBody org.json.simple.JSONObject jsonObject) {
        LOGGER.debug("JSON received from client: {}", jsonObject);
        return jsonObject.toString();
    }

    @PostMapping(value = TaskTrackerURI.TEST_API_REQ_BODY_HEADERS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "custom-header")
    public String testReqBodyHeaders(@RequestBody org.json.simple.JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader("custom-header");
    }

    @PostMapping(value = TaskTrackerURI.TEST_API_REQ_BODY_PARAMETER, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, params = "userId")
    public String testReqBodyParams(@RequestParam HashMap<String,String> map) {
        return map.get("userId");
    }


    @GetMapping(value = TaskTrackerURI.TRANS_TEST_GET_API_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> transTestGet(@NotNull @RequestBody int testType) throws InterruptedException {
        LOGGER.debug("Enter in APITestResource.transTestGet()");
        testService.transReadOnly();
        testService.trans2ReadOnly();
        LOGGER.debug("Exit in APITestResource.transTestGet()");
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

    @GetMapping(value = TaskTrackerURI.CACHE_TEST_API_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> cacheTestSet(@NotNull @RequestBody int testType) throws InterruptedException {
        LOGGER.debug("Enter in APITestResource.cacheTestSet()");
        testService.callCacheService(testType);
        LOGGER.debug("Exit in APITestResource.cacheTestSet()");
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

    @GetMapping(value = TaskTrackerURI.TYPESENSE_TEST_API_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> typesenseTestSet(@NotNull @RequestBody int testType) throws InterruptedException {
        LOGGER.debug("Enter in APITestResource.typesenseTestSet()");
        testService.callTypesenseService(testType);
        LOGGER.debug("Exit in APITestResource.typesenseTestSet()");
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

}
