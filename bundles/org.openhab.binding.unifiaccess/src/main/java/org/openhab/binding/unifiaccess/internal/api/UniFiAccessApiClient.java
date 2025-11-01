/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiaccess.internal.api;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.unifiaccess.internal.dto.AccessPolicy;
import org.openhab.binding.unifiaccess.internal.dto.AccessPolicyHolidayGroup;
import org.openhab.binding.unifiaccess.internal.dto.AccessPolicySchedule;
import org.openhab.binding.unifiaccess.internal.dto.ApiResponse;
import org.openhab.binding.unifiaccess.internal.dto.Device;
import org.openhab.binding.unifiaccess.internal.dto.DeviceAccessMethodSettings;
import org.openhab.binding.unifiaccess.internal.dto.Door;
import org.openhab.binding.unifiaccess.internal.dto.DoorEmergencySettings;
import org.openhab.binding.unifiaccess.internal.dto.DoorLockRule;
import org.openhab.binding.unifiaccess.internal.dto.DoorUnlockRequest;
import org.openhab.binding.unifiaccess.internal.dto.Image;
import org.openhab.binding.unifiaccess.internal.dto.NfcEnrollSession;
import org.openhab.binding.unifiaccess.internal.dto.NfcEnrollStatus;
import org.openhab.binding.unifiaccess.internal.dto.Notification;
import org.openhab.binding.unifiaccess.internal.dto.UniFiAccessApiException;
import org.openhab.binding.unifiaccess.internal.dto.User;
import org.openhab.binding.unifiaccess.internal.dto.Visitor;
import org.openhab.binding.unifiaccess.internal.dto.WebhookEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * UniFi Access API client
 *
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class UniFiAccessApiClient implements Closeable {

    private Logger logger = LoggerFactory.getLogger(UniFiAccessApiClient.class);

    private static final String STATIC_BASE = "//api/v1/developer/system/static"; // double slashes intentional
    private final HttpClient httpClient;
    private final URI base;
    private final Gson gson;
    private final Map<String, String> defaultHeaders;
    private final WebSocketClient wsClient;
    private @Nullable Session wsSession;
    private long lastHeartbeatEpochMs;
    private @Nullable ScheduledFuture<?> wsMonitorFuture;
    private boolean closed = false;
    private final ScheduledExecutorService executorService;

    public UniFiAccessApiClient(HttpClient httpClient, URI base, Gson gson, String token,
            ScheduledExecutorService executorService) {
        this.httpClient = httpClient;
        this.base = ensureTrailingSlash(base);
        this.gson = gson;
        this.defaultHeaders = Map.of("Authorization", "Bearer " + token, "Accept", "application/json");
        this.wsClient = new WebSocketClient(httpClient);
        this.wsClient.unmanage(this.httpClient);
        try {
            wsClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start Jetty ws client", e);
        }
        this.executorService = executorService;
    }

    @Override
    public synchronized void close() {
        closed = true;
        try {
            Session s = wsSession;
            if (s != null) {
                try {
                    s.close();
                } finally {
                    wsSession = null;
                }
            }
        } catch (Exception e) {
            logger.debug("Error closing notifications WebSocket: {}", e.getMessage());
        }
        try {
            wsClient.stop();
        } catch (Exception e) {
            logger.debug("Error stopping WebSocket client: {}", e.getMessage());
        }
        stopWsMonitor();
    }

    public List<User> getUsers() throws UniFiAccessApiException {
        Type wrapped = TypeToken
                .getParameterized(ApiResponse.class, TypeToken.getParameterized(List.class, User.class).getType())
                .getType();
        Type raw = TypeToken.getParameterized(List.class, User.class).getType();
        ContentResponse resp = execGet("/users");
        ensure2xx(resp, "getUsers");
        return requireData(parseMaybeWrapped(resp.getContentAsString(), wrapped, raw, "getUsers"), "getUsers");
    }

    public User getUser(String userId) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, User.class).getType();
        ContentResponse resp = execGet("/users/" + userId);
        ensure2xx(resp, "getUser");
        ApiResponse<User> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "getUser");
    }

    public Visitor createVisitor(Visitor payload) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, Visitor.class).getType();
        ContentResponse resp = execPost("/visitors", payload);
        ensure2xx(resp, "createVisitor");
        ApiResponse<Visitor> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "createVisitor");
    }

    public Visitor updateVisitor(String visitorId, Visitor payload) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, Visitor.class).getType();
        ContentResponse resp = execPut("/visitors/" + visitorId, payload);
        ensure2xx(resp, "updateVisitor");
        ApiResponse<Visitor> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "updateVisitor");
    }

    public void deleteVisitor(String visitorId) throws UniFiAccessApiException {
        ContentResponse resp = execDelete("/visitors/" + visitorId);
        ensure2xx(resp, "deleteVisitor");
    }

    public List<AccessPolicy> getAccessPolicies() throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class,
                TypeToken.getParameterized(List.class, AccessPolicy.class).getType()).getType();
        Type raw = TypeToken.getParameterized(List.class, AccessPolicy.class).getType();
        ContentResponse resp = execGet("/access-policies");
        ensure2xx(resp, "getAccessPolicies");
        return requireData(parseMaybeWrapped(resp.getContentAsString(), wrapped, raw, "getAccessPolicies"),
                "getAccessPolicies");
    }

    public AccessPolicy createAccessPolicy(AccessPolicy policy) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, AccessPolicy.class).getType();
        ContentResponse resp = execPost("/access-policies", policy);
        ensure2xx(resp, "createAccessPolicy");
        ApiResponse<AccessPolicy> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "createAccessPolicy");
    }

    public AccessPolicy updateAccessPolicy(String policyId, AccessPolicy policy) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, AccessPolicy.class).getType();
        ContentResponse resp = execPut("/access-policies/" + policyId, policy);
        ensure2xx(resp, "updateAccessPolicy");
        ApiResponse<AccessPolicy> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "updateAccessPolicy");
    }

    public AccessPolicySchedule createSchedule(AccessPolicySchedule schedule) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, AccessPolicySchedule.class).getType();
        ContentResponse resp = execPost("/schedules", schedule);
        ensure2xx(resp, "createSchedule");
        ApiResponse<AccessPolicySchedule> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "createSchedule");
    }

    public AccessPolicySchedule updateSchedule(String scheduleId, AccessPolicySchedule schedule)
            throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, AccessPolicySchedule.class).getType();
        ContentResponse resp = execPut("/schedules/" + scheduleId, schedule);
        ensure2xx(resp, "updateSchedule");
        ApiResponse<AccessPolicySchedule> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "updateSchedule");
    }

    public AccessPolicyHolidayGroup createHolidayGroup(AccessPolicyHolidayGroup hg) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, AccessPolicyHolidayGroup.class).getType();
        ContentResponse resp = execPost("/holiday-groups", hg);
        ensure2xx(resp, "createHolidayGroup");
        ApiResponse<AccessPolicyHolidayGroup> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "createHolidayGroup");
    }

    public List<Device> getDevices() throws UniFiAccessApiException {
        Type wrapped = TypeToken
                .getParameterized(ApiResponse.class, TypeToken.getParameterized(List.class, Device.class).getType())
                .getType();
        Type raw = TypeToken.getParameterized(List.class, Device.class).getType();
        ContentResponse resp = execGet("/devices");
        ensure2xx(resp, "getDevices");
        String json = resp.getContentAsString();
        return parseListMaybeWrapped(json, wrapped, raw, "getDevices", "devices");
    }

    public DeviceAccessMethodSettings getDeviceAccessMethodSettings(String deviceId) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, DeviceAccessMethodSettings.class).getType();
        ContentResponse resp = execGet("/devices/" + deviceId + "/settings");
        ensure2xx(resp, "getDeviceAccessMethodSettings");
        ApiResponse<DeviceAccessMethodSettings> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "getDeviceAccessMethodSettings");
    }

    /**
     * Update Access Device's Access Method Settings.
     */
    public DeviceAccessMethodSettings updateDeviceAccessMethodSettings(String deviceId,
            DeviceAccessMethodSettings settings) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, DeviceAccessMethodSettings.class).getType();
        ContentResponse resp = execPut("/devices/" + deviceId + "/settings", settings);
        ensure2xx(resp, "updateDeviceAccessMethodSettings");
        ApiResponse<DeviceAccessMethodSettings> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "updateDeviceAccessMethodSettings");
    }

    public DoorEmergencySettings getDoorEmergencySettings(String doorId) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, DoorEmergencySettings.class).getType();
        ContentResponse resp = execGet("/doors/" + doorId + "/settings/emergency");
        ensure2xx(resp, "getDoorEmergencySettings");
        ApiResponse<DoorEmergencySettings> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "getDoorEmergencySettings");
    }

    public void setDoorEmergencySettings(String doorId, DoorEmergencySettings settings) throws UniFiAccessApiException {
        ContentResponse resp = execPut("/doors/" + doorId + "/settings/emergency", settings);
        ensure2xx(resp, "setDoorEmergencySettings");
    }

    /**
     * Starts a card enrollment session; returns session id/metadata.
     */
    public NfcEnrollSession createNfcEnrollSession() throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, NfcEnrollSession.class).getType();
        ContentResponse resp = execPost("/nfc/enroll/session", Map.of());
        ensure2xx(resp, "createNfcEnrollSession");
        ApiResponse<NfcEnrollSession> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "createNfcEnrollSession");
    }

    public NfcEnrollStatus getNfcEnrollStatus(String sessionId) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, NfcEnrollStatus.class).getType();
        ContentResponse resp = execGet("/nfc/enroll/session/" + sessionId);
        ensure2xx(resp, "getNfcEnrollStatus");
        ApiResponse<NfcEnrollStatus> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "getNfcEnrollStatus");
    }

    public void deleteNfcEnrollSession(String sessionId) throws UniFiAccessApiException {
        ContentResponse resp = execDelete("/nfc/enroll/session/" + sessionId);
        ensure2xx(resp, "deleteNfcEnrollSession");
    }

    public List<WebhookEndpoint> listWebhooks() throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class,
                TypeToken.getParameterized(List.class, WebhookEndpoint.class).getType()).getType();
        Type raw = TypeToken.getParameterized(List.class, WebhookEndpoint.class).getType();
        ContentResponse resp = execGet("/webhooks");
        ensure2xx(resp, "listWebhooks");
        return requireData(parseMaybeWrapped(resp.getContentAsString(), wrapped, raw, "listWebhooks"), "listWebhooks");
    }

    public WebhookEndpoint createWebhook(WebhookEndpoint endpoint) throws UniFiAccessApiException {
        Type wrapped = TypeToken.getParameterized(ApiResponse.class, WebhookEndpoint.class).getType();
        ContentResponse resp = execPost("/webhooks", endpoint);
        ensure2xx(resp, "createWebhook");
        ApiResponse<WebhookEndpoint> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "createWebhook");
    }

    public void deleteWebhook(String webhookId) throws UniFiAccessApiException {
        ContentResponse resp = execDelete("/webhooks/" + webhookId);
        ensure2xx(resp, "deleteWebhook");
    }

    public List<Door> getDoors() throws UniFiAccessApiException {
        var wrapped = com.google.gson.reflect.TypeToken
                .getParameterized(ApiResponse.class, TypeToken.getParameterized(List.class, Door.class).getType())
                .getType();
        var raw = TypeToken.getParameterized(List.class, Door.class).getType();
        var resp = execGet("/doors");
        ensure2xx(resp, "getDoors");
        String json = resp.getContentAsString();
        return parseListMaybeWrapped(json, wrapped, raw, "getDoors", "doors");
    }

    public Door getDoor(String doorId) throws UniFiAccessApiException {
        var wrapped = TypeToken.getParameterized(ApiResponse.class, Door.class).getType();
        var resp = execGet("/doors/" + doorId);
        ensure2xx(resp, "getDoor");
        ApiResponse<Door> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "getDoor");
    }

    /**
     * Remote unlock: optionally provide actor id/name and arbitrary passthrough
     * "extra".
     */
    public boolean unlockDoor(String doorId, DoorUnlockRequest body) throws UniFiAccessApiException {
        var resp = execPut("/doors/" + doorId + "/unlock", body);
        ensure2xx(resp, "unlockDoor");
        var wrapped = TypeToken.getParameterized(ApiResponse.class, Object.class).getType();
        ApiResponse<?> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        if (ar == null) {
            throw new UniFiAccessApiException("Missing or null response data for unlockDoor");
        }
        return ar.isSuccess();
    }

    public boolean unlockDoor(String doorId, @Nullable String actorId, @Nullable String actorName,
            @Nullable Map<String, Object> extra) throws UniFiAccessApiException {
        return unlockDoor(doorId, new DoorUnlockRequest(actorId, actorName, extra));
    }

    public boolean setDoorLockRule(String doorId, DoorLockRule rule) throws UniFiAccessApiException {
        var resp = execPut("/doors/" + doorId + "/lock_rule", rule);
        ensure2xx(resp, "setDoorLockRule");
        var wrapped = TypeToken.getParameterized(ApiResponse.class, Object.class).getType();
        ApiResponse<?> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        if (ar == null) {
            throw new UniFiAccessApiException("Missing or null response data for setDoorLockRule");
        }
        return ar.isSuccess();
    }

    public DoorLockRule getDoorLockRule(String doorId) throws UniFiAccessApiException {
        var resp = execGet("/doors/" + doorId + "/lock_rule");
        ensure2xx(resp, "getDoorLockRule");
        var wrapped = TypeToken.getParameterized(ApiResponse.class, DoorLockRule.class).getType();
        ApiResponse<DoorLockRule> ar = gson.fromJson(resp.getContentAsString(), wrapped);
        return requireData(ar == null ? null : ar.data, "getDoorLockRule");
    }

    public boolean keepDoorUnlocked(String doorId) throws UniFiAccessApiException {
        return setDoorLockRule(doorId, DoorLockRule.keepUnlock());
    }

    public boolean keepDoorLocked(String doorId) throws UniFiAccessApiException {
        return setDoorLockRule(doorId, DoorLockRule.keepLock());
    }

    public boolean unlockForMinutes(String doorId, int minutes) throws UniFiAccessApiException {
        if (minutes <= 0) {
            throw new IllegalArgumentException("minutes must be > 0");
        }
        return setDoorLockRule(doorId, DoorLockRule.customMinutes(minutes));
    }

    public boolean resetDoorLockRule(String doorId) throws UniFiAccessApiException {
        return setDoorLockRule(doorId, DoorLockRule.reset());
    }

    /** End an active keep-unlock/custom early (lock immediately). */
    public boolean lockEarly(String doorId) throws UniFiAccessApiException {
        return setDoorLockRule(doorId, DoorLockRule.lockEarly());
    }

    public Image getDoorThumbnail(String path) throws UniFiAccessApiException {
        var resp = execGet(STATIC_BASE + path);
        ensure2xx(resp, "getDoorThumbnail");
        Image image = new Image();
        image.mediaType = resp.getMediaType();
        image.data = resp.getContent();
        return image;
    }

    public synchronized void openNotifications(Runnable onOpen, Consumer<Notification> onMessage,
            Consumer<Throwable> onError, BiConsumer<Integer, String> onClosed) throws UniFiAccessApiException {
        Session session = wsSession;
        if (session != null && session.isOpen()) {
            return;
        }
        try {
            URI wsUri = toWebSocketUri("devices/notifications");
            logger.debug("Notifications WebSocket URI: {}", wsUri);
            ClientUpgradeRequest req = new ClientUpgradeRequest();
            defaultHeaders.forEach(req::setHeader);
            req.setHeader("Upgrade", "websocket");
            req.setHeader("Connection", "Upgrade");

            WebSocketAdapter socket = new WebSocketAdapter() {
                @Override
                @NonNullByDefault({})
                public void onWebSocketConnect(Session sess) {
                    super.onWebSocketConnect(sess);
                    logger.info("Notifications WebSocket connected: {}", wsUri);
                    wsSession = sess;
                    try {
                        onOpen.run();
                    } catch (Exception ignored) {
                    }
                    lastHeartbeatEpochMs = System.currentTimeMillis();
                }

                @Override
                @NonNullByDefault({})
                public void onWebSocketText(String message) {
                    try {
                        if (message != null && !message.isEmpty()) {
                            if (message.charAt(0) == '"') {
                                String normalized = message.trim();
                                lastHeartbeatEpochMs = System.currentTimeMillis();
                                logger.trace("Notifications heartbeat received: {}", normalized);
                                return;
                            } else {
                                Notification note = gson.fromJson(message, Notification.class);
                                if (note != null) {
                                    onMessage.accept(note);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("Notifications handler failed: {}", e.getMessage());
                        try {
                            if (!closed) {
                                onError.accept(e);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                @Override
                @NonNullByDefault({})
                public void onWebSocketError(Throwable cause) {
                    logger.debug("Notifications WebSocket error: {}", cause.getMessage(), cause);
                    try {
                        onError.accept(cause);
                    } catch (Exception ignored) {
                    }
                }

                @Override
                @NonNullByDefault({})
                public void onWebSocketClose(int statusCode, String reason) {
                    logger.debug("Notifications WebSocket closed: {} - {}", statusCode, reason);
                    try {
                        onClosed.accept(statusCode, reason);
                    } catch (Exception ignored) {
                    }
                }
            };

            wsClient.connect(socket, wsUri, req);
            startWsMonitor();
        } catch (IOException e) {
            throw new UniFiAccessApiException("WebSocket connect failed: " + e.getMessage(), e);
        }
    }

    private Request newRequest(HttpMethod method, String path, @Nullable Consumer<Request> customizer) {
        URI uri;
        if (path.startsWith("//")) {
            uri = base.resolve(path.substring(1));
        } else {
            uri = base.resolve(path.startsWith("/") ? path.substring(1) : path);
        }
        Request req = httpClient.newRequest(uri).method(method).header(HttpHeader.ACCEPT, "application/json");
        defaultHeaders.forEach(req::header);
        logger.debug("path: {} base: {} uri: {}", path, base, uri);
        req.getHeaders().forEach(header -> logger.debug("header {}: {}", header.getName(), header.getValue()));
        if (customizer != null) {
            customizer.accept(req);
        }
        return req;
    }

    private static URI ensureTrailingSlash(URI uri) {
        String s = uri.toString();
        return s.endsWith("/") ? uri : URI.create(s + "/");
    }

    private void ensure2xx(ContentResponse resp, String action) throws UniFiAccessApiException {
        if (logger.isTraceEnabled()) {
            String mediaType = resp.getMediaType();
            if (mediaType != null && (mediaType.equals("image/jpeg") || mediaType.equals("image/png"))) {
                logger.trace("ensure2xx status: {} mediaType: {} resp: image data", resp.getStatus(), mediaType);
            } else {
                logger.trace("ensure2xx status: {} mediaType: {} resp: {}", resp.getStatus(), mediaType,
                        resp.getContentAsString());
            }
        }
        int sc = resp.getStatus();
        if (sc < 200 || sc >= 300) {
            String msg = resp.getContentAsString();
            throw new UniFiAccessApiException("Non 2xx response for " + action + ": " + sc + " - " + msg);
        }
    }

    /**
     * Parse responses that might be either:
     * - Wrapped: {"code":0,"data":...,"msg":"ok"}
     * - Raw: {...} or [...]
     */
    private <T> T parseMaybeWrapped(String json, Type wrappedType, Type rawType, String action)
            throws UniFiAccessApiException {
        if (json.isBlank()) {
            throw new UniFiAccessApiException("Failed to parse response for " + action + ": null or blank JSON");
        }
        try {
            // Try wrapped first
            ApiResponse<T> wrapped = gson.fromJson(json, wrappedType);
            if (wrapped != null && wrapped.data != null) {
                return Objects.requireNonNull(wrapped.data);
            }
        } catch (Exception ignored) {
        }
        try {
            @Nullable
            T raw = gson.fromJson(json, rawType);
            if (raw == null) {
                throw new UniFiAccessApiException("Empty Data");
            }
            return raw;
        } catch (Exception e) {
            throw new UniFiAccessApiException("Failed to parse response for " + action + ": " + e.getMessage(), e);
        }
    }

    private String toJson(Object body) {
        return gson.toJson(body);
    }

    /**
     * Ensure parsed response data is non-null or throw a parse exception.
     */
    private <T> T requireData(@Nullable T data, String action) throws UniFiAccessApiException {
        if (data == null) {
            throw new UniFiAccessApiException("Missing or null response data for " + action);
        }
        return data;
    }

    /**
     * For endpoints that may return a list but may be wrapped or nested
     * inside an object:
     * - raw JSON array
     * - { "data": [ ... ] }
     * - { "data": { "list"|"items"|"records": [ ... ] } }
     * - { altArrayKeys[0]|altArrayKeys[1]|...: [ ... ] }
     */
    private <E> List<E> parseListMaybeWrapped(String json, Type wrappedListType, Type rawListType, String action,
            String... altArrayKeys) throws UniFiAccessApiException {
        if (json.isBlank()) {
            return Collections.emptyList();
        }

        // Try standard wrapped format
        try {
            ApiResponse<List<E>> wrapped = gson.fromJson(json, wrappedListType);
            if (wrapped != null && wrapped.data != null && !wrapped.data.isEmpty()) {
                return Objects.requireNonNull(wrapped.data);
            }
            // If code/msg present but empty data, still accept empty list
            if (wrapped != null && (wrapped.msg != null || wrapped.code != null) && wrapped.data == null) {
                return Collections.emptyList();
            }
        } catch (Exception ignored) {
        }

        // If it's already a raw array (possibly nested arrays)
        try {
            JsonElement root = JsonParser.parseString(json);
            if (root.isJsonArray()) {
                JsonElement flat = flattenArrayIfNeeded(root.getAsJsonArray());
                return nullToEmptyList(gson.fromJson(flat, rawListType));
            }

            if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();

                Function<JsonElement, @Nullable List<E>> parseArray = je -> gson.fromJson(je, rawListType);
                String[] arrayKeys = new String[] { "list", "items", "records", "rows" };

                // Containers such as data/result/payload
                for (String container : new String[] { "data", "result", "payload" }) {
                    if (!obj.has(container)) {
                        continue;
                    }
                    JsonElement data = obj.get(container);
                    if (data.isJsonArray()) {
                        JsonElement flat = flattenArrayIfNeeded(data.getAsJsonArray());
                        return nullToEmptyList(parseArray.apply(flat));
                    }
                    if (data.isJsonObject()) {
                        JsonObject dObj = data.getAsJsonObject();
                        // common array fields directly under container
                        for (String k : arrayKeys) {
                            if (dObj.has(k) && dObj.get(k).isJsonArray()) {
                                JsonElement flat = flattenArrayIfNeeded(dObj.get(k).getAsJsonArray());
                                return nullToEmptyList(parseArray.apply(flat));
                            }
                        }
                        // altArrayKeys under container (e.g., devices)
                        for (String key : altArrayKeys) {
                            if (!dObj.has(key)) {
                                continue;
                            }
                            JsonElement alt = dObj.get(key);
                            if (alt.isJsonArray()) {
                                JsonElement flat = flattenArrayIfNeeded(alt.getAsJsonArray());
                                return nullToEmptyList(parseArray.apply(flat));
                            }
                            if (alt.isJsonObject()) {
                                JsonObject aObj = alt.getAsJsonObject();
                                for (String k : arrayKeys) {
                                    if (aObj.has(k) && aObj.get(k).isJsonArray()) {
                                        JsonElement flat = flattenArrayIfNeeded(aObj.get(k).getAsJsonArray());
                                        return nullToEmptyList(parseArray.apply(flat));
                                    }
                                }
                            }
                        }
                        // as a last resort: first array value under container
                        for (var entry : dObj.entrySet()) {
                            if (entry.getValue().isJsonArray()) {
                                JsonElement flat = flattenArrayIfNeeded(entry.getValue().getAsJsonArray());
                                return nullToEmptyList(parseArray.apply(flat));
                            }
                        }
                    }
                }

                for (String key : altArrayKeys) {
                    if (!obj.has(key)) {
                        continue;
                    }
                    JsonElement alt = obj.get(key);
                    if (alt.isJsonArray()) {
                        JsonElement flat = flattenArrayIfNeeded(alt.getAsJsonArray());
                        return nullToEmptyList(parseArray.apply(flat));
                    }
                    if (alt.isJsonObject()) {
                        JsonObject aObj = alt.getAsJsonObject();
                        for (String k : arrayKeys) {
                            if (aObj.has(k) && aObj.get(k).isJsonArray()) {
                                JsonElement flat = flattenArrayIfNeeded(aObj.get(k).getAsJsonArray());
                                return nullToEmptyList(parseArray.apply(flat));
                            }
                        }
                        for (var entry : aObj.entrySet()) {
                            if (entry.getValue().isJsonArray()) {
                                JsonElement flat = flattenArrayIfNeeded(entry.getValue().getAsJsonArray());
                                return nullToEmptyList(parseArray.apply(flat));
                            }
                        }
                    }
                }
                // any direct array at root
                for (var entry : obj.entrySet()) {
                    if (entry.getValue().isJsonArray()) {
                        JsonElement flat = flattenArrayIfNeeded(entry.getValue().getAsJsonArray());
                        return nullToEmptyList(parseArray.apply(flat));
                    }
                }
            }
        } catch (Exception ignored) {
        }

        throw new UniFiAccessApiException("Failed to parse list response for " + action + ": unexpected JSON");
    }

    private static JsonElement flattenArrayIfNeeded(JsonArray array) {
        boolean hasNested = false;
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).isJsonArray()) {
                hasNested = true;
                break;
            }
        }
        if (!hasNested) {
            return array;
        }
        JsonArray flat = new JsonArray();
        for (int i = 0; i < array.size(); i++) {
            JsonElement el = array.get(i);
            if (el.isJsonArray()) {
                JsonArray inner = el.getAsJsonArray();
                for (int j = 0; j < inner.size(); j++) {
                    flat.add(inner.get(j));
                }
            } else if (el.isJsonObject()) {
                flat.add(el);
            }
        }
        return flat;
    }

    private static <E> List<E> nullToEmptyList(@Nullable List<E> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private ContentResponse execGet(String path) throws UniFiAccessApiException {
        try {
            return newRequest(HttpMethod.GET, path, null).send();
        } catch (Exception e) {
            throw new UniFiAccessApiException("GET failed for " + path + ": " + e.getMessage(), e);
        }
    }

    private ContentResponse execDelete(String path) throws UniFiAccessApiException {
        try {
            return newRequest(HttpMethod.DELETE, path, null).send();
        } catch (Exception e) {
            throw new UniFiAccessApiException("DELETE failed for " + path + ": " + e.getMessage(), e);
        }
    }

    private ContentResponse execPost(String path, Object body) throws UniFiAccessApiException {
        try {
            return newRequest(HttpMethod.POST, path, req -> {
                String json = toJson(body);
                req.header(HttpHeader.CONTENT_TYPE, "application/json");
                req.content(new StringContentProvider(json, StandardCharsets.UTF_8));
            }).send();
        } catch (Exception e) {
            throw new UniFiAccessApiException("POST failed for " + path + ": " + e.getMessage(), e);
        }
    }

    private ContentResponse execPut(String path, Object body) throws UniFiAccessApiException {
        try {
            return newRequest(HttpMethod.PUT, path, req -> {
                String json = toJson(body);
                req.header(HttpHeader.CONTENT_TYPE, "application/json");
                req.content(new StringContentProvider(json, StandardCharsets.UTF_8));
            }).send();
        } catch (Exception e) {
            throw new UniFiAccessApiException("PUT failed for " + path + ": " + e.getMessage(), e);
        }
    }

    private URI toWebSocketUri(String relativePath) {
        String scheme = "wss";
        if ("http".equalsIgnoreCase(base.getScheme())) {
            scheme = "ws";
        }
        String path = base.getPath();
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        String fullPath = path + relativePath;
        return URI
                .create(scheme + "://" + base.getHost() + (base.getPort() > 0 ? ":" + base.getPort() : "") + fullPath);
    }

    private synchronized void startWsMonitor() {
        logger.debug("Starting WS monitor");
        ScheduledFuture<?> wsMonitorFuture = this.wsMonitorFuture;
        if (wsMonitorFuture == null || wsMonitorFuture.isCancelled()) {
            this.wsMonitorFuture = executorService.scheduleWithFixedDelay(() -> {
                try {
                    Session s = wsSession;
                    if (s != null && s.isOpen()) {
                        long sinceMs = System.currentTimeMillis() - lastHeartbeatEpochMs;
                        if (sinceMs > 10_000L) {
                            logger.debug("Notifications heartbeat missing ({} ms). Reconnecting...", sinceMs);
                            try {
                                s.close();
                            } catch (Exception e) {
                                logger.debug("Error closing notifications WebSocket", e);
                            } finally {
                                wsSession = null;
                            }
                        }
                    } else {
                        logger.debug("Notifications WebSocket not open");
                    }
                } catch (Exception e) {
                    logger.debug("WS monitor error: ", e);
                }
            }, 5, 5, TimeUnit.SECONDS);
        } else {
            logger.debug("WS monitor already running!");
        }
    }

    private synchronized void stopWsMonitor() {
        try {
            ScheduledFuture<?> f = wsMonitorFuture;
            if (f != null) {
                f.cancel(true);
                wsMonitorFuture = null;
            }
        } catch (Exception ignored) {
        }
    }
}
