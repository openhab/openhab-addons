/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tedee.internal.api.TedeeApi;
import org.openhab.binding.tedee.internal.api.TedeeApiException;
import org.openhab.binding.tedee.internal.api.TedeeClient;
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
 * Handler for a Tedee Bridge.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public class TedeeBridgeHandler extends BaseBridgeHandler implements TedeeTransportProvider {
    private static final String CALLBACK_PATH = "/tedee/webhook";
    private static final long CALLBACK_CHECK_INTERVAL_SECONDS = 300;

    private final HttpClient client;
    private @Nullable TedeeApi api;
    private @Nullable ScheduledFuture<?> job;
    private @Nullable ScheduledFuture<?> callbackJob;
    private TedeeBridgeConfiguration cfg = new TedeeBridgeConfiguration();
    private final Logger logger = LoggerFactory.getLogger(TedeeBridgeHandler.class);

    public TedeeBridgeHandler(Bridge b, HttpClient c) {
        super(b);
        client = c;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(TedeeDiscoveryService.class);
    }

    public void initialize() {
        cfg = getConfigAs(TedeeBridgeConfiguration.class);
        if (cfg.ip.isBlank() || cfg.apiToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "IP and API token required");
            return;
        }

        api = new TedeeApi(client, cfg.ip, cfg.port, cfg.apiToken);

        scheduler.execute(this::ensureCallback);

        callbackJob = scheduler.scheduleWithFixedDelay(this::ensureCallback, 60, CALLBACK_CHECK_INTERVAL_SECONDS,
                TimeUnit.SECONDS);

        job = scheduler.scheduleWithFixedDelay(this::check, 30, 30, TimeUnit.SECONDS);
    }

    public @Nullable TedeeApi getApi() {
        return api;
    }

    @Override
    public @Nullable TedeeClient getClient() {
        return api;
    }

    public boolean ready() {
        return api != null && getThing().getStatus() == ThingStatus.ONLINE;
    }

    private void check() {
        try {
            if (api.bridgeStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge HTTP error");
            }
        } catch (TedeeApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
                logger.warn("Unexpected Tedee Bridge callback response: {}", callbacks);
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

                if (callbackUrl.equals(url) && "POST".equalsIgnoreCase(method) && !callbackPresent) {
                    callbackPresent = true;
                } else if (callbackId != null) {
                    currentApi.deleteCallback(callbackId);
                    logger.info("Removed stale Tedee callback {}", callbackId);
                }
            }

            if (!callbackPresent) {
                String response = currentApi.addCallback(callbackUrl);
                logger.info("Registered Tedee webhook callback at {}: {}", callbackUrl, response);
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

    private static boolean isBindingCallback(String url) {
        return url.endsWith(CALLBACK_PATH);
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

    public void handleCommand(ChannelUID c, Command x) {
    }

    public void dispose() {
        if (job != null) {
            job.cancel(true);
        }

        if (callbackJob != null) {
            callbackJob.cancel(true);
        }

        api = null;
        super.dispose();
    }
}
