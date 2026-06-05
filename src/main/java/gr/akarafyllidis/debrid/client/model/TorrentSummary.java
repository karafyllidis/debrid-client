package gr.akarafyllidis.debrid.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Torrent summary returned in the list of user torrents.
 */
public record TorrentSummary(
        String id,
        String filename,
        String hash,
        long bytes,
        String host,
        int split,
        int progress,
        TorrentStatus status,
        String added,
        List<String> links,
        String ended,
        Long speed,
        Integer seeders
) {}
