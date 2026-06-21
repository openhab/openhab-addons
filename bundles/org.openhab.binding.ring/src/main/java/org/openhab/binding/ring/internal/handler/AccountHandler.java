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
package org.openhab.binding.ring.internal.handler;

import static org.openhab.binding.ring.RingBindingConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.ring.internal.RestClient;
import org.openhab.binding.ring.internal.RingAccount;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.RingVideoServlet;
import org.openhab.binding.ring.internal.api.ProfileTO;
import org.openhab.binding.ring.internal.api.RingDevicesTO;
import org.openhab.binding.ring.internal.api.RingEventTO;
import org.openhab.binding.ring.internal.config.AccountConfiguration;
import org.openhab.binding.ring.internal.data.Tokens;
import org.openhab.binding.ring.internal.device.RingDevice;
import org.openhab.binding.ring.internal.discovery.RingDiscoveryService;
import org.openhab.binding.ring.internal.errors.AuthenticationException;
import org.openhab.binding.ring.internal.fcm.FcmClient;
import org.openhab.binding.ring.internal.fcm.FcmRegistrar;
import org.openhab.binding.ring.internal.utils.RingUtils;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link AccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wim Vissers - Initial contribution
 * @author Peter Mietlowski - oAuth upgrade and additional maintenance
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements RingAccount {
    private final RingVideoServlet servlet;
    private @Nullable ScheduledFuture<?> jobTokenRefresh = null;
    private @Nullable ScheduledFuture<?> eventRefresh = null;
    private @Nullable FcmClient fcmClient;
    private final HttpClient httpClient;
    private boolean isPolling = false;

    // Current status
    protected OnOffType status = OnOffType.OFF;
    protected OnOffType enabled = OnOffType.ON;
    protected final Logger logger = LoggerFactory.getLogger(AccountHandler.class);

    // Scheduler
    protected @Nullable ScheduledFuture<?> refreshJob;

    /**
     * The user profile retrieved when authenticating.
     */
    private volatile Tokens tokens = new Tokens("", "");
    /**
     * The registry.
     */
    private final RingDeviceRegistry registry;
    /**
     * The RestClient is used to connect to the Ring Account.
     */
    private final RestClient restClient;
    /**
     * The list with events.
     */
    private volatile List<RingEventTO> lastEvents = List.of();
    /**
     * The index to the current event.
     */
    private int eventIndex = 0;

    private long lastRegistryRefresh = 0;
    private static final long REGISTRY_CACHE_MS = 5 * 60 * 1000;

    /*
     * The number of video files to keep when auto-downloading
     */
    private int videoRetentionCount;

    /*
     * The path of where to save video files
     */
    private String videoStoragePath = "";

    private final NetworkAddressService networkAddressService;

    private final int httpPort;

    private AccountConfiguration config = new AccountConfiguration();
    private long ownerId = 0;

    public AccountHandler(Bridge bridge, NetworkAddressService networkAddressService, RingVideoServlet ringVideoServlet,
            int httpPort, HttpClient httpClient) {
        super(bridge);
        this.httpPort = httpPort;
        this.networkAddressService = networkAddressService;
        this.registry = new RingDeviceRegistry();
        this.restClient = new RestClient(httpClient);
        this.servlet = ringVideoServlet;
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (command) {
            case RefreshType ignored -> handleRefresh(channelUID);
            case OnOffType onOffCommand -> handleOnOff(channelUID, onOffCommand);
            default -> logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    private void handleRefresh(ChannelUID channelUID) {
        boolean eventListOk = lastEvents.size() > eventIndex;
        if (!eventListOk && !channelUID.getId().equals(CHANNEL_CONTROL_ENABLED)) {
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_EVENT_URL -> {
                String videoFile = restClient.downloadEventVideo(lastEvents.get(eventIndex), tokens, videoStoragePath,
                        videoRetentionCount);
                String localIP = networkAddressService.getPrimaryIpv4HostAddress();

                if (videoFile.endsWith(".mp4")) {
                    updateState(channelUID,
                            new StringType("http://" + localIP + ":" + httpPort + "/ring/video/" + videoFile));
                } else {
                    updateState(channelUID, new StringType(videoFile));
                }
            }
            case CHANNEL_EVENT_CREATED_AT -> updateState(channelUID, lastEvents.get(eventIndex).getCreatedAt());
            case CHANNEL_EVENT_KIND -> updateState(channelUID, new StringType(lastEvents.get(eventIndex).kind));
            case CHANNEL_EVENT_DOORBOT_ID ->
                updateState(channelUID, new StringType(lastEvents.get(eventIndex).doorbot.id));
            case CHANNEL_EVENT_DOORBOT_DESCRIPTION ->
                updateState(channelUID, new StringType(lastEvents.get(eventIndex).doorbot.description));
            case CHANNEL_CONTROL_ENABLED -> updateState(channelUID, enabled);
            default -> logger.debug("Refresh command received for an unknown channel: {}", channelUID.getId());
        }
    }

    private void handleOnOff(ChannelUID channelUID, OnOffType xcommand) {
        switch (channelUID.getId()) {
            case CHANNEL_CONTROL_ENABLED -> {
                if (!enabled.equals(xcommand)) {
                    enabled = xcommand;
                    updateState(channelUID, enabled);
                    if (enabled.equals(OnOffType.ON)) {
                        startAutomaticRefresh(config.refreshInterval);
                    } else {
                        stopAutomaticRefresh();
                    }
                }
            }
            default -> logger.debug("OnOff command received for an unknown channel: {}", channelUID.getId());
        }
    }

    private void refresh() {
        try {
            minuteTick();
        } catch (final Exception e) {
            logger.debug("AbstractHandler - Exception occurred during execution of startAutomaticRefresh(): {}",
                    e.getMessage(), e);
        }
    }

    protected void startAutomaticRefresh(final int refreshInterval) {
        // Prevent zombie threads! Cancel existing timer before overwriting the variable.
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
        }
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, refreshInterval, TimeUnit.SECONDS);

        ScheduledFuture<?> job = eventRefresh;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        eventRefresh = null;
        // Only run HTTP event polling when FCM is not connected (or not in use)
        if (fcmClient == null || isPolling) {
            isPolling = true;
            eventRefresh = scheduler.scheduleWithFixedDelay(this::refreshEvent, refreshInterval, refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    protected void stopAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;

        // Also stop event polling when FCM takes over
        job = eventRefresh;
        if (job != null) {
            job.cancel(true);
        }
        eventRefresh = null;
    }

    private void saveRefreshTokenToFile(String refreshToken) {
        String folderName = OpenHAB.getUserDataFolder() + "/ring";
        String thingId = getThing().getUID().getId();
        File folder = new File(folderName);
        String fileName = folderName + "/ring." + thingId + ".refreshToken";

        if (!folder.exists()) {
            logger.debug("Creating directory {}", folderName);
            folder.mkdirs();
        }
        try {
            Files.writeString(Paths.get(fileName), refreshToken);
        } catch (IOException ex) {
            logger.debug("IOException when writing refreshToken to file {}", ex.getMessage());
        }
        logger.debug("saveRefreshTokenToFile Successful {}", RingUtils.sanitizeData(refreshToken));
    }

    private String getRefreshTokenFromFile() {
        String refreshToken = "";
        String folderName = OpenHAB.getUserDataFolder() + "/ring";
        String thingId = getThing().getUID().getId();
        String fileName = folderName + "/ring." + thingId + ".refreshToken";
        File file = new File(fileName);
        if (!file.exists()) {
            return refreshToken;
        }
        try {
            refreshToken = Files.readString(Paths.get(fileName));
        } catch (IOException ex) {
            logger.debug("IOException when reading refreshToken from file {}", ex.getMessage());
        }
        logger.debug("getRefreshTokenFromFile successful {}", RingUtils.sanitizeData(refreshToken));
        return refreshToken;
    }

    private File getFcmCredentialsFile() {
        File ringDir = new File(OpenHAB.getUserDataFolder(), "ring");
        if (!ringDir.exists()) {
            ringDir.mkdirs();
        }
        return new File(ringDir, "fcm_credentials_" + getThing().getUID().getId() + ".properties");
    }

    private Optional<FcmRegistrar.FcmCredentials> loadFcmCredentials() {
        File file = getFcmCredentialsFile();
        if (!file.exists()) {
            return Optional.empty();
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            String androidId = props.getProperty("androidId");
            String securityToken = props.getProperty("securityToken");
            String fcmToken = props.getProperty("fcmToken");

            if (androidId != null && securityToken != null && fcmToken != null) {
                return Optional.of(new FcmRegistrar.FcmCredentials(androidId, securityToken, fcmToken));
            } else {
                logger.warn("FCM credentials file is incomplete. Forcing re-registration.");
            }
        } catch (Exception e) {
            logger.warn("Failed to load saved FCM credentials, will generate new ones.", e);
        }
        return Optional.empty();
    }

    private void saveFcmCredentials(FcmRegistrar.FcmCredentials creds) {
        File file = getFcmCredentialsFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Properties props = new Properties();
            props.setProperty("androidId", creds.androidId());
            props.setProperty("securityToken", creds.securityToken());
            props.setProperty("fcmToken", creds.fcmToken());
            props.store(fos, "Ring Binding FCM Credentials");
            logger.debug("Successfully saved persistent FCM credentials to disk.");
        } catch (Exception e) {
            logger.error("Failed to save FCM credentials to disk!", e);
        }
    }

    public void doLogin(String username, String password, String twofactorCode) {
        logger.debug("doLogin U:{} P:{} 2:{}", RingUtils.sanitizeData(username), RingUtils.sanitizeData(password),
                RingUtils.sanitizeData(twofactorCode));
        String hardwareId = getHardwareId();
        String refreshToken = getRefreshTokenFromFile();
        logger.debug("doLogin H:{} RT:{}", hardwareId, RingUtils.sanitizeData(refreshToken));
        try {
            tokens = restClient.getTokens(username, password, refreshToken, twofactorCode, hardwareId);
            saveRefreshTokenToFile(tokens.refreshToken());
        } catch (AuthenticationException ex) {
            logger.debug("AuthenticationException when initializing Ring Account handler{}", ex.getMessage());
            String message = ex.getMessage();
            if ((message != null) && message.startsWith("Two factor")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            }
        } catch (JsonParseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.invalid-response");
        }
        logger.debug("doLogin RT: {}", getRefreshTokenFromFile());
        try {
            refreshRegistry(true);
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException ae) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.auth-exception");
        } catch (JsonParseException pe1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "JsonParseException response from ring.com");
        }
    }

    public String getHardwareId() {
        String hardwareId = config.hardwareId;
        logger.debug("getHardwareId H:{}", hardwareId);
        Configuration updatedConfiguration = getThing().getConfiguration();
        if (hardwareId.isBlank()) {
            hardwareId = java.util.UUID.randomUUID().toString();
            config.hardwareId = hardwareId;
            updatedConfiguration.put("hardwareId", config.hardwareId);
            updateConfiguration(updatedConfiguration);
        }
        return hardwareId;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Ring Account handler");

        config = getConfigAs(AccountConfiguration.class);
        int refreshInterval = config.refreshInterval;
        String hardwareId = getHardwareId();
        String refreshToken = getRefreshTokenFromFile();

        String twofactorCode = config.twofactorCode;
        videoRetentionCount = config.videoRetentionCount;
        videoStoragePath = !config.videoStoragePath.isEmpty() ? config.videoStoragePath
                : OpenHAB.getConfigFolder() + "/html/ring/video";
        servlet.addVideoStoragePath(thing.getUID(), videoStoragePath);

        logger.debug("AccountHandler - initialize - VSP: {} OH: {}", config.videoStoragePath,
                OpenHAB.getConfigFolder());

        if ((!refreshToken.isEmpty()) || !(config.username.isEmpty() && config.password.isEmpty())) {

            // Set status to pending while we do the heavy lifting in the background
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Authenticating in background...");

            // Push all the heavy network calls to a background thread to prevent openHAB from blocking!
            scheduler.execute(() -> {
                try {
                    Configuration updatedConfiguration = getThing().getConfiguration();

                    logger.debug("Logging in with refresh token: {}", RingUtils.sanitizeData(refreshToken));
                    tokens = restClient.getTokens(config.username, config.password, refreshToken, twofactorCode,
                            hardwareId);
                    saveRefreshTokenToFile(tokens.refreshToken());
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Retrieving device list");
                    config.twofactorCode = "";
                    updatedConfiguration.put("twofactorCode", config.twofactorCode);
                    updateConfiguration(updatedConfiguration);

                    String ownerIdString = thing.getProperties().get(THING_PROPERTY_OWNER_ID);
                    if (ownerIdString != null && !ownerIdString.isEmpty()) {
                        ownerId = Long.parseLong(ownerIdString);
                    } else {
                        ProfileTO profile = restClient.getProfile(hardwareId, tokens);
                        ownerId = profile.id;
                        Map<String, String> properties = editProperties();
                        properties.put(THING_PROPERTY_OWNER_ID, Long.toString(ownerId));
                        updateProperties(properties);
                    }

                    refreshRegistry(true);

                    // Always start the token/session refresh loop
                    startSessionRefresh(refreshInterval);

                    // Start the periodic minute/device refresh loop (event polling will be disabled once FCM connects)
                    startAutomaticRefresh(refreshInterval);

                    // Attempt to connect via FCM by default
                    setupPushNotifications();
                    updateStatus(ThingStatus.ONLINE);
                } catch (AuthenticationException ex) {
                    logger.debug("AuthenticationException when initializing Ring Account handler {}", ex.getMessage());
                    String message = ex.getMessage();
                    if ((message != null) && message.startsWith("Two factor")) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                    }
                } catch (JsonParseException e) {
                    logger.debug("Invalid response from api.ring.com when initializing Ring Account handler {}",
                            e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Invalid response from api.ring.com");
                }
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please login via CLI or by updating the Thing properties");
        }
    }

    private void setupPushNotifications() {
        try {
            // 1. Try to load persistent credentials from disk
            FcmRegistrar.FcmCredentials creds = loadFcmCredentials().orElse(null);

            // 2. If missing, register with Google and save to disk
            if (creds == null) {
                logger.info("No persistent FCM credentials found. Registering as a new Android device...");
                FcmRegistrar registrar = new FcmRegistrar(httpClient);
                creds = registrar.register();
                saveFcmCredentials(creds);
            } else {
                logger.debug("Loaded persistent FCM credentials from disk. Reusing Android session.");
            }

            // 3. Always bind the token to the Ring account session on startup
            logger.debug("Binding FCM Token with Ring backend...");
            restClient.subscribeToPushNotifications(creds.fcmToken(), config.hardwareId, tokens);

            // 4. Connect the socket
            fcmClient = new FcmClient((String payload, String configStr) -> handlePushEvent(payload, configStr),
                    (Boolean status) -> onFcmStateChanged(status), creds);
            fcmClient.connect(creds.androidId(), creds.securityToken());

            // 5. Explicitly subscribe all cameras to the FCM session
            if (registry != null && !registry.getRingDevices().isEmpty()) {
                logger.debug("Subscribing all discovered devices to the active push notification session...");
                for (org.openhab.binding.ring.internal.device.RingDevice device : registry.getRingDevices()) {
                    // Pass the hardwareId into the RestClient
                    restClient.subscribeDeviceToPush(device.getId(), config.hardwareId, tokens);
                }
            }
        } catch (Exception e) {
            logger.warn("FCM Setup failed. Network restricted? Falling back to HTTP polling.", e);
            onFcmStateChanged(false);
        }
    }

    /**
     * Handles the automatic transition between FCM Push and HTTP Polling
     */
    private void onFcmStateChanged(Boolean isConnected) {
        if (scheduler == null || scheduler.isShutdown()) {
            logger.debug("Ignoring FCM state change (connected={}) because scheduler is not available", isConnected);
            return;
        }

        // NEW: If fcmClient is null, dispose() has been called. Do not spawn any new timers!
        if (fcmClient == null) {
            logger.debug("Ignoring FCM state change (connected={}) because the handler is being disposed.",
                    isConnected);
            return;
        }

        if (isConnected) {
            logger.debug("Ring FCM Socket connected. Disabling HTTP event polling.");

            ScheduledFuture<?> job = eventRefresh;
            if (job != null) {
                job.cancel(true);
            }
            eventRefresh = null;
            isPolling = false;
        } else {
            logger.warn("Ring FCM Socket disconnected. Falling back to HTTP event polling.");
            if (eventRefresh == null || eventRefresh.isCancelled()) {
                eventRefresh = scheduler.scheduleWithFixedDelay(this::refreshEvent, config.refreshInterval,
                        config.refreshInterval, TimeUnit.SECONDS);
            }
            isPolling = true;
        }
    }

    protected void handlePushEvent(String payloadJson, String androidConfigJson) {
        logger.debug("Parsing Instant Push Event Payload: {}", payloadJson);
        try {
            JsonObject json = JsonParser.parseString(payloadJson).getAsJsonObject();

            if (!json.has("device") || !json.has("event")) {
                logger.warn("Push event payload missing 'device' or 'event' objects");
                return;
            }

            JsonObject deviceObj = json.getAsJsonObject("device");
            JsonObject eventObj = json.getAsJsonObject("event");
            JsonObject dingObj = eventObj.has("ding") ? eventObj.getAsJsonObject("ding") : null;

            String deviceId = deviceObj.get("id").getAsString();
            String deviceName = deviceObj.has("name") ? deviceObj.get("name").getAsString() : "Ring Device";
            String eventIdStr = dingObj != null && dingObj.has("id") ? dingObj.get("id").getAsString()
                    : String.valueOf(System.currentTimeMillis());

            String kind = "motion";
            String detectionType = "";
            String createdAtStr = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
            if (dingObj != null) {
                if (dingObj.has("subtype")) {
                    String subtype = dingObj.get("subtype").getAsString().toLowerCase();
                    if (subtype.contains("ding") || subtype.contains("ring")) {
                        kind = "ding";
                    }
                }
                if (dingObj.has("detection_type")) {
                    detectionType = dingObj.get("detection_type").getAsString();
                }
                if (dingObj.has("created_at")) {
                    createdAtStr = dingObj.get("created_at").getAsString();
                }
            }

            long eventId = Long.parseLong(eventIdStr);

            if (!lastEvents.isEmpty() && lastEvents.getFirst().id == eventId) {
                return;
            }

            logger.info("Parsed Instant FCM Event -> Device: {}, Kind: {}", deviceName,
                    kind.toUpperCase(java.util.Locale.ROOT));

            RingEventTO instantEvent = new RingEventTO();
            instantEvent.id = eventId;
            instantEvent.kind = kind;
            instantEvent.createdAt = createdAtStr;

            instantEvent.doorbot = new org.openhab.binding.ring.internal.api.DoorbotTO();
            instantEvent.doorbot.id = deviceId;
            instantEvent.doorbot.description = deviceName;

            if (!detectionType.isEmpty()) {
                instantEvent.cvProperties = new org.openhab.binding.ring.internal.api.CVPropertiesTO();
                instantEvent.cvProperties.detectionType = detectionType;
            }

            lastEvents = java.util.List.of(instantEvent);

            updateState(CHANNEL_EVENT_CREATED_AT, instantEvent.getCreatedAt());
            updateState(CHANNEL_EVENT_KIND, new StringType(instantEvent.kind));
            updateState(CHANNEL_EVENT_DOORBOT_ID, new StringType(instantEvent.doorbot.id));
            updateState(CHANNEL_EVENT_DOORBOT_DESCRIPTION, new StringType(instantEvent.doorbot.description));

            // NEW: Use the native push notification body if provided!
            String message = "There is motion at your " + deviceName;

            if (!androidConfigJson.isEmpty()) { // FIX: Simplified check for empty string
                try {
                    JsonObject configObj = JsonParser.parseString(androidConfigJson).getAsJsonObject();
                    if (configObj.has("body")) {
                        message = configObj.get("body").getAsString();
                    }
                } catch (Exception e) {
                    logger.debug("Failed to extract body from android_config", e);
                }
            } else {
                if ("human".equals(detectionType)) {
                    message = "There is a Person at your " + deviceName;
                } else if ("vehicle".equals(detectionType)) {
                    message = "There is a Vehicle at your " + deviceName;
                } else if ("ding".equals(kind)) {
                    message = "Someone is at your " + deviceName;
                }
            }

            updateState(CHANNEL_EVENT_EXTENDED_DESCRIPTION, new StringType(message));

            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.execute(() -> getVideo(instantEvent));
            }

            // 5. Instantly trigger a snapshot update on the specific child device
            if (scheduler != null && !scheduler.isShutdown()) {
                // We delay by 3 seconds to give Ring's backend time to generate the new image frame
                scheduler.schedule(() -> {
                    for (org.openhab.core.thing.Thing child : getThing().getThings()) {
                        String childId = (String) child.getConfiguration().get("id");
                        if (deviceId.equals(childId)) {
                            org.openhab.core.thing.binding.ThingHandler handler = child.getHandler();
                            if (handler instanceof DoorbellHandler doorbellHandler) {
                                doorbellHandler.forceSnapshotUpdate();
                            } else if (handler instanceof StickupcamHandler stickupcamHandler) {
                                stickupcamHandler.forceSnapshotUpdate();
                            }
                        }
                    }
                }, 3, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Failed to parse instant push event json: {}", e.getMessage(), e);
        }
    }

    private void refreshRegistry(boolean force) throws JsonParseException, AuthenticationException {
        // If not forced, and the cache hasn't expired, skip the API call
        if (!force && (System.currentTimeMillis() - lastRegistryRefresh < REGISTRY_CACHE_MS)) {
            return;
        }

        logger.debug("AccountHandler - refreshRegistry - Fetching fresh device data from API");
        RingDevicesTO ringDevices = restClient.getRingDevices(tokens);
        registry.addOrUpdateRingDevices(ringDevices);
        lastRegistryRefresh = System.currentTimeMillis();
    }

    protected void minuteTick() {
        try {
            refreshRegistry(false);
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException | JsonParseException e) {
            String refreshToken = getRefreshTokenFromFile();
            if ((!refreshToken.isEmpty()) || !(config.username.isEmpty() && config.password.isEmpty())) {
                try {
                    tokens = restClient.getTokens(config.username, config.password, refreshToken, "",
                            config.hardwareId);
                    refreshRegistry(false);
                    updateStatus(ThingStatus.ONLINE);
                } catch (AuthenticationException ex) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Invalid credentials");
                } catch (JsonParseException e1) {
                    logger.debug("RestClient reported JsonParseException trying to get tokens: {}", e1.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Invalid response from api.ring.com");
                }
            }
        }
    }

    protected void getVideo(RingEventTO event) {
        String videoFile = restClient.downloadEventVideo(event, tokens, videoStoragePath, videoRetentionCount);
        String localIP = networkAddressService.getPrimaryIpv4HostAddress();

        if (videoFile.endsWith(".mp4")) {
            updateState(CHANNEL_EVENT_URL,
                    new StringType("http://" + localIP + ":" + httpPort + "/ring/video/" + videoFile));
        } else {
            updateState(CHANNEL_EVENT_URL, new StringType(videoFile));
        }
    }

    protected void eventTick() {
        try {
            long id = lastEvents.isEmpty() ? 0 : lastEvents.getFirst().id;
            lastEvents = restClient.getHistory(tokens, 1);
            if (!lastEvents.isEmpty()) {
                logger.debug("AccountHandler - eventTick - Event id: {} lastEvents: {}", id,
                        lastEvents.getFirst().id == id);
                if (lastEvents.getFirst().id != id) {
                    logger.debug("AccountHandler - eventTick - New Event {}", lastEvents.getFirst().id);
                    updateState(CHANNEL_EVENT_CREATED_AT, lastEvents.getFirst().getCreatedAt());
                    updateState(CHANNEL_EVENT_KIND, new StringType(lastEvents.getFirst().kind));
                    updateState(CHANNEL_EVENT_DOORBOT_ID, new StringType(lastEvents.getFirst().doorbot.id));
                    updateState(CHANNEL_EVENT_DOORBOT_DESCRIPTION,
                            new StringType(lastEvents.getFirst().doorbot.description));
                    String detectionType = null;
                    if (lastEvents.getFirst().cvProperties != null) {
                        detectionType = lastEvents.getFirst().cvProperties.detectionType;
                    }
                    if (detectionType == null) {
                        detectionType = "";
                    }
                    if ("motion".equals(lastEvents.getFirst().kind)) {
                        String desc = lastEvents.getFirst().doorbot.description;

                        String message = switch (detectionType) {
                            case "human" -> "There is a Person at your " + desc;
                            case "vehicle" -> "There is a Vehicle at your " + desc;
                            default -> "There is motion at your " + desc;
                        };

                        updateState(CHANNEL_EVENT_EXTENDED_DESCRIPTION, new StringType(message));
                    }
                    RingEventTO latestEvent = lastEvents.getFirst();
                    scheduler.execute(() -> getVideo(latestEvent));
                }
            } else {
                logger.debug("AccountHandler - eventTick - lastEvents null");
            }
        } catch (AuthenticationException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "AuthenticationException response from ring.com");
            logger.debug(
                    "RestClient reported AuthenticationException from api.ring.com when retrying refreshRegistry for the second time: {}",
                    ex.getMessage());
        } catch (JsonParseException ignored) {
            logger.debug(
                    "RestClient reported JsonParseException api.ring.com when retrying refreshRegistry for the second time: {}",
                    ignored.getMessage());

        }
    }

    private void refreshToken() {
        try {
            refreshRegistry(false);
            tokens = restClient.getTokens("", "", tokens.refreshToken(), "", config.hardwareId);
        } catch (AuthenticationException e) {
            logger.debug(
                    "AccountHandler - startSessionRefresh - Exception occurred during execution of refreshRegistry(): {}",
                    e.getMessage(), e);
        }
    }

    private void refreshEvent() {
        try {
            eventTick();
        } catch (final Exception e) {
            logger.debug(
                    "AccountHandler - startSessionRefresh - Exception occurred during execution of eventTick(): {}",
                    e.getMessage(), e);
        }
    }

    /**
     * Refresh the tokens every 20 minutes
     */
    protected void startSessionRefresh(int refreshInterval) {
        logger.debug("startSessionRefresh {}", refreshInterval);

        if (jobTokenRefresh != null && !jobTokenRefresh.isCancelled()) {
            jobTokenRefresh.cancel(true);
        }
        jobTokenRefresh = scheduler.scheduleWithFixedDelay(this::refreshToken, 90, 600, TimeUnit.SECONDS);

        // DO NOT start eventRefresh here! It causes a duplicate timer leak.
        // Event polling is strictly handled by startAutomaticRefresh and the FCM fallback logic.
    }

    protected void stopSessionRefresh() {
        ScheduledFuture<?> job = jobTokenRefresh;
        if (job != null) {
            job.cancel(true);
        }
        jobTokenRefresh = null;

        job = eventRefresh;
        if (job != null) {
            job.cancel(true);
        }
        eventRefresh = null;
    }

    /**
     * Dispose of the refreshJob nicely and shut down the FCM socket.
     */
    @Override
    public void dispose() {
        logger.debug("Disposing AccountHandler and shutting down connections...");
        servlet.removeVideoStoragePath(thing.getUID());

        // 1. Immediately null out the client so callbacks know we are shutting down
        FcmClient clientToClose = fcmClient;
        fcmClient = null;

        // 2. Kill the active FCM Socket and its Virtual Threads
        if (clientToClose != null) {
            clientToClose.disconnect();
        }

        stopSessionRefresh();
        stopAutomaticRefresh();
        super.dispose();
    }

    @Override
    public Collection<RingDevice> getAllDevices() {
        return registry.getRingDevices().stream()
                .filter(device -> !config.limitToOwner || device.getDeviceStatus().owner.id == ownerId).toList();
    }

    @Override
    public @Nullable RingDevice getDevice(String id) {
        return registry.getRingDevice(id);
    }

    @Override
    public long getSnapshotTimestamp(String id) {
        try {
            return restClient.getSnapshotTimestamp(id, tokens);
        } catch (AuthenticationException ae) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.invalid-response");
            return -1;
        }
    }

    @Override
    public byte[] getSnapshot(String id) {
        try {
            return restClient.getSnapshot(id, tokens);
        } catch (AuthenticationException ae) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.auth-exception");
        }
        return new byte[0];
    }

    @Override
    public void sendCommand(String url) {
        try {
            logger.debug("sending url {} to Ring API", url);
            restClient.sendCommand(url, tokens);
        } catch (AuthenticationException ae) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.invalid-response");
        }
    }

    @Override
    public void sendCommand(String url, HttpMethod httpMethod, String payload) {
        try {
            logger.trace("sending url {} with payload {} to Ring API", url, payload);
            restClient.sendCommand(url, httpMethod, payload, tokens);
        } catch (AuthenticationException ae) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.invalid-response");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RingDiscoveryService.class);
    }
}
