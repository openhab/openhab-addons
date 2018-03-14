/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.handler;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.amazonechocontrol.internal.AccountConfiguration;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.LoginServlet;
import org.openhab.binding.amazonechocontrol.internal.StateStorage;
import org.openhab.binding.amazonechocontrol.internal.discovery.AmazonEchoDiscovery;
import org.openhab.binding.amazonechocontrol.internal.discovery.IAmazonEchoDiscovery;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonFeed;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonSmartHomeDevice;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles the connection to the amazon server.
 *
 * @author Michael Geramb - Initial Contribution
 */
public class AccountHandler extends BaseBridgeHandler implements IAmazonEchoDiscovery {

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private StateStorage stateStorage;
    private AccountConfiguration config;
    private Connection connection;
    private List<EchoHandler> echoHandlers = new ArrayList<>();
    private List<SmartHomeBaseHandler> smartHomeHandlers = new ArrayList<>();
    private List<FlashBriefingProfileHandler> flashBriefingProfileHandlers = new ArrayList<>();
    private Object synchronizeConnection = new Object();
    private Map<String, Device> jsonSerialNumberDeviceMapping = new HashMap<>();
    private ScheduledFuture<?> refreshJob;
    private ScheduledFuture<?> refreshLogin;
    private boolean updateSmartHomeDeviceList;
    private boolean discoverFlashProfiles;
    private boolean smartHodeDeviceListEnabled;
    private String currentFlashBriefingJson = "";
    private HttpService httpService;
    private LoginServlet loginServlet;

    public AccountHandler(@NonNull Bridge bridge, @NonNull HttpService httpService) {
        super(bridge);
        this.httpService = httpService;
        stateStorage = new StateStorage(bridge);
        AmazonEchoDiscovery.setHandlerExist();

    }

    public Device[] getLastKnownDevices() {
        Map<String, Device> temp = jsonSerialNumberDeviceMapping;
        if (temp == null) {
            return new Device[0];
        }
        Device[] devices = new Device[temp.size()];
        temp.values().toArray(devices);
        return devices;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        if (command instanceof RefreshType) {
            refreshData();
        }
    }

    @Override
    public void initialize() {

        start();
    }

    public void addEchoHandler(@NonNull EchoHandler echoHandler) {
        synchronized (echoHandlers) {
            if (!echoHandlers.contains(echoHandler)) {
                echoHandlers.add(echoHandler);
            }
        }
        Connection temp = connection;
        if (temp != null) {
            initializeEchoHandler(echoHandler, temp);
        }
    }

    public void addFlashBriefingProfileHandler(@NonNull FlashBriefingProfileHandler flashBriefingProfileHandler) {
        synchronized (flashBriefingProfileHandlers) {
            if (!flashBriefingProfileHandlers.contains(flashBriefingProfileHandler)) {
                flashBriefingProfileHandlers.add(flashBriefingProfileHandler);
            }
        }
        Connection temp = connection;
        if (temp != null) {
            if (currentFlashBriefingJson.isEmpty()) {
                updateFlashBriefingProfiles(temp);
            }

            flashBriefingProfileHandler.initialize(this, currentFlashBriefingJson);
        }
    }

    public void addSmartHomeHandler(@NonNull SmartHomeBaseHandler smartHomeHandler) {
        synchronized (smartHomeHandlers) {
            if (!smartHomeHandlers.contains(smartHomeHandler)) {
                smartHomeHandlers.add(smartHomeHandler);
            }
        }
        Connection temp = connection;
        if (temp != null) {
            smartHomeHandler.initialize(temp);
        }
    }

    private void initializeEchoHandler(@NonNull EchoHandler echoHandler, @NonNull Connection temp) {
        intializeChildDevice(temp, echoHandler);

        Device device = findDeviceJson(echoHandler);
        BluetoothState state = null;

        JsonBluetoothStates states = null;
        try {
            if (temp.getIsLoggedIn()) {
                states = temp.getBluetoothConnectionStates();
            }
        } catch (Exception e) {

            logger.info("getBluetoothConnectionStates failed: {}", e);
        }
        if (states != null) {
            state = states.findStateByDevice(device);
        }
        echoHandler.updateState(device, state);
    }

