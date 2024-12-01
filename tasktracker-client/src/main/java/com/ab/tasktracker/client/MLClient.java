package com.ab.tasktracker.client;

import feign.Feign;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "MLClient", url = "http://bbhatia.asite.com/task")
public interface MLClient {

//  For Testing purpose : url = "https://1d5a-2402-a00-400-788f-7e69-4b02-9a17-cb37.ngrok-free.app"

    @GetMapping(value = "/callML/{taskName}", produces = MediaType.APPLICATION_JSON_VALUE)
    String getCategoryByTaskName(@PathVariable("taskName") String taskName, @RequestHeader("Authorization") String authToken);


}
