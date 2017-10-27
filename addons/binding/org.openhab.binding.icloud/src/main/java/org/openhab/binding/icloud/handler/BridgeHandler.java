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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.icloud.discovery.DeviceDiscovery;
import org.openhab.binding.icloud.internal.Address;
import org.openhab.binding.icloud.internal.AddressLookup;
import org.openhab.binding.icloud.internal.AddressLookupParser;
import org.openhab.binding.icloud.internal.Configuration;
import org.openhab.binding.icloud.internal.Connection;
import org.openhab.binding.icloud.internal.DeviceInformationParser;
import org.openhab.binding.icloud.internal.json.icloud.Content;
import org.openhab.binding.icloud.internal.json.icloud.JSONRootObject;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrik Gfeller
 * @author Hans-Jörg Merk
 */
public class BridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);
    private Connection connection;
    private Configuration config;
    ServiceRegistration<?> service;
    DeviceDiscovery discoveryService;

    private Object synchronizeRefresh = new Object();

    private List<DeviceHandler> iCloudDeviceHandlers = Collections.synchronizedList(new ArrayList<DeviceHandler>());
    ScheduledFuture<?> refreshJob;
    private JSONRootObject iCloudData;

    public BridgeHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        if (channelId.equals(FORCEDREFRESH)) {
            if (command == OnOffType.ON) {
                refreshData();
                updateState(FORCEDREFRESH, OnOffType.OFF);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("iCloud bridge handler initializing ...");
        startHandler();
        logger.debug("lCloud bridge initialized.");
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        refreshJob.cancel(true);
        refreshJob = null;
        this.startHandler();
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }

    public void registerDevice(DeviceHandler device) {
        iCloudDeviceHandlers.add(device);
    }

    public void unregisterDevice(DeviceHandler device) {
        iCloudDeviceHandlers.remove(device);
    }

    @Override
    public void dispose() {
        service.unregister();
        this.refreshJob.cancel(true);
        super.dispose();
    }

    @Override
    public void childHandlerInitialized(@NonNull ThingHandler childHandler, @NonNull Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (iCloudData != null) {
            ((DeviceHandler) childHandler).update(iCloudData.getContent());
        }
    }

    /***
     * Use google API to lookup the address.
     *
     * @param location
     * @return
     */
    public Address getAddress(PointType location) {
        Address address = null;
        String json = null;

        try {
            json = new AddressLookup(config.GoogleAPIKey).getAddressJSON(location);
            if (json != null && !json.equals("")) {
                AddressLookupParser parser = new AddressLookupParser(json);
                address = parser.getAddress();
            }
        } catch (Exception e) {
            logger.debug("getAddress failed:");
            logger.debug("{}", json);
            logException(e);
        }

        return address;
    }

    public void pingPhone(String deviceId) {
        try {
            connection.pingPhone(deviceId);
        } catch (Exception e) {
            logger.debug("{}", e.getMessage(), e.getStackTrace().toString());
        }
    }

    private void startHandler() {
        logger.debug("iCloud bridge starting handler ...");
        config = getConfigAs(Configuration.class);

        registerDeviceDiscoveryService();

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            refreshData();
        }, 0, config.RefreshTimeInMinutes, TimeUnit.MINUTES);
        logger.debug("iCloud bridge handler started.");
    }

    private void refreshData() {
        synchronized (synchronizeRefresh) {
            try {
                logger.debug("iCloud bridge refreshing data ...");

                connection = new Connection(config.AppleId, config.Password);

                String json = connection.requestDeviceStatusJSON();
                if (json != null && !json.equals("")) {
                    DeviceInformationParser parser = new DeviceInformationParser(json);
                    iCloudData = parser.data;

                    int statusCode = Integer.parseUnsignedInt(iCloudData.getStatusCode());
                    if (statusCode == 200) {
                        updateStatus(ThingStatus.ONLINE);

                        updateBridgeChannels(iCloudData);
                        updateDevices(iCloudData.getContent());

                        discoveryService.discover(iCloudData.getContent());
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Status = " + statusCode);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }

                logger.debug("iCloud bridge data refresh complete.");
            } catch (Exception e) {
                logException(e);
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    private void registerDeviceDiscoveryService() {
        discoveryService = new DeviceDiscovery(this);
        service = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    private void updateDevices(ArrayList<Content> content) {
        this.iCloudDeviceHandlers.forEach(device -> device.update(content));
    }

    private void updateBridgeChannels(JSONRootObject iCloudData) {
        String firstName = iCloudData.getUserInfo().getFirstName();
        String lastName = iCloudData.getUserInfo().getLastName();

        updateState(NUMBEROFDEVICES, new DecimalType(iCloudData.getContent().toArray().length));
        updateState(OWNER, new StringType(firstName + " " + lastName));
    }

    private void logException(Exception exception) {
        logger.error("{}", exception.getMessage() + "\n" + exception.getStackTrace().toString());
    }

}
