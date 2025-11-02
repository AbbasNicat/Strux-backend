package com.strux.notification_service.controller;

import com.strux.notification_service.kafka.NotificationEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final NotificationEventHandler eventHandler;

    @PostMapping("/trigger-issue-created")
    public String testIssueCreated(@RequestBody Map<String, Object> event) {
        eventHandler.handleIssueCreated(event);
        return "Event triggered! Check Mailtrap inbox.";
    }

    @PostMapping("/trigger-user-registered")
    public String testUserRegistered(@RequestBody Map<String, Object> event) {
        eventHandler.handleUserRegistered(event);
        return "User registered event triggered!";
    }
}
