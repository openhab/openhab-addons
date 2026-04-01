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
package org.openhab.binding.unifiaccess.internal.api;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
import org.openhab.binding.unifiaccess.internal.dto.Device;
import org.openhab.binding.unifiaccess.internal.dto.DeviceAccessMethodSettings;
import org.openhab.binding.unifiaccess.internal.dto.Door;
import org.openhab.binding.unifiaccess.internal.dto.DoorEmergencySettings;
import org.openhab.binding.unifiaccess.internal.dto.DoorLockRule;
import org.openhab.binding.unifiaccess.internal.dto.DoorState;
import org.openhab.binding.unifiaccess.internal.dto.Image;
import org.openhab.binding.unifiaccess.internal.dto.Notification;
import org.openhab.binding.unifiaccess.internal.dto.UniFiAccessApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * UniFi Access v2 API client using session-based authentication.
 * <p>
 * Authenticates against the internal v2 proxy API on port 443 using
 * username/password credentials. Maintains a session cookie and CSRF token
 * for all subsequent requests. Automatically re-authenticates on 401 responses.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class UniFiAccessApiClient implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(UniFiAccessApiClient.class);

    private static final long HTTP_TIMEOUT_MS = 30_000;
    private static final String V2_BASE = "/proxy/access/api/v2";
    private static final String CSRF_HEADER = "x-csrf-token";
    private static final String UPDATED_CSRF_HEADER = "X-Updated-CSRF-Token";

    private final HttpClient httpClient;
    private final String host;
    private final Gson gson;
    private final String username;
    private final String password;
    private final ScheduledExecutorService executorService;

    private final WebSocketClient wsClient;
    private @Nullable Session wsSession;
    private long lastHeartbeatEpochMs;
    private @Nullable ScheduledFuture<?> wsMonitorFuture;
    private boolean closed = false;

    /** Session cookie from login (e.g. "TOKEN=..."). */
    private @Nullable String sessionCookie;
    /** CSRF token for authenticated requests. */
    private @Nullable String csrfToken;
    /** Flag to prevent recursive re-auth loops. */
    private boolean reauthenticating = false;

    public UniFiAccessApiClient(HttpClient httpClient, String host, Gson gson, String username, String password,
            ScheduledExecutorService executor) {
        this.httpClient = httpClient;
        this.host = host;
        this.gson = gson;
        this.username = username;
        this.password = password;
        this.executorService = executor;
        this.wsClient = new WebSocketClient(httpClient);
        this.wsClient.unmanage(this.httpClient);
        try {
            wsClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start Jetty ws client", e);
        }
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

    // ---- Authentication ----

    /**
     * Performs session-based authentication against the v2 API.
     * <ol>
     * <li>GET / to obtain the initial CSRF token from the X-CSRF-Token response header</li>
     * <li>POST /api/auth/login with credentials to establish a session</li>
     * <li>Extracts the session cookie and updated CSRF token from the login response</li>
     * </ol>
     */
    public void login() throws UniFiAccessApiException {
        try {
            // Step 1: Try to GET the landing page for an initial CSRF token (optional)
            String initialCsrf = "";
            try {
                ContentResponse csrfResp = httpClient.newRequest(baseUri("/")).method(HttpMethod.GET)
                        .timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
                String csrf = csrfResp.getHeaders().get("X-CSRF-Token");
                if (csrf != null && !csrf.isBlank()) {
                    initialCsrf = csrf;
                    logger.debug("Obtained initial CSRF token");
                } else {
                    logger.debug("No initial CSRF token from GET / - will obtain from login response");
                }
            } catch (Exception e) {
                logger.debug("Failed to fetch initial CSRF token: {}", e.getMessage());
            }

            // Step 2: POST login with credentials
            JsonObject loginBody = new JsonObject();
            loginBody.addProperty("username", username);
            loginBody.addProperty("password", password);
            loginBody.addProperty("rememberMe", true);
            loginBody.addProperty("token", "");

            var loginReq = httpClient.newRequest(baseUri("/api/auth/login")).method(HttpMethod.POST)
                    .header(HttpHeader.CONTENT_TYPE, "application/json")
                    .content(new StringContentProvider(loginBody.toString(), StandardCharsets.UTF_8))
                    .timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!initialCsrf.isEmpty()) {
                loginReq.header(CSRF_HEADER, initialCsrf);
            }
            ContentResponse loginResp = loginReq.send();

            int status = loginResp.getStatus();
            if (status == 401 || status == 403) {
                throw new UniFiAccessApiException(
                        "Authentication failed: " + status + " - " + loginResp.getContentAsString(), true);
            }
            if (status < 200 || status >= 300) {
                throw new UniFiAccessApiException(
                        "Login failed with status " + status + ": " + loginResp.getContentAsString());
            }

            // Step 3: Extract session cookie
            String setCookie = loginResp.getHeaders().get("Set-Cookie");
            if (setCookie != null && !setCookie.isBlank()) {
                int semicolon = setCookie.indexOf(';');
                this.sessionCookie = semicolon > 0 ? setCookie.substring(0, semicolon) : setCookie;
            } else {
                throw new UniFiAccessApiException("No Set-Cookie header in login response");
            }

            // Step 4: Extract updated CSRF token (prefer X-Updated-CSRF-Token, fall back to X-CSRF-Token)
            String updatedCsrf = loginResp.getHeaders().get(UPDATED_CSRF_HEADER);
            if (updatedCsrf == null || updatedCsrf.isBlank()) {
                updatedCsrf = loginResp.getHeaders().get("X-CSRF-Token");
            }
            if (updatedCsrf != null && !updatedCsrf.isBlank()) {
                this.csrfToken = updatedCsrf;
            } else {
                // Fall back to initial CSRF if no updated token provided
                this.csrfToken = initialCsrf;
            }

            logger.debug("Login successful, session established");
        } catch (UniFiAccessApiException e) {
            throw e;
        } catch (Exception e) {
            throw new UniFiAccessApiException("Login failed: " + e.getMessage(), e);
        }
    }

    // ---- Bootstrap ----

    private volatile @Nullable JsonObject cachedBootstrap;
    private volatile long bootstrapCacheTimeMs;
    private static final long BOOTSTRAP_CACHE_TTL_MS = 30_000; // 30 seconds

    /**
     * Fetches the bootstrap topology from the v2 API.
     * Caches the result for 30 seconds to avoid redundant calls during a sync cycle.
     *
     * @return the raw bootstrap JSON object (first element of the data array)
     */
    public synchronized JsonObject getBootstrap() throws UniFiAccessApiException {
        JsonObject cached = this.cachedBootstrap;
        if (cached != null && (System.currentTimeMillis() - bootstrapCacheTimeMs) < BOOTSTRAP_CACHE_TTL_MS) {
            return cached;
        }
        ContentResponse resp = execGet(V2_BASE + "/devices/topology4");
        ensure2xx(resp, "getBootstrap");
        String json = resp.getContentAsString();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        checkV2Success(root, "getBootstrap");

        JsonArray dataArray = root.getAsJsonArray("data");
        if (dataArray == null || dataArray.isEmpty()) {
            throw new UniFiAccessApiException("Empty bootstrap data array");
        }
        JsonObject result = dataArray.get(0).getAsJsonObject();
        this.cachedBootstrap = result;
        this.bootstrapCacheTimeMs = System.currentTimeMillis();
        return result;
    }

    // ---- Doors ----

    /**
     * Extracts doors from the bootstrap topology.
     * Flattens: bootstrap.floors[].doors[] into a single list.
     */
    public List<Door> getDoors() throws UniFiAccessApiException {
        JsonObject bootstrap = getBootstrap();
        List<Door> doors = new ArrayList<>();

        JsonArray floors = bootstrap.getAsJsonArray("floors");
        if (floors != null) {
            for (JsonElement floorEl : floors) {
                JsonObject floor = floorEl.getAsJsonObject();
                JsonArray floorDoors = floor.getAsJsonArray("doors");
                if (floorDoors != null) {
                    for (JsonElement doorEl : floorDoors) {
                        Door door = parseDoor(doorEl.getAsJsonObject());
                        if (door != null) {
                            doors.add(door);
                        }
                    }
                }
            }
        }
        return doors;
    }

    /**
     * Parses a door JSON object from the bootstrap topology into a Door DTO.
     */
    private @Nullable Door parseDoor(JsonObject doorObj) {
        Door door = new Door();
        door.id = getStringOrNull(doorObj, "unique_id");
        if (door.id == null) {
            return null;
        }
        door.name = getStringOrNull(doorObj, "name");
        door.fullName = getStringOrNull(doorObj, "full_name");

        String locationType = getStringOrNull(doorObj, "location_type");
        door.type = locationType != null ? locationType : "door";

        // Extract door thumbnail from extras
        JsonObject extras = doorObj.getAsJsonObject("extras");
        if (extras != null) {
            door.doorThumbnail = getStringOrNull(extras, "door_thumbnail");
        }

        // Extract door lock/position state from the hub device's configs
        // The hub device (with capability "is_hub") under this door's device_groups
        // has configs like "output_d1_lock_relay" and "input_d1_dps"
        JsonArray deviceGroups = doorObj.getAsJsonArray("device_groups");
        door.isBindHub = deviceGroups != null && !deviceGroups.isEmpty();
        if (deviceGroups != null) {
            for (JsonElement dgEl : deviceGroups) {
                JsonObject hubObj = findHubDevice(dgEl);
                if (hubObj != null) {
                    door.hubDeviceId = getStringOrNull(hubObj, "unique_id");
                    Map<String, String> hubConfigs = parseConfigs(hubObj.getAsJsonArray("configs"));

                    // Check extensions for relay/DPS inversion settings
                    boolean relayNc = false;
                    boolean dpsReverse = false;
                    JsonArray extensions = hubObj.getAsJsonArray("extensions");
                    if (extensions != null) {
                        for (JsonElement extEl : extensions) {
                            if (extEl.isJsonObject()) {
                                JsonArray targetConfig = extEl.getAsJsonObject().getAsJsonArray("target_config");
                                if (targetConfig != null) {
                                    for (JsonElement tcEl : targetConfig) {
                                        if (tcEl.isJsonObject()) {
                                            JsonObject tc = tcEl.getAsJsonObject();
                                            String configKey = getStringOrNull(tc, "config_key");
                                            if ("is_relay_nc".equals(configKey)) {
                                                relayNc = getBooleanOrFalse(tc, "config_value");
                                            } else if ("dps_reverse".equals(configKey)) {
                                                dpsReverse = getBooleanOrFalse(tc, "config_value");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Lock relay: normally open (default) → off=locked, on=unlocked
                    // If is_relay_nc (normally closed) → off=unlocked, on=locked
                    String lockRelay = hubConfigs.get("output_d1_lock_relay");
                    if (lockRelay != null) {
                        boolean relayOn = "on".equalsIgnoreCase(lockRelay);
                        boolean locked = relayNc ? relayOn : !relayOn;
                        door.doorLockRelayStatus = locked ? DoorState.LockState.LOCKED : DoorState.LockState.UNLOCKED;
                    }
                    // DPS: default → on=closed, off=open
                    // If dps_reverse → on=open, off=closed
                    String dps = hubConfigs.get("input_d1_dps");
                    if (dps != null) {
                        boolean dpsOn = "on".equalsIgnoreCase(dps);
                        boolean closed = dpsReverse ? !dpsOn : dpsOn;
                        door.doorPositionStatus = closed ? DoorState.DoorPosition.CLOSE : DoorState.DoorPosition.OPEN;
                    }
                    break;
                }
            }
        }

        // Floor ID from up_id
        door.floorId = getStringOrNull(doorObj, "up_id");

        return door;
    }

    // ---- Devices ----

    /**
     * Extracts all devices from the bootstrap topology.
     * Flattens device_groups from floors and doors into a single list.
     */
    public List<Device> getDevices() throws UniFiAccessApiException {
        JsonObject bootstrap = getBootstrap();
        List<Device> devices = new ArrayList<>();

        // Devices from floors -> doors -> device_groups
        JsonArray floors = bootstrap.getAsJsonArray("floors");
        if (floors != null) {
            for (JsonElement floorEl : floors) {
                JsonObject floor = floorEl.getAsJsonObject();

                // Devices directly under floor's device_groups
                collectDevices(floor.getAsJsonArray("device_groups"), devices);

                // Devices under each door
                JsonArray floorDoors = floor.getAsJsonArray("doors");
                if (floorDoors != null) {
                    for (JsonElement doorEl : floorDoors) {
                        JsonObject doorObj = doorEl.getAsJsonObject();
                        collectDevices(doorObj.getAsJsonArray("device_groups"), devices);
                    }
                }
            }
        }

        // Top-level device_groups (devices not under a floor)
        collectDevices(bootstrap.getAsJsonArray("device_groups"), devices);

        return devices;
    }

    /**
     * Collects Device objects from a device_groups JSON array.
     */
    private void collectDevices(@Nullable JsonArray deviceGroups, List<Device> devices) {
        if (deviceGroups == null) {
            return;
        }
        for (JsonElement dgEl : deviceGroups) {
            if (dgEl.isJsonArray()) {
                // Nested array of devices — recurse
                collectDevices(dgEl.getAsJsonArray(), devices);
            } else if (dgEl.isJsonObject()) {
                Device device = parseDevice(dgEl.getAsJsonObject());
                if (device != null) {
                    devices.add(device);
                }
            }
        }
    }

    /**
     * Parses a device JSON object from the bootstrap into a Device DTO.
     */
    private @Nullable Device parseDevice(JsonObject devObj) {
        Device device = new Device();
        device.id = getStringOrNull(devObj, "unique_id");
        if (device.id == null) {
            return null;
        }
        String alias = getStringOrNull(devObj, "alias");
        String name = getStringOrNull(devObj, "name");
        device.alias = alias;
        device.name = (alias != null && !alias.isBlank()) ? alias : name;
        device.type = getStringOrNull(devObj, "device_type");
        device.locationId = getStringOrNull(devObj, "location_id");

        if (devObj.has("is_online") && !devObj.get("is_online").isJsonNull()) {
            device.isOnline = devObj.get("is_online").getAsBoolean();
        }
        if (devObj.has("is_adopted") && !devObj.get("is_adopted").isJsonNull()) {
            device.isAdopted = devObj.get("is_adopted").getAsBoolean();
        }
        if (devObj.has("is_connected") && !devObj.get("is_connected").isJsonNull()) {
            device.isConnected = devObj.get("is_connected").getAsBoolean();
        }
        if (devObj.has("is_managed") && !devObj.get("is_managed").isJsonNull()) {
            device.isManaged = devObj.get("is_managed").getAsBoolean();
        }

        device.mac = getStringOrNull(devObj, "mac");
        device.ip = getStringOrNull(devObj, "ip");
        device.firmware = getStringOrNull(devObj, "firmware");
        device.displayModel = getStringOrNull(devObj, "display_model");
        device.connectedUahId = getStringOrNull(devObj, "connected_uah_id");

        // Capabilities
        JsonArray caps = devObj.getAsJsonArray("capabilities");
        if (caps != null) {
            List<String> capList = new ArrayList<>();
            for (JsonElement c : caps) {
                capList.add(c.getAsString());
            }
            device.capabilities = capList;
        }

        // Parse configs key-value array into a flat map
        JsonArray configs = devObj.getAsJsonArray("configs");
        if (configs != null) {
            for (JsonElement cfgEl : configs) {
                if (cfgEl.isJsonObject()) {
                    JsonObject cfg = cfgEl.getAsJsonObject();
                    String key = getStringOrNull(cfg, "key");
                    String value = getStringOrNull(cfg, "value");
                    if (key != null && value != null) {
                        device.configMap.put(key, value);
                    }
                }
            }
        }

        return device;
    }

    /**
     * Finds the hub device (with "is_hub" capability) in a device_groups element,
     * which may be a single device object or a nested array.
     */
    private @Nullable JsonObject findHubDevice(JsonElement dgEl) {
        if (dgEl.isJsonObject()) {
            JsonObject obj = dgEl.getAsJsonObject();
            if (hasCapability(obj, "is_hub")) {
                return obj;
            }
        } else if (dgEl.isJsonArray()) {
            for (JsonElement inner : dgEl.getAsJsonArray()) {
                if (inner.isJsonObject()) {
                    JsonObject obj = inner.getAsJsonObject();
                    if (hasCapability(obj, "is_hub")) {
                        return obj;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasCapability(JsonObject devObj, String capability) {
        JsonArray caps = devObj.getAsJsonArray("capabilities");
        if (caps != null) {
            for (JsonElement c : caps) {
                if (capability.equals(c.getAsString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, String> parseConfigs(@Nullable JsonArray configs) {
        Map<String, String> map = new HashMap<>();
        if (configs != null) {
            for (JsonElement cfgEl : configs) {
                if (cfgEl.isJsonObject()) {
                    JsonObject cfg = cfgEl.getAsJsonObject();
                    String key = getStringOrNull(cfg, "key");
                    String value = getStringOrNull(cfg, "value");
                    if (key != null && value != null) {
                        map.put(key, value);
                    }
                }
            }
        }
        return map;
    }

    // ---- Door Control ----

    /**
     * Unlocks a door by its location ID.
     */
    public boolean unlockDoor(String locationId) throws UniFiAccessApiException {
        ContentResponse resp = execPut(V2_BASE + "/location/" + locationId + "/unlock", "{}");
        ensure2xx(resp, "unlockDoor");
        return checkV2SuccessBool(resp, "unlockDoor");
    }

    /**
     * Compatibility overload for handlers that pass actor/extra parameters.
     * The v2 API does not support actor parameters on unlock, so they are ignored.
     */
    public boolean unlockDoor(String locationId, @Nullable String actorId, @Nullable String actorName,
            @Nullable Map<String, Object> extra) throws UniFiAccessApiException {
        return unlockDoor(locationId);
    }

    /**
     * Sets a lock rule on a device.
     */
    public boolean setDoorLockRule(String deviceId, DoorLockRule rule) throws UniFiAccessApiException {
        JsonObject body = new JsonObject();
        if (rule.type != null) {
            body.addProperty("type", gson.toJson(rule.type).replace("\"", ""));
        }
        if (rule.interval != null && rule.interval > 0) {
            body.addProperty("interval", rule.interval);
        }
        ContentResponse resp = execPut(V2_BASE + "/device/" + deviceId + "/lock_rule?get_result=true", body.toString());
        ensure2xx(resp, "setDoorLockRule");
        return checkV2SuccessBool(resp, "setDoorLockRule");
    }

    public boolean keepDoorUnlocked(String deviceId) throws UniFiAccessApiException {
        return setDoorLockRule(deviceId, DoorLockRule.keepUnlock());
    }

    public boolean keepDoorLocked(String deviceId) throws UniFiAccessApiException {
        return setDoorLockRule(deviceId, DoorLockRule.keepLock());
    }

    public boolean unlockForMinutes(String deviceId, int minutes) throws UniFiAccessApiException {
        if (minutes <= 0) {
            throw new IllegalArgumentException("minutes must be > 0");
        }
        return setDoorLockRule(deviceId, DoorLockRule.customMinutes(minutes));
    }

    public boolean resetDoorLockRule(String deviceId) throws UniFiAccessApiException {
        return setDoorLockRule(deviceId, DoorLockRule.reset());
    }

    /** End an active keep-unlock/custom early (lock immediately). */
    public boolean lockEarly(String deviceId) throws UniFiAccessApiException {
        return setDoorLockRule(deviceId, DoorLockRule.lockEarly());
    }

    /** Terminate both unlock schedule and temporary unlock, locking immediately. */
    public boolean lockNow(String deviceId) throws UniFiAccessApiException {
        return setDoorLockRule(deviceId, DoorLockRule.lockNow());
    }

    /**
     * Gets the current lock rule for a door. The v2 API does not have a dedicated
     * endpoint for this, so we return a default reset rule.
     */
    public DoorLockRule getDoorLockRule(String doorId) throws UniFiAccessApiException {
        // The v2 topology does not expose a separate lock_rule GET endpoint.
        // Return a default "reset" rule; actual state is derived from bootstrap/notifications.
        return DoorLockRule.reset();
    }

    /**
     * Trigger a doorbell ring on a device.
     * Not yet supported on the v2 internal API.
     */
    public void triggerDoorbell(String deviceId) throws UniFiAccessApiException {
        throw new UniFiAccessApiException("triggerDoorbell is not supported on the v2 internal API");
    }

    // ---- Device Settings ----

    /**
     * Builds device access method settings from a device's config map (from bootstrap).
     * The v2 API does not have a separate /settings endpoint; settings are in the
     * device's configs array with tag "open_door_mode" and "device_setting".
     */
    public DeviceAccessMethodSettings getDeviceAccessMethodSettings(String deviceId) throws UniFiAccessApiException {
        // This is called with a deviceId but we need the configs.
        // If this is called without configs, search the last bootstrap.
        throw new UniFiAccessApiException("Use buildSettingsFromConfigs(configMap) instead");
    }

    /**
     * Builds DeviceAccessMethodSettings from a device's config map extracted from bootstrap.
     */
    public static DeviceAccessMethodSettings buildSettingsFromConfigs(Map<String, String> configMap) {
        DeviceAccessMethodSettings s = new DeviceAccessMethodSettings();

        s.nfc = new DeviceAccessMethodSettings.Nfc();
        s.nfc.setEnabled("yes".equalsIgnoreCase(configMap.getOrDefault("nfc", "no")));

        s.pinCode = new DeviceAccessMethodSettings.PinCode();
        s.pinCode.setEnabled("yes".equalsIgnoreCase(configMap.getOrDefault("pin_code", "no")));

        s.face = new DeviceAccessMethodSettings.Face();
        s.face.setEnabled("yes".equalsIgnoreCase(configMap.getOrDefault("face", "no")));
        s.face.antiSpoofingLevel = configMap.get("face_anti_spoofing_level");
        String detectDist = configMap.get("face_detect_distance_v2");
        if (detectDist == null) {
            detectDist = configMap.get("face_detect_distance");
        }
        s.face.detectDistance = detectDist;

        s.btTap = new DeviceAccessMethodSettings.Bt();
        s.btTap.setEnabled("yes".equalsIgnoreCase(configMap.getOrDefault("bt_tap", "no")));

        s.btButton = new DeviceAccessMethodSettings.Bt();
        s.btButton.setEnabled("yes".equalsIgnoreCase(configMap.getOrDefault("bt_button", "no")));

        s.btShake = new DeviceAccessMethodSettings.Bt();
        s.btShake.setEnabled("yes".equalsIgnoreCase(configMap.getOrDefault("bt", "no")));

        s.mobileWave = new DeviceAccessMethodSettings.MobileWave();
        s.mobileWave.setEnabled("yes".equalsIgnoreCase(configMap.getOrDefault("camera_mobile_unlock", "no")));

        s.wave = new DeviceAccessMethodSettings.Wave();
        // No direct "wave" key in v2 configs; assume not available
        s.wave.setEnabled(false);

        s.qrCode = new DeviceAccessMethodSettings.QrCode();
        s.qrCode.setEnabled("yes".equalsIgnoreCase(configMap.getOrDefault("qr_code", "no")));

        s.touchPass = new DeviceAccessMethodSettings.TouchPass();
        s.touchPass.setEnabled("yes".equalsIgnoreCase(configMap.getOrDefault("apple_pass", "no")));

        return s;
    }

    /**
     * Updates a single device config key/value via the v2 configs API.
     */
    public void updateDeviceConfig(String deviceId, String key, String value) throws UniFiAccessApiException {
        JsonObject entry = new JsonObject();
        entry.addProperty("key", key);
        entry.addProperty("value", value);
        JsonArray body = new JsonArray();
        body.add(entry);
        ContentResponse resp = execPut(V2_BASE + "/device/" + deviceId + "/configs", body.toString());
        ensure2xx(resp, "updateDeviceConfig");
        String json = resp.getContentAsString();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        checkV2Success(root, "updateDeviceConfig");
    }

    /**
     * Updates device access method settings for a reader (legacy - uses configs API).
     */
    public DeviceAccessMethodSettings updateDeviceAccessMethodSettings(String deviceId,
            DeviceAccessMethodSettings settings) throws UniFiAccessApiException {
        ContentResponse resp = execPut(V2_BASE + "/device/" + deviceId + "/configs", gson.toJson(settings));
        ensure2xx(resp, "updateDeviceAccessMethodSettings");
        String json = resp.getContentAsString();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        checkV2Success(root, "updateDeviceAccessMethodSettings");

        JsonElement dataEl = root.get("data");
        if (dataEl == null || dataEl.isJsonNull()) {
            throw new UniFiAccessApiException("Missing data in updateDeviceAccessMethodSettings response");
        }
        DeviceAccessMethodSettings result = gson.fromJson(dataEl, DeviceAccessMethodSettings.class);
        return Objects.requireNonNull(result);
    }

    // ---- Emergency Settings ----

    public DoorEmergencySettings getEmergencySettings() throws UniFiAccessApiException {
        ContentResponse resp = execGet(V2_BASE + "/doors/settings/emergency");
        ensure2xx(resp, "getEmergencySettings");
        String json = resp.getContentAsString();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        checkV2Success(root, "getEmergencySettings");

        JsonElement dataEl = root.get("data");
        if (dataEl == null || dataEl.isJsonNull()) {
            throw new UniFiAccessApiException("Missing data in getEmergencySettings response");
        }
        DoorEmergencySettings result = gson.fromJson(dataEl, DoorEmergencySettings.class);
        return Objects.requireNonNull(result);
    }

    public void setEmergencySettings(DoorEmergencySettings settings) throws UniFiAccessApiException {
        ContentResponse resp = execPut(V2_BASE + "/doors/settings/emergency", gson.toJson(settings));
        ensure2xx(resp, "setEmergencySettings");
    }

    // ---- Thumbnails ----

    public Image getDoorThumbnail(String path) throws UniFiAccessApiException {
        // Thumbnails are served from the same host via the proxy path
        String thumbnailPath = path.startsWith("/") ? path : "/" + path;
        ContentResponse resp = execGet(thumbnailPath);
        ensure2xx(resp, "getDoorThumbnail");
        Image image = new Image();
        String mediaType = resp.getMediaType();
        image.mediaType = mediaType != null ? mediaType : "application/octet-stream";
        image.data = resp.getContent();
        return image;
    }

    // ---- WebSocket Notifications ----

    /**
     * Opens the v2 notifications WebSocket.
     * <p>
     * URL: wss://{host}/proxy/access/api/v2/ws/notification
     * Auth: Cookie header with session cookie
     */
    public synchronized void openNotifications(Runnable onOpen, Consumer<Notification> onMessage,
            Consumer<Throwable> onError, BiConsumer<Integer, String> onClosed) throws UniFiAccessApiException {
        Session session = wsSession;
        if (session != null && session.isOpen()) {
            return;
        }
        // Ensure we have a valid session before connecting
        if (sessionCookie == null || csrfToken == null) {
            login();
        }
        try {
            URI wsUri = URI.create("wss://" + host + V2_BASE + "/ws/notification");
            logger.debug("Notifications WebSocket URI: {}", wsUri);
            ClientUpgradeRequest req = new ClientUpgradeRequest();
            String cookie = sessionCookie;
            if (cookie != null) {
                req.setHeader("Cookie", cookie);
            }
            req.setHeader("Upgrade", "websocket");
            req.setHeader("Connection", "Upgrade");

            WebSocketAdapter socket = new WebSocketAdapter() {
                @Override
                @NonNullByDefault({})
                public void onWebSocketConnect(Session sess) {
                    super.onWebSocketConnect(sess);
                    logger.debug("Notifications WebSocket connected: {}", wsUri);
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
                                lastHeartbeatEpochMs = System.currentTimeMillis();
                                logger.trace("Notifications heartbeat received: {}", message.trim());
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

    // ---- HTTP Helpers ----

    private ContentResponse execGet(String path) throws UniFiAccessApiException {
        try {
            return newAuthenticatedRequest(HttpMethod.GET, path, null).send();
        } catch (UniFiAccessApiException e) {
            throw e;
        } catch (Exception e) {
            throw new UniFiAccessApiException("GET failed for " + path + ": " + e.getMessage(), e);
        }
    }

    private ContentResponse execPut(String path, String body) throws UniFiAccessApiException {
        try {
            return newAuthenticatedRequest(HttpMethod.PUT, path, body).send();
        } catch (UniFiAccessApiException e) {
            throw e;
        } catch (Exception e) {
            throw new UniFiAccessApiException("PUT failed for " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Builds an authenticated request with session cookie and CSRF token.
     * If no session exists, triggers a login first.
     */
    private Request newAuthenticatedRequest(HttpMethod method, String path, @Nullable String body)
            throws UniFiAccessApiException {
        if (sessionCookie == null || csrfToken == null) {
            login();
        }

        URI uri = baseUri(path);
        Request req = httpClient.newRequest(uri).method(method).header(HttpHeader.ACCEPT, "application/json")
                .timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        String cookie = sessionCookie;
        if (cookie != null) {
            req.header("Cookie", cookie);
        }
        String csrf = csrfToken;
        if (csrf != null) {
            req.header(CSRF_HEADER, csrf);
        }

        if (body != null) {
            req.header(HttpHeader.CONTENT_TYPE, "application/json");
            req.content(new StringContentProvider(body, StandardCharsets.UTF_8));
        }

        logger.debug("{} {}", method, uri);
        return req;
    }

    private URI baseUri(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        return URI.create("https://" + host + normalized);
    }

    /**
     * Ensures the HTTP response is 2xx. On 401, attempts re-authentication.
     */
    private void ensure2xx(ContentResponse resp, String action) throws UniFiAccessApiException {
        if (logger.isTraceEnabled()) {
            String mediaType = resp.getMediaType();
            if ("image/jpeg".equalsIgnoreCase(mediaType) || "image/png".equalsIgnoreCase(mediaType)) {
                logger.trace("ensure2xx status: {} mediaType: {} resp: image data", resp.getStatus(), mediaType);
            } else {
                logger.trace("ensure2xx status: {} mediaType: {} resp: {}", resp.getStatus(), mediaType,
                        resp.getContentAsString());
            }
        }

        int sc = resp.getStatus();
        if (sc == 401) {
            if (!reauthenticating) {
                reauthenticating = true;
                try {
                    logger.debug("Received 401 for {}, re-authenticating", action);
                    login();
                } finally {
                    reauthenticating = false;
                }
                // Signal caller to retry - throw a re-auth exception
                throw new UniFiAccessApiException("Re-authenticated after 401 for " + action + ", retry required");
            }
            throw new UniFiAccessApiException(
                    "Authentication failed for " + action + ": " + sc + " - " + resp.getContentAsString(), true);
        }
        if (sc == 403) {
            throw new UniFiAccessApiException("Forbidden for " + action + ": " + sc + " - " + resp.getContentAsString(),
                    true);
        }
        if (sc < 200 || sc >= 300) {
            throw new UniFiAccessApiException(
                    "Non 2xx response for " + action + ": " + sc + " - " + resp.getContentAsString());
        }

        // Update CSRF token from response if present
        String updatedCsrf = resp.getHeaders().get(UPDATED_CSRF_HEADER);
        if (updatedCsrf != null && !updatedCsrf.isBlank()) {
            this.csrfToken = updatedCsrf;
        }
    }

    /**
     * Checks the v2 response envelope for success.
     * V2 responses use: {"code":200,"codeS":"SUCCESS","msg":"...","data":{...}}
     * Success check: codeS === "SUCCESS"
     */
    private void checkV2Success(JsonObject root, String action) throws UniFiAccessApiException {
        String codeS = getStringOrNull(root, "codeS");
        if (codeS == null || !"SUCCESS".equals(codeS)) {
            String msg = getStringOrNull(root, "msg");
            throw new UniFiAccessApiException(
                    action + " failed: codeS=" + codeS + ", msg=" + (msg != null ? msg : "unknown"));
        }
    }

    /**
     * Checks a v2 response for success and returns a boolean.
     */
    private boolean checkV2SuccessBool(ContentResponse resp, String action) throws UniFiAccessApiException {
        String json = resp.getContentAsString();
        if (json.isBlank()) {
            return true; // Some endpoints return empty body on success
        }
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String codeS = getStringOrNull(root, "codeS");
            return "SUCCESS".equals(codeS);
        } catch (Exception e) {
            logger.debug("Could not parse v2 response for {}: {}", action, e.getMessage());
            return true; // HTTP was 2xx, treat as success
        }
    }

    // ---- JSON Helpers ----

    private static @Nullable String getStringOrNull(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) {
            return null;
        }
        return el.getAsString();
    }

    private static boolean getBooleanOrFalse(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) {
            return false;
        }
        try {
            return el.getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Flattens nested arrays if needed (e.g., [[obj1, obj2], [obj3]]).
     */
    static JsonElement flattenArrayIfNeeded(JsonArray array) {
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

    /**
     * Parse a list that may be wrapped in various response formats.
     */
    @SuppressWarnings("unused")
    private <E> List<E> parseListMaybeWrapped(String json, java.lang.reflect.Type wrappedListType,
            java.lang.reflect.Type rawListType, String action, String... altArrayKeys) throws UniFiAccessApiException {
        if (json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            JsonElement root = JsonParser.parseString(json);
            if (root.isJsonArray()) {
                JsonElement flat = flattenArrayIfNeeded(root.getAsJsonArray());
                List<E> result = gson.fromJson(flat, rawListType);
                return result != null ? result : Collections.emptyList();
            }
            if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                JsonElement data = obj.get("data");
                if (data != null && data.isJsonArray()) {
                    JsonElement flat = flattenArrayIfNeeded(data.getAsJsonArray());
                    List<E> result = gson.fromJson(flat, rawListType);
                    return result != null ? result : Collections.emptyList();
                }
            }
        } catch (Exception e) {
            throw new UniFiAccessApiException("Failed to parse list response for " + action + ": " + e.getMessage(), e);
        }
        throw new UniFiAccessApiException("Failed to parse list response for " + action + ": unexpected JSON");
    }

    // ---- WebSocket Monitor ----

    private synchronized void startWsMonitor() {
        logger.debug("Starting WS monitor");
        ScheduledFuture<?> existing = this.wsMonitorFuture;
        if (existing == null || existing.isCancelled()) {
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
