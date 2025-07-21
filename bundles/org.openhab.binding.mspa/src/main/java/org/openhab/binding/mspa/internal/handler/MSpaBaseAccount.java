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
package org.openhab.binding.mspa.internal.handler;

import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.mspa.internal.MSpaConstants.ServiceRegion;
import org.openhab.binding.mspa.internal.MSpaUtils;
import org.openhab.binding.mspa.internal.config.MSpaOwnerAccountConfiguration;
import org.openhab.binding.mspa.internal.config.MSpaVisitorAccountConfiguration;
import org.openhab.binding.mspa.internal.discovery.MSpaDiscoveryService;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MSpaBaseAccount} abstract base class for MSpaVisitor and MSpaOwner accounts
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public abstract class MSpaBaseAccount extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(MSpaBaseAccount.class);
    private final MSpaDiscoveryService discovery;
    private HttpClient httpClient;

    protected static final String TOKEN = "accesstoken";
    protected static final String GRANTS = "grants";
    protected Optional<MSpaOwnerAccountConfiguration> ownerConfig = Optional.empty();
    protected Optional<MSpaVisitorAccountConfiguration> visitorConfig = Optional.empty();
    protected AccessTokenResponse token;
    protected Storage<String> store;

    public MSpaBaseAccount(Bridge bridge, HttpClient httpClient, MSpaDiscoveryService discovery,
            Storage<String> store) {
        super(bridge);
        this.httpClient = httpClient;
        this.discovery = discovery;
        this.store = store;
        token = MSpaUtils.getInvalidToken();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands supported
    }

    @Override
    public void initialize() {
        getToken();
        if (MSpaUtils.isTokenValid(token)) {
            updateStatus(ThingStatus.ONLINE);
            discovery.addAccount(this);
            startDiscovery();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/status.mspa.invalid-token");
        }
    }

    @Override
    public void dispose() {
        discovery.removeAccount(this);
    }

    public void startDiscovery() {
        Request discovery = getRequest(HttpMethod.GET, ENDPOINT_DEVICE_LIST);
        try {
            ContentResponse cr = discovery.timeout(10, TimeUnit.SECONDS).send();
            int status = cr.getStatus();
            String response = cr.getContentAsString();
            if (status == 200) {
                logger.trace("Device list {}", response);
                decodeDevices(response);
            } else {
                logger.warn("Failed to get device list - reason {}", response);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to get device list - reason {}", e.toString());
            handlePossibleInterrupt(e);
        }
    }

    public void decodeDevices(String response) {
        JSONObject devices = new JSONObject(response);
        if (devices.has("data")) {
            JSONObject data = devices.getJSONObject("data");
            if (data.has("list")) {
                JSONArray list = data.getJSONArray("list");
                list.forEach(entry -> {
                    if (entry instanceof JSONObject jsonEntry) {
                        // ensure necessary ids are present
                        if (jsonEntry.has("device_id") && jsonEntry.has("product_id")) {
                            Map<String, Object> properties = jsonEntry.toMap();
                            Map<String, Object> discoveryProperties = MSpaUtils.getDiscoveryProperties(properties);
                            discovery.deviceDiscovered(THING_TYPE_POOL, this.getThing().getUID(), discoveryProperties);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void handleRemoval() {
        ownerConfig.ifPresent(config -> {
            store.remove(config.email);
        });
        visitorConfig.ifPresent(config -> {
            store.remove(config.visitorId);
        });
    }

    /**
     * Gets token as String if AccessToken is valid and not expired. If not try to request a new token. Thing is set to
     * ONLINE / OFFLINE based on request result.
     *
     * @return token as String, UNKNOWN in case of unsuccessful refresh
     */
    public String getToken() {
        if (MSpaUtils.isTokenValid(token)) {
            return token.getAccessToken();
        } else {
            requestToken();
            // token shall be fine now.
            if (MSpaUtils.isTokenValid(token)) {
                return token.getAccessToken();
            } // else fall through to UNKNOWN
        }
        return UNKNOWN;
    }

    public Request getRequest(HttpMethod method, String endPoint) {
        ServiceRegion region = ServiceRegion.ROW; // default region;
        if (ownerConfig.isPresent()) {
            region = ServiceRegion.valueOf(ownerConfig.get().region);
        } else if (visitorConfig.isPresent()) {
            region = ServiceRegion.valueOf(visitorConfig.get().region);
        }
        long timestamp = Instant.now().getEpochSecond();
        String nonce = UUID.randomUUID().toString().replace("-", EMPTY);

        Request request;
        if (HttpMethod.GET.equals(method)) {
            request = httpClient.newRequest(HOSTS.get(region) + endPoint);
        } else if (HttpMethod.POST.equals(method)) {
            request = httpClient.POST(HOSTS.get(region) + endPoint);
        } else {
            return httpClient.newRequest(HOSTS.get(region) + endPoint);
        }

        request.header("push_type", "Android");
        request.header("appid", APP_IDS.get(region));
        request.header("nonce", nonce);
        request.header("ts", String.valueOf(timestamp));
        request.header("sign", MSpaUtils.getSignature(nonce, timestamp, region));
        request.header(HttpHeader.CONTENT_TYPE, "application/json; charset=utf-8");
        request.header(HttpHeader.USER_AGENT, "okhttp/4.9.0");
        if (!ENDPOINT_TOKEN.equals(endPoint) && !ENDPOINT_VISITOR.equals(endPoint)) {
            // no authorization header if token shall be requested
            request.header(HttpHeader.AUTHORIZATION, "token " + getToken());
        }
        return request;
    }

    protected JSONObject getStorage(String id) {
        String storedString = store.get(id);
        if (storedString != null) {
            try {
                JSONObject storedJson = new JSONObject(storedString);
                return storedJson;
            } catch (JSONException e) {
                logger.warn("Persistence store {} format exception {}", storedString, e.toString());
            }
        }
        return new JSONObject("{}");
    }

    protected void persist(String id, JSONObject json) {
        logger.trace("Persist {} : {}", id, json);
        store.put(id, json.toString());
    }

    protected void handlePossibleInterrupt(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    public abstract void requestToken();
}
