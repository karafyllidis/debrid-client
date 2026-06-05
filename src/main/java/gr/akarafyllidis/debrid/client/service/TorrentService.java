package gr.akarafyllidis.debrid.client.service;

import gr.akarafyllidis.debrid.client.model.ActiveCount;
import gr.akarafyllidis.debrid.client.model.AddedTorrent;
import gr.akarafyllidis.debrid.client.model.AvailableHost;
import gr.akarafyllidis.debrid.client.model.TorrentDetails;
import gr.akarafyllidis.debrid.client.model.TorrentSummary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class TorrentService {

    private final RestClient restClient;

    public TorrentService(RestClient realDebridRestClient) {
        this.restClient = realDebridRestClient;
    }

    /**
     * GET /torrents
     * Returns the list of the user's torrents.
     *
     * @param offset     Starting offset (optional)
     * @param page       Page number (optional)
     * @param limit      Entries per page, max 100 (optional)
     * @param filter     "active" to filter active torrents only (optional)
     */
    public List<TorrentSummary> listTorrents(Integer offset, Integer page, Integer limit, String filter) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/torrents");
                    if (offset != null) uriBuilder.queryParam("offset", offset);
                    if (page != null) uriBuilder.queryParam("page", page);
                    if (limit != null) uriBuilder.queryParam("limit", limit);
                    if (filter != null) uriBuilder.queryParam("filter", filter);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * GET /torrents/info/{id}
     * Returns information about a specific torrent.
     */
    public TorrentDetails getTorrentInfo(String id) {
        return restClient.get()
                .uri("/torrents/info/{id}", id)
                .retrieve()
                .body(TorrentDetails.class);
    }

    /**
     * GET /torrents/instantAvailability/{hash}
     * Returns instant availability of a torrent by its hash(es).
     * The response structure is dynamic: hash → hoster → list of file variant sets.
     *
     * @param hashes One or more SHA1 hashes separated by "/"
     */
    public Map<String, Object> getInstantAvailability(String... hashes) {
        String hashPath = String.join("/", hashes);
        return restClient.get()
                .uri("/torrents/instantAvailability/{hashes}", hashPath)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * GET /torrents/availableHosts
     * Returns the list of hosts available for torrent file uploading.
     */
    public List<AvailableHost> getAvailableHosts() {
        return restClient.get()
                .uri("/torrents/availableHosts")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * GET /torrents/activeCount
     * Returns the number of currently active torrents and the account limit.
     */
    public ActiveCount getActiveCount() {
        return restClient.get()
                .uri("/torrents/activeCount")
                .retrieve()
                .body(ActiveCount.class);
    }

    /**
     * PUT /torrents/addTorrent
     * Adds a .torrent file. The binary content of the file is passed as the request body.
     *
     * @param torrentBytes Raw bytes of the .torrent file
     * @param host         Optional hoster domain to use
     */
    public AddedTorrent addTorrent(byte[] torrentBytes, String host) {
        return restClient.put()
                .uri(uriBuilder -> {
                    uriBuilder.path("/torrents/addTorrent");
                    if (host != null) uriBuilder.queryParam("host", host);
                    return uriBuilder.build();
                })
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(torrentBytes)
                .retrieve()
                .body(AddedTorrent.class);
    }

    /**
     * POST /torrents/addMagnet
     * Adds a magnet link.
     *
     * @param magnet Magnet link URI
     * @param host   Optional hoster domain to use
     */
    public AddedTorrent addMagnet(String magnet, String host) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("magnet", magnet);
        if (host != null) form.add("host", host);

        return restClient.post()
                .uri("/torrents/addMagnet")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(AddedTorrent.class);
    }

    /**
     * POST /torrents/selectFiles/{id}
     * Selects the files to download from a torrent.
     *
     * @param id    Torrent ID
     * @param files Comma-separated list of file IDs, or "all" to select all files
     */
    public void selectFiles(String id, String files) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("files", files);

        restClient.post()
                .uri("/torrents/selectFiles/{id}", id)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * DELETE /torrents/delete/{id}
     * Deletes a torrent from the user's account.
     */
    public void deleteTorrent(String id) {
        restClient.delete()
                .uri("/torrents/delete/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
