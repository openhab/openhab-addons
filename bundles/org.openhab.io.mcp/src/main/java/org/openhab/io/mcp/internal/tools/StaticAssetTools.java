/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.mcp.internal.tools;

import static org.openhab.io.mcp.internal.tools.McpToolUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tool for managing files in the openHAB static-asset folder ({@code $OPENHAB_CONF/html},
 * served at {@code /static/*} by the openhab-webui {@code UIServlet}). Lets an agent list,
 * read, upload, and delete plan-page backgrounds, custom widget icons, and CSS overrides in
 * the same conversation that authors the pages referencing them.
 *
 * <p>
 * There is no openHAB REST endpoint for managing this folder, so reads and writes go directly
 * to the filesystem. To keep the safety story honest:
 * <ul>
 * <li>Every path argument is sanitized (no {@code ..}, no absolute paths, no null bytes, no
 * hidden files) and the resolved {@link Path} must stay under the canonicalized root.</li>
 * <li>File extensions are whitelisted — binary image formats, plus a small set of textual
 * asset types ({@code css}, {@code js}, {@code html}, {@code json}, {@code txt}, {@code md},
 * {@code svg}). The latter four execute in the browser when served from {@code /static/}, so
 * the tool description spells out the XSS implication.</li>
 * <li>Per-call uploads are capped at {@link #MAX_UPLOAD_BYTES}.</li>
 * <li>Every action requires the session's bearer token to belong to an administrator. We
 * verify by probing {@code GET /rest/logging/} (the same admin-gated endpoint
 * {@link LoggingTools} uses) and accepting the call only on HTTP 200. This works for both
 * the legacy bearer-token path and the JWT/OAuth path, where the username is not captured
 * in the transport — so a UserRegistry-based check would silently 403 admin OAuth users.</li>
 * </ul>
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class StaticAssetTools {

    private static final String STATIC_URL_PREFIX = "/static/";
    private static final int MAX_UPLOAD_BYTES = 10 * 1024 * 1024;
    private static final int DEFAULT_LIST_LIMIT = 500;
    private static final int MAX_LIST_LIMIT = 5000;
    private static final int ADMIN_PROBE_TIMEOUT_SECONDS = 10;
    private static final String ADMIN_PROBE_PATH = "/rest/logging/";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp", "svg", "ico",
            "css", "js", "html", "htm", "json", "txt", "md");
    private static final Set<String> TEXT_EXTENSIONS = Set.of("css", "js", "html", "htm", "json", "txt", "md", "svg");

    private final Logger logger = LoggerFactory.getLogger(StaticAssetTools.class);

    private final Path htmlRoot;
    /**
     * {@link #htmlRoot} canonicalized via {@link Path#toRealPath} (or, when the directory does not exist yet,
     * its normalized absolute form). Used only for the safety check in {@link #resolveSafe} — file operations
     * still go through {@link #htmlRoot} so the user-visible path matches their config layout.
     */
    private final Path htmlRootReal;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Function<String, @Nullable String> tokenForSession;
    private final McpJsonMapper jsonMapper;
    private final String description;

    public StaticAssetTools(Path htmlRoot, String baseUrl, HttpClient httpClient,
            Function<String, @Nullable String> tokenForSession, McpJsonMapper jsonMapper) {
        this.htmlRoot = htmlRoot;
        this.htmlRootReal = canonicalize(htmlRoot);
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = httpClient;
        this.tokenForSession = tokenForSession;
        this.jsonMapper = jsonMapper;
        this.description = loadDescription();
    }

    private static Path canonicalize(Path p) {
        try {
            if (Files.exists(p)) {
                return p.toRealPath();
            }
        } catch (IOException ignored) {
            // fall through to absolute-normalized
        }
        return p.toAbsolutePath().normalize();
    }

    public McpSchema.Tool getManageStaticAssetTool() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("action", Map.of("type", "string", "description",
                "list: enumerate files; get: read one file; put: upload/overwrite one file; delete: remove one file.",
                "enum", List.of("list", "get", "put", "delete")));
        p.put("path",
                Map.of("type", "string", "description",
                        "Path within the static folder, e.g. 'plans/floor-1.png'. No leading slash, no '..',"
                                + " no hidden files. Required for get/put/delete; optional 'pathPrefix' for list."));
        p.put("pathPrefix", Map.of("type", "string", "description",
                "list-only: scope the listing to a subdirectory (e.g. 'plans/' to only return plan backgrounds)."));
        p.put("recursive", Map.of("type", "boolean", "description",
                "list-only: walk subdirectories (default true). Set to false for a one-level listing."));
        p.put("maxEntries",
                Map.of("type", "integer", "description", "list-only: cap on returned entries (default "
                        + DEFAULT_LIST_LIMIT + ", max " + MAX_LIST_LIMIT
                        + "). If the listing is larger the response is truncated and 'truncated: true' is set."));
        p.put("encoding",
                Map.of("type", "string", "description",
                        "get/put: 'base64' for binary content (default for image/binary extensions), 'utf8' for text.",
                        "enum", List.of("base64", "utf8")));
        p.put("content",
                Map.of("type", "string", "description", "put-only: the file body, encoded according to 'encoding'."));
        p.put("overwrite", Map.of("type", "boolean", "description",
                "put-only: allow overwriting an existing file (default false; fails if the target exists)."));
        p.put("responseDetail", Map.of("type", "string", "description",
                "Default 'minimal' keeps put/delete responses compact (just success+path+url). 'full' echoes file metadata.",
                "enum", List.of("minimal", "full")));

        return McpSchema.Tool.builder().name("manage_static_asset").description(description)
                .inputSchema(new McpSchema.JsonSchema("object", p, List.of("action"), null, null, null)).build();
    }

    public CallToolResult handleManageStaticAsset(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String action = getStringArg(args, "action");
        if (action == null || action.isBlank()) {
            return errorResult("'action' is required (one of: list, get, put, delete).");
        }
        String token = tokenForSession.apply(exchange.sessionId());
        if (token == null) {
            return errorResult(
                    "No bearer token captured for this session. Reconnect with Authorization: Bearer <token>.");
        }
        if (!tokenHasAdminScope(token)) {
            return errorResult("This tool requires an administrator-scoped token. "
                    + "Generate the token from an openHAB user with administrator privileges, or have the operator "
                    + "do so on your behalf.");
        }
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "list" -> listAssets(args);
            case "get" -> getAsset(args);
            case "put" -> putAsset(args);
            case "delete" -> deleteAsset(args);
            default -> errorResult("Invalid action '" + action + "'. Use one of: list, get, put, delete.");
        };
    }

    private CallToolResult listAssets(Map<String, Object> args) {
        if (!Files.isDirectory(htmlRoot)) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("files", List.of());
            response.put("total", 0);
            response.put("root", htmlRoot.toString());
            response.put("note", "The static asset folder does not exist yet; it will be created on the first put.");
            return textResult(jsonMapper, response);
        }
        String prefix = getStringArg(args, "pathPrefix");
        boolean recursive = getBooleanArg(args, "recursive", true);
        Path scope = htmlRoot;
        if (prefix != null && !prefix.isBlank()) {
            try {
                scope = resolveSafe(prefix, true);
            } catch (AssetPathException e) {
                return errorResult(messageOf(e));
            }
            if (!Files.isDirectory(scope)) {
                return errorResult(
                        "'pathPrefix' '" + prefix + "' does not point to a directory under " + STATIC_URL_PREFIX);
            }
        }
        int limit = Math.max(1, Math.min(MAX_LIST_LIMIT, getIntArg(args, "maxEntries", DEFAULT_LIST_LIMIT)));
        List<Map<String, Object>> files = new ArrayList<>();
        int skipped = 0;
        try (Stream<Path> walk = recursive ? Files.walk(scope) : Files.list(scope)) {
            List<Path> sorted = walk.filter(Files::isRegularFile).sorted(Comparator.naturalOrder()).toList();
            for (Path file : sorted) {
                String name = file.getFileName().toString();
                if (name.startsWith(".") || name.contains(".mcp-tmp")) {
                    continue;
                }
                if (files.size() >= limit) {
                    skipped++;
                    continue;
                }
                files.add(describeFile(file));
            }
        } catch (IOException e) {
            return errorResult("Failed to list static assets: " + messageOf(e));
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("files", files);
        response.put("total", files.size());
        if (skipped > 0) {
            response.put("truncated", true);
            response.put("skipped", skipped);
            response.put("hint", "Listing capped at maxEntries=" + limit
                    + ". Narrow with 'pathPrefix' or raise 'maxEntries' (max " + MAX_LIST_LIMIT + ").");
        }
        if (prefix != null && !prefix.isBlank()) {
            response.put("pathPrefix", prefix);
        }
        response.put("root", htmlRoot.toString());
        return textResult(jsonMapper, response);
    }

    private CallToolResult getAsset(Map<String, Object> args) {
        String path = getStringArg(args, "path");
        if (path == null || path.isBlank()) {
            return errorResult("'path' is required for action='get'.");
        }
        Path target;
        try {
            target = resolveSafe(path, false);
        } catch (AssetPathException e) {
            return errorResult(messageOf(e));
        }
        if (!Files.isRegularFile(target)) {
            return errorResult("Static asset not found: " + path);
        }
        String encArg = getStringArg(args, "encoding");
        String encoding = encArg != null ? encArg.toLowerCase(Locale.ROOT) : defaultEncodingFor(target);
        if (!"base64".equals(encoding) && !"utf8".equals(encoding)) {
            return errorResult("Invalid 'encoding' '" + encArg + "'. Use 'base64' or 'utf8'.");
        }
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(target);
        } catch (IOException e) {
            return errorResult("Failed to read static asset: " + messageOf(e));
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", path);
        response.put("url", buildUrl(path));
        response.put("viewUrl", baseUrl + buildUrl(path));
        response.put("sizeBytes", bytes.length);
        response.put("mimeType", guessMimeType(target));
        response.put("encoding", encoding);
        if ("utf8".equals(encoding)) {
            response.put("content", new String(bytes, StandardCharsets.UTF_8));
        } else {
            response.put("content", Base64.getEncoder().encodeToString(bytes));
        }
        return textResult(jsonMapper, response);
    }

    private CallToolResult putAsset(Map<String, Object> args) {
        String path = getStringArg(args, "path");
        String content = getStringArg(args, "content");
        if (path == null || path.isBlank()) {
            return errorResult("'path' is required for action='put'.");
        }
        if (content == null) {
            return errorResult("'content' is required for action='put'.");
        }
        boolean overwrite = getBooleanArg(args, "overwrite", false);
        String encArg = getStringArg(args, "encoding");
        String encoding = encArg != null ? encArg.toLowerCase(Locale.ROOT) : "base64";
        if (!"base64".equals(encoding) && !"utf8".equals(encoding)) {
            return errorResult("Invalid 'encoding' '" + encArg + "'. Use 'base64' or 'utf8'.");
        }
        // Validate the path before touching the payload so a bad path doesn't waste a base64 decode of a large body.
        Path target;
        try {
            target = resolveSafe(path, false);
        } catch (AssetPathException e) {
            return errorResult(messageOf(e));
        }
        // Cheap early-reject for grossly oversized payloads so we don't decode a huge base64 string just to
        // throw the result away. content.length() is UTF-16 code units, not bytes — so for non-ASCII utf8
        // content this UNDER-estimates the decoded byte count. The post-decode check below is the real cap.
        if (content.length() > MAX_UPLOAD_BYTES * 4L / 3L + 16) {
            return errorResult("Upload exceeds the " + MAX_UPLOAD_BYTES + "-byte per-call cap.");
        }
        byte[] bytes;
        try {
            bytes = "utf8".equals(encoding) ? content.getBytes(StandardCharsets.UTF_8)
                    : Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException e) {
            return errorResult("Invalid base64 content: " + messageOf(e));
        }
        if (bytes.length > MAX_UPLOAD_BYTES) {
            return errorResult("Upload exceeds the " + MAX_UPLOAD_BYTES + "-byte per-call cap.");
        }
        if (Files.exists(target) && !overwrite) {
            return errorResult(
                    "Refusing to overwrite existing file '" + path + "'. Pass overwrite=true to replace it.");
        }
        try {
            Files.createDirectories(target.getParent());
            // createTempFile gives us an OS-unique sibling so concurrent puts to the same logical path
            // can't truncate each other's tmp file. The .mcp-tmp suffix lets us spot crash debris.
            Path tmp = Files.createTempFile(target.getParent(), target.getFileName().toString() + ".", ".mcp-tmp");
            try {
                Files.write(tmp, bytes);
                try {
                    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ame) {
                    // NTFS, tmpfs, overlay-fs and some Docker mounts don't support atomic + replace; fall back
                    // to a non-atomic replace. A reader hitting the path during this brief window may see
                    // either the old or the partially-replaced file — acceptable for static assets.
                    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                // Defensive: if move succeeded, tmp is gone and this is a no-op. If it failed, we don't
                // want to leave a half-written .mcp-tmp behind for the next list call to surface.
                Files.deleteIfExists(tmp);
            }
        } catch (IOException e) {
            return errorResult("Failed to write static asset: " + messageOf(e));
        }
        logger.debug("Wrote {} bytes to static asset {}", bytes.length, target);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("action", "put");
        response.put("path", path);
        response.put("url", buildUrl(path));
        response.put("viewUrl", baseUrl + buildUrl(path));
        response.put("sizeBytes", bytes.length);
        if (wantsFullDetail(args)) {
            response.put("mimeType", guessMimeType(target));
            response.put("lastModified", lastModifiedIso(target));
        }
        return textResult(jsonMapper, response);
    }

    private CallToolResult deleteAsset(Map<String, Object> args) {
        String path = getStringArg(args, "path");
        if (path == null || path.isBlank()) {
            return errorResult("'path' is required for action='delete'.");
        }
        Path target;
        try {
            target = resolveSafe(path, false);
        } catch (AssetPathException e) {
            return errorResult(messageOf(e));
        }
        if (!Files.isRegularFile(target)) {
            return errorResult("Static asset not found: " + path);
        }
        try {
            Files.delete(target);
            pruneEmptyParents(target.getParent());
        } catch (IOException e) {
            return errorResult("Failed to delete static asset: " + messageOf(e));
        }
        logger.debug("Deleted static asset {}", target);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("action", "delete");
        response.put("path", path);
        return textResult(jsonMapper, response);
    }

    /**
     * Resolves a user-supplied path against {@link #htmlRoot} and guarantees the result is
     * underneath it. Used for both file and directory targets — pass {@code allowDirectory=true}
     * when listing into a subdirectory so we don't require an extension.
     */
    private Path resolveSafe(String rawPath, boolean allowDirectory) throws AssetPathException {
        if (rawPath.indexOf('\0') >= 0) {
            throw new AssetPathException("'path' contains a null byte.");
        }
        if (rawPath.startsWith("/") || rawPath.startsWith("\\")) {
            throw new AssetPathException("'path' must be relative — drop the leading slash.");
        }
        String normalized = rawPath.replace('\\', '/');
        for (String segment : normalized.split("/")) {
            if (segment.isEmpty() || ".".equals(segment)) {
                continue;
            }
            if ("..".equals(segment)) {
                throw new AssetPathException("'path' must not contain '..' segments.");
            }
            if (segment.startsWith(".")) {
                throw new AssetPathException("'path' must not contain hidden segments (starting with '.').");
            }
        }
        if (!allowDirectory) {
            String ext = fileExtension(normalized);
            if (ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)) {
                throw new AssetPathException("File extension '" + ext + "' is not allowed. Allowed extensions: "
                        + String.join(", ", ALLOWED_EXTENSIONS) + ".");
            }
        }
        Path candidate = htmlRoot.resolve(normalized).normalize();
        // First guard: textual containment under the configured root. Defeats `..` and other tricks
        // before we touch the filesystem.
        if (!candidate.startsWith(htmlRoot)) {
            throw new AssetPathException("'path' resolves outside the static asset folder.");
        }
        // Second guard: follow any symlinks in the existing portion of the path and verify the resolved
        // location is still inside the canonicalized root. Path.startsWith is textual only, so without
        // this a symlink anywhere in the existing tree can point outside conf/html and let the tool
        // read or clobber arbitrary files. For new files (put), walk up to the first existing ancestor.
        Path existing = candidate;
        Path missing = candidate.getFileSystem().getPath("");
        while (existing != null && !Files.exists(existing)) {
            Path name = existing.getFileName();
            missing = name != null ? existing.getFileSystem().getPath(name.toString()).resolve(missing) : missing;
            existing = existing.getParent();
        }
        if (existing == null) {
            throw new AssetPathException("'path' has no resolvable parent directory.");
        }
        Path resolvedReal;
        try {
            resolvedReal = existing.toRealPath().resolve(missing).normalize();
        } catch (IOException e) {
            throw new AssetPathException("Failed to canonicalize 'path': " + messageOf(e));
        }
        if (!resolvedReal.startsWith(htmlRootReal)) {
            throw new AssetPathException("'path' resolves outside the static asset folder via a symlink.");
        }
        return candidate;
    }

    /**
     * Walks up from {@code start} deleting empty directories until either a non-empty directory or the
     * canonicalized root is reached. Keeps {@code conf/html} itself in place even if it becomes empty.
     * Best-effort: any IO failure stops the walk silently — leftover empty dirs are harmless.
     */
    private void pruneEmptyParents(@Nullable Path start) {
        Path current = start;
        while (current != null && !current.equals(htmlRoot) && current.startsWith(htmlRoot)) {
            try (Stream<Path> s = Files.list(current)) {
                if (s.findAny().isPresent()) {
                    return;
                }
            } catch (IOException e) {
                return;
            }
            try {
                Files.delete(current);
            } catch (IOException e) {
                return;
            }
            current = current.getParent();
        }
    }

    private Map<String, Object> describeFile(Path file) {
        String relative = htmlRoot.relativize(file).toString().replace('\\', '/');
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("path", relative);
        m.put("url", buildUrl(relative));
        try {
            m.put("sizeBytes", Files.size(file));
        } catch (IOException e) {
            m.put("sizeBytes", -1);
        }
        m.put("lastModified", lastModifiedIso(file));
        m.put("mimeType", guessMimeType(file));
        return m;
    }

    private static String buildUrl(String relativePath) {
        return STATIC_URL_PREFIX + relativePath.replace('\\', '/');
    }

    private static String fileExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String defaultEncodingFor(Path file) {
        return TEXT_EXTENSIONS.contains(fileExtension(file.getFileName().toString())) ? "utf8" : "base64";
    }

    private static String guessMimeType(Path file) {
        try {
            String probed = Files.probeContentType(file);
            if (probed != null) {
                return probed;
            }
        } catch (IOException ignored) {
            // fall through to extension-based mapping
        }
        return switch (fileExtension(file.getFileName().toString())) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "html", "htm" -> "text/html";
            case "json" -> "application/json";
            case "txt" -> "text/plain";
            case "md" -> "text/markdown";
            default -> "application/octet-stream";
        };
    }

    private static String lastModifiedIso(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant().toString();
        } catch (IOException e) {
            return "";
        }
    }

    private static boolean wantsFullDetail(Map<String, Object> args) {
        String detail = getStringArg(args, "responseDetail");
        return detail != null && "full".equalsIgnoreCase(detail);
    }

    private static String messageOf(Throwable t) {
        String m = t.getMessage();
        return m != null ? m : t.getClass().getSimpleName();
    }

    /**
     * Probes an admin-gated REST endpoint with the session token; HTTP 200 means the token
     * belongs to a user with the administrator role. Matches the pattern {@link LoggingTools}
     * uses for its own admin check.
     */
    private boolean tokenHasAdminScope(String token) {
        try {
            ContentResponse resp = httpClient.newRequest(URI.create(baseUrl + ADMIN_PROBE_PATH)).method(HttpMethod.GET)
                    .header("Authorization", "Bearer " + token).header("Accept", "application/json")
                    .timeout(ADMIN_PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            return resp.getStatus() == 200;
        } catch (Exception e) {
            logger.debug("Admin scope probe failed: {}", e.getMessage());
            return false;
        }
    }

    private String loadDescription() {
        try (InputStream in = StaticAssetTools.class
                .getResourceAsStream("/widgets/descriptions/manage_static_asset.txt")) {
            if (in == null) {
                logger.debug("Description resource not found at /widgets/descriptions/manage_static_asset.txt; "
                        + "using inline fallback.");
                return "List, read, upload, or delete files in $OPENHAB_CONF/html (the folder served at /static/*). "
                        + "Use to upload plan-page backgrounds, custom widget icons, and CSS overrides referenced "
                        + "from Main UI components. Requires administrator-scoped bearer token.";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            logger.debug("Failed to load manage_static_asset description: {}", e.getMessage());
            return "manage_static_asset (description unavailable)";
        }
    }

    /** Internal signal for any user-supplied path that fails validation. */
    private static final class AssetPathException extends Exception {
        private static final long serialVersionUID = 1L;

        AssetPathException(String message) {
            super(message);
        }
    }
}
