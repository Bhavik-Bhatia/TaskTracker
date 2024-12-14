package com.ab.tasktracker.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(value = "email", url = "http://bbhatia.asite.com:8080/notification")
public interface EmailClient {

//  For Testing purpose : url = "https://1d5a-2402-a00-400-788f-7e69-4b02-9a17-cb37.ngrok-free.app"

    @PostMapping(value = "/sendemail", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Boolean> sendEmail(@RequestParam Map<String, String> mailParam);


}
