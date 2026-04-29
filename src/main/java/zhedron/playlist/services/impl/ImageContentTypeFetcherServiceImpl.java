package zhedron.playlist.services.impl;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import zhedron.playlist.services.ImageContentTypeFetcherService;

@Service
public class ImageContentTypeFetcherServiceImpl implements ImageContentTypeFetcherService {
    private final WebClient webClient = WebClient.builder().build();

    @Override
    public String getImageContentType(String imageUrl) {
        return webClient
                .head()
                .uri(imageUrl)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    HttpHeaders headers = response.getHeaders();
                    MediaType mediaType = headers.getContentType();

                    return mediaType != null ? mediaType.toString() : null;
                })
                .block();
    }
}
