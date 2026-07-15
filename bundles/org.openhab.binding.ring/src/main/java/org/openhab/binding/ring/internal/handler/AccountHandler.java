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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
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
    private @Nullable ScheduledFuture<?> fcmRetryJob = null;
    private @Nullable FcmClient fcmClient;
    private final HttpClient httpClient;
    private boolean isPolling = false;

    // Current status
    protected OnOffType status = OnOffType.OFF;
    protected OnOffType enabled = OnOffType.ON;
    protected final Logger logger = LoggerFactory.getLogger(AccountHandler.class);

    // FCM Retry Tracking
    private int fcmRetryCount = 0;
    private static final int MAX_FCM_RETRIES = 3;
    private static final long FCM_RETRY_DELAY_SECONDS = 30;

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
    private volatile String lastEventId = "";

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

    protected void startAutomaticRefresh(final int configuredInterval) {
        // 1. Clean up existing jobs to prevent zombie threads
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
        }
        if (eventRefresh != null && !eventRefresh.isCancelled()) {
            eventRefresh.cancel(true);
        }
        eventRefresh = null;

        boolean isFcmActive = (fcmClient != null && !isPolling);

        int registryInterval = isFcmActive ? Math.max(60, configuredInterval) : configuredInterval;

        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, registryInterval, TimeUnit.SECONDS);

        if (!isFcmActive) {
            isPolling = true;
            eventRefresh = scheduler.scheduleWithFixedDelay(this::refreshEvent, configuredInterval, configuredInterval,
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
        } catch (IOException e) {
            logger.warn("Failed to load saved FCM credentials, will generate new ones.", e);
        }
        return Optional.empty();
    }

    private void saveFcmCredentials(FcmRegistrar.FcmCredentials creds) {
        File file = getFcmCredentialsFile();
        // Best-effort: restrict credentials file permissions to the current user only
        file.setReadable(false, false);
        file.setWritable(false, false);
        file.setReadable(true, true);
        file.setWritable(true, true);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Properties props = new Properties();
            props.setProperty("androidId", creds.androidId());
            props.setProperty("securityToken", creds.securityToken());
            props.setProperty("fcmToken", creds.fcmToken());
            props.store(fos, "Ring Binding FCM Credentials");
            logger.debug("Successfully saved persistent FCM credentials to disk.");
        } catch (IOException e) {
            logger.debug("Failed to save FCM credentials to disk!", e);
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
            if (fcmClient != null) {
                fcmClient.disconnect();
            }

            FcmRegistrar.FcmCredentials creds = loadFcmCredentials().orElse(null);

            if (creds == null) {
                logger.info("No persistent FCM credentials found. Registering as a new Android device...");
                FcmRegistrar registrar = new FcmRegistrar(httpClient);
                creds = registrar.register();
                saveFcmCredentials(creds);
            } else {
                logger.debug("Loaded persistent FCM credentials from disk. Reusing Android session.");
            }

            logger.debug("Binding FCM Token with Ring backend...");
            restClient.subscribeToPushNotifications(creds.fcmToken(), config.hardwareId, tokens);

            fcmClient = new FcmClient(this::handlePushEvent, this::onFcmStateChanged);
            fcmClient.connect(creds.androidId(), creds.securityToken());

            if (registry != null && !getAllDevices().isEmpty()) {
                logger.debug("Subscribing all discovered devices to the active push notification session...");
                for (RingDevice device : getAllDevices()) {
                    restClient.subscribeDeviceToPush(device.getId(), device.getKind(), config.hardwareId, tokens);
                }
            }
        } catch (AuthenticationException e) {
            logger.warn("FCM Setup failed. Network restricted? Falling back to HTTP polling.", e);
            onFcmStateChanged(false);
        }
    }

    /**
     * Handles the automatic transition between FCM Push and HTTP Polling
     */
    private void onFcmStateChanged(Boolean isConnected) {
        if (scheduler == null || scheduler.isShutdown() || fcmClient == null) {
            return;
        }

        if (isConnected) {
            logger.debug("Ring FCM Socket connected. Disabling HTTP event polling & throttling API refreshes.");
            fcmRetryCount = 0;

            if (fcmRetryJob != null && !fcmRetryJob.isCancelled()) {
                fcmRetryJob.cancel(true);
            }

            isPolling = false;

            // Swap gears: Applies the 60s minimum for the API
            startAutomaticRefresh(config.refreshInterval);
        } else {
            if (fcmRetryCount < MAX_FCM_RETRIES) {
                fcmRetryCount++;
                logger.debug("Ring FCM Socket disconnected. Attempt {} of {} to reconnect in {} seconds...",
                        fcmRetryCount, MAX_FCM_RETRIES, FCM_RETRY_DELAY_SECONDS);

                if (fcmRetryJob == null || fcmRetryJob.isDone()) {
                    fcmRetryJob = scheduler.schedule(this::setupPushNotifications, FCM_RETRY_DELAY_SECONDS,
                            TimeUnit.SECONDS);
                }
            } else if (!isPolling) {
                logger.debug("Ring FCM Socket disconnected. Max retries reached. Restoring rapid HTTP polling.");
                isPolling = true;

                // Swap gears: Restores the fast user-configured interval
                startAutomaticRefresh(config.refreshInterval);
            }
        }
    }

    void handlePushEvent(String payloadJson, String androidConfigJson, @Nullable String imgJson) {
        logger.debug("Parsing Instant Push Event Payload: {}", payloadJson);

        try {
            JsonObject payloadObj = JsonParser.parseString(payloadJson).getAsJsonObject();

            if (!payloadObj.has("device") || !payloadObj.has("event")) {
                return;
            }
            JsonObject deviceObj = payloadObj.getAsJsonObject("device");
            JsonObject eventObj = payloadObj.getAsJsonObject("event");
            JsonObject dingObj = eventObj.has("ding") ? eventObj.getAsJsonObject("ding") : null;

            String deviceId = deviceObj.has("id") ? deviceObj.get("id").getAsString() : "";
            String deviceName = deviceObj.has("name") ? deviceObj.get("name").getAsString() : "Unknown Device";
            String deviceKind = deviceObj.has("kind") ? deviceObj.get("kind").getAsString() : "";

            String category = "";
            String extendedDescription = "";
            if (androidConfigJson != null && !androidConfigJson.isEmpty()) {
                try {
                    JsonObject configObj = JsonParser.parseString(androidConfigJson).getAsJsonObject();
                    if (configObj.has("category")) {
                        category = configObj.get("category").getAsString();
                    }
                    if (configObj.has("body")) {
                        // Grab the localized text and optionally strip the bell emoji
                        extendedDescription = configObj.get("body").getAsString().replace("🔔 ", "");
                    }
                } catch (Exception e) {
                    logger.debug("Failed to parse android_config payload: {}", e.getMessage());
                }
            }

            boolean isIntercom = category.contains("intercom");

            if (dingObj == null || !dingObj.has("id")) {
                return;
            }

            String eventId = dingObj.get("id").getAsString();

            if (eventId.equals(lastEventId)) {
                return;
            }
            lastEventId = eventId;

            String kind = isIntercom ? "intercom_ding" : "motion";
            String pushSnapshotUrl = null;
            String createdAtStr = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                    .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME);

            if (dingObj.has("subtype")) {
                String subtype = dingObj.get("subtype").getAsString().toLowerCase();
                // Map doorbell pushes OR intercom button presses to the 'ding' kind
                if (!isIntercom
                        && (subtype.contains("ding") || subtype.contains("ring") || subtype.contains("button_press"))) {
                    kind = "ding";
                }
            }

            if (dingObj.has("created_at")) {
                createdAtStr = dingObj.get("created_at").getAsString();
            }

            if (extendedDescription.isEmpty()) {
                extendedDescription = isIntercom ? "Intercom Ringing" : "Motion Detected";
            }

            if (imgJson != null && !imgJson.isEmpty()) {
                try {
                    JsonObject imgObj = JsonParser.parseString(imgJson).getAsJsonObject();
                    if (imgObj.has("snapshot_url") && !imgObj.get("snapshot_url").isJsonNull()) {
                        pushSnapshotUrl = imgObj.get("snapshot_url").getAsString();
                    }
                } catch (Exception e) {
                    logger.debug("Failed to parse img payload: {}", e.getMessage());
                }
            }

            logger.debug("Ring Push Event Received: {} at {}", kind, deviceName);

            updateState(CHANNEL_EVENT_CREATED_AT, new DateTimeType(ZonedDateTime
                    .parse(createdAtStr, DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(ZoneId.systemDefault())));
            updateState(CHANNEL_EVENT_KIND, new StringType(kind));
            updateState(CHANNEL_EVENT_DOORBOT_ID, new StringType(deviceId));
            updateState(CHANNEL_EVENT_DOORBOT_DESCRIPTION, new StringType(deviceName));
            updateState(CHANNEL_EVENT_EXTENDED_DESCRIPTION, new StringType(extendedDescription));

            for (Thing child : getThing().getThings()) {
                String childId = (String) child.getConfiguration().get(THING_CONFIG_ID);
                if (deviceId.equals(childId)) {
                    if (child.getHandler() instanceof RingDeviceHandler ringHandler) {
                        ringHandler
                                .updateInstantEvent(
                                        new DateTimeType(
                                                ZonedDateTime.parse(createdAtStr, DateTimeFormatter.ISO_DATE_TIME)
                                                        .withZoneSameInstant(ZoneId.systemDefault())),
                                        kind, extendedDescription);
                    }
                    break;
                }
            }

            // Skip all media fetching (Snapshots and MP4s) for audio-only intercoms
            if (!"intercom_handset_audio".equalsIgnoreCase(deviceKind)) {
                if (pushSnapshotUrl != null && !pushSnapshotUrl.isEmpty()) {
                    logger.debug("Received Rich Notification URL! Downloading instantly...");
                    if (scheduler != null && !scheduler.isShutdown()) {
                        final String finalUrl = pushSnapshotUrl;
                        scheduler.execute(() -> updateChildSnapshots(deviceId, finalUrl));
                    }
                } else {
                    logger.debug("No snapshot URL in push. Scheduling 3-second fallback to camera API...");
                    if (scheduler != null && !scheduler.isShutdown()) {
                        scheduler.schedule(() -> {
                            updateChildSnapshots(deviceId, null);
                        }, 3, java.util.concurrent.TimeUnit.SECONDS);
                    }
                }

                scheduler.schedule(() -> {
                    try {
                        logger.debug("Recording finished. Fetching completed video for event {}", eventId);
                        // Fetch the last 5 events from the API to guarantee we find the completed RingEventTO
                        List<RingEventTO> recentEvents = restClient.getHistory(tokens, 5);
                        for (RingEventTO histEvent : recentEvents) {
                            if (eventId.equals(String.valueOf(histEvent.id))) {
                                logger.debug("Found matching completed event in history. Downloading MP4...");
                                getVideo(histEvent);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to download delayed video for event {}: {}", eventId, e.getMessage());
                    }
                }, 65, TimeUnit.SECONDS);

            } else {
                logger.debug("Push event mapped to Intercom. Skipping camera snapshot and MP4 video retrieval.");
            }

        } catch (Exception e) {
            logger.debug("Failed to parse instant push event json: {}", e.getMessage(), e);
        }
    }

    private void updateChildSnapshots(String deviceId, @Nullable String snapshotUrl) {
        for (Thing child : getThing().getThings()) {
            String childId = (String) child.getConfiguration().get("id");
            if (deviceId.equals(childId)) {
                ThingHandler handler = child.getHandler();
                if (handler instanceof DoorbellHandler doorbellHandler) {
                    doorbellHandler.forceSnapshotUpdate(snapshotUrl);
                } else if (handler instanceof StickupcamHandler stickupcamHandler) {
                    stickupcamHandler.forceSnapshotUpdate(snapshotUrl);
                }
            }
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
                    logger.warn(
                            "Communication or authentication failure during minuteTick recovery. Preserving token. Reason: {}",
                            ex.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "API Error - Will retry on next tick.");
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
                    String message = "";
                    if ("motion".equals(lastEvents.getFirst().kind)) {
                        String desc = lastEvents.getFirst().doorbot.description;

                        message = switch (detectionType) {
                            case "human" -> "There is a Person at your " + desc;
                            case "vehicle" -> "There is a Vehicle at your " + desc;
                            default -> "There is motion at your " + desc;
                        };

                        updateState(CHANNEL_EVENT_EXTENDED_DESCRIPTION, new StringType(message));
                    }
                    for (Thing child : getThing().getThings()) {
                        String childId = (String) child.getConfiguration().get(THING_CONFIG_ID);
                        if (Long.toString(id).equals(childId)) {
                            if (child.getHandler() instanceof RingDeviceHandler ringHandler) {
                                ringHandler.updateInstantEvent(lastEvents.getFirst().getCreatedAt(),
                                        lastEvents.getFirst().kind, message);
                            }
                            break;
                        }
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
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException e) {
            logger.warn("Failed to refresh Ring token. Network error or token revoked: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Token refresh failed - API Error");
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
     * Refresh the tokens every 45 minutes
     */
    protected void startSessionRefresh(int refreshInterval) {
        logger.debug("startSessionRefresh {}", refreshInterval);

        if (jobTokenRefresh != null && !jobTokenRefresh.isCancelled()) {
            jobTokenRefresh.cancel(true);
        }
        jobTokenRefresh = scheduler.scheduleWithFixedDelay(this::refreshToken, 90, 2700, TimeUnit.SECONDS);
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

        FcmClient clientToClose = fcmClient;
        fcmClient = null;

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
    public byte[] downloadDirectSnapshot(String url) {
        try {
            return restClient.downloadDirectSnapshot(url, tokens);
        } catch (AuthenticationException ae) {
            logger.debug("Failed to fetch direct snapshot from push URL.");
            return new byte[0];
        }
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
