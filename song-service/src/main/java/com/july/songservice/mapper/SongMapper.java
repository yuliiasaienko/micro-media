package com.july.songservice.mapper;

import com.july.songservice.dto.SongRequestDto;
import com.july.songservice.dto.SongResponseDto;
import com.july.songservice.entity.SongEntity;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {

    public SongEntity toEntity(SongRequestDto dto) {
        SongEntity entity = new SongEntity();
        entity.setId(dto.id());
        entity.setName(dto.name());
        entity.setArtist(dto.artist());
        entity.setAlbum(dto.album());
        entity.setDuration(dto.duration());
        entity.setYear(dto.year());
        return entity;
    }

    public SongResponseDto toDto(SongEntity entity) {
        return new SongResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getArtist(),
                entity.getAlbum(),
                entity.getDuration(),
                entity.getYear()
        );
    }
}
