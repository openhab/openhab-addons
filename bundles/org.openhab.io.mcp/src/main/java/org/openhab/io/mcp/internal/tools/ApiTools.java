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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Opt-in meta-tools that expose the full openHAB REST API to MCP clients through
 * three endpoints: list, describe, and invoke. The tools piggyback on the caller's
 * own bearer token so permissions mirror a direct REST call.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ApiTools {

    private static final long SPEC_TTL_MS = 5 * 60_000L;
    private static final int MAX_RESPONSE_CHARS = 256 * 1024;

    private final Logger logger = LoggerFactory.getLogger(ApiTools.class);

    private final HttpClient httpClient;
    private final String baseUrl;
    private final Function<String, @Nullable String> tokenForSession;
    private final McpJsonMapper jsonMapper;
    private final ObjectMapper jackson = McpToolUtils.jackson();

    private volatile @Nullable JsonNode specCache;
    private volatile long specFetchedAt;

    /**
     * Creates a new {@code ApiTools} instance.
     *
     * @param httpClient the Jetty HTTP client used to call the openHAB REST API
     * @param baseUrl the base URL of the openHAB instance (e.g. {@code http://localhost:8080})
     * @param tokenForSession function that resolves a bearer token for a given MCP session ID
     * @param jsonMapper the MCP JSON mapper used to serialize tool results
     */
    public ApiTools(HttpClient httpClient, String baseUrl, Function<String, @Nullable String> tokenForSession,
            McpJsonMapper jsonMapper) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.tokenForSession = tokenForSession;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Returns the {@code list_api_endpoints} tool schema.
     * Describes a tool that lists all openHAB REST endpoints from the live OpenAPI spec,
     * with optional tag and HTTP method filters.
     *
     * @return the MCP tool definition for {@code list_api_endpoints}
     */
    public McpSchema.Tool getListApiEndpointsTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("tag", Map.of("type", "string", "description",
                "Optional tag filter (e.g. 'items', 'rules', 'discovery'). Matches any tag on the endpoint."));
        props.put("method", Map.of("type", "string", "description",
                "Optional HTTP method filter (GET, POST, PUT, DELETE, PATCH). Case-insensitive."));
        return McpSchema.Tool.builder().name("list_api_endpoints").description("""
                List every openHAB REST endpoint available on this server. Returns a compact \
                array of {method, path, summary, tags} objects drawn from the live OpenAPI \
                spec. Filter by tag or HTTP method. Use this to discover endpoints not \
                covered by the curated tools, then call describe_api_endpoint for the schema \
                before invoking with call_api.""")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of(), null, null, null)).build();
    }

    /**
     * Handles a {@code list_api_endpoints} call.
     * Fetches the OpenAPI spec and returns a filtered list of endpoint summaries.
     *
     * @param exchange the MCP server exchange providing session context
     * @param request the incoming tool call request containing optional tag and method filters
     * @return a result containing the matching endpoints as a JSON array
     */
    public CallToolResult handleListApiEndpoints(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        SpecResult specResult = loadSpec(exchange);
        JsonNode spec = specResult.spec;
        if (spec == null) {
            return errorResult("Could not load OpenAPI spec from " + baseUrl + "/rest/spec: " + specResult.error
                    + " — ensure the openhab-misc-restdocs add-on is installed and your MCP client sends a bearer token.");
        }
        Map<String, Object> args = request.arguments();
        String tagFilter = getStringArg(args, "tag");
        String methodFilter = getStringArg(args, "method");
        if (methodFilter != null) {
            methodFilter = methodFilter.toLowerCase(Locale.ROOT);
        }

        List<Map<String, Object>> endpoints = new ArrayList<>();
        JsonNode paths = spec.get("paths");
        if (paths == null || !paths.isObject()) {
            return textResult(jsonMapper, Map.of("endpoints", endpoints));
        }
        Iterator<Map.Entry<String, JsonNode>> pathIter = paths.fields();
        while (pathIter.hasNext()) {
            Map.Entry<String, JsonNode> pathEntry = pathIter.next();
            String path = pathEntry.getKey();
            JsonNode methods = pathEntry.getValue();
            if (methods == null || !methods.isObject()) {
                continue;
            }
            Iterator<Map.Entry<String, JsonNode>> methodIter = methods.fields();
            while (methodIter.hasNext()) {
                Map.Entry<String, JsonNode> methodEntry = methodIter.next();
                String method = methodEntry.getKey();
                if (!isHttpMethod(method)) {
                    continue;
                }
                if (methodFilter != null && !methodFilter.equals(method)) {
                    continue;
                }
                JsonNode op = methodEntry.getValue();
                List<String> tags = readStringArray(op.get("tags"));
                if (tagFilter != null && !tags.contains(tagFilter)) {
                    continue;
                }
                Map<String, Object> endpoint = new LinkedHashMap<>();
                endpoint.put("method", method.toUpperCase(Locale.ROOT));
                endpoint.put("path", path);
                JsonNode summary = op.get("summary");
                if (summary != null && summary.isTextual()) {
                    endpoint.put("summary", summary.asText());
                }
                if (!tags.isEmpty()) {
                    endpoint.put("tags", tags);
                }
                endpoints.add(endpoint);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("count", endpoints.size());
        out.put("endpoints", endpoints);
        return textResult(jsonMapper, out);
    }

    /**
     * Returns the {@code describe_api_endpoint} tool schema.
     * Describes a tool that returns the full OpenAPI schema fragment for a single endpoint,
     * including parameters, request body, and response shapes.
     *
     * @return the MCP tool definition for {@code describe_api_endpoint}
     */
    public McpSchema.Tool getDescribeApiEndpointTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("method", Map.of("type", "string", "description", "HTTP method (GET, POST, PUT, DELETE, PATCH)."));
        props.put("path", Map.of("type", "string", "description",
                "Exact endpoint path from list_api_endpoints (e.g. '/items/{itemname}')."));
        return McpSchema.Tool.builder().name("describe_api_endpoint").description("""
                Return the OpenAPI schema fragment for one endpoint: parameters, request body, \
                response shapes, and description. Call this after list_api_endpoints and before \
                call_api so you know what arguments are required and how to interpret the \
                response.""")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of("method", "path"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code describe_api_endpoint} call.
     * Looks up the requested method+path in the OpenAPI spec and returns its schema fragment.
     *
     * @param exchange the MCP server exchange providing session context
     * @param request the incoming tool call request containing required method and path arguments
     * @return a result containing the OpenAPI operation object, or an error if not found
     */
    public CallToolResult handleDescribeApiEndpoint(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        String method = getStringArg(request.arguments(), "method");
        String path = getStringArg(request.arguments(), "path");
        if (method == null || path == null) {
            return errorResult("'method' and 'path' are required.");
        }
        SpecResult specResult = loadSpec(exchange);
        JsonNode spec = specResult.spec;
        if (spec == null) {
            return errorResult("Could not load OpenAPI spec from " + baseUrl + "/rest/spec: " + specResult.error);
        }
        JsonNode op = spec.path("paths").path(path).path(method.toLowerCase(Locale.ROOT));
        if (op.isMissingNode() || op.isNull()) {
            return errorResult("No such endpoint: " + method.toUpperCase(Locale.ROOT) + " " + path);
        }
        return textResult(jsonMapper, jackson.convertValue(op, Object.class));
    }

    /**
     * Returns the {@code call_api} tool schema.
     * Describes a tool that invokes an arbitrary openHAB REST endpoint using the caller's
     * bearer token, supporting all HTTP methods with path/query parameters and a JSON body.
     *
     * @return the MCP tool definition for {@code call_api}
     */
    public McpSchema.Tool getCallApiTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("method", Map.of("type", "string", "description", "HTTP method (GET, POST, PUT, DELETE, PATCH)."));
        props.put("path",
                Map.of("type", "string", "description",
                        "Endpoint path starting with '/' (e.g. '/items', '/things/{thingUID}'). "
                                + "Path parameters are substituted from pathParams."));
        props.put("pathParams", Map.of("type", "object", "description",
                "Map of path parameter name to value (URL-encoded automatically)."));
        props.put("queryParams", Map.of("type", "object", "description",
                "Map of query parameter name to value. Values are URL-encoded."));
        props.put("body",
                Map.of("description", "Optional JSON body. Pass as an object/array/primitive; it will be serialized."));
        return McpSchema.Tool.builder().name("call_api").description("""
                Invoke an arbitrary openHAB REST endpoint. The call uses your own bearer token, \
                so permissions match a direct REST call. Supports all HTTP methods. Returns \
                {status, body} where body is parsed JSON when possible, otherwise raw text. \
                Use list_api_endpoints and describe_api_endpoint first to understand the \
                request shape. BE CAREFUL — this tool can invoke destructive endpoints \
                (delete items, modify things, change service configs).""")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of("method", "path"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code call_api} call.
     * Builds and executes an HTTP request against the openHAB REST API, returning the
     * status code and parsed response body.
     *
     * @param exchange the MCP server exchange providing session context and bearer token
     * @param request the incoming tool call request containing method, path, and optional params/body
     * @return a result containing the HTTP status and response body
     */
    public CallToolResult handleCallApi(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String method = getStringArg(args, "method");
        String path = getStringArg(args, "path");
        if (method == null || path == null) {
            return errorResult("'method' and 'path' are required.");
        }
        HttpMethod httpMethod = HttpMethod.fromString(method.toUpperCase(Locale.ROOT));
        if (httpMethod == null) {
            return errorResult("Unsupported HTTP method: " + method);
        }
        String token = tokenForSession.apply(exchange.sessionId());
        if (token == null) {
            return errorResult(
                    "No bearer token captured for this session. Reconnect with Authorization: Bearer <token>.");
        }

        Map<String, Object> pathParams = McpToolUtils.getObjectMapArg(args, "pathParams");
        String resolvedPath = substitutePathParams(path, pathParams);
        if (resolvedPath.contains("..")) {
            return errorResult("Path must not contain '..' segments.");
        }
        String url = baseUrl + "/rest" + (resolvedPath.startsWith("/") ? resolvedPath : "/" + resolvedPath);
        Map<String, Object> queryParams = McpToolUtils.getObjectMapArg(args, "queryParams");
        String query = buildQueryString(queryParams);
        if (!query.isEmpty()) {
            url = url + (url.contains("?") ? "&" : "?") + query;
        }

        try {
            Request req = httpClient.newRequest(URI.create(url)).method(httpMethod)
                    .header("Authorization", "Bearer " + token).header("Accept", "application/json");
            Object body = args.get("body");
            if (body != null && httpMethod != HttpMethod.GET && httpMethod != HttpMethod.DELETE) {
                String bodyJson = jackson.writeValueAsString(body);
                req.content(new StringContentProvider(bodyJson, StandardCharsets.UTF_8), "application/json");
            }
            ContentResponse resp = req.send();
            return textResult(jsonMapper, buildResponse(resp));
        } catch (Exception e) {
            logger.debug("call_api failed for {} {}: {}", method, url, e.getMessage());
            return errorResult("Request failed: " + e.getMessage());
        }
    }

    private record SpecResult(@Nullable JsonNode spec, String error) {
    }

    private SpecResult loadSpec(McpSyncServerExchange exchange) {
        long now = System.currentTimeMillis();
        JsonNode cached = specCache;
        if (cached != null && (now - specFetchedAt) < SPEC_TTL_MS) {
            return new SpecResult(cached, "");
        }
        String sessionId = exchange.sessionId();
        String token = tokenForSession.apply(sessionId);
        if (token == null) {
            logger.debug("loadSpec: no token captured for session {}", sessionId);
            return new SpecResult(null,
                    "no bearer token captured for this MCP session (client must send Authorization: Bearer oh.*)");
        }
        try {
            ContentResponse resp = httpClient.newRequest(URI.create(baseUrl + "/rest/spec")).method(HttpMethod.GET)
                    .header("Authorization", "Bearer " + token).header("Accept", "application/json").send();
            if (resp.getStatus() != 200) {
                logger.debug("Spec fetch returned {} ({})", resp.getStatus(), resp.getReason());
                return new SpecResult(null, "HTTP " + resp.getStatus() + " " + resp.getReason());
            }
            JsonNode parsed = jackson.readTree(resp.getContentAsString());
            specCache = parsed;
            specFetchedAt = now;
            return new SpecResult(parsed, "");
        } catch (Exception e) {
            logger.debug("Failed to fetch OpenAPI spec: {}", e.getMessage(), e);
            return new SpecResult(null, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static String substitutePathParams(String path, @Nullable Map<String, Object> pathParams) {
        if (pathParams == null || pathParams.isEmpty()) {
            return path;
        }
        String result = path;
        for (Map.Entry<String, Object> entry : pathParams.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() == null ? "" : entry.getValue().toString();
            result = result.replace(placeholder, URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
        return result;
    }

    private static String buildQueryString(@Nullable Map<String, Object> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private Map<String, Object> buildResponse(ContentResponse resp) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", resp.getStatus());
        String text = resp.getContentAsString();
        if (text.length() > MAX_RESPONSE_CHARS) {
            out.put("truncated", true);
            text = text.substring(0, MAX_RESPONSE_CHARS);
        }
        if (text.isEmpty()) {
            return out;
        }
        try {
            JsonNode parsed = jackson.readTree(text);
            out.put("body", jackson.convertValue(parsed, Object.class));
        } catch (Exception e) {
            out.put("body", text);
        }
        return out;
    }

    private static boolean isHttpMethod(String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "get", "post", "put", "delete", "patch", "head", "options" -> true;
            default -> false;
        };
    }

    private static List<String> readStringArray(@Nullable JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode element : node) {
            if (element.isTextual()) {
                out.add(element.asText());
            }
        }
        return out;
    }

    /**
     * Invalidates the OpenAPI spec cache so the next list/describe call re-fetches.
     * Called when the server restarts to avoid serving stale data across reloads.
     */
    public void invalidateSpecCache() {
        specCache = null;
        specFetchedAt = 0;
    }
}
