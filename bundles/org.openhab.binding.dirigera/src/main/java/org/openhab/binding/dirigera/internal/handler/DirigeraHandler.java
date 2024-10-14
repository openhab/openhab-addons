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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
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
import org.openhab.binding.dirigera.internal.exception.ModelMissingException;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.binding.dirigera.internal.network.RestAPI;
import org.openhab.binding.dirigera.internal.network.Websocket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
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
    private Optional<ScheduledFuture<?>> modelUpdater = Optional.empty();
    private Optional<ScheduledFuture<?>> heartbeat = Optional.empty();

    private final Map<String, BaseDeviceHandler> deviceTree = new HashMap<>();
    private List<Object> knownDevices = new ArrayList<>();
    private final DirigeraDiscoveryManager discoveryManager;
    private DirigeraConfiguration config;
    private Storage<String> storage;
    private HttpClient httpClient;
    private String token = PROPERTY_EMPTY;

    public DirigeraHandler(Bridge bridge, HttpClient insecureClient, Storage<String> bindingStorage,
            DirigeraDiscoveryManager discoveryManager) {
        super(bridge);
        logger.info("DIRIGERA HANDLER onstructor");
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
        config = getConfigAs(DirigeraConfiguration.class); // get new config in case of previous update

        // for discovery known device are stored in storage in order not to report them again and again through
        // DiscoveryService
        getKnownDevicesFromStorage();

        // get token from storage
        token = getTokenFromStorage();
        if (token.isBlank()) {
            logger.warn("DIRIGERA HANDLER no token in storage");
        } else {
            logger.info("DIRIGERA HANDLER obtained token {} from storage", token);
        }

        // Step 2 - check if token is available or stored
        if (token.isBlank()) {
            // Step 2.2 - if token wasn't recovered from storage begin pairing process
            String codeVerifier = generateCodeVerifier();
            String challenge = generateCodeChallenge(codeVerifier);
            String code = getCode(challenge);
            if (!code.isBlank()) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NOT_YET_READY, "Press Button on DIRIGERA Gateway!");
                Instant stopAuth = Instant.now().plusSeconds(180); // 3 mins possible to push button
                while (Instant.now().isBefore(stopAuth) && token.isBlank()) {
                    try {
                        logger.info("DIRIGERA HANDLER press button on DIRIGERA gateway - wait 3 secs");
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        logger.info("DIRIGERA HANDLER error during waiting {}", e.getMessage());
                    }
                    token = getToken(code, codeVerifier);
                    if (token.isBlank()) {
                        logger.info("DIRIGERA HANDLER no token received");
                    } else {
                        logger.info("DIRIGERA HANDLER token {} received", token);
                    }
                }
                if (token.isBlank()) {
                    logger.info("DIRIGERA HANDLER pairing failed - Stop/Start bridge to initialize new pairing");
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NOT_YET_READY,
                            "Pairing failed. Stop and start bridge to initialize new pairing.");
                    return;
                }

                if (!token.isBlank() && !config.id.isBlank()) {
                    storeToken(token, true);
                    // store token in config
                    Configuration configUpdate = editConfiguration();
                    configUpdate.put(PROPERTY_TOKEN, token);
                    updateConfiguration(configUpdate);
                } else {
                    logger.info("DIRIGERA HANDLER token {} received for id {}", token, config.id);
                }
            }
        } else {
            logger.info("DIRIGERA HANDLER token available");
        }

        // Step 3 - ip address and token fine, now start initializing
        logger.info("DIRIGERA HANDLER Step 3 with {}", config.toString());
        if (!config.ipAddress.isBlank() && !token.isBlank()) {
            // looks good, ip and token fine, now create api and model
            RestAPI restAPI = new RestAPI(httpClient, this);
            api = Optional.of(restAPI);
            Model houseModel = new Model(this);
            model = Optional.of(houseModel);
            houseModel.update();
            // Step 3.1 - check if id is already obtained
            if (config.id.isBlank()) {
                JSONArray gatewayArray = houseModel.getIdsForType(DEVICE_TYPE_GATEWAY);
                logger.info("DIRIGERA HANDLER try to get gateway id {}", gatewayArray);
                if (gatewayArray.isEmpty()) {
                    logger.info("DIRIGERA HANDLER no Gateway found in model");
                } else if (gatewayArray.length() > 1) {
                    logger.info("DIRIGERA HANDLER found {} Gateways - don't choose, ambigious result",
                            gatewayArray.length());
                } else {
                    logger.info("DIRIGERA HANDLER try found id {}", gatewayArray.getString(0));
                    Configuration configUpdate = editConfiguration();
                    configUpdate.put(PROPERTY_DEVICE_ID, gatewayArray.getString(0));
                    updateConfiguration(configUpdate);

                    // now we've ip, token and id so let's store the token
                    storeToken(token, true);
                }
            }
            // now start websocket an listen to changes
            websocket = Optional.of(new Websocket(this, httpClient));
            websocket.get().start();
            // when done do:
            modelUpdater = Optional.of(scheduler.scheduleWithFixedDelay(this::update, 5, 5, TimeUnit.MINUTES));
            heartbeat = Optional.of(scheduler.scheduleWithFixedDelay(this::heartbeat, 1, 1, TimeUnit.MINUTES));
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        heartbeat.ifPresent(refresher -> {
            refresher.cancel(false);
        });
        modelUpdater.ifPresent(refresher -> {
            refresher.cancel(false);
        });
        websocket.ifPresent(client -> {
            client.dispose();
        });
    }

    private String getTokenFromStorage() {
        logger.info("DIRIGERA HANDLER try to get token from storage");
        config = getConfigAs(DirigeraConfiguration.class);
        if (!config.id.isBlank()) {
            JSONObject gatewayStorageJson = getStorageJson();
            if (gatewayStorageJson.has(PROPERTY_TOKEN)) {
                String token = gatewayStorageJson.getString(PROPERTY_TOKEN);
                if (!token.isBlank()) {
                    return token;
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
            logger.info("DIRIGERA HANDLER write into store {}", tokenStore.toString());
            storage.put(config.id, tokenStore.toString());
        } else {
            logger.info("DIRIGERA HANDLER Cannnot store token, device id missing");
        }
    }

    private void getKnownDevicesFromStorage() {
        JSONObject gatewayStorageJson = getStorageJson();
        if (gatewayStorageJson.has(PROPERTY_DEVICES)) {
            String knownDeviceString = gatewayStorageJson.getString(PROPERTY_DEVICES);
            logger.info("DIRIGERA HANDLER storage deliverd {} known devices", knownDeviceString);
            JSONArray arr = new JSONArray(knownDeviceString);
            knownDevices = arr.toList();
            logger.info("DIRIGERA HANDLER storage deliverd {} known devices", knownDevices.size());
        } else {
            logger.info("DIRIGERA HANDLER no known devices stored");
        }
    }

    private void storeKnownDevices() {
        JSONObject gatewayStorageJson = getStorageJson();
        JSONArray toStoreArray = new JSONArray(knownDevices);
        logger.info("DIRIGERA HANDLER store {} known devices", knownDevices.size());
        gatewayStorageJson.put(PROPERTY_DEVICES, toStoreArray.toString());
        logger.info("DIRIGERA HANDLER want to write {}", gatewayStorageJson);
        storage.put(config.id, gatewayStorageJson.toString());
    }

    private JSONObject getStorageJson() {
        logger.info("DIRIGERA HANDLER get storage for {}", config.id);
        config = getConfigAs(DirigeraConfiguration.class);
        if (!config.id.isBlank()) {
            String gatewayStorageObject = storage.get(config.id);
            logger.info("DIRIGERA HANDLER get storage contains {}", gatewayStorageObject);
            if (gatewayStorageObject != null) {
                JSONObject gatewayStorageJson = new JSONObject(gatewayStorageObject.toString());
                logger.info("DIRIGERA HANDLER get storage for {} delivered {}", config.id,
                        gatewayStorageJson.toString());
                return gatewayStorageJson;
            }
        }
        logger.info("DIRIGERA HANDLER nothing found in storage for Gateway {}", config.id);
        return new JSONObject();
    }

    private void heartbeat() {
        JSONObject gatewayInfo = api().readDevice(config.id);
        if (gatewayInfo.has(PROPERTY_HTTP_ERROR_STATUS)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Gateway HTTP Status " + gatewayInfo.get(PROPERTY_HTTP_ERROR_STATUS));
        } else {
            updateStatus(ThingStatus.ONLINE);
            websocket.ifPresentOrElse((client) -> {
                if (!client.isRunning()) {
                    logger.info("DIRIGERA HANDLER WS restart necessary");
                    client.start();
                } else {
                    logger.info("DIRIGERA HANDLER WS running fine");
                    client.ping();
                }
            }, () -> {
                logger.info("DIRIGERA HANDLER WS creation necessary");
                Websocket ws = new Websocket(this, httpClient);
                ws.start();
                websocket = Optional.of(ws);
            });
        }
    }

    private void update() {
        model().update();
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

            String url = String.format(OAUTH_URL, config.ipAddress);
            Request codeRequest = httpClient.newRequest(url).param("audience", "homesmart.local")
                    .param("response_type", "code").param("code_challenge", challenge)
                    .param("code_challenge_method", "S256");
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

            // Instant stopTime = Instant.now().plus(1, ChronoUnit.MINUTES);
            int status = 200;

            String urlEncoded = UrlEncoded.encode(baseParams, StandardCharsets.UTF_8, false);
            Request tokenRequest = httpClient.POST(url)
                    .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .content(new StringContentProvider("application/x-www-form-urlencoded", urlEncoded,
                            StandardCharsets.UTF_8))
                    .followRedirects(true);

            ContentResponse response = tokenRequest.timeout(10, TimeUnit.SECONDS).send();
            logger.info("DIRIGERA HANDLER token response {} : {}", response.getStatus(), response.getContentAsString());
            status = response.getStatus();
            if (status != 200) {
                return "";
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

    /**
     * register a running device
     */
    @Override
    public void registerDevice(BaseDeviceHandler deviceHandler) {
        logger.info("DIRIGERA HANDLER device {} regsitered", deviceHandler.getThing().getThingTypeUID());
        String deviceId = deviceHandler.getId();
        if (!deviceId.isBlank()) {
            // if id isn't known yet - store it
            if (!knownDevices.contains(deviceId)) {
                knownDevices.add(deviceId);
                storeKnownDevices();
            }
        } else {
            logger.info("DIRIGERA HANDLER device {} regsitered without id", deviceHandler.getThing().getThingTypeUID());
        }
        deviceTree.put(deviceId, deviceHandler);
    }

    /**
     * unregister device, not running but still available
     */
    @Override
    public void unregisterDevice(BaseDeviceHandler deviceHandler) {
        String deviceId = deviceHandler.getId();
        deviceTree.remove(deviceId);
        // unregister happens but don't remove it from known devices
    }

    /**
     * remove device due to removal of Handler
     */
    @Override
    public void deleteDevice(BaseDeviceHandler deviceHandler) {
        String deviceId = deviceHandler.getId();
        deviceTree.remove(deviceId);
        // removal of handler - store new known devices
        if (knownDevices.contains(deviceId)) {
            knownDevices.remove(deviceId);
            storeKnownDevices();
        }
    }

    @Override
    public void newDevice(String id) {
        if (!config.discovery) {
            // don't discover anything if not configured
            return;
        }
        if (!knownDevices.contains(id)) {
            ThingTypeUID discoveredThingTypeUID = model().identifyDevice(id);
            if (THING_TYPE_UNKNNOWN.equals(discoveredThingTypeUID)) {
                logger.warn("DIRIGERA HANDLER cannot identify {}", model().getAllFor(id));
            } else if (THING_TYPE_GATEWAY.equals(discoveredThingTypeUID)) {
                logger.warn("DIRIGERA HANDLER cannot identify {}", model().getAllFor(id));
                // ignore gateway findings
            } else {
                String customName = model().getCustonNameFor(id);
                logger.info("DIRIGERA HANDLER deliver result {} with name {} and is supported {}",
                        discoveredThingTypeUID.getAsString(), customName,
                        SUPPORTED_THING_TYPES_UIDS.contains(discoveredThingTypeUID));
                DiscoveryResult result = DiscoveryResultBuilder
                        .create(new ThingUID(discoveredThingTypeUID, this.getThing().getUID(), id))
                        .withBridge(this.getThing().getUID()).withProperty(PROPERTY_DEVICE_ID, id)
                        .withRepresentationProperty(PROPERTY_DEVICE_ID).withLabel(customName).build();
                discoveryManager.forward(result);
            }
        } else {
            logger.info("DIRIGERA HANDLER received new device id from model but already known");
        }
    }

    @Override
    public RestAPI api() {
        if (api.isEmpty()) {
            throw new ApiMissingException("No API available yet");
        }
        return api.get();
    }

    @Override
    public Model model() {
        if (model.isEmpty()) {
            throw new ModelMissingException("No Model available yet");
        }
        return model.get();
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getIpAddress() {
        return config.ipAddress;
    }

    @Override
    public void websocketUpdate(JSONObject update) {
        String type = update.getString("type");
        logger.info("DIRIGERA HANDLER update received for type {} - {}", type, update);
        switch (type) {
            case EVENT_TYPE_STATE_CHANGE:
                JSONObject data = update.getJSONObject("data");
                String targetId = data.getString("id");
                if (targetId != null) {
                    if (targetId.equals(config.id)) {
                        this.handleUpdate(data);
                    } else {
                        BaseDeviceHandler targetHandler = deviceTree.get(targetId);
                        if (targetHandler != null && targetId != null) {
                            targetHandler.handleUpdate(data);
                        } else {
                            logger.info("DIRIGERA HANDLER no targetHandler found for update {}", update);
                        }
                    }
                }
                break;
            default:
                logger.info("DIRIGERA HANDLER unkown type {} for websocket update {}", type, update);
        }
    }

    private void handleUpdate(JSONObject data) {
        // TODO Auto-generated method stub
    }
}
