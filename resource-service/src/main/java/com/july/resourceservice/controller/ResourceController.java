package com.july.resourceservice.controller;

import com.july.resourceservice.dto.DeleteResponseDto;
import com.july.resourceservice.dto.ResourceResponseDto;
import com.july.resourceservice.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping(consumes = MediaType.ALL_VALUE)
    public ResponseEntity<ResourceResponseDto> uploadResource(
            @RequestBody byte[] fileBytes,
            @RequestHeader(value = "Content-Type", required = false) String contentType) {
        ResourceResponseDto response = resourceService.save(fileBytes, contentType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable Long id) {
        byte[] data = resourceService.getResource(id);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(data);
    }

    @DeleteMapping
    public ResponseEntity<DeleteResponseDto> deleteResources(@RequestParam("id") String ids) {
        DeleteResponseDto response = resourceService.delete(ids);
        return ResponseEntity.ok(response);
    }
}
