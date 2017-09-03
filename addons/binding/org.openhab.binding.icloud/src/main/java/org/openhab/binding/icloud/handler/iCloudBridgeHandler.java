/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.handler;

import static org.openhab.binding.icloud.iCloudBindingConstants.*;

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
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.icloud.discovery.iCloudDeviceDiscovery;
import org.openhab.binding.icloud.internal.Address;
import org.openhab.binding.icloud.internal.AddressLookup;
import org.openhab.binding.icloud.internal.AddressLookupParser;
import org.openhab.binding.icloud.internal.iCloudConfiguration;
import org.openhab.binding.icloud.internal.iCloudConnection;
import org.openhab.binding.icloud.internal.iCloudDeviceInformationParser;
import org.openhab.binding.icloud.internal.json.iCloud.Content;
import org.openhab.binding.icloud.internal.json.iCloud.JSONRootObject;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrik Gfeller
 * @author Hans-Jörg Merk
 */
public class iCloudBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(iCloudBridgeHandler.class);
    private iCloudConnection connection;
    private iCloudConfiguration config;
    ServiceRegistration<?> service;
    iCloudDeviceDiscovery discoveryService;

    private List<iCloudDeviceHandler> iCloudDeviceHandlers = Collections
            .synchronizedList(new ArrayList<iCloudDeviceHandler>());
    ScheduledFuture<?> refreshJob;
    private JSONRootObject iCloudData;

    public iCloudBridgeHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
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

    public void registerDevice(iCloudDeviceHandler device) {
        iCloudDeviceHandlers.add(device);
    }

    public void unregisterDevice(iCloudDeviceHandler device) {
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
        ((iCloudDeviceHandler) childHandler).update(iCloudData.getContent());
    }

    /***
     * Use google API to lookup the address.
     *
     * @param location
     * @return
     */
    public Address getAddress(PointType location) {
        Address address = new Address();

        try {
            String json = new AddressLookup().getAddressJSON(location);
            if (json != null && !json.equals("")) {
                AddressLookupParser parser = new AddressLookupParser(json);
                address = parser.getAddress();
            }

        } catch (Exception e) {
            logException(e);
        }

        return address;
    }

    public void pingPhone(String deviceId) {
        try {
            connection.pingPhone(deviceId);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e.getStackTrace().toString());
        }
    }

    private void startHandler() {
        logger.debug("iCloud bridge starting handler ...");
        config = getConfigAs(iCloudConfiguration.class);

        registerDeviceDiscoveryService();

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("iCloud bridge refreshing data ...");

                connection = new iCloudConnection(config.AppleId, config.Password);

                updateStatus(ThingStatus.ONLINE);

                String json = connection.requestDeviceStatusJSON();
                if (json != null && !json.equals("")) {
                    iCloudDeviceInformationParser parser = new iCloudDeviceInformationParser(json);

                    iCloudData = parser.data;

                    updateBridgeChannels(iCloudData);
                    updateDevices(iCloudData.getContent());
                    discoveryService.discover(iCloudData.getContent());
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }

                logger.debug("iCloud bridge data refresh complete.");
            } catch (Exception e) {
                logException(e);
                updateStatus(ThingStatus.OFFLINE);
            }
        }, 0, config.RefreshTimeInMinutes, TimeUnit.MINUTES);
        logger.debug("iCloud bridge handler started.");
    }

    private void registerDeviceDiscoveryService() {
        discoveryService = new iCloudDeviceDiscovery(this);
        service = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());

    }

    private void updateDevices(ArrayList<Content> content) {
        this.iCloudDeviceHandlers.forEach(device -> device.update(content));
    }

    private void updateBridgeChannels(JSONRootObject iCloudData) {
        String firstName = iCloudData.getUserInfo().getFirstName();
        String lastName = iCloudData.getUserInfo().getLastName();
        int httpStatusCode = Integer.parseUnsignedInt(iCloudData.getStatusCode());

        updateState(NUMBEROFDEVICES, new DecimalType(iCloudData.getContent().toArray().length));
        updateState(OWNER, new StringType(firstName + " " + lastName));
        updateState(HTTPSTATUSCODE, new DecimalType(httpStatusCode));
    }

    private void logException(Exception exception) {
        logger.error(exception.getMessage() + "\n" + exception.getStackTrace());
    }

}
