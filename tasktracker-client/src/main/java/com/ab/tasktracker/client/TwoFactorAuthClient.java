package com.ab.tasktracker.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(url = "localhost:8181/2fa/")
public interface TwoFactorAuthClient {
}
