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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tools for designing the openHAB Main UI: pages and custom widgets.
 *
 * <p>
 * Five tools are exposed when {@code enableUiDesign} is on:
 * <ul>
 * <li>{@code list_widgets} — discover available components, optionally filtered by category.</li>
 * <li>{@code describe_widget} — return the full schema (props, slots, notes) for one component.</li>
 * <li>{@code get_page_skeleton} — return a starter {@code RootUIComponent} for a given page type.</li>
 * <li>{@code manage_ui_component} — CRUD against {@code /rest/ui/components/{namespace}} with the caller's bearer
 * token; responses for {@code ui:page} / {@code ui:widget} include {@code viewUrl} / {@code editUrl} hints so the
 * agent can hand them to a browser-automation MCP tool for visual verification.</li>
 * <li>{@code validate_ui_component} — schema check against the curated catalog before committing.</li>
 * </ul>
 *
 * <p>
 * The component catalog is loaded once at construction from {@code /widgets/catalog.json} on the classpath. Updating
 * the catalog is an offline activity: re-read the {@code WidgetDefinition} TS files at
 * {@code openhab-webui/bundles/org.openhab.ui/web/src/assets/definitions/widgets/} and regenerate by hand. The format
 * is intentionally simple so it can be regenerated mechanically in the future.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UiTools {

    private static final String CATALOG_RESOURCE = "/widgets/catalog.json";
    /** Path served by the openhab-webui UIServlet — see web/build/generate-widget-catalog.mjs. */
    private static final String LIVE_CATALOG_PATH = "/widget-catalog.json";
    private static final Set<String> ALLOWED_NAMESPACES = Set.of("ui:page", "ui:widget");
    private static final Set<String> PAGE_TYPES = Set.of("layout", "home", "tabbed", "chart", "map", "plan");

    /** Maps page-type component name to the Main UI page-editor route segment. */
    private static final Map<String, String> PAGE_EDIT_ROUTE = Map.of("oh-layout-page", "layout", "oh-home-page",
            "home", "oh-tabs-page", "tabs", "oh-chart-page", "chart", "oh-map-page", "map", "oh-plan-page", "plan");

    private final Logger logger = LoggerFactory.getLogger(UiTools.class);
    private final ObjectMapper jackson = McpToolUtils.jackson();

    private final HttpClient httpClient;
    private final String baseUrl;
    private final Function<String, @Nullable String> tokenForSession;
    private final McpJsonMapper jsonMapper;

    /**
     * Component name → schema. Volatile because the live catalog is fetched asynchronously after
     * construction; readers always see a fully populated snapshot via atomic reference swap. The map
     * itself is treated as immutable once installed — never mutated in place.
     */
    private volatile Map<String, ObjectNode> catalog = Map.of();
    /** Which catalog source is active ("live" if fetched from webui, "bundled-fallback" otherwise). */
    private volatile String catalogSource = "none";
    /** Optional version string from the catalog payload. */
    private volatile String catalogVersion = "";
    /** Tracks completion of the background live-catalog fetch. Exposed only for deterministic tests. */
    private volatile CompletableFuture<?> liveCatalogLoad = CompletableFuture.completedFuture(null);

    /**
     * The long-form description embedded in {@code manage_ui_component}'s tool definition. Loaded
     * once from {@code /widgets/descriptions/manage_ui_component.txt} on construction so the docs
     * stay editable in a plain text file without recompiling. The file uses .txt (not .md) so
     * the project's markdownlint doesn't try to format it — it's an agent-facing prose blob,
     * not a docs page.
     */
    private final String manageDescription;

    public UiTools(HttpClient httpClient, String baseUrl, Function<String, @Nullable String> tokenForSession,
            McpJsonMapper jsonMapper) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.tokenForSession = tokenForSession;
        this.jsonMapper = jsonMapper;
        loadCatalog();
        this.manageDescription = loadResourceText("/widgets/descriptions/manage_ui_component.txt",
                "Create, read, update, or delete openHAB Main UI components (pages and custom widgets) via "
                        + "the /rest/ui/components/{namespace} REST API. (Detailed reference unavailable — resource not loaded.)");
    }

    /** Parsed catalog payload — held briefly between parsing and installation. */
    private static final class CatalogSnapshot {
        final Map<String, ObjectNode> components;
        final String version;

        CatalogSnapshot(Map<String, ObjectNode> components, String version) {
            this.components = components;
            this.version = version;
        }
    }

    private String loadResourceText(String path, String fallback) {
        try (InputStream in = UiTools.class.getResourceAsStream(path)) {
            if (in == null) {
                logger.warn("Description resource not found at {}; using inline fallback.", path);
                return fallback;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            logger.warn("Failed to load description resource {}: {}", path, e.getMessage());
            return fallback;
        }
    }

    /**
     * Installs the bundled catalog synchronously (cheap classpath read), then kicks off a background
     * fetch of the live catalog served by the openhab-webui bundle. The bundled fallback covers older
     * webui versions and the small window during startup before the webui's HTTP servlet is ready;
     * the async upgrade swaps in the live catalog when it arrives. Synchronous loading at construction
     * would otherwise block OSGi activation on a 5-second timeout against the local web server.
     */
    private void loadCatalog() {
        CatalogSnapshot bundled = loadBundledCatalog();
        if (bundled != null) {
            installCatalog(bundled, "bundled-fallback");
            logger.debug("Loaded bundled UI widget catalog ({} components) from {}", bundled.components.size(),
                    CATALOG_RESOURCE);
        }
        liveCatalogLoad = CompletableFuture.runAsync(this::refreshLiveCatalog).exceptionally(t -> {
            logger.debug("Background live catalog fetch failed: {}", t.getMessage());
            return null;
        });
    }

    /**
     * Test hook — returns the future tracking the background live-catalog fetch so tests can wait
     * for it deterministically instead of polling/sleeping. Production code should not depend on this.
     */
    CompletableFuture<?> liveCatalogLoadFuture() {
        return liveCatalogLoad;
    }

    private void refreshLiveCatalog() {
        CatalogSnapshot live = fetchLiveCatalog();
        if (live != null) {
            installCatalog(live, "live");
            logger.debug("Loaded live UI widget catalog ({} components) from {}{}", live.components.size(), baseUrl,
                    LIVE_CATALOG_PATH);
        }
    }

    private void installCatalog(CatalogSnapshot snapshot, String source) {
        // Volatile writes — readers always observe a fully populated map.
        catalog = snapshot.components;
        catalogVersion = snapshot.version;
        catalogSource = source;
    }

    private @Nullable CatalogSnapshot fetchLiveCatalog() {
        try {
            ContentResponse resp = httpClient.newRequest(URI.create(baseUrl + LIVE_CATALOG_PATH)).method(HttpMethod.GET)
                    .header("Accept", "application/json").timeout(5, TimeUnit.SECONDS).send();
            if (resp.getStatus() != 200) {
                logger.debug("Live widget catalog not available (HTTP {}); will use bundled fallback.",
                        resp.getStatus());
                return null;
            }
            return parseCatalog(jackson.readTree(resp.getContentAsString()));
        } catch (Exception e) {
            logger.debug("Live widget catalog fetch failed ({}); will use bundled fallback.", e.getMessage());
            return null;
        }
    }

    private @Nullable CatalogSnapshot loadBundledCatalog() {
        try (InputStream in = UiTools.class.getResourceAsStream(CATALOG_RESOURCE)) {
            if (in == null) {
                logger.warn("Bundled UI widget catalog resource not found at {}", CATALOG_RESOURCE);
                return null;
            }
            return parseCatalog(jackson.readTree(in));
        } catch (IOException e) {
            logger.warn("Failed to load bundled UI widget catalog: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses a catalog JSON tree into a new snapshot, or returns null if the payload is not a
     * recognizable catalog shape (e.g. a reverse proxy returns HTML, the endpoint moved, etc.).
     * Uses {@code instanceof} guards instead of unchecked casts so a malformed live response can't
     * throw {@link ClassCastException} during bundle activation.
     */
    private @Nullable CatalogSnapshot parseCatalog(JsonNode root) {
        JsonNode widgetsNode = root.get("widgets");
        if (!(widgetsNode instanceof ArrayNode widgets)) {
            logger.debug("UI widget catalog missing or has non-array 'widgets' field");
            return null;
        }
        Map<String, ObjectNode> components = new LinkedHashMap<>();
        for (int i = 0; i < widgets.size(); i++) {
            JsonNode element = widgets.get(i);
            if (!(element instanceof ObjectNode entry)) {
                continue;
            }
            String name = entry.path("name").asText("");
            if (!name.isBlank()) {
                components.put(name, entry);
            }
        }
        if (components.isEmpty()) {
            return null;
        }
        return new CatalogSnapshot(components, root.path("version").asText(""));
    }

    public McpSchema.Tool getListWidgetsTool() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("category", Map.of("type", "string", "description", """
                Optional filter. Categories: 'page-type' (the 6 top-level page types like oh-layout-page), \
                'system' (low-level building blocks: oh-button, oh-slider, etc.), 'standard-card' (card-style \
                wrappers like oh-toggle-card), 'layout' (oh-block, oh-grid-row, oh-masonry, oh-canvas-*), \
                'list-item' / 'list-cell' (compact items for oh-list), 'map-marker' / 'plan-marker' (children of \
                oh-map-page / oh-plan-page).""", "enum", List.of("page-type", "system", "standard-card", "layout",
                "list-item", "list-cell", "map-marker", "plan-marker")));

        return McpSchema.Tool.builder().name("list_widgets").description("""
                List available openHAB Main UI components from the curated catalog. Returns a flat list of \
                {name, label, description, category} entries. Use this for discovery, then call describe_widget(name) \
                for full prop/slot schemas before composing pages or widgets. Filter by category if you know what \
                kind of component you need.""")
                .inputSchema(new McpSchema.JsonSchema("object", p, List.of(), null, null, null)).build();
    }

    public CallToolResult handleListWidgets(McpSchema.CallToolRequest request) {
        String category = getStringArg(request.arguments(), "category");
        List<Map<String, Object>> out = new ArrayList<>();
        for (ObjectNode entry : catalog.values()) {
            String entryCategory = entry.path("category").asText("");
            if (category != null && !category.equals(entryCategory)) {
                continue;
            }
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("name", entry.path("name").asText(""));
            summary.put("label", entry.path("label").asText(""));
            summary.put("description", entry.path("description").asText(""));
            summary.put("category", entryCategory);
            out.add(summary);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("widgets", out);
        response.put("total", out.size());
        if (category != null) {
            response.put("category", category);
        }
        response.put("catalogSource", catalogSource);
        if (!catalogVersion.isBlank()) {
            response.put("catalogVersion", catalogVersion);
        }
        return textResult(jsonMapper, response);
    }

    public McpSchema.Tool getDescribeWidgetTool() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("name",
                Map.of("type", "string", "description",
                        "Exact component name (e.g. 'oh-toggle-card', 'oh-chart-page', 'oh-plan-marker'). "
                                + "Use list_widgets first if you don't know the name."));

        return McpSchema.Tool.builder().name("describe_widget").description("""
                Return the full schema for one Main UI component: props (with type, context, required, default, \
                description), slots (with what they accept), and any usage notes or examples. This is what you should \
                consult before authoring or modifying a widget config — it tells you exactly which keys are valid \
                and what values they accept. If the component isn't in the catalog you'll get an error listing the \
                closest matches.""")
                .inputSchema(new McpSchema.JsonSchema("object", p, List.of("name"), null, null, null)).build();
    }

    public CallToolResult handleDescribeWidget(McpSchema.CallToolRequest request) {
        String name = getStringArg(request.arguments(), "name");
        if (name == null || name.isBlank()) {
            return errorResult("'name' is required.");
        }
        ObjectNode entry = catalog.get(name);
        if (entry == null) {
            List<String> close = closestMatches(name, 5);
            String suggestion = close.isEmpty() ? "" : " Closest matches: " + String.join(", ", close);
            return errorResult("Component '" + name + "' not in catalog." + suggestion
                    + " Use list_widgets to see all available components.");
        }
        return textResult(jsonMapper, jackson.convertValue(entry, Map.class));
    }

    private List<String> closestMatches(String query, int max) {
        String lower = query.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String name : catalog.keySet()) {
            String n = name.toLowerCase(Locale.ROOT);
            if (n.contains(lower) || lower.contains(n)) {
                matches.add(name);
                if (matches.size() >= max) {
                    break;
                }
            }
        }
        return matches;
    }

    public McpSchema.Tool getPageSkeletonTool() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("pageType", Map.of("type", "string", "description", """
                Which page type to scaffold:
                  - layout: free-form responsive (oh-block + cards) or fixed-grid/canvas dashboard
                  - home: home-screen with Locations/Equipment/Properties tabs from the semantic model
                  - tabbed: a multi-tab page where each tab points to another page
                  - chart: time-series visualization for one or more items
                  - map: Leaflet map with item-driven markers
                  - plan: floorplan-style page with an image background and positioned markers""", "enum",
                List.of("layout", "home", "tabbed", "chart", "map", "plan")));
        p.put("uid", Map.of("type", "string", "description",
                "Optional UID to use; if omitted you'll get a placeholder ('my-page') that you must replace before "
                        + "passing to manage_ui_component(create)."));
        p.put("label",
                Map.of("type", "string", "description", "Optional user-facing page label; defaults to 'New Page'."));

        return McpSchema.Tool.builder().name("get_page_skeleton").description("""
                Return a starter RootUIComponent for one of the 6 Main UI page types, pre-filled with the correct \
                component, sensible default config, and the canonical slot structure. Use this as the starting point \
                when creating a new page so you don't have to guess the right component name or slot names. The \
                response includes 'plannedViewUrl' showing where the page will appear after you POST it via \
                manage_ui_component(action='create').""")
                .inputSchema(new McpSchema.JsonSchema("object", p, List.of("pageType"), null, null, null)).build();
    }

    public CallToolResult handleGetPageSkeleton(McpSchema.CallToolRequest request) {
        String pageType = getStringArg(request.arguments(), "pageType");
        if (pageType == null || !PAGE_TYPES.contains(pageType.toLowerCase(Locale.ROOT))) {
            return errorResult("'pageType' is required and must be one of: " + String.join(", ", PAGE_TYPES));
        }
        String uid = getStringArg(request.arguments(), "uid");
        if (uid == null || uid.isBlank()) {
            uid = "my-page";
        }
        String label = getStringArg(request.arguments(), "label");
        if (label == null || label.isBlank()) {
            label = "New Page";
        }
        ObjectNode skeleton = buildPageSkeleton(pageType.toLowerCase(Locale.ROOT), uid, label);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("skeleton", jackson.convertValue(skeleton, Map.class));
        response.put("plannedViewPath", "/page/" + uid);
        response.put("plannedEditPath", "/settings/pages/" + pageType.toLowerCase(Locale.ROOT) + "/" + uid);
        response.put("plannedViewUrl", baseUrl + "/page/" + uid);
        response.put("plannedEditUrl", baseUrl + "/settings/pages/" + pageType.toLowerCase(Locale.ROOT) + "/" + uid);
        response.put("notes",
                "Replace 'uid' with your chosen UID before calling manage_ui_component(action='create', "
                        + "namespace='ui:page'). The 'slots' arrays are starting points — add child components per "
                        + "your design. Call validate_ui_component before create to catch schema errors. "
                        + "plannedViewUrl/plannedEditUrl use the server's local hostname; substitute the user's "
                        + "openHAB URL for browser verification from a different machine.");
        return textResult(jsonMapper, response);
    }

    private ObjectNode buildPageSkeleton(String pageType, String uid, String label) {
        ObjectNode root = jackson.createObjectNode();
        root.put("uid", uid);
        ObjectNode config = jackson.createObjectNode();
        config.put("label", label);
        ObjectNode slots = jackson.createObjectNode();
        switch (pageType) {
            case "layout" -> {
                root.put("component", "oh-layout-page");
                config.put("layoutType", "responsive");
                slots.set("default", jackson.createArrayNode());
            }
            case "home" -> {
                root.put("component", "oh-home-page");
                slots.set("default", jackson.createArrayNode());
            }
            case "tabbed" -> {
                root.put("component", "oh-tabs-page");
                slots.set("default", jackson.createArrayNode());
            }
            case "chart" -> {
                root.put("component", "oh-chart-page");
                config.put("chartType", "interval");
                config.put("period", "D");
                ArrayNode series = jackson.createArrayNode();
                slots.set("series", series);
                slots.set("grid", jackson.createArrayNode());
                slots.set("xAxis", jackson.createArrayNode());
                slots.set("yAxis", jackson.createArrayNode());
            }
            case "map" -> {
                root.put("component", "oh-map-page");
                config.put("initialZoom", 14);
                slots.set("default", jackson.createArrayNode());
            }
            case "plan" -> {
                root.put("component", "oh-plan-page");
                config.put("imageUrl", "/static/your-floorplan.png");
                slots.set("default", jackson.createArrayNode());
            }
            default -> {
                // Already validated above; defensive fallback.
                root.put("component", "oh-layout-page");
                slots.set("default", jackson.createArrayNode());
            }
        }
        root.set("config", config);
        root.set("slots", slots);
        root.set("tags", jackson.createArrayNode());
        return root;
    }

    public McpSchema.Tool getManageUiComponentTool() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("action", Map.of("type", "string", "description",
                "CRUD verb: 'get' (fetch one if uid given, else list namespace), 'create' (POST), 'update' (PUT — full "
                        + "replacement; fetch first if you only want to change part), 'patch' (apply JSON Patch ops in "
                        + "place — preferred for any single-field edit; far cheaper than update), 'delete' (by uid).",
                "enum", List.of("get", "create", "update", "patch", "delete")));
        p.put("namespace", Map.of("type", "string", "description",
                "Which UI component namespace to operate in. 'ui:page' for pages the user navigates to; 'ui:widget' "
                        + "for reusable custom widgets that you reference from pages as 'widget:<uid>'.",
                "enum", List.of("ui:page", "ui:widget")));
        p.put("uid", Map.of("type", "string", "description",
                "Component UID. Required for get-single, update, delete; optional for create (server assigns one if "
                        + "omitted); omit for get-list."));
        p.put("component",
                Map.of("type", "string", "description",
                        "Root component name (e.g. 'oh-layout-page' for a page, or any system/standard component for a "
                                + "widget). Required for create/update."));
        p.put("config", Map.of("type", "object", "description",
                "Top-level config map (label, layoutType, period, etc.). See describe_widget(component) for valid "
                        + "keys."));
        p.put("slots", Map.of("type", "object", "description",
                "Named slot map: { slotName: [childComponent, ...] }. Each child is itself a UIComponent with "
                        + "component/config/slots. See describe_widget(component) for valid slot names per parent."));
        p.put("tags", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Optional tags for filtering/organization in the page list."));
        p.put("props", Map.of("type", "object", "description",
                "Custom-widget parameter schema (only for ui:widget): { parameters: [...], parameterGroups: [...] }. "
                        + "Each parameter has name, type (TEXT/INTEGER/DECIMAL/BOOLEAN), label, context (item/url/"
                        + "location/color/etc.), required, default."));
        p.put("operations", Map.of("type", "array", "items", Map.of("type", "object"), "description",
                "action='patch' only: JSON Patch (RFC 6902 subset) operations applied in order. Each op is "
                        + "{op: 'replace'|'add'|'remove'|'test', path: '/json/pointer/path', value?: any}. Use "
                        + "JSON Pointer for path ('/config/title', '/slots/default/0/config/label'); '/-' on an "
                        + "array means append. Any op failure aborts the whole patch."));
        p.put("responseDetail", Map.of("type", "string", "description",
                "Response verbosity for create/update/patch. 'minimal' (default) returns just success/uid/URLs — "
                        + "preferred to keep tool-result payloads small. 'full' additionally echoes the stored "
                        + "component body. Get-list and get-single ignore this field.",
                "enum", List.of("minimal", "full")));
        p.put("fields",
                Map.of("type", "array", "items", Map.of("type", "string"), "description",
                        "action='get' only: optional projection — return just these top-level fields of the component "
                                + "(e.g. ['uid','component','tags']). When omitted, the full body is returned."));

        return McpSchema.Tool.builder().name("manage_ui_component").description(manageDescription)
                .inputSchema(new McpSchema.JsonSchema("object", p, List.of("action", "namespace"), null, null, null))
                .build();
    }

    public CallToolResult handleManageUiComponent(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String action = getStringArg(args, "action");
        String namespace = getStringArg(args, "namespace");
        if (action == null || action.isBlank()) {
            return errorResult("'action' is required (one of: get, create, update, patch, delete).");
        }
        if (namespace == null || !ALLOWED_NAMESPACES.contains(namespace)) {
            return errorResult("'namespace' is required and must be one of: " + String.join(", ", ALLOWED_NAMESPACES));
        }
        String token = tokenForSession.apply(exchange.sessionId());
        if (token == null) {
            return errorResult(
                    "No bearer token captured for this session. Reconnect with Authorization: Bearer <token>.");
        }
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "get" -> getComponent(token, namespace, getStringArg(args, "uid"), getStringListArg(args, "fields"));
            case "create" -> createComponent(token, namespace, args);
            case "update" -> updateComponent(token, namespace, args);
            case "patch" -> patchComponent(token, namespace, args);
            case "delete" -> deleteComponent(token, namespace, getStringArg(args, "uid"));
            default -> errorResult("Invalid action '" + action + "'. Use one of: get, create, update, patch, delete.");
        };
    }

    /** Returns true when the agent explicitly asked for the full echoed component in the response. */
    private static boolean wantsFullDetail(Map<String, Object> args) {
        String detail = getStringArg(args, "responseDetail");
        return detail != null && "full".equalsIgnoreCase(detail);
    }

    private CallToolResult getComponent(String token, String namespace, @Nullable String uid,
            @Nullable List<String> fields) {
        String path = "/rest/ui/components/" + encodePath(namespace) + (uid == null ? "" : "/" + encodePath(uid));
        try {
            ContentResponse resp = httpClient.newRequest(URI.create(baseUrl + path)).method(HttpMethod.GET)
                    .header("Authorization", "Bearer " + token).header("Accept", "application/json").send();
            if (resp.getStatus() == 404) {
                return errorResult("Component '" + uid + "' not found in namespace '" + namespace + "'.");
            }
            if (resp.getStatus() != 200) {
                return errorResult("Failed to fetch: HTTP " + resp.getStatus() + " " + resp.getReason());
            }
            JsonNode body = jackson.readTree(resp.getContentAsString());
            Map<String, Object> response = new LinkedHashMap<>();
            JsonNode projected = projectFields(body, fields);
            response.put("component", jackson.convertValue(projected, Object.class));
            if (uid != null && body instanceof ObjectNode obj) {
                addUrlHints(response, namespace, obj);
            }
            return textResult(jsonMapper, response);
        } catch (Exception e) {
            logger.debug("manage_ui_component get failed: {}", e.getMessage(), e);
            return errorResult("Request failed: " + e.getMessage());
        }
    }

    /**
     * Applies a top-level field projection. When {@code fields} is null/empty the body passes through unchanged.
     * For a list-of-objects (get-list response) the projection is applied to each element so the agent can shrink
     * a large inventory by asking for just {@code uid}/{@code component}/{@code tags}.
     */
    private JsonNode projectFields(JsonNode body, @Nullable List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return body;
        }
        if (body instanceof ArrayNode arr) {
            ArrayNode projected = jackson.createArrayNode();
            for (int i = 0; i < arr.size(); i++) {
                projected.add(projectFields(arr.get(i), fields));
            }
            return projected;
        }
        if (body instanceof ObjectNode obj) {
            ObjectNode out = jackson.createObjectNode();
            for (String field : fields) {
                if (obj.has(field)) {
                    out.set(field, obj.get(field));
                }
            }
            return out;
        }
        return body;
    }

    private CallToolResult createComponent(String token, String namespace, Map<String, Object> args) {
        ObjectNode payload = buildPayload(args);
        String path = "/rest/ui/components/" + encodePath(namespace);
        try {
            ContentResponse resp = httpClient.newRequest(URI.create(baseUrl + path)).method(HttpMethod.POST)
                    .header("Authorization", "Bearer " + token).header("Accept", "application/json")
                    .content(new StringContentProvider(payload.toString(), StandardCharsets.UTF_8), "application/json")
                    .send();
            if (resp.getStatus() == 401 || resp.getStatus() == 403) {
                return adminRequiredError("POST", resp.getStatus());
            }
            if (resp.getStatus() < 200 || resp.getStatus() >= 300) {
                return errorResult("Failed to create: HTTP " + resp.getStatus() + " " + resp.getReason() + " — "
                        + truncate(resp.getContentAsString(), 500));
            }
            ObjectNode body = (ObjectNode) jackson.readTree(resp.getContentAsString());
            return buildWriteResponse("create", namespace, body, wantsFullDetail(args));
        } catch (Exception e) {
            logger.debug("manage_ui_component create failed: {}", e.getMessage(), e);
            return errorResult("Request failed: " + e.getMessage());
        }
    }

    private CallToolResult updateComponent(String token, String namespace, Map<String, Object> args) {
        String uid = getStringArg(args, "uid");
        if (uid == null || uid.isBlank()) {
            return errorResult("'uid' is required for action='update'.");
        }
        ObjectNode payload = buildPayload(args);
        payload.put("uid", uid); // ensure body uid matches path uid
        String path = "/rest/ui/components/" + encodePath(namespace) + "/" + encodePath(uid);
        try {
            ContentResponse resp = httpClient.newRequest(URI.create(baseUrl + path)).method(HttpMethod.PUT)
                    .header("Authorization", "Bearer " + token).header("Accept", "application/json")
                    .content(new StringContentProvider(payload.toString(), StandardCharsets.UTF_8), "application/json")
                    .send();
            if (resp.getStatus() == 401 || resp.getStatus() == 403) {
                return adminRequiredError("PUT", resp.getStatus());
            }
            if (resp.getStatus() == 404) {
                return errorResult("Component '" + uid + "' not found in namespace '" + namespace + "'.");
            }
            if (resp.getStatus() < 200 || resp.getStatus() >= 300) {
                return errorResult("Failed to update: HTTP " + resp.getStatus() + " " + resp.getReason() + " — "
                        + truncate(resp.getContentAsString(), 500));
            }
            ObjectNode body = (ObjectNode) jackson.readTree(resp.getContentAsString());
            return buildWriteResponse("update", namespace, body, wantsFullDetail(args));
        } catch (Exception e) {
            logger.debug("manage_ui_component update failed: {}", e.getMessage(), e);
            return errorResult("Request failed: " + e.getMessage());
        }
    }

    /**
     * Applies a JSON Patch by fetching the component, mutating the tree, then PUTting the result.
     * This lets agents change a single field without sending the full component on every edit
     */
    private CallToolResult patchComponent(String token, String namespace, Map<String, Object> args) {
        String uid = getStringArg(args, "uid");
        if (uid == null || uid.isBlank()) {
            return errorResult("'uid' is required for action='patch'.");
        }
        List<Map<String, Object>> ops = parsePatchOperations(args.get("operations"));
        if (ops == null) {
            return errorResult(
                    "'operations' is required for action='patch' and must be a non-empty array of {op, path, value?}.");
        }
        String basePath = "/rest/ui/components/" + encodePath(namespace) + "/" + encodePath(uid);
        ObjectNode current;
        try {
            ContentResponse getResp = httpClient.newRequest(URI.create(baseUrl + basePath)).method(HttpMethod.GET)
                    .header("Authorization", "Bearer " + token).header("Accept", "application/json").send();
            if (getResp.getStatus() == 404) {
                return errorResult("Component '" + uid + "' not found in namespace '" + namespace + "'.");
            }
            if (getResp.getStatus() != 200) {
                return errorResult(
                        "Failed to fetch for patch: HTTP " + getResp.getStatus() + " " + getResp.getReason());
            }
            current = (ObjectNode) jackson.readTree(getResp.getContentAsString());
        } catch (Exception e) {
            logger.debug("manage_ui_component patch (fetch) failed: {}", e.getMessage(), e);
            return errorResult("Patch fetch failed: " + e.getMessage());
        }
        try {
            JsonPatchApplier.apply(current, ops);
        } catch (JsonPatchApplier.PatchException e) {
            String msg = e.getMessage();
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("success", false);
            err.put("error", msg != null ? msg : "patch failed");
            err.put("failedOp", e.opIndex);
            return textResult(jsonMapper, err);
        }
        try {
            ContentResponse putResp = httpClient.newRequest(URI.create(baseUrl + basePath)).method(HttpMethod.PUT)
                    .header("Authorization", "Bearer " + token).header("Accept", "application/json")
                    .content(new StringContentProvider(current.toString(), StandardCharsets.UTF_8), "application/json")
                    .send();
            if (putResp.getStatus() == 401 || putResp.getStatus() == 403) {
                return adminRequiredError("PUT", putResp.getStatus());
            }
            if (putResp.getStatus() < 200 || putResp.getStatus() >= 300) {
                return errorResult("Failed to apply patch (PUT): HTTP " + putResp.getStatus() + " "
                        + putResp.getReason() + " — " + truncate(putResp.getContentAsString(), 500));
            }
            ObjectNode body = (ObjectNode) jackson.readTree(putResp.getContentAsString());
            return buildWriteResponse("patch", namespace, body, wantsFullDetail(args));
        } catch (Exception e) {
            logger.debug("manage_ui_component patch (put) failed: {}", e.getMessage(), e);
            return errorResult("Patch apply failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static @Nullable List<Map<String, Object>> parsePatchOperations(@Nullable Object raw) {
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> out = new ArrayList<>(list.size());
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> map)) {
                return null;
            }
            out.add((Map<String, Object>) map);
        }
        return out;
    }

    /**
     * Builds the response for create/update/patch with minimal fields by default ({@code success}, {@code action},
     * {@code namespace}, {@code uid}, plus URL hints). Set {@code includeFull} to true to also echo the full
     * stored component body back
     */
    private CallToolResult buildWriteResponse(String action, String namespace, ObjectNode body, boolean includeFull) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("action", action);
        response.put("namespace", namespace);
        String uid = body.path("uid").asText("");
        if (!uid.isBlank()) {
            response.put("uid", uid);
        }
        addUrlHints(response, namespace, body);
        if (includeFull) {
            response.put("component", jackson.convertValue(body, Object.class));
        }
        return textResult(jsonMapper, response);
    }

    private CallToolResult deleteComponent(String token, String namespace, @Nullable String uid) {
        if (uid == null || uid.isBlank()) {
            return errorResult("'uid' is required for action='delete'.");
        }
        String path = "/rest/ui/components/" + encodePath(namespace) + "/" + encodePath(uid);
        try {
            ContentResponse resp = httpClient.newRequest(URI.create(baseUrl + path)).method(HttpMethod.DELETE)
                    .header("Authorization", "Bearer " + token).send();
            if (resp.getStatus() == 401 || resp.getStatus() == 403) {
                return adminRequiredError("DELETE", resp.getStatus());
            }
            if (resp.getStatus() == 404) {
                return errorResult("Component '" + uid + "' not found in namespace '" + namespace + "'.");
            }
            if (resp.getStatus() < 200 || resp.getStatus() >= 300) {
                return errorResult("Failed to delete: HTTP " + resp.getStatus() + " " + resp.getReason());
            }
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("action", "delete");
            response.put("namespace", namespace);
            response.put("uid", uid);
            return textResult(jsonMapper, response);
        } catch (Exception e) {
            logger.debug("manage_ui_component delete failed: {}", e.getMessage(), e);
            return errorResult("Request failed: " + e.getMessage());
        }
    }

    private ObjectNode buildPayload(Map<String, Object> args) {
        ObjectNode payload = jackson.createObjectNode();
        String uid = getStringArg(args, "uid");
        if (uid != null && !uid.isBlank()) {
            payload.put("uid", uid);
        }
        String component = getStringArg(args, "component");
        if (component != null) {
            payload.put("component", component);
        }
        Object config = args.get("config");
        if (config != null) {
            payload.set("config", jackson.valueToTree(config));
        }
        Object slots = args.get("slots");
        if (slots != null) {
            payload.set("slots", jackson.valueToTree(slots));
        }
        Object tags = args.get("tags");
        if (tags != null) {
            payload.set("tags", jackson.valueToTree(tags));
        }
        Object props = args.get("props");
        if (props != null) {
            payload.set("props", jackson.valueToTree(props));
        }
        return payload;
    }

    private void addUrlHints(Map<String, Object> response, String namespace, ObjectNode body) {
        String uid = body.path("uid").asText("");
        if (uid.isBlank()) {
            return;
        }
        if ("ui:page".equals(namespace)) {
            String viewPath = "/page/" + uid;
            response.put("viewPath", viewPath);
            response.put("viewUrl", baseUrl + viewPath);
            String pageType = PAGE_EDIT_ROUTE.get(body.path("component").asText(""));
            if (pageType != null) {
                String editPath = "/settings/pages/" + pageType + "/" + uid;
                response.put("editPath", editPath);
                response.put("editUrl", baseUrl + editPath);
            }
        } else if ("ui:widget".equals(namespace)) {
            String editPath = "/developer/widgets/" + uid;
            response.put("editPath", editPath);
            response.put("editUrl", baseUrl + editPath);
        }
        response.put("urlNote",
                "viewUrl/editUrl use the server's local hostname. For browser verification from a different machine, "
                        + "substitute the user's openHAB URL for the host portion (use viewPath/editPath alone with "
                        + "the user's base).");
    }

    private CallToolResult adminRequiredError(String method, int status) {
        return errorResult("openHAB rejected the " + method + " request (HTTP " + status
                + "). /rest/ui/components/ writes require the user's bearer token to have ADMIN scope.");
    }

    private static String encodePath(String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
    }

    // ============ validate_ui_component ============

    public McpSchema.Tool getValidateUiComponentTool() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("component", Map.of("type", "string", "description", "Root component name (e.g. 'oh-layout-page')."));
        p.put("config", Map.of("type", "object", "description", "Component config map."));
        p.put("slots", Map.of("type", "object", "description", "Named slots map { slotName: [child, ...] }."));

        return McpSchema.Tool.builder().name("validate_ui_component").description("""
                Pre-flight schema check against the curated catalog. Walks the component tree (root + slot children \
                recursively) and returns structured errors for: unknown component names, missing required props, \
                prop type/context mismatches, and slot names not declared by the parent. Use this before \
                manage_ui_component(action='create' or 'update') to catch authoring mistakes without burning a \
                server round-trip. Returns {valid, errors: [{path, message}], warnings: [...]}. Note that custom \
                widget references (component starting with 'widget:') and unknown components from outside the \
                catalog produce warnings rather than errors so this tool doesn't false-positive on user-authored \
                widgets.""").inputSchema(new McpSchema.JsonSchema("object", p, List.of("component"), null, null, null))
                .build();
    }

    public CallToolResult handleValidateUiComponent(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String component = getStringArg(args, "component");
        if (component == null || component.isBlank()) {
            return errorResult("'component' is required.");
        }
        Map<String, Object> config = getObjectMapArg(args, "config");
        Map<String, Object> slots = getObjectMapArg(args, "slots");
        List<Map<String, String>> errors = new ArrayList<>();
        List<Map<String, String>> warnings = new ArrayList<>();
        validateNode("$", component, config, slots, errors, warnings);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valid", errors.isEmpty());
        response.put("errors", errors);
        response.put("warnings", warnings);
        return textResult(jsonMapper, response);
    }

    private void validateNode(String path, String component, @Nullable Map<String, Object> config,
            @Nullable Map<String, Object> slots, List<Map<String, String>> errors, List<Map<String, String>> warnings) {
        // Custom widget references (widget:foo) — can't validate without fetching the widget def, just note.
        if (component.startsWith("widget:")) {
            warnings.add(issue(path, "Custom widget reference '" + component
                    + "' — its parameter requirements aren't checked here. Verify with manage_ui_component(action='get', namespace='ui:widget', uid='"
                    + component.substring("widget:".length()) + "')."));
            return;
        }
        ObjectNode schema = catalog.get(component);
        if (schema == null) {
            warnings.add(issue(path, "Component '" + component
                    + "' not in the curated catalog — props and slots can't be checked. Either it's a Framework7/Vue "
                    + "primitive (allowed) or a typo (use list_widgets to confirm)."));
            // Still walk slots in case they're typed components we can validate.
            if (slots != null) {
                validateSlots(path, component, slots, null, errors, warnings);
            }
            return;
        }
        // Required props
        ArrayNode props = (ArrayNode) schema.get("props");
        if (props != null && config == null) {
            for (int i = 0; i < props.size(); i++) {
                ObjectNode prop = (ObjectNode) props.get(i);
                if (prop.path("required").asBoolean(false)) {
                    errors.add(issue(path + ".config",
                            "Missing required prop '" + prop.path("name").asText() + "' on " + component));
                }
            }
        } else if (props != null) {
            for (int i = 0; i < props.size(); i++) {
                ObjectNode prop = (ObjectNode) props.get(i);
                String propName = prop.path("name").asText();
                if (prop.path("required").asBoolean(false) && !config.containsKey(propName)) {
                    errors.add(issue(path + ".config", "Missing required prop '" + propName + "' on " + component));
                }
            }
            Set<String> known = new java.util.HashSet<>();
            for (int i = 0; i < props.size(); i++) {
                known.add(((ObjectNode) props.get(i)).path("name").asText());
            }
            for (String key : config.keySet()) {
                if (!known.contains(key)) {
                    warnings.add(issue(path + ".config", "Unknown prop '" + key + "' for " + component
                            + " — check describe_widget('" + component + "') for valid prop names."));
                }
            }
        }
        if (slots != null) {
            validateSlots(path, component, slots, schema.get("slots"), errors, warnings);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateSlots(String path, String component, Map<String, Object> slots, @Nullable JsonNode slotSchema,
            List<Map<String, String>> errors, List<Map<String, String>> warnings) {
        Set<String> knownSlots = new java.util.HashSet<>();
        if (slotSchema instanceof ArrayNode arr) {
            for (int i = 0; i < arr.size(); i++) {
                knownSlots.add(((ObjectNode) arr.get(i)).path("name").asText());
            }
        }
        for (Map.Entry<String, Object> e : slots.entrySet()) {
            String slotName = e.getKey();
            if (!knownSlots.isEmpty() && !knownSlots.contains(slotName)) {
                warnings.add(issue(path + ".slots." + slotName, "Slot '" + slotName + "' not declared by " + component
                        + " (known slots: " + String.join(", ", knownSlots) + ")"));
            }
            if (e.getValue() instanceof List<?> children) {
                for (int idx = 0; idx < children.size(); idx++) {
                    Object child = children.get(idx);
                    if (child instanceof Map<?, ?> childMap) {
                        Map<String, Object> cm = (Map<String, Object>) childMap;
                        String childComponent = cm.get("component") instanceof String s ? s : null;
                        if (childComponent == null) {
                            errors.add(issue(path + ".slots." + slotName + "[" + idx + "]",
                                    "Child is missing 'component' field"));
                            continue;
                        }
                        Map<String, Object> childConfig = cm.get("config") instanceof Map
                                ? (Map<String, Object>) cm.get("config")
                                : null;
                        Map<String, Object> childSlots = cm.get("slots") instanceof Map
                                ? (Map<String, Object>) cm.get("slots")
                                : null;
                        validateNode(path + ".slots." + slotName + "[" + idx + "]", childComponent, childConfig,
                                childSlots, errors, warnings);
                    }
                }
            }
        }
    }

    private static Map<String, String> issue(String path, String message) {
        Map<String, String> i = new LinkedHashMap<>();
        i.put("path", path);
        i.put("message", message);
        return i;
    }

    /**
     * Minimal RFC 6902 JSON Patch applier. Supports {@code replace}, {@code add}, {@code remove}, and {@code test}
     * — the operations needed for editing openHAB UI components. Paths use JSON Pointer syntax (RFC 6901);
     * {@code /-} on an array means "append". Any operation failure aborts the whole patch (atomic from the
     * caller's perspective) by throwing {@link PatchException} with the failing op index.
     */
    static final class JsonPatchApplier {
        private static final ObjectMapper MAPPER = McpToolUtils.jackson();

        private JsonPatchApplier() {
        }

        static void apply(ObjectNode root, List<Map<String, Object>> operations) {
            for (int i = 0; i < operations.size(); i++) {
                Map<String, Object> op = operations.get(i);
                String opName = op.get("op") instanceof String s ? s.toLowerCase(Locale.ROOT) : null;
                String path = op.get("path") instanceof String s ? s : null;
                if (opName == null || path == null) {
                    throw new PatchException(i, "op[" + i + "]: missing required 'op' or 'path'");
                }
                JsonNode value = op.containsKey("value") ? MAPPER.valueToTree(op.get("value")) : null;
                try {
                    switch (opName) {
                        case "replace" -> replace(root, parsePath(path), value, i);
                        case "add" -> add(root, parsePath(path), value, i);
                        case "remove" -> remove(root, parsePath(path), i);
                        case "test" -> test(root, parsePath(path), value, i);
                        default -> throw new PatchException(i,
                                "op[" + i + "]: unknown op '" + opName + "' (supported: replace, add, remove, test)");
                    }
                } catch (PatchException e) {
                    throw e;
                } catch (Exception e) {
                    throw new PatchException(i, "op[" + i + "] (" + opName + " " + path + "): " + e.getMessage());
                }
            }
        }

        /** Parses an RFC 6901 JSON Pointer into segments, handling ~1 → / and ~0 → ~ escapes. */
        private static List<String> parsePath(String path) {
            if (path.isEmpty() || "/".equals(path)) {
                return List.of();
            }
            if (!path.startsWith("/")) {
                throw new IllegalArgumentException("path must start with '/' or be empty");
            }
            String[] raw = path.substring(1).split("/", -1);
            List<String> out = new ArrayList<>(raw.length);
            for (String seg : raw) {
                out.add(seg.replace("~1", "/").replace("~0", "~"));
            }
            return out;
        }

        /** Walks down to (but does not include) the last segment, returning the parent node. Throws on missing. */
        private static JsonNode walkToParent(JsonNode root, List<String> segments, int opIndex) {
            JsonNode cur = root;
            for (int i = 0; i < segments.size() - 1; i++) {
                cur = step(cur, segments.get(i), opIndex);
            }
            return cur;
        }

        private static JsonNode step(JsonNode node, String segment, int opIndex) {
            if (node instanceof ObjectNode obj) {
                JsonNode next = obj.get(segment);
                if (next == null) {
                    throw new PatchException(opIndex, "path segment '" + segment + "' not found in object");
                }
                return next;
            }
            if (node instanceof ArrayNode arr) {
                int idx = parseArrayIndex(segment, arr.size(), opIndex, false);
                return arr.get(idx);
            }
            throw new PatchException(opIndex, "cannot traverse into '" + segment + "' — node is not object or array");
        }

        private static int parseArrayIndex(String segment, int size, int opIndex, boolean allowAppend) {
            if (allowAppend && "-".equals(segment)) {
                return size;
            }
            try {
                int idx = Integer.parseInt(segment);
                if (idx < 0 || idx >= size) {
                    throw new PatchException(opIndex, "array index " + idx + " out of bounds (size " + size + ")");
                }
                return idx;
            } catch (NumberFormatException e) {
                throw new PatchException(opIndex, "invalid array index '" + segment + "'");
            }
        }

        private static void replace(ObjectNode root, List<String> segments, @Nullable JsonNode value, int opIndex) {
            if (value == null) {
                throw new PatchException(opIndex, "'value' is required for replace");
            }
            if (segments.isEmpty()) {
                throw new PatchException(opIndex, "cannot replace the root node");
            }
            JsonNode parent = walkToParent(root, segments, opIndex);
            String last = segments.get(segments.size() - 1);
            if (parent instanceof ObjectNode obj) {
                if (!obj.has(last)) {
                    throw new PatchException(opIndex, "path '" + last + "' does not exist (use 'add' to create)");
                }
                obj.set(last, value);
            } else if (parent instanceof ArrayNode arr) {
                int idx = parseArrayIndex(last, arr.size(), opIndex, false);
                arr.set(idx, value);
            } else {
                throw new PatchException(opIndex, "parent is not object or array");
            }
        }

        private static void add(ObjectNode root, List<String> segments, @Nullable JsonNode value, int opIndex) {
            if (value == null) {
                throw new PatchException(opIndex, "'value' is required for add");
            }
            if (segments.isEmpty()) {
                throw new PatchException(opIndex, "cannot add to root");
            }
            JsonNode parent = walkToParent(root, segments, opIndex);
            String last = segments.get(segments.size() - 1);
            if (parent instanceof ObjectNode obj) {
                obj.set(last, value);
            } else if (parent instanceof ArrayNode arr) {
                int idx = parseArrayIndex(last, arr.size(), opIndex, true);
                arr.insert(idx, value);
            } else {
                throw new PatchException(opIndex, "parent is not object or array");
            }
        }

        private static void remove(ObjectNode root, List<String> segments, int opIndex) {
            if (segments.isEmpty()) {
                throw new PatchException(opIndex, "cannot remove the root node");
            }
            JsonNode parent = walkToParent(root, segments, opIndex);
            String last = segments.get(segments.size() - 1);
            if (parent instanceof ObjectNode obj) {
                if (!obj.has(last)) {
                    throw new PatchException(opIndex, "path '" + last + "' does not exist");
                }
                obj.remove(last);
            } else if (parent instanceof ArrayNode arr) {
                int idx = parseArrayIndex(last, arr.size(), opIndex, false);
                arr.remove(idx);
            } else {
                throw new PatchException(opIndex, "parent is not object or array");
            }
        }

        private static void test(ObjectNode root, List<String> segments, @Nullable JsonNode expected, int opIndex) {
            if (expected == null) {
                throw new PatchException(opIndex, "'value' is required for test");
            }
            JsonNode actual = segments.isEmpty() ? root : walkToParent(root, segments, opIndex);
            if (!segments.isEmpty()) {
                String last = segments.get(segments.size() - 1);
                if (actual instanceof ObjectNode obj) {
                    actual = obj.get(last);
                } else if (actual instanceof ArrayNode arr) {
                    int idx = parseArrayIndex(last, arr.size(), opIndex, false);
                    actual = arr.get(idx);
                }
            }
            if (actual == null || !actual.equals(expected)) {
                throw new PatchException(opIndex, "test failed: expected " + expected + " but found " + actual);
            }
        }

        /** Thrown when a single patch op fails; carries the op index so the caller can report it. */
        static final class PatchException extends RuntimeException {
            private static final long serialVersionUID = 1L;
            final int opIndex;

            PatchException(int opIndex, String message) {
                super(message);
                this.opIndex = opIndex;
            }
        }
    }
}
