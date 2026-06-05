package gr.akarafyllidis.debrid.client.service;

import gr.akarafyllidis.debrid.client.model.DownloadSummary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class DownloadService {

    private final RestClient restClient;

    public DownloadService(RestClient realDebridRestClient) {
        this.restClient = realDebridRestClient;
    }

    /**
     * GET /downloads
     * Returns the list of the user's downloads.
     *
     * @param offset Starting offset (optional)
     * @param page   Page number (optional)
     * @param limit  Entries per page, max 100 (optional)
     */
    public List<DownloadSummary> listDownloads(Integer offset, Integer page, Integer limit) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/downloads");
                    if (offset != null) uriBuilder.queryParam("offset", offset);
                    if (page != null) uriBuilder.queryParam("page", page);
                    if (limit != null) uriBuilder.queryParam("limit", limit);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * DELETE /downloads/delete/{id}
     * Deletes a download from the user's account.
     */
    public void deleteDownload(String id) {
        restClient.delete()
                .uri("/downloads/delete/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
