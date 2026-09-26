package com.job.mapper;

import com.job.dto.request.JobRequestDTO;
import com.job.dto.response.JobResponseDTO;
import com.job.entity.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "companyName", source = "employer.companyName")
    @Mapping(target = "profilePicture", source = "employer.profilePictureUrl")
    @Mapping(target = "employerId", source = "employer.id")
    @Mapping(target = "responsibilities", source = "responsibilities", qualifiedByName = "nullToEmptyList")
    @Mapping(target = "requiredSkills", source = "requiredSkills", qualifiedByName = "nullToEmptyList")
    @Mapping(target = "screeningQuestions", source = "screeningQuestions", qualifiedByName = "nullToEmptyList")
    JobResponseDTO toResponseDTO(Job job);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true)
    @Mapping(target = "postedAt", ignore = true)
    @Mapping(target = "applications", ignore = true)
    Job toEntity(JobRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true)
    @Mapping(target = "postedAt", ignore = true)
    @Mapping(target = "applications", ignore = true)
    void updateJobFromDto(JobRequestDTO dto, @MappingTarget Job job);

    @Named("nullToEmptyList")
    default List<String> nullToEmptyList(List<String> source) {
        return source != null ? new ArrayList<>(source) : new ArrayList<>();
    }
}
