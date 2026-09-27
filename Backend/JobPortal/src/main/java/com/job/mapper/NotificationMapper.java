package com.job.mapper;

import com.job.dto.response.NotificationDTO;
import com.job.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "applicationId", source = "application.id")
    @Mapping(target = "companyLogoUrl", source = "application.job.employer.profilePictureUrl")
    NotificationDTO toDTO(Notification notification);
}
