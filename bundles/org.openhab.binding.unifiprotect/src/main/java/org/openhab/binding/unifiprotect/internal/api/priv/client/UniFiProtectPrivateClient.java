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
package org.openhab.binding.unifiprotect.internal.api.priv.client;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Camera;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Chime;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Doorlock;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Light;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Sensor;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.ApiKey;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.ApiKeyResponse;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.Bootstrap;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.Nvr;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.UserCertificate;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType;
import org.openhab.binding.unifiprotect.internal.api.priv.exception.AuthenticationException;
import org.openhab.binding.unifiprotect.internal.api.priv.exception.BadRequestException;
import org.openhab.binding.unifiprotect.internal.api.priv.exception.NvrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main client for UniFi Protect Private API
 * Provides async operations for all API endpoints
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiProtectPrivateClient {

    private final Logger logger = LoggerFactory.getLogger(UniFiProtectPrivateClient.class);
    private static final String PRIVATE_API_PATH = "/proxy/protect/api/";
    private static final Duration BOOTSTRAP_REFRESH_INTERVAL = Duration.ofMinutes(15);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration FUTURE_TIMEOUT = Duration.ofSeconds(60);

    private final HttpClient httpClient;
    private final String baseUrl;
    private final UniFiProtectAuthenticator authenticator;
    private final ScheduledExecutorService scheduler;

    private volatile @Nullable Bootstrap cachedBootstrap;
    private volatile @Nullable Instant lastBootstrapRefresh;
    private volatile @Nullable UniFiProtectPrivateWebSocket webSocket;
    private volatile @Nullable ScheduledFuture<?> bootstrapRefreshTask;
    private volatile @Nullable CompletableFuture<Bootstrap> inFlightBootstrapRefresh;

    /**
     * Create a new UniFi Protect client
     *
     * @param httpClient The HTTP client to use (provided by openHAB, already configured for SSL)
     * @param scheduler The scheduled executor service to use for periodic tasks
     * @param host The hostname or IP address of the UniFi Protect NVR
     * @param port The port (typically 443 for HTTPS)
     * @param username The username for authentication
     * @param password The password for authentication
     */
    public UniFiProtectPrivateClient(HttpClient httpClient, ScheduledExecutorService scheduler, String host, int port,
            String username, String password) {
        this.httpClient = httpClient;
        this.scheduler = scheduler;
        this.baseUrl = "https://" + host + ":" + port;
        this.authenticator = new UniFiProtectAuthenticator(httpClient, scheduler, baseUrl, username, password, true);
    }

    /**
     * Initialize the client - authenticate and fetch bootstrap
     */
    public CompletableFuture<Void> initialize() {
        return ensureAuthenticated().thenCompose(v -> getBootstrap()).thenAccept(bootstrap -> {
            logger.debug("Client initialized successfully");
            startPeriodicBootstrapRefresh();
        });
    }

    /**
     * Start periodic bootstrap refresh task
     * Refreshes the bootstrap every 15 minutes to ensure state consistency
     */
    private void startPeriodicBootstrapRefresh() {
        // Cancel existing task if any
        stopPeriodicBootstrapRefresh();

        // Schedule periodic refresh
        bootstrapRefreshTask = scheduler.scheduleWithFixedDelay(() -> {
            try {
                CompletableFuture<Bootstrap> existing = inFlightBootstrapRefresh;
                if (existing != null && !existing.isDone()) {
                    logger.debug("Skipping periodic refresh - previous refresh still in progress");
                    return;
                }

                logger.debug("Performing periodic bootstrap refresh");
                CompletableFuture<Bootstrap> refresh = refreshBootstrap().whenComplete((bootstrap, ex) -> {
                    inFlightBootstrapRefresh = null;
                    if (ex != null) {
                        logger.debug("Periodic bootstrap refresh failed", ex);
                    } else {
                        logger.debug("Periodic bootstrap refresh completed successfully");
                    }
                });
                inFlightBootstrapRefresh = refresh;
            } catch (Exception e) {
                logger.debug("Error during periodic bootstrap refresh", e);
            }
        }, BOOTSTRAP_REFRESH_INTERVAL.toMinutes(), BOOTSTRAP_REFRESH_INTERVAL.toMinutes(), TimeUnit.MINUTES);

        logger.debug("Started periodic bootstrap refresh (interval: {} minutes)",
                BOOTSTRAP_REFRESH_INTERVAL.toMinutes());
    }

    /**
     * Stop periodic bootstrap refresh task
     */
    private void stopPeriodicBootstrapRefresh() {
        ScheduledFuture<?> bootstrapRefreshTask = this.bootstrapRefreshTask;
        if (bootstrapRefreshTask != null && !bootstrapRefreshTask.isCancelled()) {
            bootstrapRefreshTask.cancel(false);
            logger.debug("Stopped periodic bootstrap refresh");
        }
        this.bootstrapRefreshTask = null;

        CompletableFuture<Bootstrap> inFlight = inFlightBootstrapRefresh;
        if (inFlight != null && !inFlight.isDone()) {
            inFlight.cancel(true);
        }
        inFlightBootstrapRefresh = null;
    }

    /**
     * Get the bootstrap object, either from cache or from the API
     */
    public CompletableFuture<Bootstrap> getBootstrap() {
        Bootstrap cachedBootstrap = this.cachedBootstrap;
        Instant lastBootstrapRefresh = this.lastBootstrapRefresh;
        // Check if we have a cached bootstrap that's still fresh
        if (cachedBootstrap != null && lastBootstrapRefresh != null) {
            Duration age = Duration.between(lastBootstrapRefresh, Instant.now());
            if (age.compareTo(BOOTSTRAP_REFRESH_INTERVAL) < 0) {
                logger.debug("Returning cached bootstrap (age: {})", age);
                return CompletableFuture.completedFuture(cachedBootstrap);
            }
        }

        // No valid cache, fetch new bootstrap
        return apiRequest(HttpMethod.GET, "bootstrap", null, Bootstrap.class).thenApply(bootstrap -> {
            this.cachedBootstrap = bootstrap;
            this.lastBootstrapRefresh = Instant.now();
            logger.debug("Bootstrap refreshed");
            return bootstrap;
        });
    }

    /**
     * Force refresh of bootstrap
     */
    public CompletableFuture<Bootstrap> refreshBootstrap() {
        lastBootstrapRefresh = null; // Force refresh
        return getBootstrap();
    }

    /**
     * Enable WebSocket for real-time updates
     */
    public CompletableFuture<Void> enableWebSocket(
            Consumer<UniFiProtectPrivateWebSocket.WebSocketUpdate> updateHandler) {
        return ensureAuthenticated().thenCompose(v -> {
            if (webSocket != null) {
                logger.debug("WebSocket already enabled");
                return CompletableFuture.completedFuture(null);
            }

            String wsUrl = baseUrl.replace("https://", "wss://").replace("http://", "ws://")
                    + "/proxy/protect/ws/updates";
            String cookie = authenticator.getAuthCookie();
            if (cookie == null) {
                return CompletableFuture.failedFuture(new IllegalStateException("Not authenticated"));
            }
            webSocket = new UniFiProtectPrivateWebSocket(wsUrl, cookie, updateHandler, this, httpClient);
            return webSocket.connect();
        });
    }

    /**
     * Generic API request
     */
    /**
     * Make a direct API request (without /proxy/protect/api/ prefix)
     * Used for non-Protect APIs like user management
     */
    private <T> CompletableFuture<T> directApiRequest(HttpMethod method, String path, @Nullable Object body,
            Class<T> responseType) {
        return makeApiRequest(method, path, body, responseType);
    }

    /**
     * Make a Protect API request (with /proxy/protect/api/ prefix)
     */
    public <T> CompletableFuture<T> apiRequest(HttpMethod method, String path, @Nullable Object body,
            Class<T> responseType) {
        return makeApiRequest(method, PRIVATE_API_PATH + path, body, responseType);
    }

    /**
     * Core API request implementation
     */
    private <T> CompletableFuture<T> makeApiRequest(HttpMethod method, String path, @Nullable Object body,
            Class<T> responseType) {
        return ensureAuthenticated().thenCompose(v -> {
            CompletableFuture<T> future = new CompletableFuture<>();

            try {
                String url = baseUrl + path;
                Request request = httpClient.newRequest(url).method(method).timeout(DEFAULT_TIMEOUT.toMillis(),
                        TimeUnit.MILLISECONDS);

                authenticator.addAuthHeaders(request);

                // Add body if present
                String requestBody = null;
                if (body != null) {
                    requestBody = JsonUtil.toJson(body);
                    request.header("Content-Type", "application/json");
                    request.content(new StringContentProvider(requestBody));
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("HTTP Request: {} {}", method, url);
                    if (requestBody != null) {
                        logger.trace("Request Body: {}", requestBody);
                    }
                }

                request.send(new BufferingResponseListener() {
                    @Override
                    public void onComplete(Result result) {
                        try {
                            if (result.isFailed()) {
                                future.completeExceptionally(new NvrException("Request failed", result.getFailure()));
                                return;
                            }

                            Response response = result.getResponse();
                            int status = response.getStatus();

                            if (status == HttpStatus.UNAUTHORIZED_401 || status == HttpStatus.FORBIDDEN_403) {
                                logger.debug("Authentication error, will retry: {}", status);
                                authenticator.clearAuth();
                                future.completeExceptionally(
                                        new AuthenticationException("Authentication required: " + status));
                                return;
                            }

                            if (status < 200 || status >= 300) {
                                String reason = response.getReason();
                                if (status >= 400 && status < 500) {
                                    future.completeExceptionally(
                                            new BadRequestException("Bad request: " + reason, status));
                                } else {
                                    future.completeExceptionally(new NvrException("Request failed: " + reason, status));
                                }
                                return;
                            }

                            String content = getContentAsString();

                            if (logger.isTraceEnabled()) {
                                logger.trace("HTTP Response: {} {}", status, response.getReason());
                                if (content != null && !content.isEmpty()) {
                                    logger.trace("Response Body: {}", content);
                                }
                            }

                            if (content == null || content.isEmpty()) {
                                future.complete(null);
                            } else {
                                try {
                                    T parsed = JsonUtil.fromJson(content, responseType);
                                    future.complete(parsed);
                                } catch (Exception parseEx) {
                                    logger.debug("Failed to parse response as {}: {}", responseType.getSimpleName(),
                                            parseEx.getMessage(), parseEx);
                                    future.completeExceptionally(parseEx);
                                }
                            }

                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    }
                });

            } catch (Exception e) {
                future.completeExceptionally(e);
            }

            return future;
        }).orTimeout(FUTURE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Ensure we're authenticated, authenticate if not
     */
    private CompletableFuture<Void> ensureAuthenticated() {
        if (authenticator.isAuthenticated()) {
            return CompletableFuture.completedFuture(null);
        }
        return authenticator.authenticate();
    }

    /**
     * Get cached bootstrap (synchronous)
     */
    public @Nullable Bootstrap getCachedBootstrap() {
        return cachedBootstrap;
    }

    /**
     * Update cached bootstrap (called by WebSocket)
     */
    public void updateCachedBootstrap(Bootstrap bootstrap) {
        this.cachedBootstrap = bootstrap;
        this.lastBootstrapRefresh = Instant.now();
    }

    /**
     * Update a device by sending a PATCH request with the changes
     * 
     * @param modelType The model type (camera, light, sensor, etc.)
     * @param deviceId The device ID
     * @param updates Map of field names to new values
     * @return CompletableFuture that completes with the updated device data
     */
    public <T> CompletableFuture<T> updateDevice(String modelType, String deviceId, Object updates,
            Class<T> responseType) {
        String path = modelType + "s/" + deviceId; // cameras/{id}, lights/{id}, etc.
        return apiRequest(HttpMethod.PATCH, path, updates, responseType);
    }

    /**
     * Update camera settings
     * 
     * @param cameraId The camera ID
     * @param updates Map of settings to update (e.g., {"name": "New Name", "isMicEnabled": true})
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> updateCamera(String cameraId, Object updates) {
        return updateDevice("camera", cameraId, updates, Camera.class);
    }

    /**
     * Update light settings
     * 
     * @param lightId The light ID
     * @param updates Map of settings to update
     * @return CompletableFuture with updated Light
     */
    public CompletableFuture<Light> updateLight(String lightId, Object updates) {
        return updateDevice("light", lightId, updates, Light.class);
    }

    /**
     * Update sensor settings
     * 
     * @param sensorId The sensor ID
     * @param updates Map of settings to update
     * @return CompletableFuture with updated Sensor
     */
    public CompletableFuture<Sensor> updateSensor(String sensorId, Object updates) {
        return updateDevice("sensor", sensorId, updates, Sensor.class);
    }

    /**
     * Update chime settings
     * 
     * @param chimeId The chime ID
     * @param updates Map of settings to update
     * @return CompletableFuture with updated Chime
     */
    public CompletableFuture<Chime> updateChime(String chimeId, Object updates) {
        return updateDevice("chime", chimeId, updates, Chime.class);
    }

    /**
     * Update doorlock settings
     * 
     * @param doorlockId The doorlock ID
     * @param updates Map of settings to update
     * @return CompletableFuture with updated Doorlock
     */
    public CompletableFuture<Doorlock> updateDoorlock(String doorlockId, Object updates) {
        return updateDevice("doorlock", doorlockId, updates, Doorlock.class);
    }

    /**
     * Update NVR settings
     * 
     * @param nvrId The NVR ID
     * @param updates Map of settings to update
     * @return CompletableFuture with updated NVR
     */
    public CompletableFuture<Nvr> updateNvr(String nvrId, Object updates) {
        return updateDevice("nvr", nvrId, updates, Nvr.class);
    }

    /**
     * Reboot a device
     * 
     * @param deviceId The device ID
     * @param modelType The model type (camera, light, sensor, etc.)
     * @return CompletableFuture that completes when reboot command is sent
     */
    public CompletableFuture<Void> rebootDevice(String modelType, String deviceId) {
        String path = modelType + "s/" + deviceId + "/reboot";
        return apiRequest(HttpMethod.POST, path, null, Void.class);
    }

    /**
     * Set camera recording mode
     * 
     * @param cameraId The camera ID
     * @param recordingMode The recording mode ("always", "motion", "never")
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Camera> setCameraRecordingMode(
            String cameraId, String recordingMode) {
        Map<String, Object> updates = Map.of("recordingSettings", Map.of("mode", recordingMode));
        return updateCamera(cameraId, updates);
    }

    /**
     * Enable/disable camera microphone
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraMicEnabled(String cameraId, boolean enabled) {
        return updateCamera(cameraId, Map.of("isMicEnabled", enabled));
    }

    /**
     * Set camera microphone volume
     * 
     * @param cameraId The camera ID
     * @param volume Volume level (0-100)
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraMicVolume(String cameraId, int volume) {
        return updateCamera(cameraId, Map.of("micVolume", volume));
    }

    /**
     * Set camera IR LED mode
     * 
     * @param cameraId The camera ID
     * @param mode IR mode ("auto", "on", "off", "autoFilterOnly")
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraIRMode(String cameraId, String mode) {
        Map<String, Object> updates = Map.of("ispSettings", Map.of("irLedMode", mode));
        return updateCamera(cameraId, updates);
    }

    /**
     * Set camera status light on/off
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable status light, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraStatusLight(String cameraId, boolean enabled) {
        Map<String, Object> updates = Map.of("ledSettings", Map.of("isEnabled", enabled));
        return updateCamera(cameraId, updates);
    }

    /**
     * Take a snapshot from camera
     * 
     * @param cameraId The camera ID
     * @return CompletableFuture with snapshot image bytes
     */
    public CompletableFuture<byte[]> getCameraSnapshot(String cameraId) {
        return ensureAuthenticated().thenCompose(v -> {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            try {
                String url = baseUrl + PRIVATE_API_PATH + "cameras/" + cameraId + "/snapshot";
                Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(DEFAULT_TIMEOUT.toMillis(),
                        TimeUnit.MILLISECONDS);
                authenticator.addAuthHeaders(request);

                request.send(new BufferingResponseListener() {
                    @Override
                    public void onComplete(Result result) {
                        if (result.isFailed()) {
                            future.completeExceptionally(
                                    new NvrException("Snapshot request failed", result.getFailure()));
                        } else {
                            future.complete(getContent());
                        }
                    }
                });
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
            return future;
        }).orTimeout(FUTURE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Set light on/off
     * 
     * @param lightId The light ID
     * @param enabled True to turn on, false to turn off
     * @return CompletableFuture with updated Light
     */
    public CompletableFuture<Light> setLight(String lightId, boolean enabled) {
        return updateLight(lightId, Map.of("isLightForceEnabled", enabled));
    }

    /**
     * Set light brightness (LED level)
     * 
     * @param lightId The light ID
     * @param brightness Brightness level (1-6)
     * @return CompletableFuture with updated Light
     */
    public CompletableFuture<Light> setLightBrightness(String lightId, int brightness) {
        Map<String, Object> updates = Map.of("lightDeviceSettings", Map.of("ledLevel", brightness));
        return updateLight(lightId, updates);
    }

    /**
     * Set light PIR sensitivity
     * 
     * @param lightId The light ID
     * @param sensitivity Sensitivity level (0-100)
     * @return CompletableFuture with updated Light
     */
    public CompletableFuture<Light> setLightPirSensitivity(String lightId, int sensitivity) {
        Map<String, Object> updates = Map.of("lightDeviceSettings", Map.of("pirSensitivity", sensitivity));
        return updateLight(lightId, updates);
    }

    /**
     * Play chime
     * 
     * @param chimeId The chime ID
     * @param volume Volume level (0-100), or null for default
     * @param repeatTimes Number of times to repeat, or null for default
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> playChime(String chimeId, Integer volume, Integer repeatTimes) {
        String path = "chimes/" + chimeId + "/play-speaker";
        Map<String, Object> body = null;
        if (volume != null && repeatTimes != null) {
            body = Map.ofEntries(Map.entry("volume", volume), Map.entry("repeatTimes", repeatTimes));
        } else if (volume != null) {
            body = Map.of("volume", volume);
        } else if (repeatTimes != null) {
            body = Map.of("repeatTimes", repeatTimes);
        }
        return apiRequest(HttpMethod.POST, path, body, Void.class);
    }

    /**
     * Set chime volume
     * 
     * @param chimeId The chime ID
     * @param volume Volume level (0-100)
     * @return CompletableFuture with updated Chime
     */
    public CompletableFuture<Chime> setChimeVolume(String chimeId, int volume) {
        return updateChime(chimeId, Map.of("volume", volume));
    }

    /**
     * Play chime buzzer (different from speaker)
     * 
     * @param chimeId The chime ID
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> playChimeBuzzer(String chimeId) {
        String path = "chimes/" + chimeId + "/play-buzzer";
        return apiRequest(HttpMethod.POST, path, null, Void.class);
    }

    /**
     * Set chime repeat times
     * 
     * @param chimeId The chime ID
     * @param repeatTimes Number of times to repeat (1-6)
     * @return CompletableFuture with updated Chime
     */
    public CompletableFuture<Chime> setChimeRepeatTimes(String chimeId, int repeatTimes) {
        return updateChime(chimeId, Map.of("repeatTimes", repeatTimes));
    }

    /**
     * Add camera to chime (pair doorbell with chime)
     * 
     * @param chimeId The chime ID
     * @param cameraId The camera ID to add
     * @return CompletableFuture with updated Chime
     */
    public CompletableFuture<Chime> addCameraToChime(String chimeId, String cameraId) {
        return getBootstrap().thenCompose(bootstrap -> {
            var chime = bootstrap.chimes.get(chimeId);
            if (chime == null || chime.cameraIds == null) {
                return CompletableFuture.failedFuture(new BadRequestException("Invalid chime ID", 400));
            }
            List<String> cameraIds = new ArrayList<>(chime.cameraIds);
            if (!cameraIds.contains(cameraId)) {
                cameraIds.add(cameraId);
            }
            return updateChime(chimeId, Map.of("cameraIds", cameraIds));
        });
    }

    /**
     * Remove camera from chime (unpair doorbell from chime)
     * 
     * @param chimeId The chime ID
     * @param cameraId The camera ID to remove
     * @return CompletableFuture with updated Chime
     */
    public CompletableFuture<Chime> removeCameraFromChime(String chimeId, String cameraId) {
        return getBootstrap().thenCompose(bootstrap -> {
            var chime = bootstrap.chimes.get(chimeId);
            if (chime == null || chime.cameraIds == null) {
                return CompletableFuture.failedFuture(new BadRequestException("Invalid chime ID", 400));
            }
            List<String> cameraIds = new ArrayList<>(chime.cameraIds);
            cameraIds.remove(cameraId);
            return updateChime(chimeId, Map.of("cameraIds", cameraIds));
        });
    }

    /**
     * Unlock doorlock
     * 
     * @param doorlockId The doorlock ID
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> unlockDoorlock(String doorlockId) {
        String path = "doorlocks/" + doorlockId + "/open";
        return apiRequest(HttpMethod.POST, path, null, Void.class);
    }

    /**
     * Lock doorlock
     * 
     * @param doorlockId The doorlock ID
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> lockDoorlock(String doorlockId) {
        String path = "doorlocks/" + doorlockId + "/close";
        return apiRequest(HttpMethod.POST, path, null, Void.class);
    }

    /**
     * Calibrate doorlock (door must be open and lock unlocked)
     * 
     * @param doorlockId The doorlock ID
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> calibrateDoorlock(String doorlockId) {
        String path = "doorlocks/" + doorlockId + "/calibrate";
        return apiRequest(HttpMethod.POST, path, Map.of("auto", true), Void.class);
    }

    /**
     * Set doorlock auto-close time
     * 
     * @param doorlockId The doorlock ID
     * @param durationSeconds Auto-close duration in seconds (0 = disabled, max 3600)
     * @return CompletableFuture with updated Doorlock
     */
    public CompletableFuture<Doorlock> setDoorlockAutoCloseTime(String doorlockId, int durationSeconds) {
        return updateDoorlock(doorlockId, Map.of("autoCloseTimeMs", durationSeconds * 1000));
    }

    /**
     * Move PTZ camera relatively
     * 
     * @param cameraId The camera ID
     * @param pan Pan position change (native steps)
     * @param tilt Tilt position change (native steps)
     * @param panSpeed Pan speed (1-100)
     * @param tiltSpeed Tilt speed (1-100)
     * @param scale Scale factor
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> ptzRelativeMove(String cameraId, float pan, float tilt, int panSpeed, int tiltSpeed,
            int scale) {
        String path = "cameras/" + cameraId + "/move";
        Map<String, Object> payload = Map.ofEntries(Map.entry("panPos", pan), Map.entry("tiltPos", tilt),
                Map.entry("panSpeed", panSpeed), Map.entry("tiltSpeed", tiltSpeed), Map.entry("scale", scale));
        Map<String, Object> body = Map.ofEntries(Map.entry("type", "relative"), Map.entry("payload", payload));
        return apiRequest(HttpMethod.POST, path, body, Void.class);
    }

    /**
     * Center PTZ camera on viewport coordinates
     * 
     * @param cameraId The camera ID
     * @param x X coordinate (0-1000, where 0=left, 500=center, 1000=right)
     * @param y Y coordinate (0-1000, where 0=top, 500=center, 1000=bottom)
     * @param z Zoom level (0-1000)
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> ptzCenter(String cameraId, int x, int y, int z) {
        String path = "cameras/" + cameraId + "/move";
        Map<String, Object> payload = Map.ofEntries(Map.entry("x", x), Map.entry("y", y), Map.entry("z", z));
        Map<String, Object> body = Map.ofEntries(Map.entry("type", "center"), Map.entry("payload", payload));
        return apiRequest(HttpMethod.POST, path, body, Void.class);
    }

    /**
     * Zoom PTZ camera
     * 
     * @param cameraId The camera ID
     * @param zoom Zoom level (native steps)
     * @param speed Zoom speed (1-100)
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> ptzZoom(String cameraId, float zoom, int speed) {
        String path = "cameras/" + cameraId + "/move";
        Map<String, Object> payload = Map.ofEntries(Map.entry("zoomPos", zoom), Map.entry("zoomSpeed", speed));
        Map<String, Object> body = Map.ofEntries(Map.entry("type", "zoom"), Map.entry("payload", payload));
        return apiRequest(HttpMethod.POST, path, body, Void.class);
    }

    /**
     * Go to PTZ preset position
     * 
     * @param cameraId The camera ID
     * @param slot Preset slot number
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> ptzGotoPreset(String cameraId, int slot) {
        String path = "cameras/" + cameraId + "/move";
        Map<String, Object> payload = Map.of("slot", slot);
        Map<String, Object> body = Map.ofEntries(Map.entry("type", "goto"), Map.entry("payload", payload));
        return apiRequest(HttpMethod.POST, path, body, Void.class);
    }

    /**
     * Create PTZ preset at current position
     * 
     * @param cameraId The camera ID
     * @param slot Preset slot number
     * @param name Preset name
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> ptzCreatePreset(String cameraId, int slot, String name) {
        String path = "cameras/" + cameraId + "/ptz/preset";
        return apiRequest(HttpMethod.POST, path, Map.ofEntries(Map.entry("slot", slot), Map.entry("name", name)),
                Void.class);
    }

    /**
     * Delete PTZ preset
     * 
     * @param cameraId The camera ID
     * @param slot Preset slot number
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> ptzDeletePreset(String cameraId, int slot) {
        String path = "cameras/" + cameraId + "/ptz/preset/" + slot;
        return apiRequest(HttpMethod.DELETE, path, null, Void.class);
    }

    /**
     * Set PTZ home position to current position
     * 
     * @param cameraId The camera ID
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> ptzSetHome(String cameraId) {
        String path = "cameras/" + cameraId + "/ptz/home";
        return apiRequest(HttpMethod.POST, path, null, Void.class);
    }

    /**
     * Enable/disable motion detection
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraMotionDetection(String cameraId, boolean enabled) {
        Map<String, Object> updates = Map.of("recordingSettings", Map.of("enableMotionDetection", enabled));
        return updateCamera(cameraId, updates);
    }

    /**
     * Set camera to use global recording settings
     * 
     * @param cameraId The camera ID
     * @param useGlobal True to use global settings, false for per-camera settings
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraUseGlobal(String cameraId, boolean useGlobal) {
        return updateCamera(cameraId, Map.of("useGlobal", useGlobal));
    }

    /**
     * Set camera HDR mode
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable HDR, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraHDR(String cameraId, boolean enabled) {
        return updateCamera(cameraId, Map.of("hdrMode", enabled));
    }

    /**
     * Set camera video mode
     * 
     * @param cameraId The camera ID
     * @param videoMode Video mode ("default", "highFps", "hdr")
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraVideoMode(String cameraId, String videoMode) {
        return updateCamera(cameraId, Map.of("videoMode", videoMode));
    }

    /**
     * Set camera optical zoom level
     * 
     * @param cameraId The camera ID
     * @param zoomLevel Zoom level (0-100)
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraZoom(String cameraId, int zoomLevel) {
        Map<String, Object> updates = Map.of("ispSettings", Map.of("zoomPosition", zoomLevel));
        return updateCamera(cameraId, updates);
    }

    /**
     * Set camera WDR (Wide Dynamic Range) level
     * 
     * @param cameraId The camera ID
     * @param wdrLevel WDR level (0-3)
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraWDR(String cameraId, int wdrLevel) {
        Map<String, Object> updates = Map.of("ispSettings", Map.of("wdr", wdrLevel));
        return updateCamera(cameraId, updates);
    }

    /**
     * Set camera speaker volume
     * 
     * @param cameraId The camera ID
     * @param volume Volume level (0-100)
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setCameraSpeakerVolume(String cameraId, int volume) {
        Map<String, Object> updates = Map.of("speakerSettings", Map.of("volume", volume));
        return updateCamera(cameraId, updates);
    }

    /**
     * Set doorbell ring volume
     * 
     * @param cameraId The camera ID (must be doorbell)
     * @param volume Volume level (0-100)
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setDoorbellRingVolume(String cameraId, int volume) {
        Map<String, Object> updates = Map.of("speakerSettings", Map.of("ringVolume", volume));
        return updateCamera(cameraId, updates);
    }

    /**
     * Set doorbell chime duration
     * 
     * @param cameraId The camera ID (must be doorbell)
     * @param durationMs Chime duration in milliseconds
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setDoorbellChimeDuration(String cameraId, int durationMs) {
        return updateCamera(cameraId, Map.of("chimeDuration", durationMs));
    }

    /**
     * Enable/disable person smart detection
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setPersonDetection(String cameraId, boolean enabled) {
        return setSmartDetectType(cameraId, "person", enabled);
    }

    /**
     * Enable/disable vehicle smart detection
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setVehicleDetection(String cameraId, boolean enabled) {
        return setSmartDetectType(cameraId, "vehicle", enabled);
    }

    /**
     * Enable/disable face smart detection
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setFaceDetection(String cameraId, boolean enabled) {
        return setSmartDetectType(cameraId, "face", enabled);
    }

    /**
     * Enable/disable license plate smart detection
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setLicensePlateDetection(String cameraId, boolean enabled) {
        return setSmartDetectType(cameraId, "licensePlate", enabled);
    }

    /**
     * Enable/disable package smart detection
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setPackageDetection(String cameraId, boolean enabled) {
        return setSmartDetectType(cameraId, "package", enabled);
    }

    /**
     * Enable/disable animal smart detection
     * 
     * @param cameraId The camera ID
     * @param enabled True to enable, false to disable
     * @return CompletableFuture with updated Camera
     */
    public CompletableFuture<Camera> setAnimalDetection(String cameraId, boolean enabled) {
        return setSmartDetectType(cameraId, "animal", enabled);
    }

    /**
     * Helper method to set smart detection types
     */
    private CompletableFuture<Camera> setSmartDetectType(String cameraId, String detectType, boolean enabled) {
        return getBootstrap().thenCompose(bootstrap -> {
            var camera = bootstrap.cameras.get(cameraId);
            if (camera == null || camera.smartDetectSettings == null
                    || camera.smartDetectSettings.objectTypes == null) {
                return CompletableFuture.failedFuture(
                        new BadRequestException("Camera not found or doesn't support smart detection", 400));
            }
            // Convert string to enum
            SmartDetectObjectType enumType;
            switch (detectType.toLowerCase(Locale.ROOT)) {
                case "person":
                    enumType = SmartDetectObjectType.PERSON;
                    break;
                case "vehicle":
                    enumType = SmartDetectObjectType.VEHICLE;
                    break;
                case "package":
                    enumType = SmartDetectObjectType.PACKAGE;
                    break;
                case "animal":
                    enumType = SmartDetectObjectType.ANIMAL;
                    break;
                case "licenseplate":
                    enumType = SmartDetectObjectType.LICENSE_PLATE;
                    break;
                case "face":
                    enumType = SmartDetectObjectType.FACE;
                    break;
                default:
                    return CompletableFuture
                            .failedFuture(new BadRequestException("Unknown detection type: " + detectType, 400));
            }

            List<org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType> objectTypes = new ArrayList<>(
                    camera.smartDetectSettings.objectTypes);
            if (enabled && !objectTypes.contains(enumType)) {
                objectTypes.add(enumType);
            } else if (!enabled) {
                objectTypes.remove(enumType);
            }
            Map<String, Object> updates = Map.of("smartDetectSettings", Map.of("objectTypes", objectTypes));
            return updateCamera(cameraId, updates);
        });
    }

    /**
     * Adopt/manage a device
     * 
     * @param modelType The model type (camera, light, sensor, etc.)
     * @param deviceId The device ID
     * @return CompletableFuture that completes when device is adopted
     */
    public CompletableFuture<Void> adoptDevice(String modelType, String deviceId) {
        String path = "devices/adopt";
        Map<String, Object> body = Map.of(modelType + "s", Map.of(deviceId, Map.of()));
        return apiRequest(HttpMethod.POST, path, body, Void.class);
    }

    /**
     * Unadopt/unmanage a device
     * 
     * @param modelType The model type (camera, light, sensor, etc.)
     * @param deviceId The device ID
     * @return CompletableFuture that completes when device is unadopted
     */
    public CompletableFuture<Void> unadoptDevice(String modelType, String deviceId) {
        String path = modelType + "s/" + deviceId;
        return apiRequest(HttpMethod.DELETE, path, null, Void.class);
    }

    /**
     * Enable/disable SSH access on device
     * 
     * @param modelType The model type (camera, light, sensor, etc.)
     * @param deviceId The device ID
     * @param enabled True to enable SSH, false to disable
     * @return CompletableFuture with updated device
     */
    public <T> CompletableFuture<T> setDeviceSSH(String modelType, String deviceId, boolean enabled,
            Class<T> responseType) {
        return updateDevice(modelType, deviceId, Map.of("isSshEnabled", enabled), responseType);
    }

    /**
     * Clear tamper flag on sensor
     * 
     * @param sensorId The sensor ID
     * @return CompletableFuture that completes when command is sent
     */
    public CompletableFuture<Void> clearSensorTamper(String sensorId) {
        String path = "sensors/" + sensorId + "/clear-tamper-flag";
        return apiRequest(HttpMethod.POST, path, null, Void.class);
    }

    /**
     * Set light mode and settings
     * 
     * @param lightId The light ID
     * @param mode Light mode ("off", "motion", "dark", "always")
     * @param enableAt When to enable ("sunrise", "sunset", null for always)
     * @return CompletableFuture with updated Light
     */
    public CompletableFuture<Light> setLightMode(String lightId, String mode, @Nullable String enableAt) {
        Map<String, Object> lightModeSettings = enableAt == null ? Map.of("mode", mode)
                : Map.ofEntries(Map.entry("mode", mode), Map.entry("enableAt", enableAt));
        Map<String, Object> updates = Map.of("lightModeSettings", lightModeSettings);
        return updateLight(lightId, updates);
    }

    /**
     * Set light PIR duration (how long light stays on after motion)
     * 
     * @param lightId The light ID
     * @param durationMs Duration in milliseconds (15000-900000, i.e., 15s-900s)
     * @return CompletableFuture with updated Light
     */
    public CompletableFuture<Light> setLightDuration(String lightId, int durationMs) {
        Map<String, Object> updates = Map.of("lightDeviceSettings", Map.of("pirDuration", durationMs));
        return updateLight(lightId, updates);
    }

    /**
     * Close the client
     */
    public void close() {
        logger.debug("Closing UniFi Protect client");

        // Stop periodic bootstrap refresh
        stopPeriodicBootstrapRefresh();

        if (webSocket != null) {
            webSocket.disconnect();
        }
    }

    /**
     * List all API keys for a user
     *
     * @param userId The user ID
     * @return CompletableFuture with list of API keys
     */
    public CompletableFuture<List<ApiKey>> listApiKeys(String userId) {
        return directApiRequest(HttpMethod.GET, "/proxy/users/api/v2/user/" + userId + "/keys", null,
                ApiKeyResponse.class).thenApply(response -> {
                    if (response.data instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.data;
                        return dataList.stream().map(map -> {
                            ApiKey key = JsonUtil.getGson().fromJson(JsonUtil.getGson().toJsonTree(map), ApiKey.class);
                            if (key == null) {
                                throw new NvrException("Failed to parse API key from response");
                            }
                            return key;
                        }).collect(Collectors.toList());
                    }
                    return List.of();
                });
    }

    /**
     * Create a new API key
     *
     * @param userId The user ID
     * @param name The name for the API key
     * @return CompletableFuture with the created API key (includes full_api_key)
     */
    public CompletableFuture<ApiKey> createApiKey(String userId, String name) {
        Map<String, String> body = Map.of("name", name);
        return directApiRequest(HttpMethod.POST, "/proxy/users/api/v2/user/" + userId + "/keys", body,
                ApiKeyResponse.class).thenApply(response -> {
                    if (response.data instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> dataMap = (Map<String, Object>) response.data;
                        ApiKey key = JsonUtil.getGson().fromJson(JsonUtil.getGson().toJsonTree(dataMap), ApiKey.class);
                        if (key == null) {
                            throw new NvrException("Failed to parse API key from response");
                        }
                        return key;
                    }
                    throw new NvrException("Failed to create API key: unexpected response format");
                });
    }

    /**
     * Delete an API key
     *
     * @param keyId The API key ID to delete
     * @return CompletableFuture that completes when the key is deleted
     */
    public CompletableFuture<@Nullable Void> deleteApiKey(String keyId) {
        return directApiRequest(HttpMethod.DELETE, "/proxy/users/api/v2/keys/" + keyId, null, Object.class)
                .thenApply(v -> null);
    }

    /**
     * Get the current user's ID from login response
     *
     * @return CompletableFuture with the user ID
     */
    public CompletableFuture<String> getCurrentUserId() {
        return CompletableFuture.completedFuture(authenticator.getUserId());
    }

    /**
     * Find an API key by name
     *
     * @param userId The user ID
     * @param name The key name to search for
     * @return CompletableFuture with the API key if found, null otherwise
     */
    public CompletableFuture<@Nullable ApiKey> findApiKeyByName(String userId, String name) {
        return listApiKeys(userId).thenApply(keys -> {
            return keys.stream().filter(k -> name.equals(k.name)).findFirst().orElse(null);
        });
    }

    /**
     * Get or create an API key with the specified name
     * If a key with the name exists, it will be deleted and recreated
     *
     * @param userId The user ID
     * @param name The key name
     * @return CompletableFuture with the API key (includes full_api_key)
     */
    public CompletableFuture<org.openhab.binding.unifiprotect.internal.api.priv.dto.system.ApiKey> getOrCreateApiKey(
            String userId, String name) {
        return findApiKeyByName(userId, name).thenCompose(existingKey -> {
            if (existingKey != null) {
                logger.debug("Found existing API key '{}', deleting and recreating...", name);
                return deleteApiKey(existingKey.id).thenCompose(v -> createApiKey(userId, name));
            } else {
                logger.debug("Creating new API key '{}'", name);
                return createApiKey(userId, name);
            }
        });
    }

    /**
     * List all user certificates
     *
     * @return CompletableFuture with list of user certificates
     */
    public CompletableFuture<List<UserCertificate>> listUserCertificates() {
        return directApiRequest(HttpMethod.GET, "/api/userCertificates", null, UserCertificate[].class)
                .thenApply(array -> {
                    if (array != null) {
                        return List.of(array);
                    }
                    return List.of();
                });
    }

    /**
     * Create a new user certificate, after creating, you then need to activate the certificate by calling
     * updateUserCertificate with the certificate ID and active=true
     *
     * @param name The name for the certificate
     * @param certificateContent The certificate content (PEM format)
     * @param keyContent The private key content (PEM format)
     * @return CompletableFuture with the created certificate
     */
    public CompletableFuture<UserCertificate> createUserCertificate(String name, String certificateContent,
            String keyContent) {
        Map<String, String> body = Map.of("name", name, "certificate", certificateContent, "key", keyContent);
        return directApiRequest(HttpMethod.POST, "/api/userCertificates", body, UserCertificate.class);
    }

    /**
     * Get a single user certificate status
     *
     * @param certificateId The certificate ID
     * @return CompletableFuture with the certificate
     */
    public CompletableFuture<UserCertificate> getUserCertificate(String certificateId) {
        return directApiRequest(HttpMethod.GET, "/api/userCertificates/" + certificateId + "/status", null,
                UserCertificate.class);
    }

    /**
     * Update a user certificate (e.g., to activate/deactivate)
     *
     * @param certificateId The certificate ID
     * @param certificate The updated certificate object
     * @return CompletableFuture with the updated certificate
     */
    public CompletableFuture<UserCertificate> updateUserCertificate(String certificateId, UserCertificate certificate) {
        return directApiRequest(HttpMethod.PUT, "/api/userCertificates/" + certificateId + "/status", certificate,
                UserCertificate.class);
    }

    /**
     * Set a user certificate active/inactive status
     *
     * @param certificateId The certificate ID
     * @param active True to activate, false to deactivate
     * @return CompletableFuture with the updated certificate
     */
    public CompletableFuture<UserCertificate> setUserCertificateActive(String certificateId, boolean active) {
        return getUserCertificate(certificateId).thenCompose(cert -> {
            cert.active = active;
            return updateUserCertificate(certificateId, cert);
        });
    }

    /**
     * Delete a user certificate
     *
     * @param certificateId The certificate ID to delete
     * @return CompletableFuture that completes when the certificate is deleted
     */
    public CompletableFuture<@Nullable Void> deleteUserCertificate(String certificateId) {
        return directApiRequest(HttpMethod.DELETE, "/api/userCertificates/" + certificateId + "/status", null,
                Object.class).thenApply(v -> null);
    }
}
