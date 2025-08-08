package com.job.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employer extends User {

    private String companyName;

    private String industry;

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Job> jobs;
}

