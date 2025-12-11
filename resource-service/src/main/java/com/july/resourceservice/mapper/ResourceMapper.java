package com.july.resourceservice.mapper;

import com.july.resourceservice.dto.ResourceResponseDto;
import com.july.resourceservice.entity.ResourceEntity;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {
    public ResourceResponseDto toDto(ResourceEntity entity) {
        return new ResourceResponseDto(entity.getId());
    }
}

