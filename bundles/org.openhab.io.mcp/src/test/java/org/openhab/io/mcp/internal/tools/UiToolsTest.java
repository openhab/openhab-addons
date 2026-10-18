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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.io.mcp.internal.McpTestHelper.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link UiTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class UiToolsTest {

    private static final String TOKEN = "test-token";
    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    @Nullable
    HttpClient httpClient;

    @Mock
    @Nullable
    Request request;

    @Mock
    @Nullable
    ContentResponse response;

    @Mock
    @Nullable
    McpSyncServerExchange exchange;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();
    private final Function<String, @Nullable String> tokenSupplier = sid -> TOKEN;

    /**
     * Constructs a fresh UiTools and clears mock invocations recorded by the constructor's
     * live-catalog fetch attempt — otherwise verify() calls in tests see the bonus construction-time
     * interactions and fail with "Wanted 1 time, but was 2 times". Construction's fetch always
     * returns 0 (unstubbed mock response status) so it falls through to the bundled catalog.
     * The live fetch runs on a background thread, so wait for it to finish before clearing invocations
     * to avoid a race between the constructor's mock calls and the test's clearInvocations().
     */
    private UiTools tools() {
        UiTools t = new UiTools(requireNonNull(httpClient), BASE_URL, tokenSupplier, jsonMapper);
        awaitLiveLoad(t);
        clearInvocations(requireNonNull(httpClient), requireNonNull(request));
        return t;
    }

    private static void awaitLiveLoad(UiTools t) {
        try {
            t.liveCatalogLoadFuture().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError("live-catalog load future failed", e);
        }
    }

    private static <T> T requireNonNull(@Nullable T value) {
        assertNotNull(value);
        return value;
    }

    @BeforeEach
    void setUp() throws Exception {
        Request r = requireNonNull(request);
        lenient().when(r.method(any(HttpMethod.class))).thenReturn(r);
        lenient().when(r.header(anyString(), anyString())).thenReturn(r);
        lenient().when(r.content(any(), anyString())).thenReturn(r);
        lenient().when(r.timeout(anyLong(), any(TimeUnit.class))).thenReturn(r);
        lenient().when(r.send()).thenReturn(requireNonNull(response));
        lenient().when(httpClient.newRequest(any(URI.class))).thenReturn(r);
        lenient().when(exchange.sessionId()).thenReturn("session-1");
    }

    // ============ catalog loading ============

    @Test
    @SuppressWarnings("unchecked")
    void catalogLoadsAndExposesEntries() throws Exception {
        CallToolResult result = tools().handleListWidgets(createRequest(Map.of()));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        Object widgets = parsed.get("widgets");
        assertNotNull(widgets);
        List<Map<String, Object>> list = (List<Map<String, Object>>) widgets;
        // 71 entries shipped at time of writing; assert reasonable lower bound to avoid brittleness.
        assertTrue(list.size() >= 60, "Catalog has fewer entries than expected: " + list.size());
    }

    @Test
    void catalogFallsBackToBundledWhenLiveFetchFails() throws Exception {
        // Default @BeforeEach setup leaves response.getStatus() unstubbed (returns 0), so the live fetch
        // doesn't return 200 — UiTools falls back to the bundled resource.
        CallToolResult result = tools().handleListWidgets(createRequest(Map.of()));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals("bundled-fallback", parsed.get("catalogSource"));
    }

    @Test
    void catalogUsesLiveWhenFetchSucceeds() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(
                "{\"version\":\"99-test\",\"widgets\":[{\"name\":\"oh-fake-widget\",\"label\":\"Fake\",\"category\":\"system\",\"description\":\"\",\"props\":[],\"slots\":[]}]}");
        // Don't call tools() helper here — we need to verify the construction-time fetch and inspect catalog state.
        UiTools t = new UiTools(requireNonNull(httpClient), BASE_URL, tokenSupplier, jsonMapper);
        // Live fetch is now async — wait for it before asserting the catalog has been replaced.
        awaitLiveLoad(t);
        CallToolResult result = t.handleListWidgets(createRequest(Map.of()));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals("live", parsed.get("catalogSource"));
        assertEquals("99-test", parsed.get("catalogVersion"));
        // Catalog should reflect the live payload (just the one fake widget).
        assertEquals(1, parsed.get("total"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void listWidgetsFiltersByCategory() throws Exception {
        CallToolResult result = tools().handleListWidgets(createRequest(Map.of("category", "page-type")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> list = (List<Map<String, Object>>) parsed.get("widgets");
        assertNotNull(list);
        assertTrue(list.size() >= 4, "expected at least a few page-type widgets, got " + list.size());
        for (Map<String, Object> entry : list) {
            assertEquals("page-type", entry.get("category"));
        }
    }

    // ============ describe_widget ============

    @Test
    @SuppressWarnings("unchecked")
    void describeWidgetReturnsSchema() throws Exception {
        CallToolResult result = tools().handleDescribeWidget(createRequest(Map.of("name", "oh-toggle-card")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals("oh-toggle-card", parsed.get("name"));
        assertEquals("standard-card", parsed.get("category"));
        List<Map<String, Object>> props = (List<Map<String, Object>>) parsed.get("props");
        assertNotNull(props);
        // oh-toggle-card requires an 'item' prop.
        boolean hasItem = props.stream().anyMatch(p -> "item".equals(p.get("name")));
        assertTrue(hasItem, "oh-toggle-card schema is missing 'item' prop");
    }

    @Test
    void describeWidgetUnknownReturnsError() throws Exception {
        CallToolResult result = tools().handleDescribeWidget(createRequest(Map.of("name", "oh-no-such-widget")));
        assertErrorContains(result, "not in catalog");
    }

    @Test
    void describeWidgetMissingName() throws Exception {
        CallToolResult result = tools().handleDescribeWidget(createRequest(Map.of()));
        assertErrorContains(result, "required");
    }

    // ============ get_page_skeleton ============

    @Test
    void getPageSkeletonLayoutHasCorrectShape() throws Exception {
        CallToolResult result = tools()
                .handleGetPageSkeleton(createRequest(Map.of("pageType", "layout", "uid", "my-test", "label", "Test")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(BASE_URL + "/page/my-test", parsed.get("plannedViewUrl"));
        assertEquals(BASE_URL + "/settings/pages/layout/my-test", parsed.get("plannedEditUrl"));
        @SuppressWarnings("unchecked")
        Map<String, Object> skeleton = (Map<String, Object>) parsed.get("skeleton");
        assertEquals("oh-layout-page", skeleton.get("component"));
        assertEquals("my-test", skeleton.get("uid"));
    }

    @Test
    void getPageSkeletonChartHasChartSlots() throws Exception {
        CallToolResult result = tools().handleGetPageSkeleton(createRequest(Map.of("pageType", "chart")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> skeleton = (Map<String, Object>) parsed.get("skeleton");
        assertEquals("oh-chart-page", skeleton.get("component"));
        @SuppressWarnings("unchecked")
        Map<String, Object> slots = (Map<String, Object>) skeleton.get("slots");
        assertTrue(slots.containsKey("series"), "Chart skeleton must declare 'series' slot");
    }

    @Test
    void getPageSkeletonInvalidPageType() throws Exception {
        CallToolResult result = tools().handleGetPageSkeleton(createRequest(Map.of("pageType", "bogus")));
        assertErrorContains(result, "must be one of");
    }

    // ============ manage_ui_component CRUD ============

    @Test
    void manageUiComponentMissingActionFails() throws Exception {
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange),
                createRequest(Map.of("namespace", "ui:page")));
        assertErrorContains(result, "action");
    }

    @Test
    void manageUiComponentRejectsForeignNamespace() throws Exception {
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange),
                createRequest(Map.of("action", "get", "namespace", "ui:icon")));
        assertErrorContains(result, "namespace");
    }

    @Test
    void manageUiComponentNoToken() throws Exception {
        UiTools t = new UiTools(requireNonNull(httpClient), BASE_URL, sid -> null, jsonMapper);
        CallToolResult result = t.handleManageUiComponent(requireNonNull(exchange),
                createRequest(Map.of("action", "get", "namespace", "ui:page")));
        assertErrorContains(result, "bearer token");
    }

    @Test
    void manageUiComponentGetListSucceeds() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("[{\"uid\":\"overview\",\"component\":\"oh-layout-page\"}]");
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange),
                createRequest(Map.of("action", "get", "namespace", "ui:page")));
        assertSuccess(result);
        verify(httpClient).newRequest(URI.create(BASE_URL + "/rest/ui/components/ui%3Apage"));
        verify(request).method(HttpMethod.GET);
        verify(request).header("Authorization", "Bearer " + TOKEN);
    }

    @Test
    void manageUiComponentGetSingleAddsUrlHints() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString())
                .thenReturn("{\"uid\":\"office\",\"component\":\"oh-layout-page\",\"config\":{}}");
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange),
                createRequest(Map.of("action", "get", "namespace", "ui:page", "uid", "office")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(BASE_URL + "/page/office", parsed.get("viewUrl"));
        assertEquals(BASE_URL + "/settings/pages/layout/office", parsed.get("editUrl"));
    }

    @Test
    void manageUiComponentCreateForwardsPayload() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString())
                .thenReturn("{\"uid\":\"new-page\",\"component\":\"oh-layout-page\",\"config\":{}}");
        Map<String, Object> args = new HashMap<>();
        args.put("action", "create");
        args.put("namespace", "ui:page");
        args.put("uid", "new-page");
        args.put("component", "oh-layout-page");
        args.put("config", Map.of("label", "New"));
        args.put("slots", Map.of("default", List.of()));

        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("create", parsed.get("action"));
        assertEquals(BASE_URL + "/page/new-page", parsed.get("viewUrl"));
        verify(request).method(HttpMethod.POST);
    }

    @Test
    void manageUiComponentCreate403ReportsAdminRequired() throws Exception {
        when(response.getStatus()).thenReturn(403);
        Map<String, Object> args = Map.of("action", "create", "namespace", "ui:page", "component", "oh-layout-page");
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertErrorContains(result, "ADMIN");
    }

    @Test
    void manageUiComponentUpdateRequiresUid() throws Exception {
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange),
                createRequest(Map.of("action", "update", "namespace", "ui:page", "component", "oh-layout-page")));
        assertErrorContains(result, "uid");
    }

    @Test
    void manageUiComponentDeleteSucceeds() throws Exception {
        when(response.getStatus()).thenReturn(200);
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange),
                createRequest(Map.of("action", "delete", "namespace", "ui:page", "uid", "old")));
        assertSuccess(result);
        verify(request).method(HttpMethod.DELETE);
    }

    @Test
    void manageUiComponentWidgetCreateGetsEditUrl() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"uid\":\"my-widget\",\"component\":\"oh-card\"}");
        Map<String, Object> args = Map.of("action", "create", "namespace", "ui:widget", "uid", "my-widget", "component",
                "oh-card");
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(BASE_URL + "/developer/widgets/my-widget", parsed.get("editUrl"));
        // Widgets have no viewUrl since they're not standalone pages.
        assertNull(parsed.get("viewUrl"));
    }

    // ============ validate_ui_component ============

    @Test
    @SuppressWarnings("unchecked")
    void validateUiComponentMissingRequiredProp() throws Exception {
        // oh-chart-page requires 'chartType' — leave it out.
        Map<String, Object> args = Map.of("component", "oh-chart-page", "config", Map.of("label", "Foo"));
        CallToolResult result = tools().handleValidateUiComponent(createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(false, parsed.get("valid"));
        List<Map<String, String>> errors = (List<Map<String, String>>) parsed.get("errors");
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        boolean hasChartTypeError = errors.stream().anyMatch(e -> e.get("message").contains("chartType"));
        assertTrue(hasChartTypeError, "expected error mentioning missing 'chartType' prop, got: " + errors);
    }

    @Test
    @SuppressWarnings("unchecked")
    void validateUiComponentUnknownComponentIsWarning() throws Exception {
        Map<String, Object> args = Map.of("component", "oh-no-such-widget");
        CallToolResult result = tools().handleValidateUiComponent(createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        // Unknown components are warnings, not errors, so the result is still valid.
        assertEquals(true, parsed.get("valid"));
        List<Map<String, String>> warnings = (List<Map<String, String>>) parsed.get("warnings");
        assertNotNull(warnings);
        assertFalse(warnings.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void validateUiComponentCustomWidgetReferenceIsWarning() throws Exception {
        Map<String, Object> args = Map.of("component", "widget:my-custom-thing");
        CallToolResult result = tools().handleValidateUiComponent(createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("valid"));
        List<Map<String, String>> warnings = (List<Map<String, String>>) parsed.get("warnings");
        assertNotNull(warnings);
        boolean mentionsCustom = warnings.stream().anyMatch(w -> w.get("message").contains("Custom widget reference"));
        assertTrue(mentionsCustom);
    }

    @Test
    @SuppressWarnings("unchecked")
    void validateUiComponentValidatesNestedSlots() throws Exception {
        // Page contains a chart-page nested as a child (contrived, but uses a known required-prop widget)
        // without the required 'chartType' — should bubble up as a slot-path error.
        Map<String, Object> child = Map.of("component", "oh-chart-page", "config", Map.of("label", "x"));
        Map<String, Object> slots = Map.of("default", List.of(child));
        Map<String, Object> args = Map.of("component", "oh-layout-page", "config", Map.of("label", "Test"), "slots",
                slots);
        CallToolResult result = tools().handleValidateUiComponent(createRequest(args));
        Map<String, Object> parsed = parseResult(result);
        assertEquals(false, parsed.get("valid"));
        List<Map<String, String>> errors = (List<Map<String, String>>) parsed.get("errors");
        boolean hasNestedError = errors.stream().anyMatch(e -> e.get("path").contains("slots.default[0]"));
        assertTrue(hasNestedError, "expected nested-path error, got: " + errors);
    }

    @Test
    void validateUiComponentMissingComponentName() throws Exception {
        CallToolResult result = tools().handleValidateUiComponent(createRequest(Map.of()));
        assertErrorContains(result, "required");
    }

    // ============ responseDetail (minimal write responses) ============

    @Test
    void createWriteResponseIsMinimalByDefault() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"uid\":\"p1\",\"component\":\"oh-layout-page\","
                + "\"config\":{\"label\":\"A\"},\"slots\":{\"default\":[]}}");
        Map<String, Object> args = Map.of("action", "create", "namespace", "ui:page", "uid", "p1", "component",
                "oh-layout-page");
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("p1", parsed.get("uid"));
        assertNotNull(parsed.get("viewUrl"));
        // Default behavior strips the echoed component body — the whole point of this feature.
        assertNull(parsed.get("component"), "minimal response must not include echoed 'component' body");
    }

    @Test
    void createWriteResponseFullEchoesComponent() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString())
                .thenReturn("{\"uid\":\"p1\",\"component\":\"oh-layout-page\",\"config\":{\"label\":\"A\"}}");
        Map<String, Object> args = Map.of("action", "create", "namespace", "ui:page", "uid", "p1", "component",
                "oh-layout-page", "responseDetail", "full");
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertNotNull(parsed.get("component"));
    }

    // ============ action='patch' ============

    @Test
    void patchReplaceTopLevelConfigField() throws Exception {
        // Two-call sequence: GET returns the current widget, PUT returns the (server-stored) result.
        String current = "{\"uid\":\"w1\",\"component\":\"oh-card\",\"config\":{\"title\":\"Old\"},\"slots\":{}}";
        String afterPut = "{\"uid\":\"w1\",\"component\":\"oh-card\",\"config\":{\"title\":\"New\"},\"slots\":{}}";
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(current, afterPut);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "patch");
        args.put("namespace", "ui:widget");
        args.put("uid", "w1");
        args.put("operations", List.of(Map.of("op", "replace", "path", "/config/title", "value", "New")));

        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("patch", parsed.get("action"));
        verify(request).method(HttpMethod.GET);
        verify(request).method(HttpMethod.PUT);
    }

    @Test
    void patchAddAppendsToArrayViaDashSuffix() throws Exception {
        String current = "{\"uid\":\"p1\",\"component\":\"oh-layout-page\",\"config\":{},\"slots\":{\"default\":[]}}";
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(current, current);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "patch");
        args.put("namespace", "ui:page");
        args.put("uid", "p1");
        args.put("operations", List.of(Map.of("op", "add", "path", "/slots/default/-", "value",
                Map.of("component", "oh-toggle-card", "config", Map.of("item", "X")))));

        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
    }

    @Test
    void patchRemoveDeletesPath() throws Exception {
        String current = "{\"uid\":\"p1\",\"component\":\"oh-layout-page\","
                + "\"config\":{\"label\":\"X\"},\"slots\":{\"default\":[{\"component\":\"oh-toggle-card\"}]}}";
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(current, current);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "patch");
        args.put("namespace", "ui:page");
        args.put("uid", "p1");
        args.put("operations", List.of(Map.of("op", "remove", "path", "/slots/default/0")));

        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
    }

    @Test
    void patchTestSucceedsWhenValueMatches() throws Exception {
        String current = "{\"uid\":\"p1\",\"component\":\"oh-layout-page\",\"config\":{\"label\":\"Office\"}}";
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(current, current);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "patch");
        args.put("namespace", "ui:page");
        args.put("uid", "p1");
        args.put("operations", List.of(Map.of("op", "test", "path", "/component", "value", "oh-layout-page"),
                Map.of("op", "replace", "path", "/config/label", "value", "Renamed")));

        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
    }

    @Test
    void patchTestFailureAbortsWholePatch() throws Exception {
        String current = "{\"uid\":\"p1\",\"component\":\"oh-layout-page\",\"config\":{\"label\":\"X\"}}";
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(current);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "patch");
        args.put("namespace", "ui:page");
        args.put("uid", "p1");
        args.put("operations", List.of(Map.of("op", "test", "path", "/component", "value", "oh-chart-page"),
                Map.of("op", "replace", "path", "/config/label", "value", "Renamed")));

        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        // Test op failed — server returns a structured success:false response (NOT an HTTP error).
        Map<String, Object> parsed = parseResult(result);
        assertEquals(false, parsed.get("success"));
        assertEquals(0, parsed.get("failedOp"));
        // The PUT must NOT have been issued since the patch aborted before it.
        verify(request, never()).method(HttpMethod.PUT);
    }

    @Test
    void patchInvalidPathReturnsStructuredError() throws Exception {
        String current = "{\"uid\":\"p1\",\"component\":\"oh-layout-page\",\"config\":{}}";
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(current);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "patch");
        args.put("namespace", "ui:page");
        args.put("uid", "p1");
        args.put("operations", List.of(Map.of("op", "replace", "path", "/nonexistent/field", "value", "X")));

        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        Map<String, Object> parsed = parseResult(result);
        assertEquals(false, parsed.get("success"));
        assertNotNull(parsed.get("error"));
    }

    @Test
    void patchMissingOperationsIsRejected() throws Exception {
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange),
                createRequest(Map.of("action", "patch", "namespace", "ui:widget", "uid", "w1")));
        assertErrorContains(result, "operations");
    }

    @Test
    void patchMissingUidIsRejected() throws Exception {
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange),
                createRequest(Map.of("action", "patch", "namespace", "ui:widget", "operations",
                        List.of(Map.of("op", "replace", "path", "/config/title", "value", "X")))));
        assertErrorContains(result, "uid");
    }

    // ============ get with fields projection ============

    @Test
    @SuppressWarnings("unchecked")
    void getWithFieldsProjectionReturnsOnlyRequestedKeys() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString())
                .thenReturn("{\"uid\":\"p1\",\"component\":\"oh-layout-page\",\"config\":{\"label\":\"X\"},"
                        + "\"slots\":{\"default\":[]},\"tags\":[\"a\",\"b\"]}");
        Map<String, Object> args = Map.of("action", "get", "namespace", "ui:page", "uid", "p1", "fields",
                List.of("uid", "component"));
        CallToolResult result = tools().handleManageUiComponent(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        Map<String, Object> component = (Map<String, Object>) parsed.get("component");
        assertNotNull(component);
        assertEquals("p1", component.get("uid"));
        assertEquals("oh-layout-page", component.get("component"));
        assertNull(component.get("slots"), "projected response must not include unrequested 'slots'");
        assertNull(component.get("config"), "projected response must not include unrequested 'config'");
        assertNull(component.get("tags"), "projected response must not include unrequested 'tags'");
    }
}
