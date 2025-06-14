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
package org.openhab.binding.dirigera.internal.handler;

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.DirigeraCommandProvider;
import org.openhab.binding.dirigera.internal.config.DirigeraConfiguration;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryService;
import org.openhab.binding.dirigera.internal.exception.ApiException;
import org.openhab.binding.dirigera.internal.exception.ModelException;
import org.openhab.binding.dirigera.internal.interfaces.DebugHandler;
import org.openhab.binding.dirigera.internal.interfaces.DirigeraAPI;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.binding.dirigera.internal.model.DirigeraModel;
import org.openhab.binding.dirigera.internal.network.DirigeraAPIImpl;
import org.openhab.binding.dirigera.internal.network.Websocket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.StringUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DirigeraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DirigeraHandler extends BaseBridgeHandler implements Gateway, DebugHandler {

    private final Logger logger = LoggerFactory.getLogger(DirigeraHandler.class);

    // Can be overwritten by Unit test for mocking API
    protected Map<String, State> channelStateMap = new HashMap<>();
    protected Class<?> apiProvider = DirigeraAPIImpl.class;

    private final Map<String, BaseHandler> deviceTree = new HashMap<>();
    private final DirigeraDiscoveryService discoveryService;
    private final DirigeraCommandProvider commandProvider;
    private final BundleContext bundleContext;
    private final Websocket websocket;

    private Optional<DirigeraAPI> api = Optional.empty();
    private Optional<Model> model = Optional.empty();
    private Optional<ScheduledFuture<?>> watchdog = Optional.empty();
    private Optional<ScheduledFuture<?>> updater = Optional.empty();
    private Optional<ScheduledFuture<?>> detectionSchedule = Optional.empty();

    // private ScheduledExecutorService sequentialScheduler;
    private List<Object> knownDevices = new ArrayList<>();
    private ArrayList<String> websocketQueue = new ArrayList<>();
    private ArrayList<DeviceUpdate> deviceModificationQueue = new ArrayList<>();
    private DirigeraConfiguration config;
    private Storage<String> storage;
    private HttpClient httpClient;
    private String token = PROPERTY_EMPTY;
    private Instant sunriseInstant = Instant.MAX;
    private Instant sunsetInstant = Instant.MIN;
    private Instant peakRecognitionTime = Instant.MIN;
    private int websocketQueueSizePeak = 0;
    private int deviceUpdateQueueSizePeak = 0;
    private boolean updateRunning = false;
    private boolean customDebug = false;

    public static final DeviceUpdate LINK_UPDATE = new DeviceUpdate(null, "", DeviceUpdate.Action.LINKS);
    public static long detectionTimeSeonds = 5;

    public DirigeraHandler(Bridge bridge, HttpClient insecureClient, Storage<String> bindingStorage,
            DirigeraDiscoveryService discoveryManager, LocationProvider locationProvider,
            DirigeraCommandProvider commandProvider, BundleContext bundleContext) {
        super(bridge);
        this.discoveryService = discoveryManager;
        this.httpClient = insecureClient;
        this.storage = bindingStorage;
        this.commandProvider = commandProvider;
        this.bundleContext = bundleContext;
        config = new DirigeraConfiguration();
        websocket = new Websocket(this, insecureClient);

        List<CommandOption> locationOptions = new ArrayList<>();
        locationOptions.add(new CommandOption("", "Remove location"));
        PointType location = locationProvider.getLocation();
        if (location != null) {
            locationOptions.add(new CommandOption(location.toFullString(), "Home location"));
        }
        commandProvider.setCommandOptions(new ChannelUID(thing.getUID(), CHANNEL_LOCATION), locationOptions);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (customDebug) {
            logger.info("DIRIGERA HANDLER command {} : {} {}", channelUID, command.toFullString(), command.getClass());
        }
        String channel = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            State cachedState = channelStateMap.get(channel);
            if (cachedState != null) {
                super.updateState(channelUID, cachedState);
            }
        } else if (CHANNEL_PAIRING.equals(channel)) {
            JSONObject permissionAttributes = new JSONObject();
            permissionAttributes.put(PROPERTY_PERMIT_JOIN, OnOffType.ON.equals(command));
            api().sendAttributes(config.id, permissionAttributes);
        } else if (CHANNEL_LOCATION.equals(channel)) {
            PointType coordinatesPoint = null;
            if (command instanceof PointType point) {
                coordinatesPoint = point;
            } else if (command instanceof StringType string) {
                if (string.toFullString().isBlank()) {
                    String nullCoordinates = model().getTemplate(Model.TEMPLATE_NULL_COORDINATES);
                    JSONObject patchCoordinates = new JSONObject(nullCoordinates);
                    if (customDebug) {
                        logger.info("DIRIGERA HANDLER send null coordinates {}", patchCoordinates);
                    }
                    api().sendPatch(config.id, patchCoordinates);
                } else {
                    try {
                        coordinatesPoint = new PointType(string.toFullString());
                    } catch (IllegalArgumentException exception) {
                        logger.warn("DIRIGERA HANDLER wrong home location format {} : {}", string,
                                exception.getMessage());
                    }
                }
            }
            if (coordinatesPoint != null) {
                String coordinatesTemplate = model().getTemplate(Model.TEMPLATE_COORDINATES);
                String coordinates = String.format(coordinatesTemplate, coordinatesPoint.getLatitude().toFullString(),
                        coordinatesPoint.getLongitude().toFullString());
                if (customDebug) {
                    logger.info("DIRIGERA HANDLER send coordinates {}", coordinates);
                }
                api().sendPatch(config.id, new JSONObject(coordinates));
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(DirigeraConfiguration.class);
        if (config.ipAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/dirigera.device.status.missing-ip");
        } else {
            // do this asynchronous in case of token and other parameters needs to be
            // obtained via Rest API calls
            updateStatus(ThingStatus.UNKNOWN);
            scheduler.execute(this::doInitialize);
        }
    }

    private void doInitialize() {
        // for discovery known device are stored in storage in order not to report them
        // again and again through DiscoveryService
        getKnownDevicesFromStorage();
        token = getTokenFromStorage();
        // Step 1 - check if token is stored or pairing is needed
        if (token.isBlank()) {
            // if token isn't recovered from storage begin pairing process
            logger.debug("DIRIGERA HANDLER no token in storage");
            String codeVerifier = generateCodeVerifier();
            String challenge = generateCodeChallenge(codeVerifier);
            String code = getCode(challenge);
            if (!code.isBlank()) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NOT_YET_READY,
                        "@text/dirigera.gateway.status.pairing-button");
                /**
                 * Approx 3 minutes possible to push DIRIGERA button
                 */
                Instant stopAuth = Instant.now().plusSeconds(180);
                while (Instant.now().isBefore(stopAuth) && token.isBlank()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        return;
                    }
                    token = getToken(code, codeVerifier);
                    if (!token.isBlank()) {
                        logger.debug("DIRIGERA HANDLER token {} received", token);
                        storeToken(token);
                    }
                }
            }
        } else {
            logger.debug("DIRIGERA HANDLER obtained token {} from storage", token);
        }

        // Step 2 - if token is fine start initializing, else set status pairing retry
        if (!token.isBlank()) {
            // now create api and model
            try {
                DirigeraAPI apiProviderInstance = (DirigeraAPI) apiProvider
                        .getConstructor(HttpClient.class, Gateway.class).newInstance(httpClient, this);
                if (apiProviderInstance != null) {
                    api = Optional.of(apiProviderInstance);
                } else {
                    throw (new InstantiationException(apiProvider.descriptorString()));
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                // this will not happen - DirirgeraAPIIMpl tested with mocks in unit tests
                logger.error("Error {}", apiProvider.descriptorString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "@text/dirigera.gateway.status.api-error" + " [\"" + apiProvider.descriptorString() + "\"]");
                return;
            }
            Model houseModel = new DirigeraModel(this);
            model = Optional.of(houseModel);
            modelUpdate(); // initialize model
            websocket.initialize();
            // checks API access and starts websocket
            // status will be set ONLINE / OFFLINE based on the connection result
            connectGateway();
            // start watchdog to check gateway connection and start recovery if necessary
            watchdog = Optional.of(scheduler.scheduleWithFixedDelay(this::watchdog, 15, 15, TimeUnit.SECONDS));
            // infrequent updates for gateway itself
            updater = Optional.of(scheduler.scheduleWithFixedDelay(this::updateGateway, 1, 15, TimeUnit.MINUTES));
        } else {
            // status "retry pairing" if token is still blank
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NOT_YET_READY,
                    "@text/dirigera.gateway.status.pairing-retry");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        watchdog.ifPresent(watchdogSchedule -> {
            watchdogSchedule.cancel(false);
        });
        updater.ifPresent(refresher -> {
            refresher.cancel(false);
        });
        websocket.dispose();
    }

    @Override
    public void handleRemoval() {
        storage.remove(config.ipAddress);
        super.handleRemoval();
    }

    private void updateProperties() {
        Map<String, Object> propertiesMap = model().getPropertiesFor(config.id);
        TreeMap<String, String> currentProperties = new TreeMap<>(editProperties());
        propertiesMap.forEach((key, value) -> {
            currentProperties.put(key, value.toString());
        });
        updateProperties(currentProperties);
    }

    private String getTokenFromStorage() {
        JSONObject gatewayStorageJson = getStorageJson();
        if (gatewayStorageJson.has(PROPERTY_TOKEN)) {
            String token = gatewayStorageJson.getString(PROPERTY_TOKEN);
            if (!token.isBlank()) {
                return token;
            }
        }
        return PROPERTY_EMPTY;
    }

    private void storeToken(String token) {
        if (!config.ipAddress.isBlank()) {
            JSONObject tokenStore = new JSONObject();
            tokenStore.put(PROPERTY_TOKEN, token);
            storage.put(config.ipAddress, tokenStore.toString());
        }
    }

    private void getKnownDevicesFromStorage() {
        JSONObject gatewayStorageJson = getStorageJson();
        if (gatewayStorageJson.has(PROPERTY_DEVICES)) {
            String knownDeviceString = gatewayStorageJson.getString(PROPERTY_DEVICES);
            JSONArray arr = new JSONArray(knownDeviceString);
            knownDevices = arr.toList();
        }
    }

    private void storeKnownDevices() {
        JSONObject gatewayStorageJson = getStorageJson();
        JSONArray toStoreArray = new JSONArray(knownDevices);
        gatewayStorageJson.put(PROPERTY_DEVICES, toStoreArray.toString());
        storage.put(config.ipAddress, gatewayStorageJson.toString());
    }

    private JSONObject getStorageJson() {
        if (!config.ipAddress.isBlank()) {
            String gatewayStorageObject = storage.get(config.ipAddress);
            if (gatewayStorageObject != null) {
                JSONObject gatewayStorageJson = new JSONObject(gatewayStorageObject.toString());
                return gatewayStorageJson;
            }
        }
        return new JSONObject();
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

            ContentResponse response = codeRequest.timeout(10, TimeUnit.SECONDS).send();
            int responseStatus = response.getStatus();
            if (responseStatus != 200) {
                String reason = response.getReason();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/dirigera.gateway.status.comm-error" + " [\"" + responseStatus + " - " + reason + "\"]");
                return "";
            }
            String responseString = response.getContentAsString();
            JSONObject codeResponse = new JSONObject(responseString);
            String code = codeResponse.getString("code");
            return code;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/dirigera.gateway.status.comm-error" + " [\"" + e.getMessage() + "\"]");
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
            String urlEncoded = UrlEncoded.encode(baseParams, StandardCharsets.UTF_8, false);
            Request tokenRequest = httpClient.POST(url)
                    .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .content(new StringContentProvider("application/x-www-form-urlencoded", urlEncoded,
                            StandardCharsets.UTF_8))
                    .followRedirects(true).timeout(10, TimeUnit.SECONDS);

            ContentResponse response = tokenRequest.send();
            logger.debug("DIRIGERA HANDLER token response {} : {}", response.getStatus(),
                    response.getContentAsString());
            int responseStatus = response.getStatus();
            if (responseStatus != 200) {
                return "";
            }
            String responseString = response.getContentAsString();
            JSONObject tokenResponse = new JSONObject(responseString);
            String accessToken = tokenResponse.getString("access_token");
            return accessToken;
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
     * Distributing the device update towards the correct function
     */
    /**
     * Distributing the device update towards the correct function
     */
    private void doDeviceUpdate() {
        DeviceUpdate deviceUpdate = null;
        synchronized (deviceModificationQueue) {
            if (deviceUpdateQueueSizePeak < deviceModificationQueue.size()) {
                deviceUpdateQueueSizePeak = deviceModificationQueue.size();
                logger.trace("DIRIGERA HANDLER Peak device updates {}", deviceUpdateQueueSizePeak);
                peakRecognitionTime = Instant.now();
            }
            if (!deviceModificationQueue.isEmpty()) {
                deviceUpdate = deviceModificationQueue.remove(0);
                /**
                 * check if other link update is still in queue. If yes dismiss this request and
                 * wait for latest one
                 */
                if (deviceUpdate.action.equals(DeviceUpdate.Action.LINKS)
                        && deviceModificationQueue.contains(deviceUpdate)) {
                    deviceUpdate = null;
                    logger.warn("DIRIGERA HANDLER Dismiss link update, there's a later one scheduled");
                }
            }
        }
        if (deviceUpdate != null) {
            BaseHandler handler = deviceUpdate.handler;
            try {
                switch (deviceUpdate.action) {
                    case ADD:
                        if (handler != null) {
                            startUpdate();
                            doRegisterDevice(handler, deviceUpdate.deviceId);
                        }
                        break;
                    case DISPOSE:
                        if (handler != null) {
                            startUpdate();
                            doUnregisterDevice(handler, deviceUpdate.deviceId);
                        }
                        break;
                    case REMOVE:
                        if (handler != null) {
                            startUpdate();
                            doDeleteDevice(handler, deviceUpdate.deviceId);
                        }
                        break;
                    case LINKS:
                        startUpdate();
                        doUpdateLinks();
                        break;
                }
            } finally {
                endUpdate();
            }
        }
        synchronized (deviceModificationQueue) {
            if (deviceModificationQueue.isEmpty() && !Instant.MIN.equals(peakRecognitionTime)) {
                logger.trace("DIRIGERA HANDLER Peak to zero time {} ms",
                        Duration.between(peakRecognitionTime, Instant.now()).toMillis());
                peakRecognitionTime = Instant.MIN;
            }
        }
    }

    private void startUpdate() {
        synchronized (this) {
            while (updateRunning) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            updateRunning = true;
        }
    }

    private void endUpdate() {
        synchronized (this) {
            updateRunning = false;
            this.notifyAll();
        }
    }

    @Override
    public void registerDevice(BaseHandler deviceHandler, String deviceId) {
        synchronized (deviceModificationQueue) {
            deviceModificationQueue.add(new DeviceUpdate(deviceHandler, deviceId, DeviceUpdate.Action.ADD));
        }
        scheduler.execute(this::doDeviceUpdate);
    }

    /**
     * register a running device
     */
    private void doRegisterDevice(BaseHandler deviceHandler, String deviceId) {
        if (!deviceId.isBlank()) {
            // if id isn't known yet - store it
            if (!knownDevices.contains(deviceId)) {
                knownDevices.add(deviceId);
                storeKnownDevices();
            }
        }
        deviceTree.put(deviceId, deviceHandler);
    }

    @Override
    public void unregisterDevice(BaseHandler deviceHandler, String deviceId) {
        synchronized (deviceModificationQueue) {
            deviceModificationQueue.add(new DeviceUpdate(deviceHandler, deviceId, DeviceUpdate.Action.DISPOSE));
        }
        scheduler.execute(this::doDeviceUpdate);
    }

    /**
     * unregister device, not running but still available
     */
    private void doUnregisterDevice(BaseHandler deviceHandler, String deviceId) {
        // unregister from dispose but don't remove it from known devices
        deviceTree.remove(deviceId);
    }

    @Override
    public void deleteDevice(BaseHandler deviceHandler, String deviceId) {
        synchronized (deviceModificationQueue) {
            deviceModificationQueue.add(new DeviceUpdate(deviceHandler, deviceId, DeviceUpdate.Action.REMOVE));
        }
        scheduler.execute(this::doDeviceUpdate);
    }

    /**
     * Called by all device on handleRe
     */
    private void doDeleteDevice(BaseHandler deviceHandler, String deviceId) {
        deviceTree.remove(deviceId);
        // removal of handler - store known devices
        knownDevices.remove(deviceId);
        storeKnownDevices();
        // before new detection the handler needs to be removed - now were in removing
        // state
        // for complex devices several removes are done so don't trigger detection every
        // time
        detectionSchedule.ifPresentOrElse(previousSchedule -> {
            if (!previousSchedule.isDone()) {
                previousSchedule.cancel(true);
            }
            detectionSchedule = Optional
                    .of(scheduler.schedule(model.get()::detection, detectionTimeSeonds, TimeUnit.SECONDS));
        }, () -> {
            detectionSchedule = Optional
                    .of(scheduler.schedule(model.get()::detection, detectionTimeSeonds, TimeUnit.SECONDS));
        });
    }

    /**
     * Interface to Model called if device isn't found anymore
     */
    @Override
    public void deleteDevice(String deviceId) {
        BaseHandler activeHandler = deviceTree.remove(deviceId);
        if (activeHandler != null) {
            // if a handler is attached the check will fail and update the status to GONE
            activeHandler.checkHandler();
        }
        // removal of handler - store new known devices
        if (knownDevices.contains(deviceId)) {
            knownDevices.remove(deviceId);
            storeKnownDevices();
        }
    }

    @Override
    public DirigeraAPI api() throws ApiException {
        if (api.isEmpty()) {
            throw new ApiException("No API available yet");
        }
        return api.get();
    }

    @Override
    public Model model() throws ModelException {
        if (model.isEmpty()) {
            throw new ModelException("No Model available yet");
        }
        return model.get();
    }

    @Override
    public DirigeraDiscoveryService discovery() {
        return discoveryService;
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public boolean isKnownDevice(String id) {
        return knownDevices.contains(id);
    }

    @Override
    public DirigeraCommandProvider getCommandProvider() {
        return commandProvider;
    }

    @Override
    public String getIpAddress() {
        return config.ipAddress;
    }

    @Override
    public boolean discoveryEnabled() {
        return config.discovery;
    }

    @Override
    public void updateLinks() {
        synchronized (deviceModificationQueue) {
            deviceModificationQueue.add(LINK_UPDATE);
        }
        scheduler.execute(this::doDeviceUpdate);
    }

    private void doUpdateLinks() {
        // first clear start update cycle, softlinks are cleared before
        synchronized (deviceTree) {
            deviceTree.forEach((id, handler) -> {
                handler.updateLinksStart();
            });
            // then update all links
            deviceTree.forEach((id, handler) -> {
                List<String> links = handler.getLinks();
                if (!links.isEmpty()) {
                    if (customDebug) {
                        logger.info("DIRIGERA HANDLER links found for {} {}", handler.getThing().getLabel(),
                                links.size());
                    }
                }
                links.forEach(link -> {
                    // assure investigated handler is different from target handler
                    if (!id.equals(link)) {
                        BaseHandler targetHandler = deviceTree.get(link);
                        if (targetHandler != null) {
                            targetHandler.addSoftlink(id);
                        } else {
                            if (customDebug) {
                                logger.info("DIRIGERA HANDLER no targethandler found to link {} to {}", id, link);
                            }
                        }
                    }
                });
            });
            // finish update cycle so handler can update states
            deviceTree.forEach((id, handler) -> {
                handler.updateLinksDone();
            });
        }
    }

    private void watchdog() {
        // check updater is still active - maybe an exception caused termination
        updater.ifPresentOrElse(refresher -> {
            if (refresher.isDone()) {
                updater = Optional.of(scheduler.scheduleWithFixedDelay(this::updateGateway, 1, 15, TimeUnit.MINUTES));
            }
        }, () -> {
            updater = Optional.of(scheduler.scheduleWithFixedDelay(this::updateGateway, 1, 15, TimeUnit.MINUTES));
        });
        // check websocket
        if (websocket.isRunning()) {
            Map<String, Instant> pingPongMap = websocket.getPingPongMap();
            if (pingPongMap.size() > 1) { // at least 2 shall be missing before watchdog trigger
                logger.debug("DIRIGERA HANDLER Watchdog Ping Pong Panic - {} pings not answered", pingPongMap.size());
                websocket.stop();
                String message = "ping not answered";
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/dirigera.gateway.status.comm-error" + " [\"" + message + "\"]");
                scheduler.execute(this::connectGateway);
            } else {
                // good case - ping socket and check in next call for answers
                websocket.ping();
            }
        } else {
            String message = "try to recover";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/dirigera.gateway.status.comm-error" + " [\"" + message + "\"]");
            scheduler.execute(this::connectGateway);
        }
    }

    /**
     * Establish connections
     * 1) Check API response
     * 2) Start websocket and wait for positive answer
     *
     * @see websocketConnected
     */
    private void connectGateway() {
        JSONObject gatewayInfo = api().readDevice(config.id);
        // check if API call was successful, otherwise starting websocket doesn't make
        // sense
        if (!gatewayInfo.has(DirigeraAPI.HTTP_ERROR_FLAG)) {
            if (!websocket.isRunning()) {
                if (customDebug) {
                    logger.info("DIRIGERA HANDLER WS restart necessary");
                }
                websocket.start();
                // onConnect shall switch to ONLINE!
            } // else websocket is running fine
        } else {
            String message = gatewayInfo.getInt(DirigeraAPI.HTTP_ERROR_STATUS) + " - "
                    + gatewayInfo.getString(DirigeraAPI.HTTP_ERROR_MESSAGE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/dirigera.gateway.status.comm-error" + " [\"" + message + "\"]");
        }
    }

    private void updateGateway() {
        JSONObject gatewayInfo = api().readDevice(config.id);
        if (!gatewayInfo.has(DirigeraAPI.HTTP_ERROR_FLAG)) {
            handleUpdate(gatewayInfo);
        } else {
            String message = gatewayInfo.getInt(DirigeraAPI.HTTP_ERROR_STATUS) + " - "
                    + gatewayInfo.getString(DirigeraAPI.HTTP_ERROR_MESSAGE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/dirigera.gateway.status.comm-error" + " [\"" + message + "\"]");
        }
    }

    private void configureGateway() {
        if (config.id.isBlank()) {
            List<String> gatewayList = model().getDevicesForTypes(List.of(DEVICE_TYPE_GATEWAY));
            if (gatewayList.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/dirigera.gateway.status.no-gateway");
            } else if (gatewayList.size() > 1) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "@text/dirigera.gateway.status.ambiguous-gateway");
            } else {
                String id = gatewayList.get(0);
                Configuration configUpdate = editConfiguration();
                configUpdate.put(PROPERTY_DEVICE_ID, id);
                updateConfiguration(configUpdate);
                // get fresh config after update
                config = getConfigAs(DirigeraConfiguration.class);
                updateProperties();
            }
        }
    }

    @Override
    public void websocketConnected(boolean connected, String reason) {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/dirigera.gateway.status.comm-error" + " [\"" + reason + "\"]");
        }
    }

    @Override
    public void websocketUpdate(String update) {
        synchronized (websocketQueue) {
            websocketQueue.add(update);
        }
        scheduler.execute(this::doUpdate);
    }

    private void doUpdate() {
        String json = "";
        synchronized (websocketQueue) {
            if (websocketQueueSizePeak < websocketQueue.size()) {
                websocketQueueSizePeak = websocketQueue.size();
                logger.trace("DIRIGERA HANDLER Websocket update queue size peak {}", websocketQueueSizePeak);
            }
            if (!websocketQueue.isEmpty()) {
                json = websocketQueue.remove(0);
            }
        }
        if (!json.isBlank()) {
            JSONObject update;
            JSONObject data;
            String targetId;
            try {
                update = new JSONObject(json);
                data = update.getJSONObject("data");
                targetId = data.getString("id");
            } catch (JSONException exception) {
                logger.debug("DIRIGERA HANDLER cannot decode update {} {}", exception.getMessage(), json);
                return;
            }

            String type = update.getString(PROPERTY_TYPE);
            switch (type) {
                case EVENT_TYPE_SCENE_CREATED:
                case EVENT_TYPE_SCENE_DELETED:
                    if (data.has(PROPERTY_TYPE)) {
                        String dataType = data.getString(PROPERTY_TYPE);
                        if (dataType.equals(TYPE_CUSTOM_SCENE)) {
                            // don't handle custom scenes so break
                            break;
                        }
                    }
                case EVENT_TYPE_DEVICE_ADDED:
                case EVENT_TYPE_DEVICE_REMOVED:
                    // update model - it will take control on newly added, changed and removed
                    // devices
                    if (customDebug) {
                        logger.info("DIRIGERA HANDLER device added / removed {}", json);
                    }
                    modelUpdate();
                    break;
                case EVENT_TYPE_DEVICE_CHANGE:
                case EVENT_TYPE_SCENE_UPDATE:
                    if (targetId != null) {
                        if (targetId.equals(config.id)) {
                            this.handleUpdate(data);
                        } else {
                            BaseHandler targetHandler = deviceTree.get(targetId);
                            if (targetHandler != null) {
                                targetHandler.handleUpdate(data);
                            } else {
                                // special case: if custom name is changed in attributes force model update
                                // in order to present the updated name in discovery
                                if (data.has(PROPERTY_ATTRIBUTES)) {
                                    JSONObject attributes = data.getJSONObject(PROPERTY_ATTRIBUTES);
                                    if (attributes.has(Model.CUSTOM_NAME)) {
                                        if (customDebug) {
                                            logger.info("DIRIGERA HANDLER possible name change detected {}",
                                                    attributes.getString(Model.CUSTOM_NAME));
                                        }
                                        modelUpdate();
                                    }
                                }
                            }
                        }
                    }
                    break;
                default:
                    logger.debug("DIRIGERA HANDLER unkown type {} for websocket update {}", type, update);
            }
        }
    }

    /**
     * Called in 3 different situations
     * 1) initialize
     * 2) adding / removing devices or scenes
     * 3) renaming devices or scenes
     */
    private void modelUpdate() {
        Instant modelUpdateStartTime = Instant.now();
        int status = model().update();
        if (status != 200) {
            logger.warn("DIRIGERA HANDLER Model update failed {}", status);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/dirigera.gateway.status.comm-error" + " [\"" + status + "\"]");
            return;
        }
        long durationUpdateTime = Duration.between(modelUpdateStartTime, Instant.now()).toMillis();
        websocket.increase(Websocket.MODEL_UPDATES);
        websocket.getStatistics().put(Websocket.MODEL_UPDATE_TIME, durationUpdateTime + " ms");
        websocket.getStatistics().put(Websocket.MODEL_UPDATE_LAST, Instant.now());
        // updateState(new ChannelUID(thing.getUID(), CHANNEL_JSON),
        // StringType.valueOf(model().getModelString()));
        configureGateway();
        updateGateway();
    }

    private void handleUpdate(JSONObject data) {
        // websocket statistics for each update
        updateState(new ChannelUID(thing.getUID(), CHANNEL_STATISTICS),
                StringType.valueOf(websocket.getStatistics().toString()));

        if (data.has(Model.ATTRIBUTES)) {
            JSONObject attributes = data.getJSONObject(Model.ATTRIBUTES);
            // check ota for each device
            if (attributes.has(PROPERTY_CUSTOM_NAME)) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_CUSTOM_NAME),
                        StringType.valueOf(attributes.getString(PROPERTY_CUSTOM_NAME)));
            }
            if (attributes.has(PROPERTY_PERMIT_JOIN)) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_PAIRING),
                        OnOffType.from(attributes.getBoolean(PROPERTY_PERMIT_JOIN)));
            }
            if (!attributes.isNull("coordinates")) {
                JSONObject coordinates = attributes.getJSONObject("coordinates");
                if (coordinates.has("latitude") && coordinates.has("longitude")) {
                    PointType homeLocation = new PointType(
                            coordinates.getDouble("latitude") + "," + coordinates.getDouble("longitude"));
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_LOCATION), homeLocation);
                } else {
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_LOCATION), UnDefType.UNDEF);
                }
            } else {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_LOCATION), UnDefType.UNDEF);
            }
            if (attributes.has(PROPERTY_OTA_STATUS)) {
                String otaStatusString = attributes.getString(PROPERTY_OTA_STATUS);
                Integer otaStatus = OTA_STATUS_MAP.get(otaStatusString);
                if (otaStatus != null) {
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), new DecimalType(otaStatus));
                } else {
                    logger.warn("DIRIGERA HANDLER {} Cannot decode ota status {}", thing.getLabel(), otaStatusString);
                }
            }
            if (attributes.has(PROPERTY_OTA_STATE)) {
                String otaStateString = attributes.getString(PROPERTY_OTA_STATE);
                if (OTA_STATE_MAP.containsKey(otaStateString)) {
                    Integer otaState = OTA_STATE_MAP.get(otaStateString);
                    if (otaState != null) {
                        updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), new DecimalType(otaState));
                        // if ota state changes also update properties to keep firmware in thing
                        // properties up to date
                        updateProperties();
                    } else {
                        logger.debug("DIRIGERA HANDLER {} Cannot decode ota state {}", thing.getLabel(),
                                otaStateString);
                    }
                } else {
                    logger.debug("DIRIGERA HANDLER {} Cannot decode ota state {}", thing.getLabel(), otaStateString);
                }
            }
            if (attributes.has(PROPERTY_OTA_PROGRESS)) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS),
                        QuantityType.valueOf(attributes.getInt(PROPERTY_OTA_PROGRESS), Units.PERCENT));
            }
            // sunrise & sunset
            if (!attributes.isNull("nextSunRise")) {
                String sunRiseString = attributes.getString("nextSunRise");
                if (sunRiseString != null) {
                    sunriseInstant = Instant.parse(sunRiseString);
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_SUNRISE), new DateTimeType(sunriseInstant));
                }
            } else {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_SUNRISE), UnDefType.UNDEF);
            }
            if (!attributes.isNull("nextSunSet")) {
                String sunsetString = attributes.getString("nextSunSet");
                if (sunsetString != null) {
                    sunsetInstant = Instant.parse(attributes.getString("nextSunSet"));
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_SUNSET), new DateTimeType(sunsetInstant));
                }
            } else {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_SUNSET), UnDefType.UNDEF);
            }
        }
    }

    @Override
    public @Nullable Instant getSunriseDateTime() {
        if (sunriseInstant.equals(Instant.MAX)) {
            return null;
        }
        return sunriseInstant;
    }

    @Override
    public @Nullable Instant getSunsetDateTime() {
        if (sunsetInstant.equals(Instant.MIN)) {
            return null;
        }
        return sunsetInstant;
    }

    /**
     * Update cache for refresh, then update state
     */

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        channelStateMap.put(channelUID.getIdWithoutGroup(), state);
        super.updateState(channelUID, state);
    }

    /**
     * Debug commands for console access
     */

    @Override
    public String getJSON() {
        String json = api().readHome().toString();
        return json;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setDebug(boolean debug, boolean all) {
        customDebug = debug;
        if (all) {
            deviceTree.forEach((key, handler) -> {
                handler.setDebug(debug, false);
            });
        }
    }

    @Override
    public String getDeviceId() {
        return config.id;
    }
}
