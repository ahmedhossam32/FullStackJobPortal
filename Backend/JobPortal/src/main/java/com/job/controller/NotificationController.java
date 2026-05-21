package com.job.controller;

import com.job.dto.response.NotificationDTO;
import com.job.entity.JobSeeker;
import com.job.service.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(
            @RequestAttribute("user") JobSeeker jobSeeker
    ) {
        return ResponseEntity.ok(notificationService.getNotificationsFor(jobSeeker));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllNotifications(@RequestAttribute("user") JobSeeker jobSeeker) {
        notificationService.deleteAllNotificationsForUser(jobSeeker);
        return ResponseEntity.ok("All notifications deleted successfully.");
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @RequestAttribute("user") JobSeeker jobSeeker
    ) {
        notificationService.markAsRead(id, jobSeeker);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@RequestAttribute("user") JobSeeker jobSeeker) {
        return ResponseEntity.ok(notificationService.getUnreadCount(jobSeeker));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @RequestAttribute("user") JobSeeker jobSeeker
    ) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(jobSeeker));
    }

}
