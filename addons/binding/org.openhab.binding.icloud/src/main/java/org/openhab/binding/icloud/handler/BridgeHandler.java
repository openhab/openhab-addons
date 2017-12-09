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
import org.openhab.binding.icloud.internal.Connection;
import org.openhab.binding.icloud.internal.DeviceInformationParser;
import org.openhab.binding.icloud.internal.configuration.AccountThingConfiguration;
import org.openhab.binding.icloud.internal.json.icloud.Content;
import org.openhab.binding.icloud.internal.json.icloud.JSONRootObject;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves the data for a given account from iCloud and passes the
 * information to {@link DeviceDiscover} and to the {@link DeviceHandler}s.
 *
 * @author Patrik Gfeller - Initial Contribution
 * @author Hans-Jörg Merk
 */
public class BridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);
    private final AddressLookupParser addressLoopupParser = new AddressLookupParser();
    private final DeviceInformationParser deviceInformationParser = new DeviceInformationParser();
    private Connection connection;
    private AccountThingConfiguration config;
    private boolean addressLookupIsEnabled = false;
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

    public void registerDevice(DeviceHandler device) {
        iCloudDeviceHandlers.add(device);
    }

    public void unregisterDevice(DeviceHandler device) {
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
            if (addressLookupIsEnabled) {
                json = new AddressLookup(config.googleAPIKey).getAddressJSON(location);
                if (json != null && !json.equals("")) {
                    address = addressLoopupParser.getAddress(json);
                }
            }
        } catch (Exception e) {
            logger.debug("getAddress failed: {}", json, e);
        }

        return address;
    }

    public void findMyDevice(String deviceId) {
        try {
            connection.findMyDevice(deviceId);
        } catch (Exception e) {
            logger.warn("Unable to execute \"find my device\" ", e);
        }
    }

    private void startHandler() {
        logger.debug("iCloud bridge starting handler ...");
        config = getConfigAs(AccountThingConfiguration.class);

        // Enable google address lookup if an API key is configured
        if (config.googleAPIKey != null && config.googleAPIKey != "") {
            addressLookupIsEnabled = true;
        } else {
            addressLookupIsEnabled = false;
        }

        registerDeviceDiscoveryService();

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            refreshData();
        }, 0, config.refreshTimeInMinutes, TimeUnit.MINUTES);
        logger.debug("iCloud bridge handler started.");
    }

    private void refreshDeviceData(Command command) {
        if (command == OnOffType.ON) {
            refreshData();
            updateState(REFRESH, OnOffType.OFF);
        }
    }

    private void refreshData() {
        synchronized (synchronizeRefresh) {
            try {
                logger.debug("iCloud bridge refreshing data ...");

                connection = new Connection(config.appleId, config.password);
                String json = connection.requestDeviceStatusJSON();

                logger.trace("json: {}", json);

                if (json != null && !json.equals("")) {
                    iCloudData = deviceInformationParser.parse(json);

                    int statusCode = Integer.parseUnsignedInt(iCloudData.getStatusCode());
                    if (statusCode == 200) {
                        updateStatus(ThingStatus.ONLINE);

                        updateBridgeChannels(iCloudData);
                        updateDevices(iCloudData.getContent());

                        discoveryService.discover(iCloudData.getContent());
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Status = " + statusCode + ", Response = " + json);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }

                logger.debug("iCloud bridge data refresh complete.");
            } catch (Exception e) {
                logger.warn("Unable to read data from iCloud", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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

        updateState(NUMBER_OF_DEVICES, new DecimalType(iCloudData.getContent().toArray().length));
        updateState(OWNER, new StringType(firstName + " " + lastName));
    }

}
