package com.job.controller;

import com.job.dto.response.NotificationDTO;
import com.job.security.SecurityUtils;
import com.job.service.NotificationService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(notificationService.getNotificationsFor(jobSeekerId));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @DeleteMapping
    public ResponseEntity<?> deleteAllNotifications() {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        log.info("Deleting all notifications for user id: {}", jobSeekerId);
        notificationService.deleteAllNotificationsForUser(jobSeekerId);
        return ResponseEntity.ok("All notifications deleted successfully.");
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable @Positive Long id) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        log.info("Marking notification id: {} as read for user id: {}", id, jobSeekerId);
        notificationService.markAsRead(id, jobSeekerId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount() {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(notificationService.getUnreadCount(jobSeekerId));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications() {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(jobSeekerId));
    }
}
