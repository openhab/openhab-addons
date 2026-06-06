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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.module.script.ScriptEngineContainer;
import org.openhab.core.automation.module.script.ScriptEngineManager;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tool for executing JavaScript snippets ad-hoc against openHAB's scripting engine.
 *
 * <p>
 * Primary use is as a dry-run / test harness: an agent composes a script, runs it through
 * {@code execute_script} to confirm there are no syntax or runtime errors, then embeds the
 * same script string as a {@code script}-typed action in a scheduled rule. Without this,
 * a script error inside a future-firing rule would surface only when the rule fires - long
 * after the agent has moved on.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ScriptTools {

    private static final String JS_MIME_TYPE = "application/javascript";
    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final int MAX_TIMEOUT_MS = 30_000;

    private final ScriptEngineManager scriptEngineManager;
    private final McpJsonMapper jsonMapper;
    private final boolean scriptingEnabled;

    public ScriptTools(ScriptEngineManager scriptEngineManager, McpJsonMapper jsonMapper, boolean scriptingEnabled) {
        this.scriptEngineManager = scriptEngineManager;
        this.jsonMapper = jsonMapper;
        this.scriptingEnabled = scriptingEnabled;
    }

    /**
     * Returns the {@code execute_script} tool schema.
     */
    public McpSchema.Tool getExecuteScriptTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("script", Map.of("type", "string", "description",
                "JavaScript source to execute. Has access to openhab-js globals: items, actions, things, rules, cache, time. "
                        + "The value of the last expression is returned."));
        properties.put("timeoutMs", Map.of("type", "integer", "description",
                "Max execution time in milliseconds (default 5000, max 30000). Script is interrupted on timeout."));

        return McpSchema.Tool.builder().name("execute_script").description("""
                Run a JavaScript snippet immediately against the openHAB scripting engine and return its result. \
                Primary use: dry-run a script before adding it as a 'script' action in a scheduled rule, so \
                syntax/runtime errors surface NOW instead of silently failing when the rule fires later. \
                Also useful for one-shot computation, querying multiple items in a single call, or testing \
                what items/actions/things expose. The value of the last expression is returned as a string. \
                Console output (console.log) goes to the openHAB log, not to this response - return values \
                explicitly to see them. Requires the openhab-automation-jsscripting add-on.""")
                .inputSchema(new McpSchema.JsonSchema("object", properties, List.of("script"), null, null, null))
                .build();
    }

    /**
     * Handles an {@code execute_script} call.
     */
    public CallToolResult handleExecuteScript(McpSchema.CallToolRequest request) {
        if (!scriptingEnabled) {
            return textResult(jsonMapper, errorPayload("ScriptingDisabled",
                    "Scripting is disabled. Set the 'enableScripting' option on the MCP server config (io:mcp) to enable.",
                    null, 0));
        }

        Map<String, Object> args = request.arguments();
        String script = getStringArg(args, "script");
        if (script == null || script.isBlank()) {
            return errorResult("'script' (non-empty string) is required.");
        }

        int timeoutMs = Math.max(1, Math.min(MAX_TIMEOUT_MS, getIntArg(args, "timeoutMs", DEFAULT_TIMEOUT_MS)));

        String identifier = "mcp-execute-" + UUID.randomUUID();
        ScriptEngineContainer container = scriptEngineManager.createScriptEngine(JS_MIME_TYPE, identifier);
        if (container == null) {
            return textResult(jsonMapper,
                    errorPayload("EngineUnavailable", "No script engine registered for MIME type '" + JS_MIME_TYPE
                            + "'. Install the openhab-automation-jsscripting add-on.", null, 0));
        }

        // Nested try-finally so the engine is always removed from the manager, and the executor is always
        // shut down, even if executor creation or submit() throws before we reach the main try block.
        try {
            ScriptEngine engine = container.getScriptEngine();
            ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "mcp-execute-script");
                t.setDaemon(true);
                return t;
            });
            try {
                long startNanos = System.nanoTime();
                Future<@Nullable Object> future = executor
                        .submit((Callable<@Nullable Object>) () -> engine.eval(script));
                try {
                    Object result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                    long elapsed = elapsedMs(startNanos);
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("success", true);
                    payload.put("result", result == null ? "" : String.valueOf(result));
                    payload.put("resultType", result == null ? "" : result.getClass().getName());
                    payload.put("executionTimeMs", elapsed);
                    return textResult(jsonMapper, payload);
                } catch (TimeoutException te) {
                    future.cancel(true);
                    return textResult(jsonMapper,
                            errorPayload("TimeoutException",
                                    "Script exceeded the " + timeoutMs + "ms timeout and was interrupted.", null,
                                    elapsedMs(startNanos)));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return textResult(jsonMapper, errorPayload("Interrupted", "Script execution was interrupted.", null,
                            elapsedMs(startNanos)));
                } catch (ExecutionException ee) {
                    Throwable cause = ee.getCause();
                    Throwable reported = cause != null ? cause : ee;
                    String errorType = reported instanceof ScriptException ? "ScriptException"
                            : reported.getClass().getSimpleName();
                    Integer line = reported instanceof ScriptException se && se.getLineNumber() >= 0
                            ? se.getLineNumber()
                            : null;
                    return textResult(jsonMapper,
                            errorPayload(errorType, messageOf(reported), line, elapsedMs(startNanos)));
                }
            } finally {
                executor.shutdownNow();
            }
        } finally {
            scriptEngineManager.removeEngine(identifier);
        }
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private static String messageOf(Throwable t) {
        String m = t.getMessage();
        return m != null ? m : t.getClass().getName();
    }

    private static Map<String, Object> errorPayload(String errorType, String message, @Nullable Integer lineNumber,
            long executionTimeMs) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", false);
        payload.put("errorType", errorType);
        payload.put("message", message);
        if (lineNumber != null) {
            payload.put("lineNumber", lineNumber);
        }
        payload.put("executionTimeMs", executionTimeMs);
        return payload;
    }
}
