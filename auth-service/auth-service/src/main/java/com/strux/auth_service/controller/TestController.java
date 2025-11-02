package com.strux.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint - no auth required");
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String email = jwt.getClaim("email");
        return ResponseEntity.ok("Protected endpoint accessed! UserId: " + userId + ", Email: " + email);
    }

    @GetMapping("/worker")
    @PreAuthorize("hasAuthority('WORKER')")
    public ResponseEntity<String> workerEndpoint(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok("Worker endpoint accessed successfully! User: " + jwt.getClaim("email"));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<String> managerEndpoint(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok("Manager endpoint accessed successfully! User: " + jwt.getClaim("email"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> adminEndpoint(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok("Admin endpoint accessed successfully! User: " + jwt.getClaim("email"));
    }
}
