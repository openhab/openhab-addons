/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.amazonechocontrol.internal.AccountServlet;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.HttpException;
import org.openhab.binding.amazonechocontrol.internal.IWebSocketCommandHandler;
import org.openhab.binding.amazonechocontrol.internal.WebSocketConnection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonActivities.Activity;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonActivities.Activity.SourceDeviceId;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAscendingAlarm.AscendingAlarmModel;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushActivity;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushActivity.Key;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushDevice.DopplerId;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDeviceNotificationState.DeviceNotificationState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonFeed;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMusicProvider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPushCommand;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonWakeWords.WakeWord;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Handles the connection to the amazon server.
 *
 * @author Michael Geramb - Initial Contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements IWebSocketCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private Storage<String> stateStorage;
    private @Nullable Connection connection;
    private @Nullable WebSocketConnection webSocketConnection;
    private final Set<EchoHandler> echoHandlers = new HashSet<>();
    private final Set<FlashBriefingProfileHandler> flashBriefingProfileHandlers = new HashSet<>();
    private final Object synchronizeConnection = new Object();
    private Map<String, Device> jsonSerialNumberDeviceMapping = new HashMap<>();
    private @Nullable ScheduledFuture<?> checkDataJob;
    private @Nullable ScheduledFuture<?> checkLoginJob;
    private @Nullable ScheduledFuture<?> refreshAfterCommandJob;
    private @Nullable ScheduledFuture<?> foceCheckDataJob;
    private String currentFlashBriefingJson = "";
    private final HttpService httpService;
    private @Nullable AccountServlet accountServlet;
    private final Gson gson = new Gson();
    int checkDataCounter;

    public AccountHandler(Bridge bridge, HttpService httpService, Storage<String> stateStorage) {
        super(bridge);
        this.httpService = httpService;
        this.stateStorage = stateStorage;
    }

    @Override
    public void initialize() {
        logger.debug("amazon account bridge starting...");

        synchronized (synchronizeConnection) {
            Connection connection = this.connection;
            if (connection == null) {
                this.connection = new Connection(null);
            }
        }
        if (this.accountServlet == null) {
            this.accountServlet = new AccountServlet(httpService, this.getThing().getUID().getId(), this);
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Wait for login");

        checkLoginJob = scheduler.scheduleWithFixedDelay(this::checkLogin, 0, 60, TimeUnit.SECONDS);
        checkDataJob = scheduler.scheduleWithFixedDelay(this::checkData, 4, 60, TimeUnit.SECONDS);

        logger.debug("amazon account bridge handler started.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);
        if (command instanceof RefreshType) {
            refreshData();
        }
    }

    public List<FlashBriefingProfileHandler> getFlashBriefingProfileHandlers() {
        return new ArrayList<>(this.flashBriefingProfileHandlers);
    }

    public List<Device> getLastKnownDevices() {
        return new ArrayList<>(jsonSerialNumberDeviceMapping.values());
    }

    public void addEchoHandler(EchoHandler echoHandler) {
        synchronized (echoHandlers) {
            if (!echoHandlers.add(echoHandler)) {
                return;
            }
        }
        forceCheckData();
    }

    void forceCheckData() {
        if (foceCheckDataJob == null) {
            foceCheckDataJob = scheduler.schedule(this::forceCheckDataHandler, 1000, TimeUnit.MILLISECONDS);
        }
    }

    void forceCheckDataHandler() {
        this.checkData();
    }

    public @Nullable Thing findThingBySerialNumber(@Nullable String deviceSerialNumber) {
        EchoHandler echoHandler = findEchoHandlerBySerialNumber(deviceSerialNumber);
        if (echoHandler != null) {
            return echoHandler.getThing();
        }
        return null;
    }

    public @Nullable EchoHandler findEchoHandlerBySerialNumber(@Nullable String deviceSerialNumber) {
        synchronized (echoHandlers) {
            for (EchoHandler echoHandler : echoHandlers) {
                if (StringUtils.equals(echoHandler.findSerialNumber(), deviceSerialNumber)) {
                    return echoHandler;
                }
            }
        }
        return null;
    }

    public void addFlashBriefingProfileHandler(FlashBriefingProfileHandler flashBriefingProfileHandler) {
        synchronized (flashBriefingProfileHandlers) {
            flashBriefingProfileHandlers.add(flashBriefingProfileHandler);
        }
        Connection connection = this.connection;
        if (connection != null) {
            if (currentFlashBriefingJson.isEmpty()) {
                updateFlashBriefingProfiles(connection);
            }
            flashBriefingProfileHandler.initialize(this, currentFlashBriefingJson);
        }
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
            synchronized (echoHandlers) {
                echoHandlers.remove(childHandler);
            }
        }
        // check for flash briefing profile handler
        if (childHandler instanceof FlashBriefingProfileHandler) {
            synchronized (flashBriefingProfileHandlers) {
                flashBriefingProfileHandlers.remove(childHandler);
            }
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
        @Nullable
        ScheduledFuture<?> refreshJob = this.checkDataJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.checkDataJob = null;
        }
        @Nullable
        ScheduledFuture<?> refreshLogin = this.checkLoginJob;
        if (refreshLogin != null) {
            refreshLogin.cancel(true);
            this.checkLoginJob = null;
        }
        @Nullable
        ScheduledFuture<?> foceCheckDataJob = this.foceCheckDataJob;
        if (foceCheckDataJob != null) {
            foceCheckDataJob.cancel(true);
            this.foceCheckDataJob = null;
        }
        @Nullable
        ScheduledFuture<?> refreshDataDelayed = this.refreshAfterCommandJob;
        if (refreshDataDelayed != null) {
            refreshDataDelayed.cancel(true);
            this.refreshAfterCommandJob = null;
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
            logger.error("check login fails with unexpected error {}", e);
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
            updateFlashBriefingHandlers();
            updateStatus(ThingStatus.ONLINE);
            checkDataCounter = 0;
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

    boolean checkWebSocketConnection() {
        WebSocketConnection webSocketConnection = this.webSocketConnection;
        if (webSocketConnection == null || webSocketConnection.isClosed()) {
            Connection connection = this.connection;
            if (connection != null && connection.getIsLoggedIn()) {
                try {
                    this.webSocketConnection = new WebSocketConnection(connection.getAmazonSite(),
                            connection.getSessionCookies(), this);
                } catch (IOException e) {
                    logger.warn("Web socket connection starting failed: {}", e);
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
                    if (checkDataCounter > 60 || foceCheckDataJob != null) {
                        checkDataCounter = 0;
                        foceCheckDataJob = null;
                    }
                    if (!checkWebSocketConnection() || checkDataCounter == 0) {
                        refreshData();
                    }
                }
                logger.debug("checkData {} finished", getThing().getUID().getAsString());
            } catch (HttpException | JsonSyntaxException | ConnectionException e) {
                logger.debug("checkData fails {}", e);
            } catch (Exception e) { // this handler can be removed later, if we know that nothing else can fail.
                logger.error("checkData fails with unexpected error {}", e);
            }
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
                updateFlashBriefingHandlers();

                DeviceNotificationState[] deviceNotificationStates = null;
                AscendingAlarmModel[] ascendingAlarmModels = null;
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
                            logger.debug("Update music provider failed {}", e);
                        }
                    }
                }
                // forward device information to echo handler
                for (EchoHandler child : echoHandlers) {
                    Device device = findDeviceJson(child);

                    @Nullable
                    JsonNotificationSound[] notificationSounds = null;
                    JsonPlaylists playlists = null;
                    if (device != null && currentConnection.getIsLoggedIn()) {
                        // update notification sounds
                        try {
                            notificationSounds = currentConnection.getNotificationSounds(device);
                        } catch (IOException | HttpException | JsonSyntaxException | ConnectionException e) {
                            logger.debug("Update notification sounds failed {}", e);
                        }
                        // update playlists
                        try {
                            playlists = currentConnection.getPlaylists(device);
                        } catch (IOException | HttpException | JsonSyntaxException | ConnectionException e) {
                            logger.debug("Update playlist failed {}", e);
                        }
                    }

                    BluetoothState state = null;
                    if (states != null) {
                        state = states.findStateByDevice(device);
                    }
                    DeviceNotificationState deviceNotificationState = null;
                    AscendingAlarmModel ascendingAlarmModel = null;
                    if (device != null) {
                        if (ascendingAlarmModels != null) {
                            for (AscendingAlarmModel current : ascendingAlarmModels) {
                                if (StringUtils.equals(current.deviceSerialNumber, device.serialNumber)) {
                                    ascendingAlarmModel = current;
                                    break;
                                }
                            }
                        }

                        if (deviceNotificationStates != null) {
                            for (DeviceNotificationState current : deviceNotificationStates) {
                                if (StringUtils.equals(current.deviceSerialNumber, device.serialNumber)) {
                                    deviceNotificationState = current;
                                    break;
                                }
                            }
                        }
                    }
                    child.updateState(this, device, state, deviceNotificationState, ascendingAlarmModel, playlists,
                            notificationSounds, musicProviders);
                }

                // update account state
                updateStatus(ThingStatus.ONLINE);

                logger.debug("refresh data {} finished", getThing().getUID().getAsString());
            } catch (HttpException | JsonSyntaxException | ConnectionException e) {
                logger.debug("refresh data fails {}", e);
            } catch (Exception e) { // this handler can be removed later, if we know that nothing else can fail.
                logger.error("refresh data fails with unexpected error {}", e);
            }
        }
    }

    public @Nullable Device findDeviceJson(EchoHandler echoHandler) {
        String serialNumber = echoHandler.findSerialNumber();
        return findDeviceJson(serialNumber);
    }

    public @Nullable Device findDeviceJson(@Nullable String serialNumber) {
        Device result = null;
        if (StringUtils.isNotEmpty(serialNumber)) {
            Map<String, Device> jsonSerialNumberDeviceMapping = this.jsonSerialNumberDeviceMapping;
            result = jsonSerialNumberDeviceMapping.get(serialNumber);
        }
        return result;
    }

    public @Nullable Device findDeviceJsonBySerialOrName(@Nullable String serialOrName) {
        if (StringUtils.isNotEmpty(serialOrName)) {
            Map<String, Device> currentJsonSerialNumberDeviceMapping = this.jsonSerialNumberDeviceMapping;
            for (Device device : currentJsonSerialNumberDeviceMapping.values()) {
                if (StringUtils.equalsIgnoreCase(device.serialNumber, serialOrName)) {
                    return device;
                }
            }
            for (Device device : currentJsonSerialNumberDeviceMapping.values()) {
                if (StringUtils.equalsIgnoreCase(device.accountName, serialOrName)) {
                    return device;
                }
            }
        }
        return null;
    }

    public List<Device> updateDeviceList() {

        Connection currentConnection = connection;
        if (currentConnection == null) {
            return new ArrayList<Device>();
        }

        List<Device> devices = null;
        try {
            if (currentConnection.getIsLoggedIn()) {
                devices = currentConnection.getDeviceList();
            }
        } catch (IOException | URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
        if (devices != null) {
            Map<String, Device> newJsonSerialDeviceMapping = new HashMap<>();
            for (Device device : devices) {
                String serialNumber = device.serialNumber;
                if (serialNumber != null) {
                    newJsonSerialDeviceMapping.put(serialNumber, device);
                }

            }
            jsonSerialNumberDeviceMapping = newJsonSerialDeviceMapping;
        }
        WakeWord[] wakeWords = currentConnection.getWakeWords();

        synchronized (echoHandlers) {
            for (EchoHandler child : echoHandlers) {
                String serialNumber = child.findSerialNumber();
                String deviceWakeWord = null;
                for (WakeWord wakeWord : wakeWords) {
                    if (wakeWord != null) {
                        if (StringUtils.equals(wakeWord.deviceSerialNumber, serialNumber)) {
                            deviceWakeWord = wakeWord.wakeWord;
                            break;
                        }
                    }
                }
                child.setDeviceAndUpdateThingState(this, findDeviceJson(child), deviceWakeWord);
            }
        }
        if (devices != null) {
            return devices;
        }
        return new ArrayList<Device>();
    }

    public void setEnabledFlashBriefingsJson(String flashBriefingJson) {
        Connection currentConnection = connection;
        JsonFeed[] feeds = gson.fromJson(flashBriefingJson, JsonFeed[].class);
        if (currentConnection != null) {
            try {
                currentConnection.setEnabledFlashBriefings(feeds);
            } catch (IOException | URISyntaxException e) {
                logger.warn("Set flashbriefing profile failed {}", e);
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
        synchronized (flashBriefingProfileHandlers) {
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
            JsonFeed[] feeds = currentConnection.getEnabledFlashBriefings();
            // Make a copy and remove changeable parts
            JsonFeed[] forSerializer = new JsonFeed[feeds.length];
            for (int i = 0; i < feeds.length; i++) {
                JsonFeed source = feeds[i];
                JsonFeed copy = new JsonFeed();
                copy.feedId = source.feedId;
                copy.skillId = source.skillId;
                // Do not copy imageUrl here, because it will change
                forSerializer[i] = copy;
            }
            this.currentFlashBriefingJson = gson.toJson(forSerializer);
        } catch (HttpException | JsonSyntaxException | IOException | URISyntaxException | ConnectionException e) {
            logger.warn("get flash briefing profiles fails {}", e);
        }

    }

    @Override
    public void webSocketCommandReceived(JsonPushCommand pushCommand) {
        try {
            handleWebsocketCommand(pushCommand);
        } catch (Exception e) {
            // should never happen, but if the exception is going out of this function, the binding stop working.
            logger.warn("handling of websockets fails: {}", e);
        }
    }

    void handleWebsocketCommand(JsonPushCommand pushCommand) {
        String command = pushCommand.command;
        if (command != null) {
            switch (command) {
                case "PUSH_ACTIVITY":
                    handlePushActivity(pushCommand.payload);
                    return;
                case "PUSH_DOPPLER_CONNECTION_CHANGE":
                case "PUSH_BLUETOOTH_STATE_CHANGE":
                    // refresh data 200ms after last command
                    @Nullable
                    ScheduledFuture<?> refreshDataDelayed = this.refreshAfterCommandJob;
                    if (refreshDataDelayed != null) {
                        refreshDataDelayed.cancel(false);
                    }
                    this.refreshAfterCommandJob = scheduler.schedule(this::refreshAfterCommand, 700,
                            TimeUnit.MILLISECONDS);
                    break;
                case "PUSH_NOTIFICATION_CHANGE":
                    // Currently ignored
                    break;
                default:
                    String payload = pushCommand.payload;
                    if (payload != null && StringUtils.isNotEmpty(payload) && payload.startsWith("{")
                            && payload.endsWith("}")) {
                        JsonCommandPayloadPushDevice devicePayload = gson.fromJson(payload,
                                JsonCommandPayloadPushDevice.class);
                        @Nullable
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
        @Nullable
        EchoHandler echoHandler = findEchoHandlerBySerialNumber(dopplerId.deviceSerialNumber);
        if (echoHandler != null) {
            echoHandler.handlePushCommand(command, payload);
        }
    }

    private void handlePushActivity(@Nullable String payload) {
        JsonCommandPayloadPushActivity pushActivity = gson.fromJson(payload, JsonCommandPayloadPushActivity.class);

        Key key = pushActivity.key;
        if (key == null) {
            return;
        }

        Connection connection = this.connection;
        if (connection == null || !connection.getIsLoggedIn()) {
            return;
        }
        Activity[] activities = connection.getActivities(10, pushActivity.timestamp);
        Activity currentActivity = null;
        String search = key.registeredUserId + "#" + key.entryId;
        for (Activity activity : activities) {
            if (StringUtils.equals(activity.id, search)) {
                currentActivity = activity;
                break;
            }
        }
        if (currentActivity == null) {
            return;
        }

        @Nullable
        SourceDeviceId @Nullable [] sourceDeviceIds = currentActivity.sourceDeviceIds;
        if (sourceDeviceIds != null) {
            for (SourceDeviceId sourceDeviceId : sourceDeviceIds) {
                if (sourceDeviceId != null) {
                    EchoHandler echoHandler = findEchoHandlerBySerialNumber(sourceDeviceId.serialNumber);
                    if (echoHandler != null) {
                        echoHandler.handlePushActivity(currentActivity);
                    }
                }
            }
        }
    }

    void refreshAfterCommand() {
        refreshData();
    }
}
