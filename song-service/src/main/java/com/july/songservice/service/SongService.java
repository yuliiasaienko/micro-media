package com.july.songservice.service;

import com.july.songservice.dto.DeleteResponseDto;
import com.july.songservice.dto.SongIdResponseDto;
import com.july.songservice.dto.SongRequestDto;
import com.july.songservice.dto.SongResponseDto;
import com.july.songservice.entity.SongEntity;
import com.july.songservice.exception.BadRequestException;
import com.july.songservice.exception.ConflictException;
import com.july.songservice.exception.NotFoundException;
import com.july.songservice.mapper.SongMapper;
import com.july.songservice.repository.SongRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SongService {
    private static final int MAX_CSV_LENGTH = 200;
    private final SongRepository repository;
    private final SongMapper mapper;

    @Transactional
    public SongIdResponseDto create(SongRequestDto dto) {
        validateId(dto.id());
        if (repository.existsById(dto.id())) {
            throw new ConflictException("Metadata for resource ID=" + dto.id() + " already exists");
        }
        // Persist metadata exactly as provided (Resource Service already sanitized tags)
        SongEntity saved = repository.save(mapper.toEntity(dto));
        return new SongIdResponseDto(saved.getId());
    }

    @Transactional(readOnly = true)
    public SongResponseDto getById(Long id) {
        validateId(id);
        SongEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Song metadata for ID=" + id + " not found"));
        return mapper.toDto(entity);
    }

    @Transactional
    public DeleteResponseDto deleteByIds(String idsCsv) {
        List<Long> ids = parseIds(idsCsv);
        if (ids.isEmpty()) {
            return new DeleteResponseDto(List.of());
        }
        List<SongEntity> foundEntities = repository.findAllById(ids);
        if (!foundEntities.isEmpty()) {
            repository.deleteAll(foundEntities);
        }
        List<Long> deleted = foundEntities.stream()
                .map(SongEntity::getId)
                .toList();
        // Return only ids we truly removed; missing ones are ignored per spec
        return new DeleteResponseDto(deleted);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            String value = id == null ? "null" : id.toString();
            throw new BadRequestException("Invalid value '" + value + "' for ID. Must be a positive integer");
        }
    }

    private List<Long> parseIds(String idsCsv) {
        if (idsCsv == null || idsCsv.isBlank()) {
            throw new BadRequestException("Id list must not be empty");
        }
        if (idsCsv.length() > MAX_CSV_LENGTH) {
            throw new BadRequestException("CSV string is too long: received " + idsCsv.length()
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
}
