package gr.akarafyllidis.debrid.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Detailed torrent information returned by GET /torrents/info/{id}.
 */
public record TorrentDetails(
        String id,
        String filename,
        @JsonProperty("original_filename") String originalFilename,
        String hash,
        long bytes,
        @JsonProperty("original_bytes") long originalBytes,
        String host,
        int split,
        int progress,
        TorrentStatus status,
        String added,
        List<TorrentFile> files,
        List<String> links,
        String ended,
        Long speed,
        Integer seeders
) {}
