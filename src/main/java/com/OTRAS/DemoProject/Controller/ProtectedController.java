package com.OTRAS.DemoProject.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("You are authenticated! User ID from token works.");
    }
}