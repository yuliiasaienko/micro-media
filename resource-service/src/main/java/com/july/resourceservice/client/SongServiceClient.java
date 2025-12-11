package com.july.resourceservice.client;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.july.resourceservice.dto.SongMetadataRequest;
import com.july.resourceservice.exception.ExternalServiceException;
import java.util.List;
import java.util.StringJoiner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class SongServiceClient {

    private final RestClient songServiceRestClient;
    private final String songsPath;

    public SongServiceClient(RestClient songServiceRestClient,
                             @Value("${song.service.path:/songs}") String songsPath) {
        this.songServiceRestClient = songServiceRestClient;
        this.songsPath = songsPath;
    }

    public void createMetadata(SongMetadataRequest request) {
        try {
            songServiceRestClient.post()
                    .uri(songsPath)
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
        if (isEmpty(ids)) {
            return;
        }
        StringJoiner joiner = new StringJoiner(",");
        ids.forEach(id -> joiner.add(String.valueOf(id)));
        try {
            songServiceRestClient.delete()
                    .uri(uriBuilder -> uriBuilder.path(songsPath)
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
