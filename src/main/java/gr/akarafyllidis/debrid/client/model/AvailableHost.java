package gr.akarafyllidis.debrid.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A host available for torrent uploading.
 */
public record AvailableHost(
        String host,
        @JsonProperty("max_file_size") long maxFileSize
) {}
