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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tools for reading openHAB log entries and adjusting logger verbosity for diagnostics.
 *
 * <p>
 * Hybrid pattern: log reads use the in-process {@link LogReaderService} (no auth surface,
 * gated by the {@code enableLoggingAccess} config flag); level reads/writes go through the
 * openHAB REST API at {@code /rest/logging/} with the caller's bearer token, so openHAB's
 * own ADMIN-scope check controls who can change levels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LoggingTools {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;
    private static final int DEFAULT_REVERT_SECONDS = 1800;
    private static final Set<String> VALID_LEVELS = Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "DEFAULT");
    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Logger logger = LoggerFactory.getLogger(LoggingTools.class);
    private final ObjectMapper jackson = McpToolUtils.jackson();

    private final LogReaderService logReaderService;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Function<String, @Nullable String> tokenForSession;
    private final ScheduledExecutorService scheduler;
    private final McpJsonMapper jsonMapper;

    private final Map<String, RevertTask> pendingReverts = new ConcurrentHashMap<>();

    /**
     * Wrapper that holds a scheduled revert task. Wrapping (instead of storing the {@link ScheduledFuture}
     * directly) lets us use {@link Map#remove(Object, Object)} from the task's own {@code finally} block so a
     * task only removes itself if it hasn't already been replaced by a newer scheduling — otherwise the
     * replacement would be silently dropped and could never be cancelled on shutdown.
     */
    private static final class RevertTask {
        @Nullable
        ScheduledFuture<?> future;
    }

    public LoggingTools(LogReaderService logReaderService, HttpClient httpClient, String baseUrl,
            Function<String, @Nullable String> tokenForSession, ScheduledExecutorService scheduler,
            McpJsonMapper jsonMapper) {
        this.logReaderService = logReaderService;
        this.httpClient = httpClient;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.tokenForSession = tokenForSession;
        this.scheduler = scheduler;
        this.jsonMapper = jsonMapper;
    }

    public McpSchema.Tool getReadLogsTool() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("loggerFilter", Map.of("type", "string", "description", """
                Optional regex matched against the logger name (e.g. 'org\\.openhab\\.binding\\.zigbee.*'). \
                Narrow this to the affected binding or component before increasing log limits."""));
        p.put("minLevel", Map.of("type", "string", "description", """
                Minimum level to return (TRACE/DEBUG/INFO/WARN/ERROR). Defaults to INFO. \
                Use DEBUG or TRACE only after bumping the relevant logger with manage_log_level(action='set').""",
                "enum", List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR")));
        p.put("sinceMs", Map.of("type", "integer", "description",
                "Only return entries newer than this epoch-millis timestamp."));
        p.put("sinceSequence", Map.of("type", "integer", "description", """
                Only return entries with a sequence number greater than this value. Pass back the \
                'lastSequence' from a previous response to poll for new entries (cheaper than time filtering)."""));
        p.put("search", Map.of("type", "string", "description",
                "Case-insensitive substring filter applied to the formatted message."));
        p.put("limit",
                Map.of("type", "integer", "description",
                        "Maximum entries to return (default 100, max 1000). Newest entries first in the buffer; "
                                + "the response is returned oldest-first."));

        return McpSchema.Tool.builder().name("get_logs").description("""
                Read recent log entries from openHAB's in-memory log buffer (typically the last 500-5000 entries, \
                with stack traces when present). Use this to diagnose problems: thing offline reasons, binding \
                errors, rule execution traces. Typical diagnostic flow: (1) call get_logs with loggerFilter \
                narrowed to the affected binding/component; (2) if there isn't enough detail, call \
                manage_log_level(action='set') on the relevant logger at DEBUG (auto-reverts in 30 min) and ask \
                the user to reproduce; (3) call get_logs again with sinceSequence=<the lastSequence from step 1> \
                to fetch only what's new. Entries are returned oldest-first so they read top-to-bottom like a tail.""")
                .inputSchema(new McpSchema.JsonSchema("object", p, List.of(), null, null, null)).build();
    }

    public CallToolResult handleGetLogs(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String loggerFilter = getStringArg(args, "loggerFilter");
        String minLevelArg = getStringArg(args, "minLevel");
        @Nullable
        Pattern loggerPattern;
        try {
            loggerPattern = loggerFilter == null ? null : Pattern.compile(loggerFilter);
        } catch (PatternSyntaxException e) {
            return errorResult("Invalid loggerFilter regex: " + e.getDescription());
        }
        int minLevelOrdinal = severityOrdinal(minLevelArg != null ? minLevelArg : "INFO");
        if (minLevelOrdinal < 0) {
            return errorResult("Invalid minLevel '" + minLevelArg + "'. Use TRACE, DEBUG, INFO, WARN, or ERROR.");
        }
        long sinceMs = getLongArg(args, "sinceMs", 0L);
        long sinceSequence = getLongArg(args, "sinceSequence", -1L);
        String search = getStringArg(args, "search");
        String searchLower = search == null ? null : search.toLowerCase(Locale.ROOT);
        int limit = Math.max(1, Math.min(MAX_LIMIT, getIntArg(args, "limit", DEFAULT_LIMIT)));

        // LogReaderService returns newest-first; iterate, filter, take up to `limit`, then reverse to chronological.
        Enumeration<LogEntry> entries = logReaderService.getLog();
        List<LogEntry> collected = new ArrayList<>(limit);
        long maxSequence = sinceSequence;
        int bufferSize = 0;
        while (entries.hasMoreElements()) {
            LogEntry entry = entries.nextElement();
            bufferSize++;
            if (collected.size() >= limit) {
                continue;
            }
            if (sinceSequence >= 0 && entry.getSequence() <= sinceSequence) {
                continue;
            }
            if (sinceMs > 0 && entry.getTime() <= sinceMs) {
                continue;
            }
            if (severityOrdinal(entry.getLogLevel().name()) < minLevelOrdinal) {
                continue;
            }
            String name = entry.getLoggerName();
            if (loggerPattern != null && (name == null || !loggerPattern.matcher(name).matches())) {
                continue;
            }
            String message = entry.getMessage();
            if (searchLower != null && (message == null || !message.toLowerCase(Locale.ROOT).contains(searchLower))) {
                continue;
            }
            collected.add(entry);
            if (entry.getSequence() > maxSequence) {
                maxSequence = entry.getSequence();
            }
        }
        Collections.reverse(collected);

        List<Map<String, Object>> out = new ArrayList<>(collected.size());
        for (LogEntry entry : collected) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sequence", entry.getSequence());
            m.put("timeMs", entry.getTime());
            m.put("level", entry.getLogLevel().name());
            m.put("logger", entry.getLoggerName());
            m.put("message", entry.getMessage());
            Throwable t = entry.getException();
            if (t != null) {
                m.put("stackTrace", formatStackTrace(t));
            }
            out.add(m);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("entries", out);
        response.put("returned", out.size());
        response.put("lastSequence", maxSequence);
        response.put("bufferSize", bufferSize);
        return textResult(jsonMapper, response);
    }

    public McpSchema.Tool getManageLogLevelTool() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("action",
                Map.of("type", "string", "description",
                        "get: list current effective levels; set: change a single logger's level.", "enum",
                        List.of("get", "set")));
        p.put("loggerFilter", Map.of("type", "string", "description",
                "action='get' only: optional substring filter applied to logger names (e.g. 'zigbee')."));
        p.put("loggerName", Map.of("type", "string", "description", """
                action='set' only: logger to change (e.g. 'org.openhab.binding.zigbee'). For diagnostics of a \
                specific add-on, scope to that add-on's package."""));
        p.put("level",
                Map.of("type", "string", "description",
                        "action='set' only: new level. Use DEFAULT to reset to the inherited level.", "enum",
                        List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "DEFAULT")));
        p.put("revertAfterSeconds", Map.of("type", "integer", "description",
                """
                        action='set' only: auto-revert to the previous level after this many seconds (default 1800 = 30 min). \
                        Pass 0 to make the change persistent (the operator will have to revert it via Main UI or another set call)."""));

        return McpSchema.Tool.builder().name("manage_log_level").description("""
                List current log levels or set a single logger's level. Action-dispatched: \
                action='get' lists effective levels (optionally filtered by loggerFilter); useful before \
                changing one so you know the original. \
                action='set' changes one logger and auto-reverts to the previous level after revertAfterSeconds \
                (default 1800 = 30 min); pass 0 for a persistent change. \
                Both actions go through the openHAB REST API at /rest/logging/ and require your bearer token \
                to have ADMIN scope. \
                BEFORE bumping any of these infrastructure loggers to DEBUG/TRACE, ASK THE USER FOR EXPLICIT \
                CONFIRMATION — they can flood logs or hurt performance: 'org.eclipse.jetty.*' (web server), \
                'org.apache.karaf.*' (Karaf runtime), 'org.apache.cxf.*' (REST stack), 'org.ops4j.pax.web.*' \
                (HTTP whiteboard). For an add-on, scope to that add-on's package (e.g. \
                'org.openhab.binding.<name>') — that's safe and almost always sufficient.""")
                .inputSchema(new McpSchema.JsonSchema("object", p, List.of("action"), null, null, null)).build();
    }

    public CallToolResult handleManageLogLevel(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String action = getStringArg(args, "action");
        if (action == null || action.isBlank()) {
            return errorResult("'action' is required (one of: get, set).");
        }
        String token = tokenForSession.apply(exchange.sessionId());
        if (token == null) {
            return errorResult(
                    "No bearer token captured for this session. Reconnect with Authorization: Bearer <token>.");
        }
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "get" -> getLogLevels(token, args);
            case "set" -> setLogLevel(token, args);
            default -> errorResult("Invalid action '" + action + "'. Use one of: get, set.");
        };
    }

    private CallToolResult getLogLevels(String token, Map<String, Object> args) {
        String filter = getStringArg(args, "loggerFilter");
        String filterLower = filter == null ? null : filter.toLowerCase(Locale.ROOT);

        try {
            ContentResponse resp = httpClient.newRequest(URI.create(baseUrl + "/rest/logging/")).method(HttpMethod.GET)
                    .header("Authorization", "Bearer " + token).header("Accept", "application/json").send();
            if (resp.getStatus() == 403 || resp.getStatus() == 401) {
                return errorResult("openHAB rejected the request (HTTP " + resp.getStatus()
                        + "). The /rest/logging/ endpoint requires the user's bearer token to have ADMIN scope.");
            }
            if (resp.getStatus() != 200) {
                return errorResult("Failed to list loggers: HTTP " + resp.getStatus() + " " + resp.getReason());
            }
            return textResult(jsonMapper, parseLoggerList(resp.getContentAsString(), filterLower));
        } catch (Exception e) {
            logger.debug("manage_log_level (get) failed: {}", e.getMessage(), e);
            return errorResult("Request failed: " + e.getMessage());
        }
    }

    private CallToolResult setLogLevel(String token, Map<String, Object> args) {
        String loggerName = getStringArg(args, "loggerName");
        String levelArg = getStringArg(args, "level");
        if (loggerName == null || loggerName.isBlank() || levelArg == null || levelArg.isBlank()) {
            return errorResult("'loggerName' and 'level' are required when action='set'.");
        }
        String level = levelArg.toUpperCase(Locale.ROOT);
        if (!VALID_LEVELS.contains(level)) {
            return errorResult("Invalid level '" + levelArg + "'. Use TRACE, DEBUG, INFO, WARN, ERROR, or DEFAULT.");
        }
        int revertAfterSeconds = getIntArg(args, "revertAfterSeconds", DEFAULT_REVERT_SECONDS);
        if (revertAfterSeconds < 0) {
            return errorResult("revertAfterSeconds must be >= 0.");
        }

        String previousLevel;
        try {
            previousLevel = fetchLoggerLevel(loggerName, token);
        } catch (AdminRequiredException ae) {
            return errorResult("openHAB rejected the request (HTTP " + ae.status
                    + "). GET /rest/logging/ requires the user's bearer token to have ADMIN scope.");
        } catch (Exception e) {
            logger.debug("Failed to fetch current level for {}: {}", loggerName, e.getMessage());
            return errorResult("Could not read current level for '" + loggerName + "': " + e.getMessage());
        }

        try {
            int status = applyLevel(loggerName, level, token);
            if (status == 401 || status == 403) {
                return errorResult("openHAB rejected the level change (HTTP " + status
                        + "). PUT/DELETE /rest/logging/ requires the user's bearer token to have ADMIN scope.");
            }
            if (status < 200 || status >= 300) {
                return errorResult("Failed to set level: HTTP " + status);
            }
        } catch (Exception e) {
            logger.debug("Failed to set level for {}: {}", loggerName, e.getMessage());
            return errorResult("Request failed: " + e.getMessage());
        }

        // Per-key atomic swap via compute(): cancel-prior + schedule-new + put-new run as one unit so two
        // concurrent setLogLevel calls for the same logger can't leave an untracked scheduled task behind.
        // The HTTP calls above stay outside the lock so unrelated loggers don't serialize on each other.
        boolean schedulingRevert = revertAfterSeconds > 0 && !level.equals(previousLevel);
        if (schedulingRevert) {
            String revertToLevel = previousLevel;
            pendingReverts.compute(loggerName, (key, prior) -> {
                cancelTask(prior);
                RevertTask task = new RevertTask();
                task.future = scheduler.schedule(() -> revertLevel(key, revertToLevel, token, task), revertAfterSeconds,
                        TimeUnit.SECONDS);
                return task;
            });
        } else {
            cancelTask(pendingReverts.remove(loggerName));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("loggerName", loggerName);
        response.put("previousLevel", previousLevel);
        response.put("newLevel", level);
        if (schedulingRevert) {
            response.put("revertAfterSeconds", revertAfterSeconds);
            response.put("revertAt",
                    LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(revertAfterSeconds).format(ISO_LOCAL));
            response.put("revertToLevel", previousLevel);
        } else {
            response.put("revertAfterSeconds", 0);
        }
        return textResult(jsonMapper, response);
    }

    private static void cancelTask(@Nullable RevertTask task) {
        if (task == null) {
            return;
        }
        ScheduledFuture<?> f = task.future;
        if (f != null) {
            f.cancel(false);
        }
    }

    private Map<String, Object> parseLoggerList(String body, @Nullable String filterLower) throws Exception {
        ObjectNode root = (ObjectNode) jackson.readTree(body);
        ArrayNode loggers = (ArrayNode) root.get("loggers");
        List<Map<String, Object>> filtered = new ArrayList<>();
        int total = loggers == null ? 0 : loggers.size();
        if (loggers != null) {
            for (int i = 0; i < loggers.size(); i++) {
                ObjectNode entry = (ObjectNode) loggers.get(i);
                String name = entry.path("loggerName").asText("");
                if (filterLower != null && !name.toLowerCase(Locale.ROOT).contains(filterLower)) {
                    continue;
                }
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", name);
                m.put("level", entry.path("level").asText(""));
                filtered.add(m);
            }
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("loggers", filtered);
        response.put("total", total);
        response.put("returned", filtered.size());
        return response;
    }

    private void revertLevel(String loggerName, String revertLevel, String token, RevertTask task) {
        try {
            int status = applyLevel(loggerName, revertLevel, token);
            if (status < 200 || status >= 300) {
                logger.debug("Auto-revert of {} to {} returned HTTP {}", loggerName, revertLevel, status);
            }
        } catch (Exception e) {
            logger.debug("Auto-revert of {} to {} failed: {}", loggerName, revertLevel, e.getMessage());
        } finally {
            // Only remove if our task is still the current entry — a concurrent setLogLevel may have
            // replaced us with a fresh scheduling, and we must not evict that newer task.
            pendingReverts.remove(loggerName, task);
        }
    }

    /**
     * Looks up the current effective level for {@code loggerName}. A 404 means the logger has no explicit
     * configuration (returns DEFAULT). A 401/403 throws {@link AdminRequiredException} so callers can surface
     * the auth requirement to the agent instead of a generic "could not read current level" error.
     */
    private String fetchLoggerLevel(String loggerName, String token) throws Exception {
        ContentResponse resp = httpClient
                .newRequest(URI.create(baseUrl + "/rest/logging/" + encodeLoggerName(loggerName)))
                .method(HttpMethod.GET).header("Authorization", "Bearer " + token).header("Accept", "application/json")
                .send();
        if (resp.getStatus() == 401 || resp.getStatus() == 403) {
            throw new AdminRequiredException(resp.getStatus());
        }
        if (resp.getStatus() == 404) {
            return "DEFAULT";
        }
        if (resp.getStatus() != 200) {
            throw new IllegalStateException("HTTP " + resp.getStatus() + " " + resp.getReason());
        }
        ObjectNode root = (ObjectNode) jackson.readTree(resp.getContentAsString());
        ArrayNode loggers = (ArrayNode) root.get("loggers");
        if (loggers != null && loggers.size() > 0) {
            return loggers.get(0).path("level").asText("DEFAULT");
        }
        return "DEFAULT";
    }

    private static final class AdminRequiredException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        final int status;

        AdminRequiredException(int status) {
            super("HTTP " + status);
            this.status = status;
        }
    }

    private int applyLevel(String loggerName, String level, String token) throws Exception {
        String url = baseUrl + "/rest/logging/" + encodeLoggerName(loggerName);
        if ("DEFAULT".equals(level)) {
            Request req = httpClient.newRequest(URI.create(url)).method(HttpMethod.DELETE).header("Authorization",
                    "Bearer " + token);
            return req.send().getStatus();
        }
        ObjectNode body = jackson.createObjectNode();
        body.put("loggerName", loggerName);
        body.put("level", level);
        Request req = httpClient.newRequest(URI.create(url)).method(HttpMethod.PUT)
                .header("Authorization", "Bearer " + token).header("Accept", "application/json")
                .content(new StringContentProvider(body.toString(), StandardCharsets.UTF_8), "application/json");
        return req.send().getStatus();
    }

    /**
     * Cancels (but does not fire) any pending auto-revert tasks. Called on bundle deactivate so we don't
     * race the scheduler against shutdown.
     */
    public void cancelPendingReverts() {
        for (RevertTask task : pendingReverts.values()) {
            cancelTask(task);
        }
        pendingReverts.clear();
    }

    /**
     * Maps {@link LogLevel} names to a comparable ordinal where higher means more severe.
     * Returns -1 for unknown level names.
     */
    private static int severityOrdinal(String levelName) {
        return switch (levelName.toUpperCase(Locale.ROOT)) {
            case "TRACE" -> 0;
            case "DEBUG" -> 1;
            case "INFO" -> 2;
            case "WARN", "WARNING" -> 3;
            case "ERROR" -> 4;
            case "AUDIT" -> 5;
            default -> -1;
        };
    }

    private static long getLongArg(Map<String, Object> args, String key, long defaultValue) {
        Object v = args.get(key);
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private static String encodeLoggerName(String name) {
        // URLEncoder targets application/x-www-form-urlencoded, where space → '+'; for path segments we
        // want '%20', so post-process. Logger names commonly contain '$' (inner classes) and other chars
        // that the bare replace(" ", "%20") would not handle correctly.
        return java.net.URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String formatStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
        }
        return sw.toString();
    }
}
