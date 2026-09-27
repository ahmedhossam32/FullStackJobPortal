package com.job.mapper;

import com.job.dto.response.ApplicationResponseDTO;
import com.job.dto.response.ApplicationViewForEmployerDTO;
import com.job.entity.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "applicationId", source = "id")
    @Mapping(target = "username", source = "jobSeeker.username")
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "jobDescription", source = "job.description")
    @Mapping(target = "jobType", source = "job.type")
    @Mapping(target = "workMode", source = "job.workMode")
    @Mapping(target = "location", source = "job.location")
    @Mapping(target = "companyName", source = "job.employer.companyName")
    @Mapping(target = "companyLogoUrl", source = "job.employer.profilePictureUrl")
    ApplicationResponseDTO toResponseDTO(Application application);

    @Mapping(target = "applicantUsername", source = "jobSeeker.username")
    @Mapping(target = "applicantEmail", source = "jobSeeker.email")
    @Mapping(target = "applicantDOB", source = "jobSeeker.dob")
    @Mapping(target = "applicantName", source = "jobSeeker.name")
    @Mapping(target = "applicantProfilePicture", source = "jobSeeker.profilePictureUrl")
    @Mapping(target = "jobTitle", source = "job.title")
    ApplicationViewForEmployerDTO toEmployerView(Application application);
}
