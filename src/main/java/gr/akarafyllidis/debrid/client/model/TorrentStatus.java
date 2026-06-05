package gr.akarafyllidis.debrid.client.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TorrentStatus {
    MAGNET_ERROR("magnet_error"),
    MAGNET_CONVERSION("magnet_conversion"),
    WAITING_FILES_SELECTION("waiting_files_selection"),
    QUEUED("queued"),
    DOWNLOADING("downloading"),
    DOWNLOADED("downloaded"),
    ERROR("error"),
    VIRUS("virus"),
    COMPRESSING("compressing"),
    UPLOADING("uploading"),
    DEAD("dead");

    private final String value;

    TorrentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
