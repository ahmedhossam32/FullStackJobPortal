package com.job.entity;

import com.job.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Job job;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status=ApplicationStatus.PENDING;

    private LocalDateTime appliedAt;

    @ManyToOne
    private JobSeeker jobSeeker;

    private String resumeUrl;


}
