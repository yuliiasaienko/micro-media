package com.july.resourceservice.dto;

public record SongMetadataRequest(
        Long id,
        String name,
        String artist,
        String album,
        String duration,
        String year
) {
}
