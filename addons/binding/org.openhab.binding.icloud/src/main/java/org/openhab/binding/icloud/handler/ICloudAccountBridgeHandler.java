/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.icloud.internal.Connection;
import org.openhab.binding.icloud.internal.DeviceInformationParser;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationListener;
import org.openhab.binding.icloud.internal.configuration.AccountThingConfiguration;
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
    private final int CACHE_EXPIRY = 5 * 1000; // 5s
    private ExpiringCache<String> iCloudDeviceInformationCache;

    ServiceRegistration<?> service;

    private Object synchronizeRefresh = new Object();

    private List<ICloudDeviceInformationListener> deviceInformationListeners = Collections
            .synchronizedList(new ArrayList<ICloudDeviceInformationListener>());

    ScheduledFuture<?> refreshJob;

    public ICloudAccountBridgeHandler(@NonNull Bridge bridge) {
        super(bridge);
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
        logger.debug("iCloud bridge handler initializing ...");
        iCloudDeviceInformationCache = new ExpiringCache<String>(CACHE_EXPIRY, () -> {
            try {
                connection = new Connection(config.appleId, config.password);
                return connection.requestDeviceStatusJSON();
            } catch (IOException e) {
                logger.warn("Unable to refresh device data", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return null;
            }
        });

        startHandler();
        logger.debug("iCloud bridge initialized.");
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        super.dispose();
    }

    public void findMyDevice(String deviceId) throws IOException {
        connection.findMyDevice(deviceId);
    }

    public void registerListener(ICloudDeviceInformationListener listener) {
        deviceInformationListeners.add(listener);
    }

    public void unregisterListener(ICloudDeviceInformationListener listener) {
        deviceInformationListeners.remove(listener);
    }

    private void startHandler() {
        logger.debug("iCloud bridge starting handler ...");
        config = getConfigAs(AccountThingConfiguration.class);

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            refreshData();
        }, 0, config.refreshTimeInMinutes, TimeUnit.MINUTES);
        logger.debug("iCloud bridge handler started.");
    }

    public void refreshData() {
        synchronized (synchronizeRefresh) {
            logger.debug("iCloud bridge refreshing data ...");

            String json = iCloudDeviceInformationCache.getValue();
            logger.trace("json: {}", json);

            if (json == null) {
                return;
            }

            JSONRootObject iCloudData = deviceInformationParser.parse(json);

            int statusCode = Integer.parseUnsignedInt(iCloudData.getStatusCode());
            if (statusCode == 200) {
                updateStatus(ThingStatus.ONLINE);
                informDeviceInformationListeners(iCloudData.getContent());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Status = " + statusCode + ", Response = " + json);
            }

            logger.debug("iCloud bridge data refresh complete.");
        }
    }

    private void informDeviceInformationListeners(List<DeviceInformation> deviceInformationList) {
        this.deviceInformationListeners.forEach(discovery -> discovery.deviceInformationUpdate(deviceInformationList));
    }
}
