package gr.akarafyllidis.debrid.client.shell;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.ColumnData;
import com.github.freva.asciitable.HorizontalAlign;
import com.github.freva.asciitable.OverflowBehaviour;
import gr.akarafyllidis.debrid.client.model.ActiveCount;
import gr.akarafyllidis.debrid.client.model.AddedTorrent;
import gr.akarafyllidis.debrid.client.model.AvailableHost;
import gr.akarafyllidis.debrid.client.model.TorrentDetails;
import gr.akarafyllidis.debrid.client.model.TorrentSummary;
import gr.akarafyllidis.debrid.client.service.TorrentService;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandGroup(name = "Torrents", description = "Manage Real-Debrid torrents", prefix = "torrent")
public class TorrentCommands {

    private final TorrentService torrentService;

    public TorrentCommands(TorrentService torrentService) {
        this.torrentService = torrentService;
    }

    @Command(name = "list", description = "List user torrents")
    public String list(
            @Option(longName = "offset", description = "Starting offset") String offset,
            @Option(longName = "page", description = "Page number") String page,
            @Option(longName = "limit", description = "Entries per page, max 100") String limit,
            @Option(longName = "active", description = "Show only active torrents") boolean active
    ) {
        List<TorrentSummary> torrents = torrentService.listTorrents(
                parseIntOrNull(offset), parseIntOrNull(page), parseIntOrNull(limit),
                active ? "active" : null);

        if (torrents == null || torrents.isEmpty()) {
            return "No torrents found.";
        }

        return AsciiTable.getTable(AsciiTable.FANCY_ASCII, torrents, List.<ColumnData<TorrentSummary>>of(
                new Column().header("ID").with(TorrentSummary::id),
                new Column().header("Filename").maxWidth(45, OverflowBehaviour.ELLIPSIS_RIGHT).with(TorrentSummary::filename),
                new Column().header("Status").with(t -> t.status() != null ? t.status().getValue() : ""),
                new Column().header("Progress").footer("Total").footerAlign(HorizontalAlign.RIGHT).headerAlign(HorizontalAlign.RIGHT).dataAlign(HorizontalAlign.RIGHT).with(t -> t.progress() + "%"),
                new Column().header("Size").footer(torrents.size() + " torrent(s)").headerAlign(HorizontalAlign.RIGHT).dataAlign(HorizontalAlign.RIGHT).with(t -> formatBytes(t.bytes()))
        ));

    }

    @Command(name = "info", description = "Get detailed information about a torrent")
    public String info(
            @Option(longName = "id", description = "Torrent ID", required = true) String id
    ) {
        TorrentDetails t = torrentService.getTorrentInfo(id);

        int fileCount = t.files() != null ? t.files().size() : 0;
        int linkCount = t.links() != null ? t.links().size() : 0;

        List<String> sections = new ArrayList<>();

        sections.add(AsciiTable.getTable(AsciiTable.FANCY_ASCII, new Column[]{
                new Column().header("Field"),
                new Column().header("Value")
                        .footer(fileCount + " file(s), " + linkCount + " link(s)")
                        .maxWidth(80, OverflowBehaviour.NEWLINE),
        }, buildDetailRows(t)));

        if (fileCount > 0) {
            long totalSize = t.files().stream().mapToLong(f -> f.bytes()).sum();
            long selectedCount = t.files().stream().filter(f -> f.selected() == 1).count();
            sections.add("Files:\n" + AsciiTable.getTable(AsciiTable.FANCY_ASCII, new Column[]{
                    new Column().header("ID").dataAlign(HorizontalAlign.RIGHT),
                    new Column().header("Path").maxWidth(60, OverflowBehaviour.ELLIPSIS_RIGHT),
                    new Column().header("Size").footer(formatBytes(totalSize))
                            .headerAlign(HorizontalAlign.RIGHT).dataAlign(HorizontalAlign.RIGHT).footerAlign(HorizontalAlign.RIGHT),
                    new Column().header("Selected").footer(selectedCount + " selected")
                            .dataAlign(HorizontalAlign.CENTER).footerAlign(HorizontalAlign.CENTER),
            }, t.files().stream()
                    .map(f -> new Object[]{f.id(), f.path(), formatBytes(f.bytes()), f.selected() == 1 ? "✓" : ""})
                    .toArray(Object[][]::new)));
        }

        if (linkCount > 0) {
            sections.add("Links:\n" + AsciiTable.getTable(AsciiTable.FANCY_ASCII, new Column[]{
                    new Column().header("Download Link").footer(linkCount + " link(s)")
                            .maxWidth(100, OverflowBehaviour.NEWLINE),
            }, t.links().stream()
                    .map(l -> new Object[]{l})
                    .toArray(Object[][]::new)));
        }

        return String.join("\n\n", sections);
    }

