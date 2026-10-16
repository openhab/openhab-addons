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

package org.openhab.binding.tedee.internal.handler;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;
import org.openhab.binding.tedee.internal.api.TedeeApi;
import org.openhab.binding.tedee.internal.api.TedeeApiException;
import org.openhab.binding.tedee.internal.api.TedeeLock;
import org.openhab.binding.tedee.internal.configuration.TedeeBridgeConfiguration;
import org.openhab.binding.tedee.internal.discovery.TedeeDiscoveryService;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Handles the local Tedee Bridge, including API communication, connection
 * monitoring, webhook registration, and access to lock operations for child Things.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public class TedeeBridgeHandler extends BaseBridgeHandler implements TedeeTransportProvider {
    private static final String CALLBACK_PATH = "/tedee/webhook";
    private static final long CALLBACK_CHECK_INTERVAL_SECONDS = 300;
    private static final int WEBHOOK_SECRET_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final HttpClient client;
    private final String webhookSecret = generateWebhookSecret();
    private volatile @Nullable TedeeApi api;
    private volatile boolean disposed;
    private @Nullable ScheduledFuture<?> job;
    private @Nullable ScheduledFuture<?> callbackJob;
    private TedeeBridgeConfiguration cfg = new TedeeBridgeConfiguration();
    private final Logger logger = LoggerFactory.getLogger(TedeeBridgeHandler.class);

    /**
     * Creates a handler for a local Tedee Bridge.
     *
     * @param b the Tedee Bridge Thing
     * @param c the HTTP client used for API communication
     */
    public TedeeBridgeHandler(Bridge b, HttpClient c) {
        super(b);
        client = c;
    }

    /**
     * Returns the handler services provided by this bridge.
     *
     * @return the collection of supported handler service classes
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(TedeeDiscoveryService.class);
    }

    /**
     * Initializes the bridge handler, validates its configuration, creates the
     * API client, and schedules periodic health checks and webhook registration.
     */
    @Override
    public void initialize() {
        disposed = false;
        cfg = getConfigAs(TedeeBridgeConfiguration.class);
        if (cfg.ip.isBlank() || cfg.apiToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.local-bridge.configuration-required");
            return;
        }

        api = new TedeeApi(client, cfg.ip, cfg.port, cfg.apiToken);

        updateStatus(ThingStatus.UNKNOWN);

        int pollInterval = Math.max(5, cfg.pollInterval);

        scheduler.execute(this::runInitialChecks);

        job = scheduler.scheduleWithFixedDelay(this::runHealthCheck, pollInterval, pollInterval, TimeUnit.SECONDS);

        callbackJob = scheduler.scheduleWithFixedDelay(this::runCallbackCheck, CALLBACK_CHECK_INTERVAL_SECONDS,
                CALLBACK_CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private synchronized void runInitialChecks() {
        runHealthCheck();
        runCallbackCheck();
    }

    private synchronized void runHealthCheck() {
        try {
            check();
        } catch (RuntimeException e) {
            logger.warn("Unexpected error while checking Tedee Bridge status", e);
        }
    }

    private synchronized void runCallbackCheck() {
        try {
            ensureCallback();
        } catch (RuntimeException e) {
            logger.warn("Unexpected error while checking Tedee Bridge callback", e);
        }
    }

    /**
     * Indicates whether the bridge API is initialized and the bridge is online.
     *
     * @return {@code true} if the API is initialized and the bridge is online;
     *         {@code false} otherwise
     */
    @Override
    public boolean ready() {
        TedeeApi currentApi = api;
        return !disposed && currentApi != null && getThing().getStatus() == ThingStatus.ONLINE;
    }

    private TedeeApi apiOrThrow() {
        TedeeApi currentApi = api;
        if (disposed || currentApi == null) {
            throw new IllegalStateException("Tedee API is not initialized");
        }
        return currentApi;
    }

    /**
     * Retrieves information about a Tedee lock through the local bridge.
     *
     * @param id the identifier of the lock
     * @return the lock information
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public TedeeLock getLock(int id) throws TedeeApiException {
        return apiOrThrow().getLock(id);
    }

    /**
     * Retrieves the locks accessible through the local bridge.
     *
     * @return the list of accessible locks
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public List<TedeeLock> getLocks() throws TedeeApiException {
        return apiOrThrow().getLocks();
    }

    /**
     * Sends a command to lock the specified Tedee lock.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void lock(int id) throws TedeeApiException {
        apiOrThrow().lock(id);
    }

    /**
     * Sends a command to unlock the specified Tedee lock.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void unlock(int id) throws TedeeApiException {
        apiOrThrow().unlock(id);
    }

    /**
     * Sends a command to unlock the specified Tedee lock without pulling the
     * latch.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void unlockWithoutPull(int id) throws TedeeApiException {
        apiOrThrow().unlockWithoutPull(id);
    }

    /**
     * Sends a command to pull the latch of the specified Tedee lock.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void pull(int id) throws TedeeApiException {
        apiOrThrow().pull(id);
    }

    /**
     * Sends a command to unlock the specified Tedee lock or pull the latch,
     * according to the supported device operation.
     *
     * @param id the identifier of the lock
     * @throws TedeeApiException if the API request fails
     */
    @Override
    public void unlockOrPull(int id) throws TedeeApiException {
        apiOrThrow().unlockOrPull(id);
    }

    private void check() {
        TedeeApi currentApi = api;
        if (currentApi == null) {
            return;
        }

        try {
            if (currentApi.bridgeStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.local-bridge.http-error");
            }
        } catch (TedeeApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    localizedStatusDescription("status.api-communication-error", e.getMessage()));
        }
    }

    private void ensureCallback() {
        TedeeApi currentApi = api;
        if (currentApi == null) {
            return;
        }

        String callbackUrl = callbackUrl();
        if (callbackUrl == null) {
            return;
        }

        try {
            String callbacks = currentApi.getCallbacks();
            JsonElement root = JsonParser.parseString(callbacks);
            JsonArray callbackArray = callbackArray(root);

            if (callbackArray == null) {
                logger.warn("Unexpected Tedee Bridge callback response format");
                return;
            }

            boolean callbackPresent = false;

            for (JsonElement element : callbackArray) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject callback = element.getAsJsonObject();
                String url = stringProperty(callback, "url");
                String method = stringProperty(callback, "method");

                if (!isBindingCallback(url)) {
                    continue;
                }

                String callbackId = callbackId(callback);

                if (callbackUrl.equals(url) && "POST".equalsIgnoreCase(method)
                        && hasWebhookSecret(callback, webhookSecret) && !callbackPresent) {
                    callbackPresent = true;
                } else if (callbackId != null) {
                    currentApi.deleteCallback(callbackId);
                    logger.info("Removed stale Tedee callback {}", callbackId);
                }
            }

            if (!callbackPresent) {
                currentApi.addCallback(callbackUrl, webhookSecret);
                logger.info("Registered Tedee webhook callback at {}", callbackUrl);
            } else {
                logger.debug("Tedee webhook callback already registered at {}", callbackUrl);
            }
        } catch (TedeeApiException | JsonParseException | IllegalStateException e) {
            logger.warn("Failed to ensure Tedee webhook callback at {}: {}", callbackUrl, e.getMessage());
            logger.debug("Tedee webhook callback registration failure", e);
        }
    }

    private @Nullable String callbackUrl() {
        Bundle bundle = FrameworkUtil.getBundle(TedeeBridgeHandler.class);
        if (bundle == null) {
            logger.warn("Cannot determine openHAB callback URL because the OSGi bundle is unavailable");
            return null;
        }

        ServiceReference<NetworkAddressService> reference = bundle.getBundleContext()
                .getServiceReference(NetworkAddressService.class);

        if (reference == null) {
            logger.warn("Cannot determine openHAB callback URL because NetworkAddressService is unavailable");
            return null;
        }

        NetworkAddressService networkAddressService = bundle.getBundleContext().getService(reference);
        if (networkAddressService == null) {
            logger.warn("Cannot determine openHAB callback URL because NetworkAddressService is unavailable");
            return null;
        }

        @Nullable
        String host = networkAddressService.getPrimaryIpv4HostAddress();

        if (host == null || host.isBlank() || isLoopback(host)) {
            logger.warn("Cannot register Tedee webhook callback because no usable primary IPv4 address was found");
            return null;
        }

        int port = HttpServiceUtil.getHttpServicePort(bundle.getBundleContext());

        if (port < 0) {
            logger.warn(
                    "Cannot register Tedee webhook callback because the openHAB HTTP service port could not be determined");
            return null;
        }

        return "http://" + host + ":" + port + CALLBACK_PATH;
    }

    /**
     * Checks whether the local bridge API is reachable.
     *
     * @return {@code true} if the bridge responds with HTTP status 200;
     *         {@code false} if the API is not initialized or the response is unsuccessful
     * @throws TedeeApiException if the API request fails
     */
    public boolean isBridgeReachable() throws TedeeApiException {
        TedeeApi currentApi = api;
        return currentApi != null && currentApi.bridgeStatus() == 200;
    }

    private static boolean isBindingCallback(String url) {
        return url.endsWith(CALLBACK_PATH);
    }

    boolean isWebhookSecret(@Nullable String presentedSecret) {
        return !disposed && api != null && presentedSecret != null && MessageDigest.isEqual(
                webhookSecret.getBytes(StandardCharsets.UTF_8), presentedSecret.getBytes(StandardCharsets.UTF_8));
    }

    private static String generateWebhookSecret() {
        byte[] bytes = new byte[WEBHOOK_SECRET_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static boolean hasWebhookSecret(JsonObject callback, String expectedSecret) {
        JsonElement headers = callback.get("headers");
        if (headers == null || !headers.isJsonArray()) {
            return false;
        }

        for (JsonElement element : headers.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject header = element.getAsJsonObject();
            if (TedeeBindingConstants.WEBHOOK_SECRET_HEADER.equals(stringProperty(header, "header_name"))
                    && expectedSecret.equals(stringProperty(header, "header_value"))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isLoopback(String host) {
        try {
            return InetAddress.getByName(host).isLoopbackAddress();
        } catch (Exception e) {
            return true;
        }
    }

    private static @Nullable JsonArray callbackArray(JsonElement root) {
        if (root.isJsonArray()) {
            return root.getAsJsonArray();
        }

        if (!root.isJsonObject()) {
            return null;
        }

        JsonObject object = root.getAsJsonObject();
        JsonElement callbacks = object.get("callbacks");

        return callbacks != null && callbacks.isJsonArray() ? callbacks.getAsJsonArray() : null;
    }

    private static String stringProperty(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && !value.isJsonNull() ? value.getAsString() : "";
    }

    private static @Nullable String callbackId(JsonObject object) {
        JsonElement value = object.get("callbackId");

        if (value == null || value.isJsonNull()) {
            value = object.get("id");
        }

        return value != null && !value.isJsonNull() ? value.getAsString() : null;
    }

    private static String localizedStatusDescription(String key, @Nullable String detail) {
        JsonArray arguments = new JsonArray();
        arguments.add(detail == null ? "Unknown error" : detail);
        return "@text/" + key + " " + arguments;
    }

    /**
     * Handles commands received by the local bridge.
     *
     * <p>
     * The bridge does not currently expose command channels, so commands
     * received here are not processed.
     *
     * @param c the channel on which the command was received
     * @param x the command to handle
     */
    @Override
    public void handleCommand(ChannelUID c, Command x) {
    }

    /**
     * Cancels the scheduled health checks and releases the API client reference
     * when the bridge handler is disposed.
     */
    @Override
    public void dispose() {
        disposed = true;

        ScheduledFuture<?> currentJob = job;
        if (currentJob != null) {
            currentJob.cancel(true);
            job = null;
        }

        ScheduledFuture<?> currentCallbackJob = callbackJob;
        if (currentCallbackJob != null) {
            currentCallbackJob.cancel(true);
            callbackJob = null;
        }

        api = null;
        super.dispose();
    }
}
