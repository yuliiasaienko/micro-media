package com.july.songservice.dto;

public record SongResponseDto(
        Long id,
        String name,
        String artist,
        String album,
        String duration,
        String year
) {
}
