package gr.akarafyllidis.debrid.client.model;

/**
 * A file entry within a torrent.
 */
public record TorrentFile(
        int id,
        String path,
        long bytes,
        int selected
) {}
