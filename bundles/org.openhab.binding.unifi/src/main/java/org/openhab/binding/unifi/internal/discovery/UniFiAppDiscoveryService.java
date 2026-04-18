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
package org.openhab.binding.unifi.internal.discovery;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.unifi.api.UniFiSession;
import org.openhab.binding.unifi.handler.UniFiControllerBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Discovers which UniFi apps (Network, Protect, Access) are installed on the console behind a
 * {@code unifi:controller} bridge and emits inbox entries for the corresponding child bridges. Attached as a
 * {@link ThingHandler} service so the framework starts and stops it with the controller bridge lifecycle.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = UniFiAppDiscoveryService.class)
@NonNullByDefault
public class UniFiAppDiscoveryService extends AbstractThingHandlerDiscoveryService<UniFiControllerBridgeHandler> {

    private final Logger logger = LoggerFactory.getLogger(UniFiAppDiscoveryService.class);

    private static final ThingTypeUID ACCESS_BRIDGE = new ThingTypeUID("unifiaccess", "bridge");
    private static final ThingTypeUID PROTECT_NVR = new ThingTypeUID("unifiprotect", "nvr");
    private static final ThingTypeUID NETWORK_SITE = new ThingTypeUID("unifi", "site");

    private static final Set<ThingTypeUID> SUPPORTED_TYPES = Set.of(ACCESS_BRIDGE, PROTECT_NVR, NETWORK_SITE);
    private static final int SCAN_TIMEOUT_SECONDS = 15;
    private static final long PROBE_TIMEOUT_MS = 5_000;

    private static final String ACCESS_PROBE_PATH = "/proxy/access/api/v2/devices/topology4";
    private static final String PROTECT_PROBE_PATH = "/proxy/protect/api/bootstrap";
    private static final String NETWORK_SITES_PATH = "/proxy/network/api/self/sites";

    public UniFiAppDiscoveryService() {
        super(UniFiControllerBridgeHandler.class, SUPPORTED_TYPES, SCAN_TIMEOUT_SECONDS, true);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof UniFiControllerBridgeHandler controller) {
            this.thingHandler = controller;
        }
    }

    @Override
    protected void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        UniFiControllerBridgeHandler controller = thingHandler;
        UniFiSession session;
        try {
            session = controller.getSessionAsync().get(PROBE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.debug("Discovery skipped: controller session not available: {}", e.getMessage());
            return;
        }

        ThingUID bridgeUID = controller.getThing().getUID();
        HttpClient httpClient = controller.getHttpClient();
        String baseUrl = session.getBaseUrl();

        probeAccess(httpClient, session, baseUrl, bridgeUID);
        probeProtect(httpClient, session, baseUrl, bridgeUID);
        probeNetwork(httpClient, session, baseUrl, bridgeUID);
    }

    private void probeAccess(HttpClient httpClient, UniFiSession session, String baseUrl, ThingUID bridgeUID) {
        int status = probe(httpClient, session, baseUrl + ACCESS_PROBE_PATH, "Access");
        if (status == HttpStatus.OK_200) {
            ThingUID uid = new ThingUID(ACCESS_BRIDGE, bridgeUID, "bridge");
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                    .withThingType(ACCESS_BRIDGE).withLabel("UniFi Access (" + bridgeUID.getId() + ")").build();
            thingDiscovered(result);
        }
    }

    private void probeProtect(HttpClient httpClient, UniFiSession session, String baseUrl, ThingUID bridgeUID) {
        int status = probe(httpClient, session, baseUrl + PROTECT_PROBE_PATH, "Protect");
        if (status == HttpStatus.OK_200) {
            ThingUID uid = new ThingUID(PROTECT_NVR, bridgeUID, "nvr");
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withThingType(PROTECT_NVR)
                    .withLabel("UniFi Protect (" + bridgeUID.getId() + ")").build();
            thingDiscovered(result);
        }
    }

    private void probeNetwork(HttpClient httpClient, UniFiSession session, String baseUrl, ThingUID bridgeUID) {
        ContentResponse resp = sendAuthenticated(httpClient, session, baseUrl + NETWORK_SITES_PATH, "Network");
        if (resp == null || resp.getStatus() != HttpStatus.OK_200) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(resp.getContentAsString()).getAsJsonObject();
            JsonArray data = root.getAsJsonArray("data");
            if (data == null) {
                return;
            }
            for (JsonElement el : data) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject site = el.getAsJsonObject();
                String name = stringOrNull(site, "name");
                String desc = stringOrNull(site, "desc");
                if (name == null) {
                    continue;
                }
                ThingUID uid = new ThingUID(NETWORK_SITE, bridgeUID, name);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                        .withThingType(NETWORK_SITE).withProperties(Map.of("sid", name))
                        .withRepresentationProperty("sid")
                        .withLabel("UniFi Site: " + (desc != null && !desc.isBlank() ? desc : name)).build();
                thingDiscovered(result);
            }
        } catch (RuntimeException e) {
            logger.debug("Failed to parse Network sites response: {}", e.getMessage());
        }
    }

    private int probe(HttpClient httpClient, UniFiSession session, String url, String app) {
        ContentResponse resp = sendAuthenticated(httpClient, session, url, app);
        return resp == null ? -1 : resp.getStatus();
    }

    private @Nullable ContentResponse sendAuthenticated(HttpClient httpClient, UniFiSession session, String url,
            String app) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(PROBE_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            session.addAuthHeaders(request);
            ContentResponse resp = request.send();
            logger.debug("Probe for {} returned {}", app, resp.getStatus());
            return resp;
        } catch (Exception e) {
            logger.debug("Probe for {} failed: {}", app, e.getMessage());
            return null;
        }
    }

    private static @Nullable String stringOrNull(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) {
            return null;
        }
        return el.getAsString();
    }
}
