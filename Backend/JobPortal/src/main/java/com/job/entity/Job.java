package com.job.entity;

import com.job.enums.JobType;
import com.job.enums.WorkMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 5000)
    private String description;

    private String location;

    @Enumerated(EnumType.STRING)
    private JobType type;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode;

    private LocalDateTime postedAt;

    @ManyToOne
    private Employer employer;

    @ElementCollection
    private List<String> responsibilities;

    @ElementCollection
    private List<String> requiredSkills;

    @ElementCollection
    private List<String> screeningQuestions;


    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Application> applications;
}
