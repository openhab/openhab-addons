/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.handler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
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
public class AccountHandler extends BaseBridgeHandler implements IAmazonEchoDiscovery {

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private StateStorage stateStorage;
    private @Nullable Connection connection;
    private final Set<EchoHandler> echoHandlers = new HashSet<>();
    private final Set<FlashBriefingProfileHandler> flashBriefingProfileHandlers = new HashSet<>();
    private final Object synchronizeConnection = new Object();
    private Map<String, Device> jsonSerialNumberDeviceMapping = new HashMap<>();
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> refreshLogin;
    private boolean discoverFlashProfiles;
    private String currentFlashBriefingJson = "";
    private final HttpService httpService;
    private @Nullable LoginServlet loginServlet;

    public AccountHandler(Bridge bridge, HttpService httpService) {
        super(bridge);
        this.httpService = httpService;
        stateStorage = new StateStorage(bridge);
        AmazonEchoDiscovery.setHandlerExist();
    }

    @Override
    public void initialize() {
        logger.debug("amazon account bridge starting...");

        AccountConfiguration config = getConfigAs(AccountConfiguration.class);

        String amazonSite = config.amazonSite;
        if (StringUtils.isEmpty(amazonSite)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Amazon site not configured");
            return;
        }
        String email = config.email;
        if (StringUtils.isEmpty(email)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Account email not configured");
            return;
        }
        String password = config.password;
        if (StringUtils.isEmpty(password)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Account password not configured");
            return;
        }
        Integer pollingIntervalInSeconds = config.pollingIntervalInSeconds;
        if (pollingIntervalInSeconds == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Polling interval not configured");
            return;
        }
        if (pollingIntervalInSeconds < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Polling interval less than 10 seconds not allowed");
            return;
        }
        synchronized (synchronizeConnection) {
            Connection connection = this.connection;
            if (connection == null || !connection.getEmail().equals(email) || !connection.getPassword().equals(password)
                    || !connection.getAmazonSite().equals(amazonSite)) {
                this.connection = new Connection(email, password, amazonSite, this.getThing().getUID().getId());
            }
        }
        if (this.loginServlet == null) {
            this.loginServlet = new LoginServlet(httpService, this.getThing().getUID().getId(), this, config);
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Wait for login");

        refreshLogin = scheduler.scheduleWithFixedDelay(this::checkLogin, 0, 60, TimeUnit.SECONDS);
        refreshJob = scheduler.scheduleWithFixedDelay(this::refreshData, 4, pollingIntervalInSeconds, TimeUnit.SECONDS);

        logger.debug("amazon account bridge handler started.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);
        if (command instanceof RefreshType) {
            refreshData();
        }
    }

    public List<Device> getLastKnownDevices() {
        return new ArrayList<>(jsonSerialNumberDeviceMapping.values());
    }

    public void addEchoHandler(EchoHandler echoHandler) {
        synchronized (echoHandlers) {
            echoHandlers.add(echoHandler);
        }
        Connection connection = this.connection;
        if (connection != null) {
            initializeEchoHandler(echoHandler, connection);
        }
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

    private void initializeEchoHandler(EchoHandler echoHandler, Connection connection) {
        intializeChildDevice(connection, echoHandler);

        @Nullable
        Device device = findDeviceJson(echoHandler);

        JsonBluetoothStates states = null;
        if (connection.getIsLoggedIn()) {
            states = connection.getBluetoothConnectionStates();
        }

        BluetoothState state = null;
        if (states != null) {
            state = states.findStateByDevice(device);
        }
        echoHandler.updateState(device, state);
    }

    private void intializeChildDevice(Connection connection, EchoHandler child) {
        Device deviceJson = this.findDeviceJson(child);
        if (deviceJson != null) {
            child.intialize(connection, deviceJson);
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
            AmazonEchoDiscovery instance = AmazonEchoDiscovery.instance;
            if (instance != null) {
                instance.removeExistingEchoHandler(childThing.getUID());
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
        logger.debug("cleanup {}", getThing().getUID().getAsString());
        @Nullable
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
        @Nullable
        ScheduledFuture<?> refreshLogin = this.refreshLogin;
        if (refreshLogin != null) {
            refreshLogin.cancel(true);
            this.refreshLogin = null;
        }
        Connection connection = this.connection;
        if (connection != null) {
            connection.logout();
            this.connection = null;
        }
    }

    private void checkLogin() {
        try {
            logger.debug("check login {}", getThing().getUID().getAsString());

            synchronized (synchronizeConnection) {
                Connection currentConnection = this.connection;
                if (currentConnection == null) {
                    return;
                }
                Date verifyTime = currentConnection.tryGetVerifyTime();
                Date currentDate = new Date();
                long currentTime = currentDate.getTime();
                if (verifyTime != null && currentTime - verifyTime.getTime() > 3600000) // Every one hour
                {
                    try {
                        if (!currentConnection.verifyLogin()) {
                            currentConnection.logout();
                        }
                    } catch (IOException | URISyntaxException e) {
                        logger.info("logout failed: {}", e.getMessage());
                        currentConnection.logout();
                    }
                }
                Date loginTime = currentConnection.tryGetLoginTime();
                if (loginTime != null && currentTime - loginTime.getTime() > 86400000 * 5) // 5 days
                {
                    // Recreate session
                    this.stateStorage.storeState("sessionStorage", "");
                    currentConnection = new Connection(currentConnection.getEmail(), currentConnection.getPassword(),
                            currentConnection.getAmazonSite(), this.getThing().getUID().getId());
                }
                boolean loginIsValid = true;
                if (!currentConnection.getIsLoggedIn()) {
                    try {

                        // read session data from property
                        String sessionStore = this.stateStorage.findState("sessionStorage");

                        // try use the session data
                        if (!currentConnection.tryRestoreLogin(sessionStore)) {
                            // session data not valid -> login
                            int retry = 0;
                            while (true) {
                                try {
                                    currentConnection.makeLogin();
                                    break;
                                } catch (ConnectionException e) {
                                    // Up to 2 retries for login
                                    retry++;
                                    if (retry >= 2) {
                                        currentConnection.logout();
                                        throw e;
                                    }
                                    // give amazon some time
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException exception) {
                                        // throw the original exception
                                        throw e;
                                    }
                                }
                            }
                            // store session data in property
                            String serializedStorage = currentConnection.serializeLoginData();
                            this.stateStorage.storeState("sessionStorage", serializedStorage);
                        }
                        this.connection = currentConnection;
                    } catch (UnknownHostException e) {
                        loginIsValid = false;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unknown host name '"
                                + e.getMessage() + "'. Maybe your internet connection is offline");
                    } catch (IOException e) {
                        loginIsValid = false;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                e.getLocalizedMessage());
                    } catch (URISyntaxException e) {
                        loginIsValid = false;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                e.getLocalizedMessage());
                    }
                    if (loginIsValid) {
                        handleValidLogin();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("check login fails {}", e);
        }
    }

    private void handleValidLogin() {
        updateDeviceList(false);
        updateStatus(ThingStatus.ONLINE);
        AmazonEchoDiscovery.addDiscoveryHandler(this);
    }

    // used to set a valid connection from the web proxy login
    public void setConnection(Connection connection) {
        this.connection = connection;
        String serializedStorage = connection.serializeLoginData();
        this.stateStorage.storeState("sessionStorage", serializedStorage);
        handleValidLogin();
    }

    private void refreshData() {
        try {
            logger.debug("refreshing data {}", getThing().getUID().getAsString());

            // check if logged in
            Connection currentConnection = null;
            synchronized (synchronizeConnection) {
                currentConnection = connection;
                if (currentConnection != null) {
                    if (!currentConnection.getIsLoggedIn()) {
                        return;
                    }
                }
            }
            if (currentConnection == null) {
                return;
            }

            // get all devices registered in the account
            updateDeviceList(false);

            // update bluetooth states
            JsonBluetoothStates states = null;
            if (currentConnection.getIsLoggedIn()) {
                states = currentConnection.getBluetoothConnectionStates();
            }

            // forward device information to echo handler
            for (EchoHandler child : echoHandlers) {
                Device device = findDeviceJson(child);
                BluetoothState state = null;
                if (states != null) {
                    state = states.findStateByDevice(device);
                }
                child.updateState(device, state);
            }

            // update account state
            updateStatus(ThingStatus.ONLINE);

            logger.debug("refresh data {} finished", getThing().getUID().getAsString());
        } catch (Exception e) {
            logger.error("refresh data failed: {}", e);
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

    @Override
    public void updateDeviceList(boolean manualScan) {
        if (manualScan) {
            discoverFlashProfiles = true;
        }

        Connection currentConnection = connection;
        if (currentConnection == null) {
            return;
        }
        AmazonEchoDiscovery discoveryService = AmazonEchoDiscovery.instance;

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

            if (discoveryService != null) {
                discoveryService.setDevices(getThing().getUID(), devices);
            }
        }
        synchronized (echoHandlers) {
            for (EchoHandler child : echoHandlers) {
                initializeEchoHandler(child, currentConnection);
            }
        }

        updateFlashBriefingHandlers(currentConnection);
    }

    public void setEnabledFlashBriefingsJson(String flashBriefingJson) {
        Connection currentConnection = connection;
        Gson gson = new Gson();
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

    public void updateFlashBriefingHandlers() {
        Connection currentConnection = connection;
        if (currentConnection != null) {
            updateFlashBriefingHandlers(currentConnection);
        }
    }

    private void updateFlashBriefingHandlers(Connection currentConnection) {
        synchronized (flashBriefingProfileHandlers) {
            if (!flashBriefingProfileHandlers.isEmpty() || currentFlashBriefingJson.isEmpty()) {
                updateFlashBriefingProfiles(currentConnection);
            }

            for (FlashBriefingProfileHandler child : flashBriefingProfileHandlers) {
                child.initialize(this, currentFlashBriefingJson);
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
            Gson gson = new Gson();
            this.currentFlashBriefingJson = gson.toJson(forSerializer);
        } catch (JsonSyntaxException | IOException | URISyntaxException e) {
            logger.warn("get flash briefing profiles fails {}", e);
        }

    }

}
