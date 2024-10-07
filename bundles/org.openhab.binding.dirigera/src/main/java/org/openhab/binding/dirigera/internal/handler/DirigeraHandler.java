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
package org.openhab.binding.dirigera.internal.handler;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.DirigeraConfiguration;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.binding.dirigera.internal.dto.CodeResponse;
import org.openhab.binding.dirigera.internal.dto.TokenResponse;
import org.openhab.binding.dirigera.internal.exception.ApiMissingException;
import org.openhab.binding.dirigera.internal.exception.IpAdressMissingException;
import org.openhab.binding.dirigera.internal.exception.TokenMissingException;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.binding.dirigera.internal.network.RestAPI;
import org.openhab.binding.dirigera.internal.network.Websocket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link DirigeraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DirigeraHandler extends BaseBridgeHandler implements Gateway {
    private final Logger logger = LoggerFactory.getLogger(DirigeraHandler.class);

    public static final Gson GSON = new Gson();

    private Optional<Websocket> websocket = Optional.empty();
    private Optional<RestAPI> api = Optional.empty();
    private Optional<Model> model = Optional.empty();

    private final TreeMap<String, BaseDeviceHandler> deviceTree = new TreeMap<>();
    private final DirigeraDiscoveryManager discoveryManager;
    private DirigeraConfiguration config;
    private Storage<String> storage;
    private HttpClient httpClient;

    public DirigeraHandler(Bridge bridge, HttpClient insecureClient, Storage<String> bindingStorage,
            DirigeraDiscoveryManager discoveryManager) {
        super(bridge);
        this.httpClient = insecureClient;
        this.storage = bindingStorage;
        this.discoveryManager = discoveryManager;
        config = new DirigeraConfiguration();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        // do this asynchronous in case of token and other parameters needs to be obtained via Rest API calls
        scheduler.execute(this::doInitialize);
    }

    private void doInitialize() {
        config = getConfigAs(DirigeraConfiguration.class);
        // Step 1 - IP is configured or detected by discovery
        if (config.ipAddress.isBlank()) {
            Map<String, String> properties = editProperties();
            String ipAddress = properties.get(PROPERTY_IP_ADDRESS);
            if (ipAddress != null) {
                logger.info("DIRIGERA HANDLER update config with ip {}", ipAddress);
                Configuration config = editConfiguration();
                config.put("ip", ipAddress);
                updateConfiguration(config);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "No IP configured or detected");
                logger.info("DIRIGERA HANDLER no ip in properties found");
                return; // no IP - no running system
            }
        } else {
            logger.info("DIRIGERA HANDLER ip already configured {}", config.ipAddress);
        }

        // inform discovery that Gateway with ip address was found
        config = getConfigAs(DirigeraConfiguration.class); // get new config in case of previous update
        discoveryManager.foundGateway(config.ipAddress);

        // Step 2 - check if token is available or stored
        if (config.token.isBlank()) {
            boolean requestToken = true;
            // Step 2.1 - check if token is available in storage
            if (!config.id.isBlank()) {
                String storedToken = getTokenFromStorage();
                if (!storedToken.isBlank()) {
                    Configuration configUpdate = editConfiguration();
                    configUpdate.put(PROPERTY_TOKEN, storedToken);
                    updateConfiguration(configUpdate);
                    logger.info("DIRIGERA HANDLER obtained token {} from storage", storedToken);
                    requestToken = false;
                }
            }

            // Step 2.2 - if token wasn't recovered from storage begin pairing process
            config = getConfigAs(DirigeraConfiguration.class); // get new config in case of previous update
            if (requestToken) {
                String codeVerifier = generateCodeVerifier();
                String challenge = generateCodeChallenge(codeVerifier);
                String code = getCode(challenge);
                if (!code.isBlank()) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NOT_YET_READY,
                            "Press Button on DIRIGERA Gateway!");
                    try {
                        logger.info("DIRIGERA HANDLER press button on gateway");
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        logger.info("DIRIGERA HANDLER error during waiting {}", e.getMessage());
                    }
                    String token = getToken(code, codeVerifier);
                    if (!token.isBlank() && !config.id.isBlank()) {
                        storeToken(token, true);
                    }
                    logger.info("DIRIGERA HANDLER token received {}", token);

                    // store token in config
                    Configuration configUpdate = editConfiguration();
                    configUpdate.put(PROPERTY_TOKEN, token);
                    updateConfiguration(configUpdate);
                }
            }
        } else {
            logger.info("DIRIGERA HANDLER token available");
        }

        // Step 3 - ip address and token fine, now start initialzing
        if (!config.ipAddress.isBlank() && !config.token.isBlank()) {
            // looks good, ip and token fine, now create api and model
            RestAPI restAPI = new RestAPI(httpClient, config);
            api = Optional.of(restAPI);
            Model houseModel = new Model(this);
            model = Optional.of(houseModel);

            // Step 3.1 - check if id is already obtained
            if (config.id.isBlank()) {
                JSONArray gatewayArray = houseModel.getIdsForType(DEVICE_TYPE_GATEWAY);
                if (gatewayArray.isEmpty()) {
                    logger.info("DIRIGERA HANDLER no Gateway found in model");
                } else if (gatewayArray.length() > 1) {
                    logger.info("DIRIGERA HANDLER found {} Gateways - don't choose, ambigious result",
                            gatewayArray.length());
                } else {
                    Configuration configUpdate = editConfiguration();
                    configUpdate.put(PROPERTY_DEVICE_ID, gatewayArray.getString(0));
                    updateConfiguration(configUpdate);
                }
            }
            // now start websocket an listen to changes
            websocket = Optional.of(new Websocket(httpClient, config));
            websocket.get().start();
            // when done do:
            scheduler.schedule(this::discover, 0, TimeUnit.SECONDS);
            scheduler.scheduleWithFixedDelay(this::keepAlive, 5, 5, TimeUnit.MINUTES);
            scheduler.scheduleWithFixedDelay(this::updateStatistics, 5, 5, TimeUnit.MINUTES);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        websocket.ifPresent(client -> {
            client.stop();
        });
    }

    private String getTokenFromStorage() {
        logger.info("DIRIGERA HANDLER try to get token from storage");
        config = getConfigAs(DirigeraConfiguration.class);
        if (!config.id.isBlank()) {
            Object gatewayStorageObject = storage.get(config.id);
            if (gatewayStorageObject != null) {
                JSONObject gatewayStorageJson = new JSONObject(gatewayStorageObject);
                if (gatewayStorageJson.has(PROPERTY_TOKEN)) {
                    String token = gatewayStorageJson.getString(PROPERTY_TOKEN);
                    if (!token.isBlank()) {
                        // store token in config
                        Configuration configUpdate = editConfiguration();
                        configUpdate.put(PROPERTY_TOKEN, token);
                        updateConfiguration(configUpdate);
                        logger.info("DIRIGERA HANDLER obtained token {} from storage", token);
                        return token;
                    }
                }
            }
        }
        logger.info("DIRIGERA HANDLER no token in storage");
        return PROPERTY_EMPTY;
    }

    private void storeToken(String token, boolean forceStore) {
        logger.info("DIRIGERA HANDLER try to store token forced {}", forceStore);
        String storedToken = getTokenFromStorage();
        if (!forceStore && !storedToken.isBlank()) {
            logger.info("DIRIGERA HANDLER don't override current token {}", forceStore);
            return;
        }
        config = getConfigAs(DirigeraConfiguration.class);
        if (!config.id.isBlank()) {
            JSONObject tokenStore = new JSONObject();
            tokenStore.put(PROPERTY_TOKEN, token);
            storage.put(config.id, tokenStore.toString());
        } else {
            logger.info("DIRIGERA HANDLER Cannnot store token, device id missing");
        }
    }

    private void keepAlive() {
        websocket.ifPresentOrElse((client) -> {
            if (!client.isRunning()) {
                client.start();
                logger.info("DIRIGERA HANDLER WS restart necessary");
            } else {
                logger.info("DIRIGERA HANDLER WS running fine");
            }
        }, () -> {
            logger.info("DIRIGERA HANDLER WS creation necessary");
            Websocket ws = new Websocket(httpClient, config);
            ws.start();
            websocket = Optional.of(ws);
        });
    }

    private void discover() {
        api.ifPresent(api -> {
            JSONObject homeObject = api.readHome();

        });
    }

    private void updateStatistics() {
        websocket.ifPresentOrElse((socket) -> {
            JSONObject statistics = socket.getStatistics();
            updateState(CHANNEL_STATISTICS, StringType.valueOf(statistics.toString()));
        }, () -> {
            logger.info("DIRIGERA HANDLER WS not present for statistics");
        });
    }

    /**
     * Everything to handle pairing process
     */

    private String getCode(String challenge) {
        try {
            MultiMap<@Nullable String> baseParams = new MultiMap<>();
            baseParams.put("audience", "homesmart.local");
            baseParams.put("response_type", "code");
            baseParams.put("code_challenge", challenge);
            baseParams.put("code_challenge_method", "S256");
            // String urlEncoded = UrlEncoded.encode(baseParams, StandardCharsets.UTF_8, false);

            String url = String.format(OAUTH_URL, config.ipAddress);
            Request codeRequest = httpClient.newRequest(url).param("audience", "homesmart.local")
                    .param("response_type", "code").param("code_challenge", challenge)
                    .param("code_challenge_method", "S256");
            // codeRequest.content(
            // new StringContentProvider("application/x-www-form-urlencoded", urlEncoded, StandardCharsets.UTF_8));
            logger.info("DIRIGERA HANDLER Call {} with params", url);

            ContentResponse response = codeRequest.timeout(10, TimeUnit.SECONDS).send();
            logger.info("DIRIGERA HANDLER code challenge {} : {}", response.getStatus(), response.getContentAsString());
            CodeResponse codeResponse = GSON.fromJson(response.getContentAsString(), CodeResponse.class);
            logger.info("DIRIGERA HANDLER got code {}", codeResponse.code);
            return codeResponse.code;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("DIRIGERA HANDLER exception during code request {}", e.getMessage());
            return "";
        }
    }

    private String getToken(String code, String codeVerifier) {
        try {
            MultiMap<@Nullable String> baseParams = new MultiMap<>();
            baseParams.put("code", code);
            baseParams.put("name", "openHAB");
            baseParams.put("grant_type", "authorization_code");
            baseParams.put("code_verifier", codeVerifier);

            String url = String.format(TOKEN_URL, config.ipAddress);
            // url = url + "?code=" + code + "&name=" + config.ip + "&grant_type=" + "authorization_code" +
            // "&code_verifier="
            // + codeVerifier;

            // Instant stopTime = Instant.now().plus(1, ChronoUnit.MINUTES);
            int status = 200;
            // while (Instant.now().isBefore(stopTime)) {

            // url = url + "?" + UrlEncoded.encode(baseParams, StandardCharsets.UTF_8, false);

            String urlEncoded = UrlEncoded.encode(baseParams, StandardCharsets.UTF_8, false);
            Request tokenRequest = httpClient.POST(url)
                    .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .content(new StringContentProvider("application/x-www-form-urlencoded", urlEncoded,
                            StandardCharsets.UTF_8))
                    .followRedirects(true);
            logger.info("DIRIGERA HANDLER Call {} with params {}", url, urlEncoded);
            // .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
            //

            // .param("code", code).param("name", config.ip)
            // .param("code_verifier", codeVerifier).param("grant_type", "authorization_code");
            // .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");

            // Request challengeRequest = httpClient.POST(url).header(HttpHeader.CONTENT_TYPE,
            // "application/x-www-form-urlencoded");

            ContentResponse response = tokenRequest.timeout(10, TimeUnit.SECONDS).send();
            logger.info("DIRIGERA HANDLER token response {} : {}", response.getStatus(), response.getContentAsString());
            status = response.getStatus();
            if (status != 200) {
                logger.info("DIRIGERA HANDLER press button on gateway");
                Thread.sleep(1000);
                // continue;
            }
            TokenResponse tokenResponse = GSON.fromJson(response.getContentAsString(), TokenResponse.class);
            logger.info("DIRIGERA HANDLER got token {}", tokenResponse.access_token);
            return tokenResponse.access_token;
            // }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("DIRIGERA HANDLER exception fetching token {}", e.getMessage());
            return "";
        }
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("sha256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("DIRIGERA HANDLER error creating code challenge {}", e.getMessage());
            return "";
        }
    }

    private String generateCodeVerifier() {
        return StringUtils.getRandomAlphanumeric(128);
    }

    @Override
    public void registerDevice(BaseDeviceHandler deviceHandler) {
        String deviceId = deviceHandler.getId();
        discoveryManager.foundDevice(deviceId);
        deviceTree.put(deviceId, deviceHandler);
    }

    @Override
    public void unregisterDevice(BaseDeviceHandler deviceHandler) {
        String deviceId = deviceHandler.getId();
        deviceTree.remove(deviceId);
    }

    @Override
    public RestAPI getAPI() {
        if (api.isEmpty()) {
            throw new ApiMissingException("No API available yet");
        }
        return api.get();
    }

    @Override
    public DirigeraConfiguration getValidConfiguration() {
        if (config.ipAddress.isBlank()) {
            throw new IpAdressMissingException("No IP address available yet");
        }
        if (config.token.isBlank()) {
            throw new TokenMissingException("No token available yet");
        }
        return config;
    }
}
