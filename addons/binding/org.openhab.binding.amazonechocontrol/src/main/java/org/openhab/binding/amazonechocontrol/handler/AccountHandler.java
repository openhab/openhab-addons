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
import org.openhab.binding.amazonechocontrol.internal.discovery.AmazonEchoDiscovery;
import org.openhab.binding.amazonechocontrol.internal.discovery.IAmazonEchoDiscovery;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the connection to the amazon server.
 *
 * @author Michael Geramb - Initial Contribution
 */
public class AccountHandler extends BaseBridgeHandler implements IAmazonEchoDiscovery {

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private AccountConfiguration config;
    private Connection connection;
    private List<EchoHandler> childs = new ArrayList<>();
    private Object synchronizeConnection = new Object();
    private Map<String, Device> jsonSerialNumberDeviceMapping = new HashMap<>();
    private ScheduledFuture<?> refreshJob;
    private ScheduledFuture<?> refreshLogin;

    public AccountHandler(@NonNull Bridge bridge) {
        super(bridge);
        AmazonEchoDiscovery.setHandlerExist();

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

    @Override
    public void childHandlerInitialized(@NonNull ThingHandler childHandler, @NonNull Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof EchoHandler) {
            EchoHandler echoHandler = (EchoHandler) childHandler;
            synchronized (childs) {
                childs.add(echoHandler);

                Connection temp = connection;
                if (temp != null) {
                    initializeChild(echoHandler, temp);
                }
            }
        }
    }

    private void initializeChild(@NonNull EchoHandler echoHandler, @NonNull Connection temp) {
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
            synchronized (childs) {
                childs.remove(childHandler);
            }

            AmazonEchoDiscovery instance = AmazonEchoDiscovery.instance;
            if (instance != null) {
                instance.removeExisting(childThing.getUID());
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
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Wait for login");
        refreshLogin = scheduler.scheduleWithFixedDelay(() -> {
            checkLogin();
        }, 0, 60, TimeUnit.SECONDS);

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
                this.updateProperty("sessionStorage", "");
                temp = new Connection(temp.getEmail(), temp.getPassword(), temp.getAmazonSite());
            }
            boolean loginIsValid = true;
            if (!temp.getIsLoggedIn()) {
                try {

                    // read session data from property
                    String sessionStore = this.thing.getProperties().get("sessionStorage");

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
                        this.updateProperty("sessionStorage", serializedStorage);
                    }
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
                    // update the device list
                    updateDeviceList();
                    updateStatus(ThingStatus.ONLINE);
                    AmazonEchoDiscovery.addDiscoveryHandler(this);
                }

            }
        }
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
            synchronized (childs) {

                updateDeviceList();

                JsonBluetoothStates states = null;
                if (temp.getIsLoggedIn()) {
                    try {
                        states = temp.getBluetoothConnectionStates();
                    } catch (Exception e) {
                        logger.info("getBluetoothConnectionStates failed: {}", e);
                    }
                }

                for (EchoHandler child : childs) {
                    Device device = findDeviceJson(child);
                    BluetoothState state = null;
                    if (states != null) {
                        state = states.findStateByDevice(device);
                    }
                    child.updateState(device, state);
                }
            }
            updateStatus(ThingStatus.ONLINE);

        } catch (Exception e) {
            logger.warn("Update states of amazon account failed: {}", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    public Device findDeviceJson(EchoHandler echoHandler) {
        String serialNumber = echoHandler.findSerialNumber();
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

    @Override
    synchronized public void updateDeviceList() {
        Connection temp = connection;
        if (temp == null) {
            return;
        }

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

            AmazonEchoDiscovery discoveryService = AmazonEchoDiscovery.instance;
            if (discoveryService != null) {
                discoveryService.setDevices(getThing().getUID(), devices);
            }
        }
        synchronized (childs) {
            for (EchoHandler child : childs) {
                if (child != null) {
                    initializeChild(child, temp);
                }
            }
        }
    }

    private void intializeChildDevice(@NonNull Connection connection, @NonNull EchoHandler child) {
        Device deviceJson = this.findDeviceJson(child);
        if (deviceJson != null) {
            child.intialize(connection, deviceJson);
        }
    }

}
