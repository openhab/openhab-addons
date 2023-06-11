/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.AccountHandlerConfig;
import org.openhab.binding.amazonechocontrol.internal.AccountServlet;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.HttpException;
import org.openhab.binding.amazonechocontrol.internal.IWebSocketCommandHandler;
import org.openhab.binding.amazonechocontrol.internal.WebSocketConnection;
import org.openhab.binding.amazonechocontrol.internal.channelhandler.ChannelHandler;
import org.openhab.binding.amazonechocontrol.internal.channelhandler.ChannelHandlerSendMessage;
import org.openhab.binding.amazonechocontrol.internal.channelhandler.IAmazonThingHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAscendingAlarm.AscendingAlarmModel;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushActivity;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushActivity.Key;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushDevice.DopplerId;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushNotificationChange;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDeviceNotificationState.DeviceNotificationState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonFeed;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMusicProvider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPushCommand;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevices.SmartHomeDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonWakeWords.WakeWord;
import org.openhab.binding.amazonechocontrol.internal.jsons.SmartHomeBaseDevice;
import org.openhab.binding.amazonechocontrol.internal.smarthome.SmartHomeDeviceStateGroupUpdateCalculator;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

/**
 * Handles the connection to the amazon server.
 *
 * @author Michael Geramb - Initial Contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements IWebSocketCommandHandler, IAmazonThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private final Storage<String> stateStorage;
    private @Nullable Connection connection;
    private @Nullable WebSocketConnection webSocketConnection;

    private final Set<EchoHandler> echoHandlers = new CopyOnWriteArraySet<>();
    private final Set<SmartHomeDeviceHandler> smartHomeDeviceHandlers = new CopyOnWriteArraySet<>();
    private final Set<FlashBriefingProfileHandler> flashBriefingProfileHandlers = new CopyOnWriteArraySet<>();

    private final Object synchronizeConnection = new Object();
    private Map<String, Device> jsonSerialNumberDeviceMapping = new HashMap<>();
    private Map<String, SmartHomeBaseDevice> jsonIdSmartHomeDeviceMapping = new HashMap<>();

    private @Nullable ScheduledFuture<?> checkDataJob;
    private @Nullable ScheduledFuture<?> checkLoginJob;
    private @Nullable ScheduledFuture<?> updateSmartHomeStateJob;
    private @Nullable ScheduledFuture<?> refreshAfterCommandJob;
    private @Nullable ScheduledFuture<?> refreshSmartHomeAfterCommandJob;
    private final Object synchronizeSmartHomeJobScheduler = new Object();
    private @Nullable ScheduledFuture<?> forceCheckDataJob;
    private String currentFlashBriefingJson = "";
    private final HttpService httpService;
    private @Nullable AccountServlet accountServlet;
    private final Gson gson;
    private int checkDataCounter;
    private final LinkedBlockingQueue<String> requestedDeviceUpdates = new LinkedBlockingQueue<>();
    private @Nullable SmartHomeDeviceStateGroupUpdateCalculator smartHomeDeviceStateGroupUpdateCalculator;
    private List<ChannelHandler> channelHandlers = new ArrayList<>();

    private AccountHandlerConfig handlerConfig = new AccountHandlerConfig();

    public AccountHandler(Bridge bridge, HttpService httpService, Storage<String> stateStorage, Gson gson) {
        super(bridge);
        this.gson = gson;
        this.httpService = httpService;
        this.stateStorage = stateStorage;
        channelHandlers.add(new ChannelHandlerSendMessage(this, this.gson));
    }

    @Override
    public void initialize() {
        handlerConfig = getConfig().as(AccountHandlerConfig.class);

        synchronized (synchronizeConnection) {
            Connection connection = this.connection;
            if (connection == null) {
                this.connection = new Connection(null, gson);
            }
        }

        if (accountServlet == null) {
            try {
                accountServlet = new AccountServlet(httpService, this.getThing().getUID().getId(), this, gson);
            } catch (IllegalStateException e) {
                logger.warn("Failed to create account servlet", e);
            }
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Wait for login");

        checkLoginJob = scheduler.scheduleWithFixedDelay(this::checkLogin, 0, 60, TimeUnit.SECONDS);
        checkDataJob = scheduler.scheduleWithFixedDelay(this::checkData, 4, 60, TimeUnit.SECONDS);

        int pollingIntervalAlexa = handlerConfig.pollingIntervalSmartHomeAlexa;
        if (pollingIntervalAlexa < 10) {
            pollingIntervalAlexa = 10;
        }
        int pollingIntervalSkills = handlerConfig.pollingIntervalSmartSkills;
        if (pollingIntervalSkills < 60) {
            pollingIntervalSkills = 60;
        }
        smartHomeDeviceStateGroupUpdateCalculator = new SmartHomeDeviceStateGroupUpdateCalculator(pollingIntervalAlexa,
                pollingIntervalSkills);
        updateSmartHomeStateJob = scheduler.scheduleWithFixedDelay(() -> updateSmartHomeState(null), 20, 10,
                TimeUnit.SECONDS);
    }

    @Override
    public void updateChannelState(String channelId, State state) {
        updateState(channelId, state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            logger.trace("Command '{}' received for channel '{}'", command, channelUID);
            Connection connection = this.connection;
            if (connection == null) {
                return;
            }

            String channelId = channelUID.getId();
            for (ChannelHandler channelHandler : channelHandlers) {
                if (channelHandler.tryHandleCommand(new Device(), connection, channelId, command)) {
                    return;
                }
            }
            if (command instanceof RefreshType) {
                refreshData();
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.info("handleCommand fails", e);
        }
    }

    @Override
    public void startAnnouncement(Device device, String speak, String bodyText, @Nullable String title,
            @Nullable Integer volume) throws IOException, URISyntaxException {
        EchoHandler echoHandler = findEchoHandlerBySerialNumber(device.serialNumber);
        if (echoHandler != null) {
            echoHandler.startAnnouncement(device, speak, bodyText, title, volume);
        }
    }

    public List<FlashBriefingProfileHandler> getFlashBriefingProfileHandlers() {
        return new ArrayList<>(flashBriefingProfileHandlers);
    }

    public List<Device> getLastKnownDevices() {
        return new ArrayList<>(jsonSerialNumberDeviceMapping.values());
    }

    public List<SmartHomeBaseDevice> getLastKnownSmartHomeDevices() {
        return new ArrayList<>(jsonIdSmartHomeDeviceMapping.values());
    }

    public void addEchoHandler(EchoHandler echoHandler) {
        if (echoHandlers.add(echoHandler)) {
            forceCheckData();
        }
    }

    public void addSmartHomeDeviceHandler(SmartHomeDeviceHandler smartHomeDeviceHandler) {
        if (smartHomeDeviceHandlers.add(smartHomeDeviceHandler)) {
            forceCheckData();
        }
    }

    public void forceCheckData() {
        if (forceCheckDataJob == null) {
            forceCheckDataJob = scheduler.schedule(this::checkData, 1000, TimeUnit.MILLISECONDS);
        }
    }

    public @Nullable Thing findThingBySerialNumber(@Nullable String deviceSerialNumber) {
        EchoHandler echoHandler = findEchoHandlerBySerialNumber(deviceSerialNumber);
        if (echoHandler != null) {
            return echoHandler.getThing();
        }
        return null;
    }

    public @Nullable EchoHandler findEchoHandlerBySerialNumber(@Nullable String deviceSerialNumber) {
        for (EchoHandler echoHandler : echoHandlers) {
            if (deviceSerialNumber != null && deviceSerialNumber.equals(echoHandler.findSerialNumber())) {
                return echoHandler;
            }
        }
        return null;
    }

    public void addFlashBriefingProfileHandler(FlashBriefingProfileHandler flashBriefingProfileHandler) {
        flashBriefingProfileHandlers.add(flashBriefingProfileHandler);
        Connection connection = this.connection;
        if (connection != null && connection.getIsLoggedIn()) {
            if (currentFlashBriefingJson.isEmpty()) {
                updateFlashBriefingProfiles(connection);
            }
            flashBriefingProfileHandler.initialize(this, currentFlashBriefingJson);
        }
    }

    private void scheduleUpdate() {
        checkDataCounter = 999;
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        scheduleUpdate();
    }

    @Override
    public void handleRemoval() {
        cleanup();
        super.handleRemoval();
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        // check for echo handler
        if (childHandler instanceof EchoHandler) {
            echoHandlers.remove(childHandler);
        }
        // check for flash briefing profile handler
        if (childHandler instanceof FlashBriefingProfileHandler) {
            flashBriefingProfileHandlers.remove(childHandler);
        }
        // check for flash briefing profile handler
        if (childHandler instanceof SmartHomeDeviceHandler) {
            smartHomeDeviceHandlers.remove(childHandler);
        }
        super.childHandlerDisposed(childHandler, childThing);
    }

    @Override
    public void dispose() {
        AccountServlet accountServlet = this.accountServlet;
        if (accountServlet != null) {
            accountServlet.dispose();
        }
        this.accountServlet = null;
        cleanup();
        super.dispose();
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
        ScheduledFuture<?> refreshLogin = this.checkLoginJob;
        if (refreshLogin != null) {
            refreshLogin.cancel(true);
            this.checkLoginJob = null;
        }
        ScheduledFuture<?> foceCheckDataJob = this.forceCheckDataJob;
        if (foceCheckDataJob != null) {
            foceCheckDataJob.cancel(true);
            this.forceCheckDataJob = null;
        }
        ScheduledFuture<?> refreshAfterCommandJob = this.refreshAfterCommandJob;
        if (refreshAfterCommandJob != null) {
            refreshAfterCommandJob.cancel(true);
            this.refreshAfterCommandJob = null;
        }
        ScheduledFuture<?> refreshSmartHomeAfterCommandJob = this.refreshSmartHomeAfterCommandJob;
        if (refreshSmartHomeAfterCommandJob != null) {
            refreshSmartHomeAfterCommandJob.cancel(true);
            this.refreshSmartHomeAfterCommandJob = null;
        }
        Connection connection = this.connection;
        if (connection != null) {
            connection.logout();
            this.connection = null;
        }
        closeWebSocketConnection();
    }

    private void checkLogin() {
        try {
            ThingUID uid = getThing().getUID();
            logger.debug("check login {}", uid.getAsString());

            synchronized (synchronizeConnection) {
                Connection currentConnection = this.connection;
                if (currentConnection == null) {
                    return;
                }

                try {
                    if (currentConnection.getIsLoggedIn()) {
                        if (currentConnection.checkRenewSession()) {
                            setConnection(currentConnection);
                        }
                    } else {
                        // read session data from property
                        String sessionStore = this.stateStorage.get("sessionStorage");

                        // try use the session data
                        if (currentConnection.tryRestoreLogin(sessionStore, null)) {
                            setConnection(currentConnection);
                        }
                    }
                    if (!currentConnection.getIsLoggedIn()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "Please login in through web site: http(s)://<YOUROPENHAB>:<YOURPORT>/amazonechocontrol/"
                                        + URLEncoder.encode(uid.getId(), "UTF8"));
                    }
                } catch (ConnectionException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                } catch (HttpException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                } catch (UnknownHostException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unknown host name '" + e.getMessage() + "'. Maybe your internet connection is offline");
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                } catch (URISyntaxException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                }
            }
        } catch (Exception e) { // this handler can be removed later, if we know that nothing else can fail.
            logger.error("check login fails with unexpected error", e);
        }
    }

    // used to set a valid connection from the web proxy login
    public void setConnection(@Nullable Connection connection) {
        this.connection = connection;
        if (connection != null) {
            String serializedStorage = connection.serializeLoginData();
            this.stateStorage.put("sessionStorage", serializedStorage);
        } else {
            this.stateStorage.put("sessionStorage", null);
            updateStatus(ThingStatus.OFFLINE);
        }
        closeWebSocketConnection();
        if (connection != null) {
            updateDeviceList();
            updateSmartHomeDeviceList(false);
            updateFlashBriefingHandlers();
            updateStatus(ThingStatus.ONLINE);
            scheduleUpdate();
            checkData();
        }
    }

    void closeWebSocketConnection() {
        WebSocketConnection webSocketConnection = this.webSocketConnection;
        this.webSocketConnection = null;
        if (webSocketConnection != null) {
            webSocketConnection.close();
        }
    }

    private boolean checkWebSocketConnection() {
        WebSocketConnection webSocketConnection = this.webSocketConnection;
        if (webSocketConnection == null || webSocketConnection.isClosed()) {
            Connection connection = this.connection;
            if (connection != null && connection.getIsLoggedIn()) {
                try {
                    this.webSocketConnection = new WebSocketConnection(connection.getAmazonSite(),
                            connection.getSessionCookies(), this);
                } catch (IOException e) {
                    logger.warn("Web socket connection starting failed", e);
                }
            }
            return false;
        }
        return true;
    }

    private void checkData() {
        synchronized (synchronizeConnection) {
            try {
                Connection connection = this.connection;
                if (connection != null && connection.getIsLoggedIn()) {
                    checkDataCounter++;
                    if (checkDataCounter > 60 || forceCheckDataJob != null) {
                        checkDataCounter = 0;
                        forceCheckDataJob = null;
                    }
                    if (!checkWebSocketConnection() || checkDataCounter == 0) {
                        refreshData();
                    }
                }
                logger.debug("checkData {} finished", getThing().getUID().getAsString());
            } catch (HttpException | JsonSyntaxException | ConnectionException e) {
                logger.debug("checkData fails", e);
            } catch (Exception e) { // this handler can be removed later, if we know that nothing else can fail.
                logger.error("checkData fails with unexpected error", e);
            }
        }
    }

    private void refreshNotifications(@Nullable JsonCommandPayloadPushNotificationChange pushPayload) {
        Connection currentConnection = this.connection;
        if (currentConnection == null) {
            return;
        }
        if (!currentConnection.getIsLoggedIn()) {
            return;
        }

        ZonedDateTime timeStamp = ZonedDateTime.now();
        try {
            List<JsonNotificationResponse> notifications = currentConnection.notifications();
            ZonedDateTime timeStampNow = ZonedDateTime.now();
            echoHandlers.forEach(echoHandler -> echoHandler.updateNotifications(timeStamp, timeStampNow, pushPayload,
                    notifications));
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.debug("refreshNotifications failed", e);
            return;
        }
    }

    private void refreshData() {
        synchronized (synchronizeConnection) {
            try {
                logger.debug("refreshing data {}", getThing().getUID().getAsString());

                // check if logged in
                Connection currentConnection = null;
                currentConnection = connection;
                if (currentConnection != null) {
                    if (!currentConnection.getIsLoggedIn()) {
                        return;
                    }
                }
                if (currentConnection == null) {
                    return;
                }

                // get all devices registered in the account
                updateDeviceList();
                updateSmartHomeDeviceList(false);
                updateFlashBriefingHandlers();

                List<DeviceNotificationState> deviceNotificationStates = List.of();
                List<AscendingAlarmModel> ascendingAlarmModels = List.of();
                JsonBluetoothStates states = null;
                List<JsonMusicProvider> musicProviders = null;
                if (currentConnection.getIsLoggedIn()) {
                    // update notification states
                    deviceNotificationStates = currentConnection.getDeviceNotificationStates();

                    // update ascending alarm
                    ascendingAlarmModels = currentConnection.getAscendingAlarm();

                    // update bluetooth states
                    states = currentConnection.getBluetoothConnectionStates();

                    // update music providers
                    if (currentConnection.getIsLoggedIn()) {
                        try {
                            musicProviders = currentConnection.getMusicProviders();
                        } catch (HttpException | JsonSyntaxException | ConnectionException e) {
                            logger.debug("Update music provider failed", e);
                        }
                    }
                }
                // forward device information to echo handler
                for (EchoHandler child : echoHandlers) {
                    Device device = findDeviceJson(child.findSerialNumber());

                    List<JsonNotificationSound> notificationSounds = List.of();
                    JsonPlaylists playlists = null;
                    if (device != null && currentConnection.getIsLoggedIn()) {
                        // update notification sounds
                        try {
                            notificationSounds = currentConnection.getNotificationSounds(device);
                        } catch (IOException | HttpException | JsonSyntaxException | ConnectionException e) {
                            logger.debug("Update notification sounds failed", e);
                        }
                        // update playlists
                        try {
                            playlists = currentConnection.getPlaylists(device);
                        } catch (IOException | HttpException | JsonSyntaxException | ConnectionException e) {
                            logger.debug("Update playlist failed", e);
                        }
                    }

                    BluetoothState state = null;
                    if (states != null) {
                        state = states.findStateByDevice(device);
                    }
                    DeviceNotificationState deviceNotificationState = null;
                    AscendingAlarmModel ascendingAlarmModel = null;
                    if (device != null) {
                        final String serialNumber = device.serialNumber;
                        if (serialNumber != null) {
                            ascendingAlarmModel = ascendingAlarmModels.stream()
                                    .filter(current -> serialNumber.equals(current.deviceSerialNumber)).findFirst()
                                    .orElse(null);
                            deviceNotificationState = deviceNotificationStates.stream()
                                    .filter(current -> serialNumber.equals(current.deviceSerialNumber)).findFirst()
                                    .orElse(null);
                        }
                    }
                    child.updateState(this, device, state, deviceNotificationState, ascendingAlarmModel, playlists,
                            notificationSounds, musicProviders);
                }

                // refresh notifications
                refreshNotifications(null);

                // update account state
                updateStatus(ThingStatus.ONLINE);

                logger.debug("refresh data {} finished", getThing().getUID().getAsString());
            } catch (HttpException | JsonSyntaxException | ConnectionException e) {
                logger.debug("refresh data fails", e);
            } catch (Exception e) { // this handler can be removed later, if we know that nothing else can fail.
                logger.error("refresh data fails with unexpected error", e);
            }
        }
    }

    public @Nullable Device findDeviceJson(@Nullable String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            return null;
        }
        return this.jsonSerialNumberDeviceMapping.get(serialNumber);
    }

    public @Nullable Device findDeviceJsonBySerialOrName(@Nullable String serialOrName) {
        if (serialOrName == null || serialOrName.isEmpty()) {
            return null;
        }

        return this.jsonSerialNumberDeviceMapping.values().stream().filter(
                d -> serialOrName.equalsIgnoreCase(d.serialNumber) || serialOrName.equalsIgnoreCase(d.accountName))
                .findFirst().orElse(null);
    }

    public List<Device> updateDeviceList() {
        Connection currentConnection = connection;
        if (currentConnection == null) {
            return new ArrayList<>();
        }

        List<Device> devices = null;
        try {
            if (currentConnection.getIsLoggedIn()) {
                devices = currentConnection.getDeviceList();
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
        if (devices != null) {
            // create new device map
            jsonSerialNumberDeviceMapping = devices.stream().filter(device -> device.serialNumber != null)
                    .collect(Collectors.toMap(d -> Objects.requireNonNull(d.serialNumber), d -> d));
        }

        List<WakeWord> wakeWords = currentConnection.getWakeWords();
        // update handlers
        for (EchoHandler echoHandler : echoHandlers) {
            String serialNumber = echoHandler.findSerialNumber();
            String deviceWakeWord = wakeWords.stream()
                    .filter(wakeWord -> serialNumber.equals(wakeWord.deviceSerialNumber)).findFirst()
                    .map(wakeWord -> wakeWord.wakeWord).orElse(null);
            echoHandler.setDeviceAndUpdateThingState(this, findDeviceJson(serialNumber), deviceWakeWord);
        }

        if (devices != null) {
            return devices;
        }
        return List.of();
    }

    public void setEnabledFlashBriefingsJson(String flashBriefingJson) {
        Connection currentConnection = connection;
        JsonFeed[] feeds = gson.fromJson(flashBriefingJson, JsonFeed[].class);
        if (currentConnection != null && feeds != null) {
            try {
                currentConnection.setEnabledFlashBriefings(Arrays.asList(feeds));
            } catch (IOException | URISyntaxException | InterruptedException e) {
                logger.warn("Set flashbriefing profile failed", e);
            }
        }
        updateFlashBriefingHandlers();
    }

    public String getNewCurrentFlashbriefingConfiguration() {
        return updateFlashBriefingHandlers();
    }

    public String updateFlashBriefingHandlers() {
        Connection currentConnection = connection;
        if (currentConnection != null) {
            return updateFlashBriefingHandlers(currentConnection);
        }
        return "";
    }

    private String updateFlashBriefingHandlers(Connection currentConnection) {
        if (!flashBriefingProfileHandlers.isEmpty() || currentFlashBriefingJson.isEmpty()) {
            updateFlashBriefingProfiles(currentConnection);
        }
        boolean flashBriefingProfileFound = false;
        for (FlashBriefingProfileHandler child : flashBriefingProfileHandlers) {
            flashBriefingProfileFound |= child.initialize(this, currentFlashBriefingJson);
        }
        if (flashBriefingProfileFound) {
            return "";
        }
        return this.currentFlashBriefingJson;
    }

    public @Nullable Connection findConnection() {
        return this.connection;
    }

    public String getEnabledFlashBriefingsJson() {
        Connection currentConnection = this.connection;
        if (currentConnection == null) {
            return "";
        }
        updateFlashBriefingProfiles(currentConnection);
        return this.currentFlashBriefingJson;
    }

    private void updateFlashBriefingProfiles(Connection currentConnection) {
        try {
            // Make a copy and remove changeable parts
            JsonFeed[] forSerializer = currentConnection.getEnabledFlashBriefings().stream()
                    .map(source -> new JsonFeed(source.feedId, source.skillId)).toArray(JsonFeed[]::new);
            this.currentFlashBriefingJson = gson.toJson(forSerializer);
        } catch (HttpException | JsonSyntaxException | IOException | URISyntaxException | ConnectionException
                | InterruptedException e) {
            logger.warn("get flash briefing profiles fails", e);
        }
    }

    @Override
    public void webSocketCommandReceived(JsonPushCommand pushCommand) {
        try {
            handleWebsocketCommand(pushCommand);
        } catch (Exception e) {
            // should never happen, but if the exception is going out of this function, the binding stop working.
            logger.warn("handling of websockets fails", e);
        }
    }

    void handleWebsocketCommand(JsonPushCommand pushCommand) {
        String command = pushCommand.command;
        if (command != null) {
            ScheduledFuture<?> refreshDataDelayed = this.refreshAfterCommandJob;
            switch (command) {
                case "PUSH_ACTIVITY":
                    handlePushActivity(pushCommand.payload);
                    break;
                case "PUSH_DOPPLER_CONNECTION_CHANGE":
                case "PUSH_BLUETOOTH_STATE_CHANGE":
                    if (refreshDataDelayed != null) {
                        refreshDataDelayed.cancel(false);
                    }
                    this.refreshAfterCommandJob = scheduler.schedule(this::refreshAfterCommand, 700,
                            TimeUnit.MILLISECONDS);
                    break;
                case "PUSH_NOTIFICATION_CHANGE":
                    JsonCommandPayloadPushNotificationChange pushPayload = gson.fromJson(pushCommand.payload,
                            JsonCommandPayloadPushNotificationChange.class);
                    refreshNotifications(pushPayload);
                    break;
                default:
                    String payload = pushCommand.payload;
                    if (payload != null && payload.startsWith("{") && payload.endsWith("}")) {
                        JsonCommandPayloadPushDevice devicePayload = Objects
                                .requireNonNull(gson.fromJson(payload, JsonCommandPayloadPushDevice.class));
                        DopplerId dopplerId = devicePayload.dopplerId;
                        if (dopplerId != null) {
                            handlePushDeviceCommand(dopplerId, command, payload);
                        }
                    }
                    break;
            }
        }
    }

    private void handlePushDeviceCommand(DopplerId dopplerId, String command, String payload) {
        EchoHandler echoHandler = findEchoHandlerBySerialNumber(dopplerId.deviceSerialNumber);
        if (echoHandler != null) {
            echoHandler.handlePushCommand(command, payload);
        }
    }

    private void handlePushActivity(@Nullable String payload) {
        if (payload == null) {
            return;
        }
        JsonCommandPayloadPushActivity pushActivity = Objects
                .requireNonNull(gson.fromJson(payload, JsonCommandPayloadPushActivity.class));

        Key key = pushActivity.key;
        if (key == null) {
            return;
        }

        Connection connection = this.connection;
        if (connection == null || !connection.getIsLoggedIn()) {
            return;
        }

        String search = key.registeredUserId + "#" + key.entryId;
        connection.getActivities(10, pushActivity.timestamp).stream().filter(activity -> search.equals(activity.id))
                .findFirst()
                .ifPresent(currentActivity -> currentActivity.getSourceDeviceIds().stream()
                        .map(sourceDeviceId -> findEchoHandlerBySerialNumber(sourceDeviceId.serialNumber))
                        .filter(Objects::nonNull).forEach(echoHandler -> Objects.requireNonNull(echoHandler)
                                .handlePushActivity(currentActivity)));
    }

    void refreshAfterCommand() {
        refreshData();
    }

    private @Nullable SmartHomeBaseDevice findSmartDeviceHomeJson(SmartHomeDeviceHandler handler) {
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
        Connection currentConnection = connection;
        if (currentConnection == null) {
            return Collections.emptyList();
        }

        if (!forceUpdate && smartHomeDeviceHandlers.isEmpty() && getSmartHomeDevicesDiscoveryMode() == 0) {
            return Collections.emptyList();
        }

        List<SmartHomeBaseDevice> smartHomeDevices = null;
        try {
            if (currentConnection.getIsLoggedIn()) {
                smartHomeDevices = currentConnection.getSmarthomeDeviceList();
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
        if (smartHomeDevices != null) {
            // create new id map
            Map<String, SmartHomeBaseDevice> newJsonIdSmartHomeDeviceMapping = new HashMap<>();
            for (Object smartHomeDevice : smartHomeDevices) {
                if (smartHomeDevice instanceof SmartHomeBaseDevice) {
                    SmartHomeBaseDevice smartHomeBaseDevice = (SmartHomeBaseDevice) smartHomeDevice;
                    String id = smartHomeBaseDevice.findId();
                    if (id != null) {
                        newJsonIdSmartHomeDeviceMapping.put(id, smartHomeBaseDevice);
                    }
                }
            }
            jsonIdSmartHomeDeviceMapping = newJsonIdSmartHomeDeviceMapping;
        }
        // update handlers
        smartHomeDeviceHandlers
                .forEach(child -> child.setDeviceAndUpdateThingState(this, findSmartDeviceHomeJson(child)));

        if (smartHomeDevices != null) {
            return smartHomeDevices;
        }

        return Collections.emptyList();
    }

    public void forceDelayedSmartHomeStateUpdate(@Nullable String deviceId) {
        if (deviceId == null) {
            return;
        }
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
            Connection connection = this.connection;
            if (connection == null || !connection.getIsLoggedIn()) {
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
            logger.debug("updateSmartHomeState started with deviceFilterId={}", deviceFilterId);
            Connection connection = this.connection;
            if (connection == null || !connection.getIsLoggedIn()) {
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
                List<SmartHomeDevice> devicesToUpdate = new ArrayList<>();
                for (SmartHomeDeviceHandler device : smartHomeDeviceHandlers) {
                    String id = device.getId();
                    SmartHomeBaseDevice baseDevice = jsonIdSmartHomeDeviceMapping.get(id);
                    SmartHomeDeviceHandler.getSupportedSmartHomeDevices(baseDevice, allDevices)
                            .forEach(devicesToUpdate::add);
                }
                smartHomeDeviceStateGroupUpdateCalculator.removeDevicesWithNoUpdate(devicesToUpdate);
                devicesToUpdate.stream().filter(Objects::nonNull).forEach(targetDevices::add);
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
        } catch (HttpException | JsonSyntaxException | ConnectionException e) {
            logger.debug("updateSmartHomeState fails", e);
        } catch (Exception e) { // this handler can be removed later, if we know that nothing else can fail.
            logger.warn("updateSmartHomeState fails with unexpected error", e);
        }
    }
}
