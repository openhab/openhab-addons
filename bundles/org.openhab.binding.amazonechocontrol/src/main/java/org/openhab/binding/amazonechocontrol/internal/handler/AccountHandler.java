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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_REFRESH_ACTIVITY;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.CHANNEL_SEND_MESSAGE;
import static org.openhab.binding.amazonechocontrol.internal.dto.TOMapper.map;
import static org.openhab.binding.amazonechocontrol.internal.push.PushConnection.State.CLOSED;
import static org.openhab.binding.amazonechocontrol.internal.push.PushConnection.State.CONNECTED;
import static org.openhab.binding.amazonechocontrol.internal.util.Util.findIn;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.openhab.binding.amazonechocontrol.internal.AccountHandlerConfig;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlCommandDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.discovery.AmazonEchoDiscovery;
import org.openhab.binding.amazonechocontrol.internal.discovery.SmartHomeDevicesDiscovery;
import org.openhab.binding.amazonechocontrol.internal.dto.AscendingAlarmModelTO;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceNotificationStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.DoNotDisturbDeviceStatusTO;
import org.openhab.binding.amazonechocontrol.internal.dto.EnabledFeedTO;
import org.openhab.binding.amazonechocontrol.internal.dto.NotificationSoundTO;
import org.openhab.binding.amazonechocontrol.internal.dto.push.NotifyNowPlayingUpdatedTO;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushCommandTO;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushDeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushDopplerIdTO;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushListItemChangeTO;
import org.openhab.binding.amazonechocontrol.internal.dto.request.SendConversationDTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.AccountTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.BluetoothStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.CustomerHistoryRecordTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.ListItemTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.MusicProviderTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.SmartHomeTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.WakeWordTO;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.JsonSmartHomeGroups;
import org.openhab.binding.amazonechocontrol.internal.dto.smarthome.SmartHomeBaseDevice;
import org.openhab.binding.amazonechocontrol.internal.push.PushConnection;
import org.openhab.binding.amazonechocontrol.internal.smarthome.SmartHomeDeviceStateGroupUpdateCalculator;
import org.openhab.binding.amazonechocontrol.internal.types.Notification;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