    @Override
    public void childHandlerDisposed(@NonNull ThingHandler childHandler, @NonNull Thing childThing) {
        if (childHandler instanceof EchoHandler) {
            synchronized (echoHandlers) {
                echoHandlers.remove(childHandler);
            }

            AmazonEchoDiscovery instance = AmazonEchoDiscovery.instance;
            if (instance != null) {
                instance.removeExistingEchoHandler(childThing.getUID());
            }
        }
        if (childHandler instanceof FlashBriefingProfileHandler) {
            synchronized (flashBriefingProfileHandlers) {
                flashBriefingProfileHandlers.remove(childHandler);
            }
        }
        if (childHandler instanceof SmartHomeBaseHandler) {
            synchronized (smartHomeHandlers) {
                smartHomeHandlers.remove(childHandler);
            }

            AmazonEchoDiscovery instance = AmazonEchoDiscovery.instance;
            if (instance != null) {
                instance.removeExistingSmartHomeHandler(childThing.getUID());
            }
        }
        super.childHandlerDisposed(childHandler, childThing);
    }

    @Override
    public void handleRemoval() {

        cleanup();
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        LoginServlet loginServlet = this.loginServlet;
        if (loginServlet != null) {
            loginServlet.dispose();
        }
        this.loginServlet = null;
        AmazonEchoDiscovery.removeDiscoveryHandler(this);
        cleanup();
        super.dispose();
    }

