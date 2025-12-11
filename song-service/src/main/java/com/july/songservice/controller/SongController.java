package com.july.songservice.controller;

import com.july.songservice.dto.DeleteResponseDto;
import com.july.songservice.dto.SongIdResponseDto;
import com.july.songservice.dto.SongRequestDto;
import com.july.songservice.dto.SongResponseDto;
import com.july.songservice.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public ResponseEntity<SongIdResponseDto> create(@Valid @RequestBody SongRequestDto requestDto) {
        SongIdResponseDto created = songService.create(requestDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponseDto> get(@PathVariable Long id) {
        SongResponseDto response = songService.getById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<DeleteResponseDto> delete(@RequestParam("id") String ids) {
        DeleteResponseDto response = songService.deleteByIds(ids);
        return ResponseEntity.ok(response);
    }
}
