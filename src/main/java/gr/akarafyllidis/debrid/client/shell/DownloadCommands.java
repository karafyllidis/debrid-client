package gr.akarafyllidis.debrid.client.shell;

import com.github.freva.asciitable.*;
import gr.akarafyllidis.debrid.client.model.DownloadSummary;
import gr.akarafyllidis.debrid.client.service.DownloadService;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;

import java.util.List;

@CommandGroup(name = "Downloads", description = "Manage Real-Debrid downloads", prefix = "download")
public class DownloadCommands {

    private final DownloadService downloadService;

    public DownloadCommands(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @Command(name = "list", description = "List user downloads")
    public String list(
            @Option(longName = "offset", description = "Starting offset") String offset,
            @Option(longName = "page", description = "Page number") String page,
            @Option(longName = "limit", description = "Entries per page, max 100") String limit
    ) {
        List<DownloadSummary> downloads = downloadService.listDownloads(
                parseIntOrNull(offset), parseIntOrNull(page), parseIntOrNull(limit));

        if (downloads == null || downloads.isEmpty()) {
            return "No downloads found.";
        }

        return AsciiTable.getTable(AsciiTable.FANCY_ASCII, downloads, List.of(
                new Column().header("ID").with(DownloadSummary::id),
                new Column().header("Filename").maxWidth(45, OverflowBehaviour.ELLIPSIS_RIGHT).with(DownloadSummary::filename),
                new Column().header("Host").with(DownloadSummary::host),
                new Column().header("Type").with(d -> d.type() != null ? d.type() : ""),
                new Column().header("Size").footer(downloads.size() + " download(s)")
                        .headerAlign(HorizontalAlign.RIGHT).dataAlign(HorizontalAlign.RIGHT)
                        .with(d -> formatBytes(d.filesize())),
                new Column().header("Generated").with(DownloadSummary::generated)
        ));
    }

    @Command(name = "delete", description = "Delete a download")
    public String delete(
            @Option(longName = "id", description = "Download ID", required = true) String id
    ) {
        downloadService.deleteDownload(id);
        return "Download " + id + " deleted.";
    }

    // --- Helpers ---

    private static Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return Integer.parseInt(value.trim());
    }

    private static String formatBytes(long bytes) {
        if (bytes == 0) return "Unknown";
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        return String.format("%.2f GB", mb / 1024.0);
    }
}