/**
 * Handles the connection to the amazon server.
 *
 * @author Michael Geramb - Initial Contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements PushConnection.Listener {
    private static final int CHECK_DATA_INTERVAL = 3600; // in seconds (always refresh every hour)
    private static final int CHECK_LOGIN_INTERVAL = 60; // in seconds (always check every minute)

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private final Storage<String> sessionStorage;
    private final AmazonEchoControlCommandDescriptionProvider commandDescriptionProvider;
    private Connection connection;

    private final Map<String, EchoHandler> echoHandlers = new ConcurrentHashMap<>();
    private final Set<SmartHomeDeviceHandler> smartHomeDeviceHandlers = new CopyOnWriteArraySet<>();
    private final Set<FlashBriefingProfileHandler> flashBriefingProfileHandlers = new CopyOnWriteArraySet<>();
    private final LinkedBlockingQueue<String> pushActivityProcessingQueue = new LinkedBlockingQueue<>();

    private final Object synchronizeConnection = new Object();
    private Map<String, DeviceTO> serialNumberDeviceMapping = new HashMap<>();
    private Map<String, SmartHomeBaseDevice> jsonIdSmartHomeDeviceMapping = new HashMap<>();

    private @Nullable ScheduledFuture<?> checkDataJob;
    private @Nullable ScheduledFuture<?> updateSmartHomeStateJob;
    private @Nullable ScheduledFuture<?> refreshActivityJob;
    private @Nullable ScheduledFuture<?> refreshSmartHomeAfterCommandJob;
    private final Object synchronizeSmartHomeJobScheduler = new Object();

    private List<EnabledFeedTO> currentFlashBriefings = List.of();
    private final Gson gson;
    private int lastMessageId = 1000;
    private long nextDataRefresh = 0;
    private long nextLoginCheck = 0;
    private long nextRefreshNotifications = 0;

    private final LinkedBlockingQueue<String> requestedDeviceUpdates = new LinkedBlockingQueue<>();
    private @Nullable SmartHomeDeviceStateGroupUpdateCalculator smartHomeDeviceStateGroupUpdateCalculator;

    private AccountHandlerConfig handlerConfig = new AccountHandlerConfig();
    private final PushConnection pushConnection;
    private boolean disposing = false;
    private @Nullable AccountTO accountInformation;

    public AccountHandler(Bridge bridge, Storage<String> stateStorage, Gson gson, HttpClient httpClient,
            HTTP2Client http2Client, AmazonEchoControlCommandDescriptionProvider commandDescriptionProvider) {
        super(bridge);
        this.gson = gson;
        this.sessionStorage = stateStorage;
        this.pushConnection = new PushConnection(http2Client, gson, this, scheduler);
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.connection = new Connection(null, gson, httpClient);
    }

    @Override
    public void initialize() {
        disposing = false;
        handlerConfig = getConfig().as(AccountHandlerConfig.class);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Wait for login");

        nextDataRefresh = 0;
        nextLoginCheck = 0;

        checkDataJob = scheduler.scheduleWithFixedDelay(this::checkLoginAndData, 0, 1, TimeUnit.SECONDS);

        int pollingIntervalAlexa = Math.min(handlerConfig.pollingIntervalSmartHomeAlexa, 10);
        int pollingIntervalSkills = Math.min(handlerConfig.pollingIntervalSmartSkills, 60);

        smartHomeDeviceStateGroupUpdateCalculator = new SmartHomeDeviceStateGroupUpdateCalculator(pollingIntervalAlexa,
                pollingIntervalSkills);
        updateSmartHomeStateJob = scheduler.scheduleWithFixedDelay(() -> updateSmartHomeState(null), 20, 10,
                TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            logger.trace("Command '{}' received for channel '{}'", command, channelUID);
            if (!connection.isLoggedIn()) {
                logger.info("Can't handle commands when account is logged out.");
                return;
            }
            String channelId = channelUID.getId();
            if (channelId.equals(CHANNEL_REFRESH_ACTIVITY) && command instanceof OnOffType) {
                for (CustomerHistoryRecordTO record : getCustomerActivity(null)) {
                    String[] keyParts = record.recordKey.split("#");
                    String serialNumber = keyParts[keyParts.length - 1];
                    EchoHandler echoHandler = echoHandlers.get(serialNumber);
                    if (echoHandler != null) {
                        echoHandler.handlePushActivity(record);
                    }
                }
            } else if (channelId.equals(CHANNEL_SEND_MESSAGE) && command instanceof StringType) {
                String commandValue = command.toFullString();
                String baseUrl = "https://alexa-comms-mobile-service." + connection.getRetailDomain();

                AccountTO currentAccount = this.accountInformation;
                if (currentAccount == null) {
                    String accountResult = connection.getRequestBuilder().get(baseUrl + "/accounts")
                            .syncSend(String.class);
                    List<AccountTO> accounts = gson.fromJson(accountResult, AccountTO.LIST_TYPE_TOKEN);
                    currentAccount = accounts.stream().filter(a -> a.signedInUser).findFirst().orElse(null);
                    this.accountInformation = currentAccount;
                }

                if (currentAccount == null || currentAccount.commsId == null) {
                    return;
                }

                SendConversationDTO conversation = new SendConversationDTO();
                conversation.conversationId = "amzn1.comms.messaging.id.conversationV2~31e6fe8f-8b0c-4e84-a1e4-80030a09009b";
                conversation.clientMessageId = java.util.UUID.randomUUID().toString();
                conversation.messageId = lastMessageId++;
                conversation.sender = currentAccount.commsId;
                conversation.time = LocalDateTime.now().toString();
                conversation.payload.put("text", commandValue);

                String sendUrl = baseUrl + "/users/" + currentAccount.commsId + "/conversations/"
                        + currentAccount.commsId + "/messages";
                connection.getRequestBuilder().post(sendUrl).withContent(List.of(conversation)).syncSend();
            }
        } catch (ConnectionException e) {
            logger.info("handleCommand fails", e);
        }
    }

    public Set<FlashBriefingProfileHandler> getFlashBriefingProfileHandlers() {
        return Set.copyOf(flashBriefingProfileHandlers);
    }

    public List<DeviceTO> getLastKnownDevices() {
        return List.copyOf(serialNumberDeviceMapping.values());
    }

    public List<SmartHomeBaseDevice> getLastKnownSmartHomeDevices() {
        return List.copyOf(jsonIdSmartHomeDeviceMapping.values());
    }

    public void addSmartHomeDeviceHandler(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        if (smartHomeDeviceHandlers.add(smartHomeDeviceHandler)) {
            forceCheckData();
        }
    }

    public void forceCheckData() {
        nextDataRefresh = 0;
    }

    public @Nullable Thing getThingBySerialNumber(@Nullable String deviceSerialNumber) {
        if (deviceSerialNumber == null) {
            return null;
        }
        EchoHandler echoHandler = echoHandlers.get(deviceSerialNumber);
        return echoHandler == null ? null : echoHandler.getThing();
    }

    @Override
    public void handleRemoval() {
        cleanup();
        super.handleRemoval();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof EchoHandler echoHandler) {
            echoHandlers.put(echoHandler.getSerialNumber(), echoHandler);
            forceCheckData();
            return;
        } else if (childHandler instanceof FlashBriefingProfileHandler flashBriefingProfileHandler) {
            flashBriefingProfileHandlers.add(flashBriefingProfileHandler);
            if (currentFlashBriefings.isEmpty()) {
                currentFlashBriefings = updateFlashBriefingProfiles();
                flashBriefingProfileHandler.updateAndCheck(currentFlashBriefings);
            }
            // set flash-briefing description on echo handlers
            commandDescriptionProvider.setEchoHandlerStartCommands(echoHandlers.values(), flashBriefingProfileHandlers);
        }
        nextDataRefresh = Math.min(nextDataRefresh, System.currentTimeMillis() + 60L * 1000); // refresh latest within
                                                                                              // one minute
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof EchoHandler) {
            echoHandlers.values().remove(childHandler);
        } else if (childHandler instanceof FlashBriefingProfileHandler) {
            flashBriefingProfileHandlers.remove(childHandler);
            commandDescriptionProvider.setEchoHandlerStartCommands(echoHandlers.values(), flashBriefingProfileHandlers);
        } else if (childHandler instanceof SmartHomeDeviceHandler) {
            smartHomeDeviceHandlers.remove(childHandler);
        }
    }

    @Override
    public void dispose() {
        disposing = true;
        cleanup();
    }

    private void cleanup() {
        logger.debug("cleanup {}", getThing().getUID().getAsString());
        ScheduledFuture<?> updateSmartHomeStateJob = this.updateSmartHomeStateJob;
        if (updateSmartHomeStateJob != null) {
            updateSmartHomeStateJob.cancel(true);
            this.updateSmartHomeStateJob = null;
        }
        ScheduledFuture<?> refreshJob = this.checkDataJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.checkDataJob = null;
        }
        ScheduledFuture<?> refreshSmartHomeAfterCommandJob = this.refreshSmartHomeAfterCommandJob;
        if (refreshSmartHomeAfterCommandJob != null) {
            refreshSmartHomeAfterCommandJob.cancel(true);
            this.refreshSmartHomeAfterCommandJob = null;
        }
        pushConnection.close();
        connection.logout(false);
    }

    private void checkLogin() {
        try {
            ThingUID uid = getThing().getUID();
            logger.debug("check login {}", uid.getAsString());

            synchronized (synchronizeConnection) {
                try {
                    if (connection.isLoggedIn()) {
                        if (connection.renewTokens()) {
                            storeSession();
                        }
                    } else {
                        // read session data from property
                        String sessionStore = sessionStorage.get("sessionStorage");

                        // try to use the session data
                        if (connection.restoreLogin(sessionStore, null)) {
                            storeSession();
                            nextDataRefresh = 0;
                        }
                    }
                    if (!connection.isLoggedIn()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "Please login in through servlet: http(s)://<YOUROPENHAB>:<YOURPORT>/amazonechocontrol/"
                                        + URLEncoder.encode(uid.getId(), StandardCharsets.UTF_8));
                        if (pushConnection.getState() != CLOSED) {
                            // close push connection if we are not logged in
                            pushConnection.close();
                        }
                    } else {
                        updateStatus(ThingStatus.ONLINE);
                        if (pushConnection.getState() == CLOSED) {
                            pushConnection.open(connection.getRetailDomain(), connection.getAccessToken());
                        } else if (pushConnection.getState() == CONNECTED) {
                            // if the push connection is already logged in, check if it is alive
                            pushConnection.sendPing();
                        }
                    }
                } catch (ConnectionException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                }
            }
        } catch (RuntimeException e) { // this handler can be removed later, if we know that nothing else can fail.
            logger.error("check login fails with unexpected error", e);
        }
    }

    public void resetConnection(boolean newDevice) {
        pushConnection.close();
        connection.logout(newDevice);
        sessionStorage.put("sessionStorage", null);

        updateStatus(ThingStatus.OFFLINE);
    }

    // used to set a valid connection from the web proxy login
    public void setConnection(Connection newConnection) {
        pushConnection.close();
        connection = newConnection;
        storeSession();

        // force data check
        nextLoginCheck = 0;
        nextDataRefresh = 0;
    }

    private void storeSession() {
        String serializedStorage = connection.getLoginData().serializeLoginData();
        sessionStorage.put("sessionStorage", serializedStorage);
    }

    private void checkLoginAndData() {
        long now = System.currentTimeMillis();
        synchronized (synchronizeConnection) {
            try {
                if (now > nextLoginCheck) {
                    nextLoginCheck = now + CHECK_LOGIN_INTERVAL * 1000;
                    checkLogin();
                }
                if (connection.isLoggedIn()) {
                    if (now > nextDataRefresh) {
                        nextDataRefresh = now + CHECK_DATA_INTERVAL * 1000;
                        refreshData();
                    }
                    if (now > nextRefreshNotifications) {
                        refreshNotifications();
                    }
                }
            } catch (RuntimeException e) { // this handler can be removed later, if we know that nothing else can fail.
                logger.warn("checkData fails with unexpected error", e);
            }
        }
    }

    private void refreshNotifications() {
        if (!connection.isLoggedIn()) {
            return;
        }
        logger.debug("refresh notifications {}", getThing().getUID().getAsString());
        ZonedDateTime requestTime = ZonedDateTime.now();
        List<Notification> notifications = connection.getNotifications().stream()
                .map(n -> map(n, requestTime, ZonedDateTime.now())).filter(Objects::nonNull)
                .map(Objects::requireNonNull).toList();
        echoHandlers.values().forEach(echoHandler -> echoHandler.updateNotifications(notifications));
        ZonedDateTime first = notifications.stream().map(Notification::nextAlarmTime)
                .min(ChronoZonedDateTime::compareTo).orElse(null);
        if (first != null) {
            nextRefreshNotifications = first.toEpochSecond() * 1000;
        } else {
            nextRefreshNotifications = Long.MAX_VALUE;
        }
    }

    private void refreshData() {
        try {
            logger.debug("refreshing data {}", getThing().getUID().getAsString());

            // check if logged in
            if (!connection.isLoggedIn()) {
                return;
            }

            // get all devices registered in the account
            updateDeviceList();
            updateSmartHomeDeviceList(false);
            updateFlashBriefingHandlers();

            List<DeviceNotificationStateTO> deviceNotificationStates = connection.getDeviceNotificationStates();
            List<AscendingAlarmModelTO> ascendingAlarmModels = connection.getAscendingAlarms();
            List<DoNotDisturbDeviceStatusTO> doNotDisturbDeviceStatuses = connection.getDoNotDisturbs();
            List<BluetoothStateTO> bluetoothStates = connection.getBluetoothConnectionStates();
            List<MusicProviderTO> musicProviders = connection.getMusicProviders();

            // forward device information to echo handler
            echoHandlers.forEach((serialNumber, echoHandler) -> {
                DeviceTO device = serialNumberDeviceMapping.get(serialNumber);
                if (device == null) {
                    return;
                }

                // update alarm sounds
                List<NotificationSoundTO> notificationSounds = connection.getNotificationSounds(device);
                commandDescriptionProvider.setEchoHandlerAlarmSounds(echoHandler, notificationSounds);

                BluetoothStateTO bluetoothState = findIn(bluetoothStates, k -> k.deviceSerialNumber,
                        device.serialNumber).orElse(null);
                AscendingAlarmModelTO ascendingAlarmModel = findIn(ascendingAlarmModels, a -> a.deviceSerialNumber,
                        device.serialNumber).orElse(null);
                DeviceNotificationStateTO deviceNotificationState = findIn(deviceNotificationStates,
                        a -> a.deviceSerialNumber, device.serialNumber).orElse(null);
                DoNotDisturbDeviceStatusTO doNotDisturbDeviceStatus = findIn(doNotDisturbDeviceStatuses,
                        a -> a.deviceSerialNumber, device.serialNumber).orElse(null);

                echoHandler.updateState(device, bluetoothState, deviceNotificationState, ascendingAlarmModel,
                        doNotDisturbDeviceStatus, musicProviders);
            });

            // refresh notifications
            refreshNotifications();

            // update account state
            updateStatus(ThingStatus.ONLINE);

            logger.debug("refresh data {} finished", getThing().getUID().getAsString());
        } catch (JsonSyntaxException e) {
            logger.debug("refresh data fails", e);
        } catch (RuntimeException e) { // this handler can be removed later, if we know that nothing else can fail.
            logger.error("refresh data fails with unexpected error", e);
        }
    }

    public @Nullable DeviceTO findDevice(@Nullable String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            return null;
        }
        return serialNumberDeviceMapping.get(serialNumber);
    }

    public @Nullable DeviceTO findDeviceBySerialOrName(@Nullable String serialOrName) {
        if (serialOrName == null || serialOrName.isEmpty()) {
            return null;
        }

        return this.serialNumberDeviceMapping.values().stream().filter(
                d -> serialOrName.equalsIgnoreCase(d.serialNumber) || serialOrName.equalsIgnoreCase(d.accountName))
                .findFirst().orElse(null);
    }

    public List<DeviceTO> updateDeviceList() {
        if (!connection.isLoggedIn()) {
            return List.of();
        }

        try {
            List<DeviceTO> devices = connection.getDeviceList();
            List<WakeWordTO> wakeWords = connection.getWakeWords();

            // create new device map
            serialNumberDeviceMapping = devices.stream().collect(Collectors.toMap(d -> d.serialNumber, d -> d));
            // notify flash briefing profile handlers of changed device list
            commandDescriptionProvider.setFlashBriefingTargets(flashBriefingProfileHandlers,
                    serialNumberDeviceMapping.values());
            commandDescriptionProvider.setEchoHandlerStartCommands(echoHandlers.values(), flashBriefingProfileHandlers);

            echoHandlers.forEach((serialNumber, echoHandler) -> {
                DeviceTO device = serialNumberDeviceMapping.get(serialNumber);
                if (device != null) {
                    String deviceWakeWord = findIn(wakeWords, w -> w.deviceSerialNumber, serialNumber)
                            .map(wakeWord -> wakeWord.wakeWord).orElse(null);
                    echoHandler.setDeviceAndUpdateThingStatus(device, deviceWakeWord);
                }
            });

            return devices;
        } catch (ConnectionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }

        return List.of();
    }

    public void setEnabledFlashBriefing(List<EnabledFeedTO> flashBriefingConfiguration) {
        if (connection.isLoggedIn()) {
            try {
                connection.setEnabledFlashBriefings(flashBriefingConfiguration);
            } catch (ConnectionException e) {
                logger.warn("Set flash-briefing profile failed", e);
            }
        }
        updateFlashBriefingHandlers();
    }

    public List<EnabledFeedTO> updateFlashBriefingHandlers() {
        List<EnabledFeedTO> currentConfiguration = getEnabledFlashBriefings();
        if (!currentConfiguration.isEmpty()) {
            boolean match = false;
            for (FlashBriefingProfileHandler handler : flashBriefingProfileHandlers) {
                match |= handler.updateAndCheck(currentConfiguration);
            }
            if (!match) {
                // there is no handler associated with the current configuration
                return currentConfiguration;
            }
        }
        return List.of();
    }

    public Connection getConnection() {
        return this.connection;
    }

    public List<EnabledFeedTO> getEnabledFlashBriefings() {
        if (!currentFlashBriefings.isEmpty()) {
            return currentFlashBriefings;
        }
        currentFlashBriefings = updateFlashBriefingProfiles();
        return currentFlashBriefings;
    }

    private List<EnabledFeedTO> updateFlashBriefingProfiles() {
        return connection.isLoggedIn() ? connection.getEnabledFlashBriefings().stream().map(this::copyFeed).toList()
                : List.of();
    }

    private EnabledFeedTO copyFeed(EnabledFeedTO feed) {
        EnabledFeedTO newFeed = new EnabledFeedTO();
        newFeed.feedId = feed.feedId;
        newFeed.skillId = feed.skillId;
        return newFeed;
    }

    @Override
    public void onPushCommandReceived(PushCommandTO pushCommand) {
        logger.debug("Processing {}", pushCommand);
        String payload = pushCommand.payload;
        String command = pushCommand.command;
        switch (command) {
            case "PUSH_ACTIVITY":
                // currently unused, seems to be removed, log a warning if it re-appears
                logger.warn("Activity detected: {}", pushCommand);
                break;
            case "PUSH_DOPPLER_CONNECTION_CHANGE":
            case "PUSH_BLUETOOTH_STATE_CHANGE":
                long now = System.currentTimeMillis();
                if (nextDataRefresh > now + 1000) {
                    nextDataRefresh = now + 1000;
                }
                break;

            case "PUSH_NOTIFICATION_CHANGE":
                refreshNotifications();
                break;
            case "PUSH_AUDIO_PLAYER_STATE":
            case "PUSH_MEDIA_QUEUE_CHANGE":
            case "PUSH_MEDIA_CHANGE":
            case "PUSH_MEDIA_PROGRESS_CHANGE":
            case "PUSH_VOLUME_CHANGE":
            case "PUSH_CONTENT_FOCUS_CHANGE":
            case "PUSH_EQUALIZER_STATE_CHANGE":
                if (payload.startsWith("{") && payload.endsWith("}")) {
                    PushDeviceTO devicePayload = Objects.requireNonNull(gson.fromJson(payload, PushDeviceTO.class));
                    PushDopplerIdTO dopplerId = devicePayload.dopplerId;
                    if (dopplerId != null) {
                        EchoHandler echoHandler = echoHandlers.get(dopplerId.deviceSerialNumber);
                        if (echoHandler == null) {
                            return;
                        }
                        echoHandler.handlePushCommand(command, payload);
                        if ("PUSH_EQUALIZER_STATE_CHANGE".equals(command) || "PUSH_VOLUME_CHANGE".equals(command)) {
                            pushActivityProcessingQueue.add(dopplerId.deviceSerialNumber);

                            // check if a processing job is already scheduled or we need to create a new one
                            ScheduledFuture<?> refreshActivityJob = this.refreshActivityJob;
                            if (refreshActivityJob == null || refreshActivityJob.isDone()) {
                                this.refreshActivityJob = scheduler.schedule(
                                        () -> handlePushActivity(pushCommand.timeStamp),
                                        handlerConfig.activityRequestDelay, TimeUnit.SECONDS);
                            }
                        }
                    }
                }
                break;
            case "NotifyNowPlayingUpdated":
                NotifyNowPlayingUpdatedTO update = Objects
                        .requireNonNull(gson.fromJson(payload, NotifyNowPlayingUpdatedTO.class));
                echoHandlers.values().forEach(e -> e.handleNowPlayingUpdated(update.update.update.nowPlayingData));
                break;
            case "NotifyMediaSessionsUpdated":
                // we can't determine which session was updated, but it only makes sense for currently playing devices
                // echoHandlers.forEach(e -> e.refreshAudioPlayerState(true));
                echoHandlers.values().forEach(EchoHandler::updateMediaSessions);
                break;
            case "PUSH_LIST_ITEM_CHANGE":
                PushListItemChangeTO itemChange = Objects
                        .requireNonNull(gson.fromJson(payload, PushListItemChangeTO.class));
                List<ListItemTO> lists = connection.getNamedListItems(itemChange.listId);
                // TODO: create channels
                break;
            default:
                logger.warn("Detected unknown command from activity stream: {}", pushCommand);
        }
    }

    private List<CustomerHistoryRecordTO> getCustomerActivity(@Nullable Long timestamp) {
        if (!connection.isLoggedIn()) {
            return List.of();
        }
        long realTimestamp = Objects.requireNonNullElse(timestamp, System.currentTimeMillis());
        long startTimestamp = realTimestamp - 120000;
        long endTimestamp = realTimestamp + 30000;

        return connection.getActivities(startTimestamp, endTimestamp);
    }

    private void handlePushActivity(@Nullable Long timestamp) {
        List<CustomerHistoryRecordTO> activityRecords = getCustomerActivity(timestamp);

        while (!pushActivityProcessingQueue.isEmpty()) {
            String deviceSerialNumber = pushActivityProcessingQueue.poll();
            try {
                Objects.requireNonNull(deviceSerialNumber);
                EchoHandler echoHandler = echoHandlers.get(deviceSerialNumber);
                if (echoHandler == null) {
                    logger.warn("Could not find thing handler for serialnumber {}", deviceSerialNumber);
                    return;
                }
                activityRecords.stream().filter(r -> r.recordKey.endsWith(deviceSerialNumber))
                        .forEach(echoHandler::handlePushActivity);
            } catch (RuntimeException e) {
                logger.warn("Could not handle push activity", e);
            }
        }
    }

    private @Nullable SmartHomeBaseDevice findSmartHomeDeviceJson(SmartHomeDeviceHandler handler) {
        String id = handler.getId();
        if (!id.isEmpty()) {
            return jsonIdSmartHomeDeviceMapping.get(id);
        }
        return null;
    }

    public int getSmartHomeDevicesDiscoveryMode() {
        return handlerConfig.discoverSmartHome;
    }

    public List<SmartHomeBaseDevice> updateSmartHomeDeviceList(boolean forceUpdate) {
        if (!forceUpdate && smartHomeDeviceHandlers.isEmpty() && handlerConfig.discoverSmartHome == 0) {
            return List.of();
        }

        String jsonQuery = "{\"query\":\"query Endpoints{endpoints{items{endpointId id friendlyName displayCategories{primary{value}} legacyIdentifiers{dmsIdentifier{deviceType{type value{text}} deviceSerialNumber{type value{text}}}} legacyAppliance{applianceId applianceTypes endpointTypeId friendlyName friendlyDescription manufacturerName connectedVia modelName entityId actions mergedApplianceIds capabilities applianceNetworkState version isEnabled customerDefinedDeviceType customerPreference alexaDeviceIdentifierList aliases driverIdentity additionalApplianceDetails isConsentRequired applianceKey appliancePairs deduplicatedPairs entityPairs deduplicatedAliasesByEntityId relations} serialNumber{value{text}} enablement model{value{text}} manufacturer{value{text}} features{name operations{name}}}}}\"}";

        try {
            if (connection.isLoggedIn()) {
                SmartHomeTO networkDetails = connection.getRequestBuilder()
                        .post(connection.getAlexaServer() + "/nexus/v1/graphql").withContent(jsonQuery).withJson(true)
                        .syncSend(SmartHomeTO.class);

                List<JsonSmartHomeDevice> smartHomeDevices = (networkDetails != null)
                        ? networkDetails.getLegacySmartHomeDevices()
                        : List.of();
                Map<String, SmartHomeBaseDevice> newJsonIdSmartHomeDeviceMapping = new HashMap<>();
                for (JsonSmartHomeDevice smartHomeDevice : smartHomeDevices) {
                    String id = smartHomeDevice.findId();
                    if (id != null) {
                        newJsonIdSmartHomeDeviceMapping.put(id, smartHomeDevice);
                    }
                }
                // TODO previously `searchSmartHomeDevicesRecursive` was called here. Due to changes in the JSON
                // structure, this may need to be re-implemented.
                // JsonSmartHomeDevice are now directly accessible from the response, JsonSmartHomeGroups.SmartHomeGroup
                // seem to be missing

                jsonIdSmartHomeDeviceMapping = newJsonIdSmartHomeDeviceMapping;

                // update handlers
                smartHomeDeviceHandlers
                        .forEach(child -> child.setDeviceAndUpdateThingState(this, findSmartHomeDeviceJson(child)));
                return newJsonIdSmartHomeDeviceMapping.values().stream().toList();
            }
        } catch (ConnectionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }

        return List.of();
    }

    private void searchSmartHomeDevicesRecursive(@Nullable Object jsonNode, List<SmartHomeBaseDevice> devices) {
        if (jsonNode instanceof Map) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Map<String, Object> map = (Map) jsonNode;
            if (map.containsKey("entityId") && map.containsKey("friendlyName") && map.containsKey("actions")) {
                // device node found, create type element and add it to the results
                JsonElement element = gson.toJsonTree(jsonNode);
                JsonSmartHomeDevice shd = parseJson(element.toString(), JsonSmartHomeDevice.class);
                if (shd != null) {
                    devices.add(shd);
                }
            } else if (map.containsKey("applianceGroupName")) {
                JsonElement element = gson.toJsonTree(jsonNode);
                JsonSmartHomeGroups.SmartHomeGroup shg = parseJson(element.toString(),
                        JsonSmartHomeGroups.SmartHomeGroup.class);
                if (shg != null) {
                    devices.add(shg);
                }
            } else {
                map.values().forEach(value -> searchSmartHomeDevicesRecursive(value, devices));
            }
        }
    }

    // parser
    private <T> @Nullable T parseJson(String json, Class<T> type) throws JsonSyntaxException, IllegalStateException {
        try {
            // gson.fromJson is non-null if json is non-null and not empty
            return gson.fromJson(json, type);
        } catch (JsonParseException | IllegalStateException e) {
            logger.warn("Parsing json failed: {}", json, e);
            throw e;
        }
    }

    public void forceDelayedSmartHomeStateUpdate(String deviceId) {
        synchronized (synchronizeSmartHomeJobScheduler) {
            requestedDeviceUpdates.add(deviceId);
            ScheduledFuture<?> refreshSmartHomeAfterCommandJob = this.refreshSmartHomeAfterCommandJob;
            if (refreshSmartHomeAfterCommandJob != null) {
                refreshSmartHomeAfterCommandJob.cancel(false);
            }
            this.refreshSmartHomeAfterCommandJob = scheduler.schedule(this::updateSmartHomeStateJob, 500,
                    TimeUnit.MILLISECONDS);
        }
    }

    private void updateSmartHomeStateJob() {
        Set<String> deviceUpdates = new HashSet<>();

        synchronized (synchronizeSmartHomeJobScheduler) {
            if (!connection.isLoggedIn()) {
                this.refreshSmartHomeAfterCommandJob = scheduler.schedule(this::updateSmartHomeStateJob, 1000,
                        TimeUnit.MILLISECONDS);
                return;
            }
            requestedDeviceUpdates.drainTo(deviceUpdates);
            this.refreshSmartHomeAfterCommandJob = null;
        }

        deviceUpdates.forEach(this::updateSmartHomeState);
    }

    private synchronized void updateSmartHomeState(@Nullable String deviceFilterId) {
        try {
            logger.trace("updateSmartHomeState started with deviceFilterId={}", deviceFilterId);
            if (!connection.isLoggedIn()) {
                return;
            }
            List<SmartHomeBaseDevice> allDevices = getLastKnownSmartHomeDevices();
            Set<SmartHomeBaseDevice> targetDevices = new HashSet<>();
            if (deviceFilterId != null) {
                allDevices.stream().filter(d -> deviceFilterId.equals(d.findId())).findFirst()
                        .ifPresent(targetDevices::add);
            } else {
                SmartHomeDeviceStateGroupUpdateCalculator smartHomeDeviceStateGroupUpdateCalculator = this.smartHomeDeviceStateGroupUpdateCalculator;
                if (smartHomeDeviceStateGroupUpdateCalculator == null) {
                    return;
                }
                if (smartHomeDeviceHandlers.isEmpty()) {
                    return;
                }
                List<JsonSmartHomeDevice> devicesToUpdate = new ArrayList<>();
                for (SmartHomeDeviceHandler device : smartHomeDeviceHandlers) {
                    String id = device.getId();
                    SmartHomeBaseDevice baseDevice = jsonIdSmartHomeDeviceMapping.get(id);
                    devicesToUpdate.addAll(SmartHomeDeviceHandler.getSupportedSmartHomeDevices(baseDevice, allDevices));
                }
                smartHomeDeviceStateGroupUpdateCalculator.removeDevicesWithNoUpdate(devicesToUpdate);
                targetDevices.addAll(devicesToUpdate);
                if (targetDevices.isEmpty()) {
                    return;
                }
            }
            Map<String, JsonArray> applianceIdToCapabilityStates = connection
                    .getSmartHomeDeviceStatesJson(targetDevices);

            for (SmartHomeDeviceHandler smartHomeDeviceHandler : smartHomeDeviceHandlers) {
                String id = smartHomeDeviceHandler.getId();
                if (requestedDeviceUpdates.contains(id)) {
                    logger.debug("Device update {} suspended", id);
                    continue;
                }
                if (deviceFilterId == null || id.equals(deviceFilterId)) {
                    smartHomeDeviceHandler.updateChannelStates(allDevices, applianceIdToCapabilityStates);
                } else {
                    logger.trace("Id {} not matching filter {}", id, deviceFilterId);
                }
            }

            logger.debug("updateSmartHomeState finished");
        } catch (JsonSyntaxException | ConnectionException e) {
            logger.debug("updateSmartHomeState fails", e);
        } catch (Exception e) { // this handler can be removed later, if we know that nothing else can fail.
            logger.warn("updateSmartHomeState fails with unexpected error", e);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AmazonEchoDiscovery.class, SmartHomeDevicesDiscovery.class);
    }

    @Override
    public void onPushConnectionStateChange(PushConnection.State state) {
        if (!disposing && state == CLOSED) {
            // force check of login
            nextLoginCheck = 0;
        }
    }
}
