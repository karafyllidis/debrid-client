package gr.akarafyllidis.debrid.client.model;

/**
 * Response returned when a torrent or magnet link is successfully added.
 */
public record AddedTorrent(
        String id,
        String uri
) {}
