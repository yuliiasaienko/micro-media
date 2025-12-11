package com.july.resourceservice.service;

import com.july.resourceservice.client.SongServiceClient;
import com.july.resourceservice.dto.DeleteResponseDto;
import com.july.resourceservice.dto.ResourceResponseDto;
import com.july.resourceservice.dto.SongMetadataRequest;
import com.july.resourceservice.entity.ResourceEntity;
import com.july.resourceservice.exception.BadRequestException;
import com.july.resourceservice.exception.NotFoundException;
import com.july.resourceservice.mapper.ResourceMapper;
import com.july.resourceservice.repository.ResourceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private static final int MAX_CSV_LENGTH = 200;

    private final ResourceRepository repository;
    private final ResourceMapper mapper;
    private final Mp3MetadataExtractor metadataExtractor;
    private final SongServiceClient songServiceClient;

    @Transactional
    public ResourceResponseDto save(byte[] data, String contentType) {
        // Parse and validate MP3 tags before persisting the binary
        SongMetadataRequest metadata = metadataExtractor.extractMetadata(data, contentType);
        ResourceEntity entity = new ResourceEntity();
        entity.setData(data);
        ResourceEntity saved = repository.save(entity);

        // Forward extracted tags to Song Service with the freshly created resource id
        SongMetadataRequest payload = new SongMetadataRequest(
                saved.getId(),
                metadata.name(),
                metadata.artist(),
                metadata.album(),
                metadata.duration(),
                metadata.year()
        );
        songServiceClient.createMetadata(payload);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public byte[] getResource(Long id) {
        validateId(id);
        ResourceEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource with ID=" + id + " not found"));
        return entity.getData();
    }

    @Transactional
    public DeleteResponseDto delete(String idsCsv) {
        List<Long> ids = parseIds(idsCsv);
        if (ids.isEmpty()) {
            return new DeleteResponseDto(List.of());
        }
        List<ResourceEntity> found = repository.findAllById(ids);
        if (!found.isEmpty()) {
            repository.deleteAll(found);
            List<Long> deletedIds = found.stream().map(ResourceEntity::getId).toList();
            // Cascade delete song metadata for removed resources
            songServiceClient.deleteMetadata(deletedIds);
            return new DeleteResponseDto(deletedIds);
        }
        return new DeleteResponseDto(List.of());
    }

    private List<Long> parseIds(String idsCsv) {
        if (idsCsv == null || idsCsv.isBlank()) {
            throw new BadRequestException("Id list must not be empty");
        }
        if (idsCsv.length() > MAX_CSV_LENGTH) {
            throw new BadRequestException(
                    "CSV string is too long: received " + idsCsv.length()
                            + " characters, maximum allowed is " + MAX_CSV_LENGTH);
        }
        String[] tokens = idsCsv.split(",");
        Set<Long> ids = new java.util.LinkedHashSet<>();
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty() || !trimmed.matches("^\\d+$")) {
                throw new BadRequestException("Invalid ID format: '" + trimmed + "'. Only positive integers are allowed");
            }
            Long id = Long.parseLong(trimmed);
            if (id <= 0) {
                throw new BadRequestException("Invalid value '" + id + "' for ID. Must be a positive integer");
            }
            ids.add(id);
        }
        return new ArrayList<>(ids);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            String value = id == null ? "null" : id.toString();
            throw new BadRequestException("Invalid value '" + value + "' for ID. Must be a positive integer");
        }
    }
}
