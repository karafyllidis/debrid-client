# debrid-client

A command-line client for the [Real-Debrid](https://real-debrid.com) API, built with **Spring Shell** and **Spring RestClient**.

It provides an interactive shell (or single-command execution) for managing your Real-Debrid torrents and downloads directly from the terminal, with output rendered as clean ASCII tables.

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 25+ |
| Maven | 3.9+ (or use the included `mvnw` wrapper) |

---

## Configuration

The client authenticates with the Real-Debrid API using a personal API token.

1. Generate your token at <https://real-debrid.com/apitoken>.
2. Export it as an environment variable before running the client:

```bash
export REALDEBRID_API_TOKEN=your_token_here
```

Alternatively, you can set it in `src/main/resources/application.yaml`:

```yaml
realdebrid:
  api-token: your_token_here
```

> **Note:** Do not commit your token to source control.

---

## Building

```bash
./mvnw clean package
```

This produces a self-contained JAR at `target/debrid-client-*.jar`.

---

## Running

### Interactive shell

```bash
java -jar target/debrid-client-*.jar
```

You will be dropped into an interactive shell where you can type commands with tab-completion and inline help.

### Single command (non-interactive)

Pass a command directly as arguments to skip the interactive prompt:

```bash
java -jar target/debrid-client-*.jar torrent list
```

---

## Commands

### Torrents (`torrent`)

| Command | Description |
|---------|-------------|
| `torrent list` | List your torrents |
| `torrent info --id <id>` | Show detailed info for a torrent |
| `torrent add-torrent --path <file>` | Upload a `.torrent` file |
| `torrent add-magnet --magnet <uri>` | Add a magnet link |
| `torrent select-files --id <id> --files <ids\|all>` | Select which files to download |
| `torrent instant-availability --hashes <hash...>` | Check cache availability for one or more hashes |
| `torrent available-hosts` | List hosts available for torrent uploading |
| `torrent active-count` | Show active torrent count vs. account limit |
| `torrent delete --id <id>` | Delete a torrent |

#### `torrent list` options

| Option | Description |
|--------|-------------|
| `--offset` | Starting offset |
| `--page` | Page number |
| `--limit` | Entries per page (max 100) |
| `--active` | Show only active torrents |

#### `torrent select-files` — `--files` values

- A comma-separated list of file IDs (e.g. `1,3,5`)
- The keyword `all` to select every file in the torrent

---

### Downloads (`download`)

| Command | Description |
|---------|-------------|
| `download list` | List your unrestricted download links |
| `download delete --id <id>` | Delete a download |

#### `download list` options

| Option | Description |
|--------|-------------|
| `--offset` | Starting offset |
| `--page` | Page number |
| `--limit` | Entries per page (max 100) |

---

## Example session

```
shell:> torrent list --active
╔══════════════╦═══════════════════════════════════════════════╦══════════════╦══════════╦═══════════╗
║ ID           ║ Filename                                      ║ Status       ║ Progress ║      Size ║
╠══════════════╬═══════════════════════════════════════════════╬══════════════╬══════════╬═══════════╣
║ ABCDE1234567 ║ My.Show.S01E01.mkv                            ║ downloading  ║      42% ║   4.20 GB ║
╠══════════════╬═══════════════════════════════════════════════╬══════════════╬══════════╬═══════════╣
║              ║                                               ║        Total ║          ║ 1 torrent ║
╚══════════════╩═══════════════════════════════════════════════╩══════════════╩══════════╩═══════════╝

shell:> torrent add-magnet --magnet "magnet:?xt=urn:btih:..."
Magnet added
╔══════════════╦═══════════════════════════════════════════════╗
║ ID           ║ URI                                           ║
╠══════════════╬═══════════════════════════════════════════════╣
║ XYZ9876      ║ https://api.real-debrid.com/rest/1.0/...      ║
╚══════════════╩═══════════════════════════════════════════════╝

shell:> torrent select-files --id XYZ9876 --files all
Files selected successfully.
```

---

## Tech Stack

- [Spring Boot 4.0](https://spring.io/projects/spring-boot)
- [Spring Shell 4.0](https://spring.io/projects/spring-shell)
- [Spring RestClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [ascii-table](https://github.com/freva/ascii-table) for tabular output
- [Real-Debrid REST API v1](https://api.real-debrid.com/)
