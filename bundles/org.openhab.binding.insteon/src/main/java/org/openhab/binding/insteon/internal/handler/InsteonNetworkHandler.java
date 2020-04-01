/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.handler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.config.InsteonNetworkConfiguration;
import org.openhab.binding.insteon.internal.discovery.InsteonDeviceDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonNetworkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class InsteonNetworkHandler extends BaseBridgeHandler {

    private static final int LOG_DEVICE_STATISTICS_DELAY_IN_SECONDS = 600;
    private static final int SETTLE_TIME_IN_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(InsteonNetworkHandler.class);

    private @Nullable InsteonNetworkConfiguration config;
    private @Nullable InsteonBinding insteonBinding;
    private @Nullable InsteonDeviceDiscoveryService insteonDeviceDiscoveryService;
    private @Nullable ScheduledFuture<?> pollingJob = null;
    private @Nullable ScheduledFuture<?> settleJob = null;
    private long lastInsteonDeviceCreatedTimestamp = 0;
    private @Nullable SerialPortManager serialPortManager;

    public static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public InsteonNetworkHandler(Bridge bridge, @Nullable SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("Starting Insteon bridge");
        config = getConfigAs(InsteonNetworkConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            insteonBinding = new InsteonBinding(this, config, serialPortManager);

            // hold off on starting to poll until devices that already are defined as things are added.
            // wait SETTLE_TIME_IN_SECONDS to start then check every second afterwards until it has been at
            // least SETTLE_TIME_IN_SECONDS since last device was created.
            settleJob = scheduler.scheduleWithFixedDelay(() -> {
                // check to see if it has been at least SETTLE_TIME_IN_SECONDS since last device was created
                if (System.currentTimeMillis() - lastInsteonDeviceCreatedTimestamp > SETTLE_TIME_IN_SECONDS * 1000) {
                    // settle time has expired start polling
                    if (insteonBinding.startPolling()) {
                        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                            insteonBinding.logDeviceStatistics();
                        }, 0, LOG_DEVICE_STATISTICS_DELAY_IN_SECONDS, TimeUnit.SECONDS);

                        insteonBinding.setIsActive(true);

                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        String msg = "Initialization failed, unable to start the Insteon bridge with the port '"
                                + config.getPort() + "'.";
                        logger.warn(msg);

                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                    }

                    settleJob.cancel(true);
                    settleJob = null;
                }
            }, SETTLE_TIME_IN_SECONDS, 1, TimeUnit.SECONDS);
        });
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Insteon bridge");

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        if (settleJob != null) {
            settleJob.cancel(true);
            settleJob = null;
        }

        insteonBinding.shutdown();

        super.dispose();
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    public void insteonDeviceWasCreated() {
        lastInsteonDeviceCreatedTimestamp = System.currentTimeMillis();
    }

    public @Nullable InsteonBinding getInsteonBinding() {
        return insteonBinding;
    }

    public void setInsteonDeviceDiscoveryService(InsteonDeviceDiscoveryService insteonDeviceDiscoveryService) {
        this.insteonDeviceDiscoveryService = insteonDeviceDiscoveryService;
    }

    public void addMissingDevices(List<String> missing) {
        scheduler.execute(() -> {
            insteonDeviceDiscoveryService.addInsteonDevices(missing, getThing().getUID());
        });
    }
}