    private void cleanup() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        if (refreshLogin != null) {
            refreshLogin.cancel(true);
            refreshLogin = null;
        }
        if (connection != null) {
            connection.logout();
            connection = null;
        }
    }

    private void start() {
        logger.debug("amazon account bridge starting handler ...");

        config = getConfigAs(AccountConfiguration.class);
        if (config.amazonSite == null || config.amazonSite.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Amazon site not configured");
            cleanup();
            return;
        }
        if (config.email == null || config.email.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Account email not configured");
            cleanup();
            return;
        }
        if (config.password == null || config.password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Account password not configured");
            cleanup();
            return;
        }
        if (config.pollingIntervalInSeconds == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Polling interval not configured");
            cleanup();
            return;
        }
        if (config.discoverSmartHomeDevices != null && config.discoverSmartHomeDevices) {
            if (!smartHodeDeviceListEnabled) {
                updateSmartHomeDeviceList = true;
            }
            smartHodeDeviceListEnabled = true;
        } else {
            smartHodeDeviceListEnabled = false;
        }

        if (config.pollingIntervalInSeconds < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Polling interval less than 10 seconds not allowed");
            cleanup();
            return;
        }
        synchronized (synchronizeConnection) {
            if (connection == null || !connection.getEmail().equals(config.email)
                    || !connection.getPassword().equals(config.password)
                    || !connection.getAmazonSite().equals(config.amazonSite)) {
                connection = new Connection(config.email, config.password, config.amazonSite);
            }
        }
        if (this.loginServlet == null) {
            this.loginServlet = new LoginServlet(httpService, this.getThing().getUID().getId(), this, config);
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Wait for login");
        if (refreshLogin != null) {
            refreshLogin.cancel(false);
        }
        refreshLogin = scheduler.scheduleWithFixedDelay(() -> {
            checkLogin();
        }, 0, 60, TimeUnit.SECONDS);

        if (refreshJob != null) {
            refreshJob.cancel(false);
        }
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            refreshData();
        }, 4, config.pollingIntervalInSeconds, TimeUnit.SECONDS);

        logger.debug("amazon account bridge handler started.");
    }

    private void checkLogin() {

        synchronized (synchronizeConnection) {
            Connection temp = connection;
            if (temp == null) {
                return;
            }
            Date loginTime = temp.tryGetLoginTime();
            Date currentDate = new Date();
            long currentTime = currentDate.getTime();
            if (loginTime != null && currentTime - loginTime.getTime() > 3600000) // One hour
            {
                try {
                    if (!temp.verifyLogin()) {
                        temp.logout();
                    }
                } catch (Exception e) {
                    logger.info("logout failed: {}", e.getMessage());
                    temp.logout();
                }
            }
            loginTime = temp.tryGetLoginTime();
            if (loginTime != null && currentTime - loginTime.getTime() > 86400000 * 5) // 5 days
            {
                // Recreate session
                this.stateStorage.storeState("sessionStorage", "");
                temp = new Connection(temp.getEmail(), temp.getPassword(), temp.getAmazonSite());
            }
            boolean loginIsValid = true;
            if (!temp.getIsLoggedIn()) {
                try {

                    // read session data from property
                    String sessionStore = this.stateStorage.findState("sessionStorage");

                    // try use the session data
                    if (!temp.tryRestoreLogin(sessionStore)) {
                        // session data not valid -> login
                        int retry = 0;
                        while (true) {
                            try {
                                temp.makeLogin();
                                break;
                            } catch (ConnectionException e) {
                                // Up to 3 retries for login
                                retry++;
                                if (retry >= 3) {
                                    temp.logout();
                                    throw e;
                                }
                                // give amazon some time
                                Thread.sleep(2000);
                            }
                        }
                        // store session data in property
                        String serializedStorage = temp.serializeLoginData();
                        if (serializedStorage == null) {
                            serializedStorage = "";
                        }
                        this.stateStorage.storeState("sessionStorage", serializedStorage);
                    }
                    updateSmartHomeDeviceList = true;
                    connection = temp;
                } catch (UnknownHostException e) {
                    loginIsValid = false;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unknown host name '" + e.getMessage() + "'. Maybe your internet connection is offline");
                } catch (Exception e) {
                    loginIsValid = false;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                }
                if (loginIsValid) {
                    handleValidLogin();
                }

            }
        }
    }

    private void handleValidLogin() {
        // update the device list
        updateDeviceList(false);
        updateStatus(ThingStatus.ONLINE);
        AmazonEchoDiscovery.addDiscoveryHandler(this);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        String serializedStorage = connection.serializeLoginData();
        if (serializedStorage == null) {
            serializedStorage = "";
        }
        this.stateStorage.storeState("sessionStorage", serializedStorage);
        updateSmartHomeDeviceList = true;
        handleValidLogin();
    }

    private void refreshData() {
        logger.debug("amazon account bridge refreshing data ...");
        try {
            Connection temp = null;
            synchronized (synchronizeConnection) {
                temp = connection;
                if (temp != null) {
                    if (!temp.getIsLoggedIn()) {
                        return;
                    }
                }
            }
            if (temp == null) {
                return;
            }

            updateDeviceList(false);

            JsonBluetoothStates states = null;
            if (temp.getIsLoggedIn()) {
                try {
                    states = temp.getBluetoothConnectionStates();
                } catch (Exception e) {
                    logger.info("getBluetoothConnectionStates failed: {}", e);
                }
            }

            for (EchoHandler child : echoHandlers) {
                Device device = findDeviceJson(child);
                BluetoothState state = null;
                if (states != null) {
                    state = states.findStateByDevice(device);
                }
                child.updateState(device, state);
            }

            updateStatus(ThingStatus.ONLINE);

        } catch (Exception e) {
            logger.warn("Update states of amazon account failed: {}", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    public Device findDeviceJson(EchoHandler echoHandler) {
        String serialNumber = echoHandler.findSerialNumber();
        return findDeviceJson(serialNumber);
    }

    public Device findDeviceJson(String serialNumber) {
        Device result = null;
        if (!serialNumber.isEmpty()) {
            Map<String, Device> temp = jsonSerialNumberDeviceMapping;
            if (temp != null) {
                result = temp.get(serialNumber);
            }
            return result;
        }
        return result;
    }

    public Device findDeviceJsonBySerialOrName(String serialOrName) {
        if (!serialOrName.isEmpty()) {
            String serialOrNameLowerCase = serialOrName.toLowerCase();
            Map<String, Device> temp = jsonSerialNumberDeviceMapping;
            for (Device device : temp.values()) {
                if (device.serialNumber != null && device.serialNumber.toLowerCase().equals(serialOrNameLowerCase)) {
                    return device;
                }
            }
            for (Device device : temp.values()) {
                if (device.accountName != null && device.accountName.toLowerCase().equals(serialOrNameLowerCase)) {
                    return device;
                }
            }
        }
        return null;
    }

    @Override
    public void updateDeviceList(boolean manualScan) {
        if (manualScan) {
            updateSmartHomeDeviceList = true;
            discoverFlashProfiles = true;
        }

        Connection temp = connection;
        if (temp == null) {
            return;
        }
        AmazonEchoDiscovery discoveryService = AmazonEchoDiscovery.instance;

        Device[] devices = null;
        try {
            if (temp.getIsLoggedIn()) {
                devices = temp.getDeviceList();
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
        if (devices != null) {
            Map<String, Device> newJsonSerialDeviceMapping = new HashMap<>();
            for (Device device : devices) {
                newJsonSerialDeviceMapping.put(device.serialNumber, device);
            }
            jsonSerialNumberDeviceMapping = newJsonSerialDeviceMapping;

            if (discoveryService != null) {
                discoveryService.setDevices(getThing().getUID(), devices);
            }
        }
        synchronized (echoHandlers) {
            for (EchoHandler child : echoHandlers) {
                if (child != null) {
                    initializeEchoHandler(child, temp);
                }
            }
        }
        synchronized (smartHomeHandlers) {
            for (SmartHomeBaseHandler child : smartHomeHandlers) {
                if (child != null) {
                    child.initialize(temp);
                }
            }
        }
        updateFlashBriefingHandlers(temp);

        if (discoveryService != null && updateSmartHomeDeviceList && smartHodeDeviceListEnabled) {
            updateSmartHomeDeviceList = false;
            List<JsonSmartHomeDevice> smartHomeDevices = null;
            try {
                smartHomeDevices = temp.getSmartHomeDevices();
            } catch (Exception e) {
                logger.warn("Update smart home list failed {}", e);
            }
            if (smartHomeDevices != null) {
                discoveryService.setSmartHomeDevices(getThing().getUID(), smartHomeDevices);
            }
        }

    }

    public void setEnabledFlashBriefingsJson(String flashBriefingJson) {
        Connection temp = connection;
        Gson gson = new Gson();
        JsonFeed[] feeds = gson.fromJson(flashBriefingJson, JsonFeed[].class);
        if (temp != null) {
            try {
                temp.setEnabledFlashBriefings(feeds);
            } catch (Exception e) {
                logger.warn("Set flashbriefing profile failed {}", e);
            }
        }
        updateFlashBriefingHandlers();
    }

    public void updateFlashBriefingHandlers() {
        Connection temp = connection;
        if (temp != null) {
            updateFlashBriefingHandlers(temp);
        }
    }

    private void updateFlashBriefingHandlers(Connection temp) {
        synchronized (smartHomeHandlers) {
            if (!flashBriefingProfileHandlers.isEmpty() || currentFlashBriefingJson.isEmpty()) {
                updateFlashBriefingProfiles(temp);
            }

            for (FlashBriefingProfileHandler child : flashBriefingProfileHandlers) {
                if (child != null) {
                    child.initialize(this, currentFlashBriefingJson);
                }
            }
            if (flashBriefingProfileHandlers.isEmpty()) {
                discoverFlashProfiles = true; // discover at least one device
            }
            AmazonEchoDiscovery discoveryService = AmazonEchoDiscovery.instance;
            if (discoveryService != null) {
                if (discoverFlashProfiles) {
                    discoverFlashProfiles = false;
                    discoveryService.discoverFlashBriefingProfiles(getThing().getUID(), this.currentFlashBriefingJson);
                }
            }
        }
    }

    public Connection findConnection() {
        return this.connection;
    }

    public String getEnabledFlashBriefingsJson() {
        Connection temp = this.connection;
        if (temp == null) {
            return "";
        }
        updateFlashBriefingProfiles(temp);
        return this.currentFlashBriefingJson;
    }

    private void updateFlashBriefingProfiles(Connection temp) {
        try {
            JsonFeed[] feeds = temp.getEnabledFlashBriefings();
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
            Gson gson = new Gson();
            this.currentFlashBriefingJson = gson.toJson(forSerializer);

        } catch (Exception e) {
            logger.warn("get flash briefing profiles fails {}", e);
        }

    }

    private void intializeChildDevice(@NonNull Connection connection, @NonNull EchoHandler child) {
        Device deviceJson = this.findDeviceJson(child);
        if (deviceJson != null) {
            child.intialize(connection, deviceJson);
        }
    }

}
