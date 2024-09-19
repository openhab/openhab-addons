/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.api.MelCloudConnection;
import org.openhab.binding.melcloud.internal.api.dto.Device;
import org.openhab.binding.melcloud.internal.api.dto.DeviceStatus;
import org.openhab.binding.melcloud.internal.api.dto.HeatpumpDeviceStatus;
import org.openhab.binding.melcloud.internal.config.AccountConfig;
import org.openhab.binding.melcloud.internal.discovery.MelCloudDiscoveryService;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudLoginException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MelCloudAccountHandler} is the handler for MELCloud API and connects it
 * to the webservice.
 *
 * @author Luca Calcaterra - Initial contribution
 * @author Pauli Anttila - Refactoring
 * @author Wietse van Buitenen - Return all devices, added heatpump device
 */
@NonNullByDefault
public class MelCloudAccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(MelCloudAccountHandler.class);

    private MelCloudConnection connection = new MelCloudConnection();
    private List<Device> devices = Collections.emptyList();
    private @Nullable ScheduledFuture<?> connectionCheckTask;
    private AccountConfig config = new AccountConfig();
    private boolean loginCredentialError = false;

    public MelCloudAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MelCloudDiscoveryService.class);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MELCloud account handler.");
        config = getConfigAs(AccountConfig.class);
        loginCredentialError = false;
        startConnectionCheck();
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        stopConnectionCheck();
        devices = Collections.emptyList();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public ThingUID getID() {
        return getThing().getUID();
    }

    public List<Device> getDeviceList() throws MelCloudCommException, MelCloudLoginException {
        connectIfNotConnected();
        return connection.fetchDeviceList();
    }

    private void connect() throws MelCloudCommException, MelCloudLoginException {
        if (loginCredentialError) {
            throw new MelCloudLoginException("Connection to MELCloud can't be opened because of wrong credentials");
        }
        logger.debug("Initializing connection to MELCloud");

        updateStatus(ThingStatus.OFFLINE);
        try {
            connection.login(config.username, config.password, config.language);
            devices = connection.fetchDeviceList();
            updateStatus(ThingStatus.ONLINE);
        } catch (MelCloudLoginException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            loginCredentialError = true;
            throw e;
        } catch (MelCloudCommException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw e;
        }
    }

    private synchronized void connectIfNotConnected() throws MelCloudCommException, MelCloudLoginException {
        if (!isConnected()) {
            connect();
        }
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public DeviceStatus sendDeviceStatus(DeviceStatus deviceStatus)
            throws MelCloudCommException, MelCloudLoginException {
        connectIfNotConnected();
        try {
            return connection.sendDeviceStatus(deviceStatus);
        } catch (MelCloudCommException e) {
            logger.debug("Sending failed, retry once with relogin");
            connect();
            return connection.sendDeviceStatus(deviceStatus);
        }
    }

    public DeviceStatus fetchDeviceStatus(int deviceId, @Nullable Integer buildingId)
            throws MelCloudCommException, MelCloudLoginException {
        connectIfNotConnected();
        int bid = buildingId != null ? buildingId : findBuildingId(deviceId);

        try {
            return connection.fetchDeviceStatus(deviceId, bid);
        } catch (MelCloudCommException e) {
            logger.debug("Sending failed, retry once with relogin");
            connect();
            return connection.fetchDeviceStatus(deviceId, bid);
        }
    }

    public HeatpumpDeviceStatus sendHeatpumpDeviceStatus(HeatpumpDeviceStatus heatpumpDeviceStatus)
            throws MelCloudCommException, MelCloudLoginException {
        connectIfNotConnected();
        try {
            return connection.sendHeatpumpDeviceStatus(heatpumpDeviceStatus);
        } catch (MelCloudCommException e) {
            logger.debug("Sending failed, retry once with relogin");
            connect();
            return connection.sendHeatpumpDeviceStatus(heatpumpDeviceStatus);
        }
    }

    public HeatpumpDeviceStatus fetchHeatpumpDeviceStatus(int deviceId, @Nullable Integer buildingId)
            throws MelCloudCommException, MelCloudLoginException {
        connectIfNotConnected();
        int bid = buildingId != null ? buildingId : findBuildingId(deviceId);

        try {
            return connection.fetchHeatpumpDeviceStatus(deviceId, bid);
        } catch (MelCloudCommException e) {
            logger.debug("Sending failed, retry once with relogin");
            connect();
            return connection.fetchHeatpumpDeviceStatus(deviceId, bid);
        }
    }

    private int findBuildingId(int deviceId) throws MelCloudCommException {
        return devices.stream().filter(d -> d.getDeviceID() == deviceId).findFirst().orElseThrow(
                () -> new MelCloudCommException(String.format("Can't find building id for device id %s", deviceId)))
                .getBuildingID();
    }

    private void startConnectionCheck() {
        ScheduledFuture<?> connectionCheckTask = this.connectionCheckTask;
        if (connectionCheckTask == null || connectionCheckTask.isCancelled()) {
            logger.debug("Start periodic connection check");
            Runnable runnable = () -> {
                logger.debug("Check MELCloud connection");
                if (connection.isConnected()) {
                    logger.debug("Connection to MELCloud open");
                } else {
                    try {
                        connect();
                    } catch (MelCloudLoginException e) {
                        logger.debug("Connection to MELCloud down due to login error, reason: {}.", e.getMessage());
                    } catch (MelCloudCommException e) {
                        logger.debug("Connection to MELCloud down, reason: {}.", e.getMessage());
                    } catch (RuntimeException e) {
                        logger.warn("Unknown error occurred during connection check, reason: {}.", e.getMessage(), e);
                    }
                }
            };
            this.connectionCheckTask = scheduler.scheduleWithFixedDelay(runnable, 0, 300, TimeUnit.SECONDS);
        } else {
            logger.debug("Connection check task already running");
        }
    }

    private void stopConnectionCheck() {
        ScheduledFuture<?> connectionCheckTask = this.connectionCheckTask;
        if (connectionCheckTask != null) {
            logger.debug("Stop periodic connection check");
            connectionCheckTask.cancel(true);
            this.connectionCheckTask = null;
        }
    }
}
