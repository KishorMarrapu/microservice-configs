package com.OTRAS.DemoProject.Controller;

import com.OTRAS.DemoProject.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(
            @RequestParam String token,
            @RequestParam String title,
            @RequestParam String body) {
        String response = notificationService.sendNotification(token, title, body);
        return ResponseEntity.ok(response);
    }
}
