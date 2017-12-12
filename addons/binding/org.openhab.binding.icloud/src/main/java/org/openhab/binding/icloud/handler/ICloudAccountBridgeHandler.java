/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.handler;

import static org.openhab.binding.icloud.BindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.icloud.internal.Connection;
import org.openhab.binding.icloud.internal.DeviceInformationParser;
import org.openhab.binding.icloud.internal.configuration.AccountThingConfiguration;
import org.openhab.binding.icloud.internal.discovery.DeviceDiscovery;
import org.openhab.binding.icloud.internal.json.DeviceInformation;
import org.openhab.binding.icloud.internal.json.JSONRootObject;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves the data for a given account from iCloud and passes the
 * information to {@link DeviceDiscover} and to the {@link ICloudDeviceHandler}s.
 *
 * @author Patrik Gfeller - Initial Contribution
 * @author Hans-Jörg Merk
 */
public class ICloudAccountBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ICloudAccountBridgeHandler.class);
    private final DeviceInformationParser deviceInformationParser = new DeviceInformationParser();
    private Connection connection;
    private AccountThingConfiguration config;
    ServiceRegistration<?> service;

    private Object synchronizeRefresh = new Object();

    private List<ICloudDeviceHandler> iCloudDeviceHandlers = Collections.synchronizedList(new ArrayList<ICloudDeviceHandler>());
    private List<DeviceDiscovery> deviceDiscoveryListeners = Collections
            .synchronizedList(new ArrayList<DeviceDiscovery>());

    ScheduledFuture<?> refreshJob;
    private JSONRootObject iCloudData;

    public ICloudAccountBridgeHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        switch (channelId) {
            case REFRESH:
                refreshDeviceData(command);
                break;
        }
    }

    @Override
    public void initialize() {
        logger.debug("iCloud bridge handler initializing ...");
        startHandler();
        logger.debug("iCloud bridge initialized.");
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }

    public void registerDevice(ICloudDeviceHandler device) {
        iCloudDeviceHandlers.add(device);
    }

    public void unregisterDevice(ICloudDeviceHandler device) {
        iCloudDeviceHandlers.remove(device);
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        super.dispose();
    }

    @Override
    public void childHandlerInitialized(@NonNull ThingHandler childHandler, @NonNull Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (iCloudData != null) {
            ((ICloudDeviceHandler) childHandler).update(iCloudData.getContent());
        }
    }

    public void findMyDevice(String deviceId) throws IOException {
        connection.findMyDevice(deviceId);
    }

    public void registerDiscovery(DeviceDiscovery deviceDiscovery) {
        deviceDiscoveryListeners.add(deviceDiscovery);
    }

    public void unregisterDiscovery(DeviceDiscovery deviceDiscovery) {
        deviceDiscoveryListeners.remove(deviceDiscovery);
    }

    private void startHandler() {
        logger.debug("iCloud bridge starting handler ...");
        config = getConfigAs(AccountThingConfiguration.class);

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                refreshData();
            } catch (IOException e) {
                logger.warn("Unable to refresh device data", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, config.refreshTimeInMinutes, TimeUnit.MINUTES);
        logger.debug("iCloud bridge handler started.");
    }

    private void refreshDeviceData(Command command) {
        if (command == OnOffType.ON) {
            try {
                refreshData();
            } catch (IOException e) {
                logger.warn("Unable to refresh device data", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            updateState(REFRESH, OnOffType.OFF);
        }
    }

    private void refreshData() throws IOException {
        synchronized (synchronizeRefresh) {
            logger.debug("iCloud bridge refreshing data ...");

            connection = new Connection(config.appleId, config.password);
            String json = connection.requestDeviceStatusJSON();

            logger.trace("json: {}", json);

            iCloudData = deviceInformationParser.parse(json);

            int statusCode = Integer.parseUnsignedInt(iCloudData.getStatusCode());
            if (statusCode == 200) {
                updateStatus(ThingStatus.ONLINE);

                updateBridgeChannels(iCloudData);
                updateDevices(iCloudData.getContent());
                updateDiscovery(iCloudData.getContent());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Status = " + statusCode + ", Response = " + json);
            }

            logger.debug("iCloud bridge data refresh complete.");
        }
    }

    private void updateDiscovery(ArrayList<DeviceInformation> content) {
        this.deviceDiscoveryListeners.forEach(discovery -> discovery.discover(content));
    }

    private void updateDevices(ArrayList<DeviceInformation> content) {
        this.iCloudDeviceHandlers.forEach(device -> device.update(content));
    }

    private void updateBridgeChannels(JSONRootObject iCloudData) {
        String firstName = iCloudData.getUserInfo().getFirstName();
        String lastName = iCloudData.getUserInfo().getLastName();

        updateState(OWNER, new StringType(firstName + " " + lastName));
    }

}
