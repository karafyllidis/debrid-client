package gr.akarafyllidis.debrid.client.model;

/**
 * Number of currently active torrents and the account limit.
 */
public record ActiveCount(
        int nb,
        int limit
) {}
