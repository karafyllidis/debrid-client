package gr.akarafyllidis.debrid.client.model;

/**
 * Download entry returned in the user's downloads list.
 */
public record DownloadSummary(
        String id,
        String filename,
        String mimeType,
        long filesize,
        String link,
        String host,
        int chunks,
        String download,
        String generated,
        String type
) {
}
