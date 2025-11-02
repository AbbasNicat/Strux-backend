package com.strux.notification_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url:http://localhost:8081}"
)
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}/email")
    String getUserEmail(@PathVariable("userId") String userId);


    @GetMapping("/api/users/{userId}/devices")
    List<String> getUserDeviceTokens(@PathVariable("userId") String userId);

    @PostMapping("/api/users/emails")
    Map<String, String> getUserEmails(@RequestBody List<String> userIds);
}
