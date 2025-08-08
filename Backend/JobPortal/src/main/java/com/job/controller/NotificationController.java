package com.job.controller;

import com.job.dto.NotificationDTO;
import com.job.entity.JobSeeker;
import com.job.entity.Notification;
import com.job.entity.User;
import com.job.repository.NotificationRepository;
import com.job.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(
            @RequestAttribute("user") JobSeeker jobSeeker
    ) {
        return ResponseEntity.ok(notificationService.getNotificationsFor(jobSeeker));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllNotifications(@RequestAttribute("user") User user) {
        notificationService.deleteAllNotificationsForUser(user);
        return ResponseEntity.ok("All notifications deleted successfully.");
    }



    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        notif.setSeen(true);
        notificationRepository.save(notif);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@RequestAttribute("user") JobSeeker jobSeeker) {
        int count = notificationRepository.countByRecipientAndSeenFalse(jobSeeker);
        return ResponseEntity.ok(count);
    }

}
