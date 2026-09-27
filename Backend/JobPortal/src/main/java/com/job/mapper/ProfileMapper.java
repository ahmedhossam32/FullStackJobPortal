package com.job.mapper;

import com.job.dto.response.EmployerProfileDTO;
import com.job.dto.response.JobSeekerProfileDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "profilePicture", source = "profilePictureUrl")
    @Mapping(target = "resume", source = "resumeUrl")
    JobSeekerProfileDTO toJobSeekerDTO(JobSeeker jobSeeker);

    @Mapping(target = "profilePicture", source = "profilePictureUrl")
    EmployerProfileDTO toEmployerDTO(Employer employer);
}
