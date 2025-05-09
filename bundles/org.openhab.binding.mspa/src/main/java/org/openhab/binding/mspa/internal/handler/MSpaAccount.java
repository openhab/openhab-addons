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
package org.openhab.binding.mspa.internal.handler;

import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.mspa.internal.MSpaUtils;
import org.openhab.binding.mspa.internal.config.MSpaAccountConfiguration;
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
 * The {@link MSpaAccount} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaAccount extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(MSpaAccount.class);
    private final MSpaDiscoveryService discovery;

    private MSpaAccountConfiguration config = new MSpaAccountConfiguration();
    private AccessTokenResponse token;
    private HttpClient httpClient;
    private Storage<String> store;

    public MSpaAccount(Bridge bridge, HttpClient httpClient, MSpaDiscoveryService discovery, Storage<String> store) {
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
        config = getConfigAs(MSpaAccountConfiguration.class);

        // check for configuration errors
        if (UNKNOWN.equals(config.email) || UNKNOWN.equals(config.password) || UNKNOWN.equals(config.region)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing configuration parameters");
            return;
        }

        // request token if necessary
        String tokenResponse = store.get(config.email);
        if (tokenResponse != null) {
            token = MSpaUtils.decodeStoredToken(tokenResponse);
            if (!MSpaUtils.isTokenValid(token)) {
                requestToken();
            }
        } else {
            requestToken();
        }

        // check if token is now valid
        if (MSpaUtils.isTokenValid(token)) {
            updateStatus(ThingStatus.ONLINE);
            discovery.addAccount(this);
            startDiscovery();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void dispose() {
        discovery.removeAccount(this);
    }

    public void startDiscovery() {
        Request discovery = getRequest(GET, DEVICE_LIST_ENDPOINT);
        try {
            ContentResponse cr = discovery.timeout(10, TimeUnit.SECONDS).send();
            int status = cr.getStatus();
            String response = cr.getContentAsString();
            if (status == 200) {
                logger.info("Device list {}", response);
                decodeDevices(response);
            } else {
                logger.warn("Failed to get device list - reason {}", response);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to get device list - reason {}", e.getMessage());
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
                            String deviceId = jsonEntry.getString("device_id");
                            String productId = jsonEntry.getString("product_id");
                            // replace keys to match with configuration variables
                            Map<String, Object> properties = jsonEntry.toMap();
                            properties.remove("device_id");
                            properties.remove("product_id");
                            properties.put("deviceId", deviceId);
                            properties.put("productId", productId);
                            discovery.deviceDiscovered(THING_TYPE_POOL, this.getThing().getUID(), properties);
                        }
                    }
                });
            }
        }
    }

    public void requestToken() {
        logger.info("Request token");
        Request tokenRequest = getRequest(POST, TOKEN_ENDPOINT);
        JSONObject body = new JSONObject();
        body.put("account", config.email);
        body.put("password", MSpaUtils.getMd5(config.password).toLowerCase());
        body.put("app_id", APP_IDS.get(config.region));
        body.put("registration_id", EMPTY);
        body.put("push_type", "android");
        tokenRequest.content(new StringContentProvider(body.toString(), "utf-8"));
        try {
            ContentResponse cr = tokenRequest.timeout(10000, TimeUnit.MILLISECONDS).send();
            int status = cr.getStatus();
            if (status == 200) {
                String response = cr.getContentAsString();
                token = MSpaUtils.decodeNewToken(response);
                if (MSpaUtils.isTokenValid(token)) {
                    JSONObject tokenStore = MSpaUtils.token2Json(token);
                    store.put(config.email, tokenStore.toString());
                } else {
                    logger.warn("Failed to get token - reason {}", response);
                }
            } else {
                logger.warn("Failed to get token - reason {}", cr.getReason());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to get token - reason {}", e.getMessage());
        }
    }

    public String getToken() {
        if (MSpaUtils.isTokenValid(token)) {
            if (token.isExpired(Instant.now(), 60)) {
                requestToken();
                if (MSpaUtils.isTokenValid(token)) {
                    return token.getAccessToken();
                }
            } else {
                return token.getAccessToken();
            }
        }
        return UNKNOWN;
    }

    public Request getRequest(String method, String endPoint) {
        long timestamp = Instant.now().getEpochSecond();
        String nonce = UUID.randomUUID().toString().replace("-", EMPTY);

        Request request;
        if (GET.equals(method)) {
            request = httpClient.newRequest(HOSTS.get(config.region) + endPoint);
        } else if (POST.equals(method)) {
            request = httpClient.POST(HOSTS.get(config.region) + endPoint);
        } else {
            logger.info("Request {} not supported", method);
            return httpClient.newRequest(HOSTS.get(config.region) + endPoint);
        }

        request.header("push_type", "Android");
        request.header("appid", APP_IDS.get(config.region));
        request.header("nonce", nonce);
        request.header("ts", String.valueOf(timestamp));
        request.header("sign", MSpaUtils.getSignature(nonce, timestamp, config.region));
        request.header(HttpHeader.CONTENT_TYPE, "application/json; charset=utf-8");
        request.header(HttpHeader.USER_AGENT, "okhttp/4.9.0");
        // tokenRequest.header("lan_code", "de");
        if (MSpaUtils.isTokenValid(token)) {
            request.header(HttpHeader.AUTHORIZATION, "token " + token.getAccessToken());
        }

        return request;
    }
}
