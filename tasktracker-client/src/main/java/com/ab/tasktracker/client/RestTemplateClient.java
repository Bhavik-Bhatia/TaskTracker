package com.ab.tasktracker.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestTemplateClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String callAPIPathVariable(String url, HttpServletRequest httpServletRequest) {
        // Set up headers
        HttpHeaders headers = new HttpHeaders();
//      headers.set("Authorization", httpServletRequest.getHeader("Authorization"));
        headers.set("Authorization", "Bearer new_key");
        headers.set("DeviceId", httpServletRequest.getHeader("DeviceId"));

        // Create the request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Make the POST request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        return response.getBody();
    }
}
