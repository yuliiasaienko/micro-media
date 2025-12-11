package com.july.resourceservice.client;

import com.july.resourceservice.dto.SongMetadataRequest;
import com.july.resourceservice.exception.ExternalServiceException;
import java.util.List;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class SongServiceClient {

    private final RestClient songServiceRestClient;

    public void createMetadata(SongMetadataRequest request) {
        try {
            songServiceRestClient.post()
                    .uri("/songs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new ExternalServiceException("Failed to save song metadata: " + ex.getStatusCode().value(), ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Failed to reach song service", ex);
        }
    }

    public void deleteMetadata(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        StringJoiner joiner = new StringJoiner(",");
        ids.forEach(id -> joiner.add(String.valueOf(id)));
        try {
            songServiceRestClient.delete()
                    .uri(uriBuilder -> uriBuilder.path("/songs")
                            .queryParam("id", joiner.toString())
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new ExternalServiceException("Failed to delete song metadata: " + ex.getStatusCode().value(), ex);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Failed to reach song service", ex);
        }
    }
}