    @Command(name = "instant-availability", description = "Check instant availability for one or more torrent hashes")
    public String instantAvailability(
            @Option(longName = "hashes", description = "Space-separated SHA1 hashes", required = true) String hashes
    ) {
        Map<String, Object> result = torrentService.getInstantAvailability(hashes.trim().split("\\s+"));

        if (result == null || result.isEmpty()) {
            return "No instant availability data found.";
        }

        return AsciiTable.getTable(AsciiTable.FANCY_ASCII, new Column[]{
                new Column().header("Hash"),
                new Column().header("Availability").maxWidth(80, OverflowBehaviour.NEWLINE),
        }, result.entrySet().stream()
                .map(e -> new Object[]{e.getKey(), String.valueOf(e.getValue())})
                .toArray(Object[][]::new));
    }

    @Command(name = "available-hosts", description = "List hosts available for torrent uploading")
    public String availableHosts() {
        List<AvailableHost> hosts = torrentService.getAvailableHosts();

        if (hosts == null || hosts.isEmpty()) {
            return "No available hosts.";
        }

        return AsciiTable.getTable(AsciiTable.FANCY_ASCII, hosts, List.<ColumnData<AvailableHost>>of(
                new Column().header("Host").with(AvailableHost::host),
                new Column().header("Max File Size").headerAlign(HorizontalAlign.RIGHT).dataAlign(HorizontalAlign.RIGHT).with(h -> formatBytes(h.maxFileSize()))
        ));
    }

    @Command(name = "active-count", description = "Show number of active torrents and account limit")
    public String activeCount() {
        ActiveCount count = torrentService.getActiveCount();
        return AsciiTable.getTable(AsciiTable.FANCY_ASCII, new Column[]{
                new Column().header("Active").dataAlign(HorizontalAlign.CENTER),
                new Column().header("Limit").dataAlign(HorizontalAlign.CENTER),
        }, new Object[][]{{count.nb(), count.limit()}});
    }

    @Command(name = "add-torrent", description = "Add a .torrent file")
    public String addTorrent(
            @Option(longName = "path", description = "Path to the .torrent file", required = true) String filePath,
            @Option(longName = "host", description = "Hoster domain to use") String host
    ) throws IOException {
        byte[] torrentBytes = Files.readAllBytes(Path.of(filePath));
        AddedTorrent added = torrentService.addTorrent(torrentBytes, nullIfBlank(host));
        return formatAdded("Torrent added", added);
    }

    @Command(name = "add-magnet", description = "Add a magnet link")
    public String addMagnet(
            @Option(longName = "magnet", description = "Magnet link URI", required = true) String magnet,
            @Option(longName = "host", description = "Hoster domain to use") String host
    ) {
        AddedTorrent added = torrentService.addMagnet(magnet, nullIfBlank(host));
        return formatAdded("Magnet added", added);
    }

    @Command(name = "select-files", description = "Select files to download from a torrent")
    public String selectFiles(
            @Option(longName = "id", description = "Torrent ID", required = true) String id,
            @Option(longName = "files", description = "Comma-separated file IDs, or 'all'", required = true) String files
    ) {
        torrentService.selectFiles(id, files);
        return "Files selected successfully.";
    }

    @Command(name = "delete", description = "Delete a torrent")
    public String delete(
            @Option(longName = "id", description = "Torrent ID", required = true) String id
    ) {
        torrentService.deleteTorrent(id);
        return "Torrent " + id + " deleted.";
    }

    // --- Helpers ---

    private static Object[][] buildDetailRows(TorrentDetails t) {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"ID", t.id()});
        rows.add(new Object[]{"Filename", t.filename()});
        rows.add(new Object[]{"Original Filename", t.originalFilename()});
        rows.add(new Object[]{"Hash", t.hash()});
        rows.add(new Object[]{"Status", t.status() != null ? t.status().getValue() : ""});
        rows.add(new Object[]{"Progress", t.progress() + "%"});
        rows.add(new Object[]{"Size", formatBytes(t.bytes())});
        rows.add(new Object[]{"Original Size", formatBytes(t.originalBytes())});
        rows.add(new Object[]{"Host", t.host()});
        rows.add(new Object[]{"Split", t.split()});
        rows.add(new Object[]{"Added", t.added()});
        if (t.ended() != null) rows.add(new Object[]{"Ended", t.ended()});
        if (t.speed() != null) rows.add(new Object[]{"Speed", formatBytes(t.speed()) + "/s"});
        if (t.seeders() != null) rows.add(new Object[]{"Seeders", t.seeders()});
        return rows.toArray(Object[][]::new);
    }

    private static String formatAdded(String label, AddedTorrent added) {
        return label + "\n" + AsciiTable.getTable(AsciiTable.FANCY_ASCII, new Column[]{
                new Column().header("ID"),
                new Column().header("URI"),
        }, new Object[][]{{added.id(), added.uri()}});
    }

    private static Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return Integer.parseInt(value.trim());
    }

    private static String nullIfBlank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        return String.format("%.2f GB", mb / 1024.0);
    }
}
