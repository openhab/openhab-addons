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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.config.InsteonNetworkConfiguration;
import org.openhab.binding.insteon.internal.discovery.InsteonDeviceDiscoveryService;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
    private static final int RETRY_DELAY_IN_SECONDS = 30;
    private static final int SETTLE_TIME_IN_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(InsteonNetworkHandler.class);

    private @Nullable InsteonNetworkConfiguration config;
    private @Nullable InsteonBinding insteonBinding;
    private @Nullable InsteonDeviceDiscoveryService insteonDeviceDiscoveryService;
    private @Nullable ScheduledFuture<?> pollingJob = null;
    private @Nullable ScheduledFuture<?> reconnectJob = null;
    private @Nullable ScheduledFuture<?> settleJob = null;
    private long lastInsteonDeviceCreatedTimestamp = 0;
    private @Nullable SerialPortManager serialPortManager;
    private Map<String, String> deviceInfo = new ConcurrentHashMap<>();
    private Map<String, String> channelInfo = new ConcurrentHashMap<>();

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
            insteonBinding = new InsteonBinding(this, config, serialPortManager, scheduler);

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

                    settleJob.cancel(false);
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

        if (reconnectJob != null) {
            reconnectJob.cancel(true);
            reconnectJob = null;
        }

        if (settleJob != null) {
            settleJob.cancel(true);
            settleJob = null;
        }

        if (insteonBinding != null) {
            insteonBinding.shutdown();
            insteonBinding = null;
        }

        deviceInfo.clear();
        channelInfo.clear();

        super.dispose();
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    public void bindingDisconnected() {
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            if (insteonBinding.reconnect()) {
                updateStatus(ThingStatus.ONLINE);
                reconnectJob.cancel(false);
                reconnectJob = null;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Port disconnected.");
            }
        }, 0, RETRY_DELAY_IN_SECONDS, TimeUnit.SECONDS);
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

    public void displayDevices(Console console) {
        display(console, deviceInfo);
    }

    public void displayChannels(Console console) {
        display(console, channelInfo);
    }

    public void displayLocalDatabase(Console console) {
        Map<String, String> databaseInfo = insteonBinding.getDatabaseInfo();
        console.println("local database contains " + databaseInfo.size() + " entries");
        display(console, databaseInfo);
    }

    public void initialized(ThingUID uid, String msg) {
        deviceInfo.put(uid.getAsString(), msg);
    }

    public void disposed(ThingUID uid) {
        deviceInfo.remove(uid.getAsString());
    }

    public boolean isChannelLinked(ChannelUID uid) {
        return channelInfo.containsKey(uid.getAsString());
    }

    public void linked(ChannelUID uid, String msg) {
        channelInfo.put(uid.getAsString(), msg);
    }

    public void unlinked(ChannelUID uid) {
        channelInfo.remove(uid.getAsString());
    }

    private void display(Console console, Map<String, String> info) {
        ArrayList<String> ids = new ArrayList<>(info.keySet());
        Collections.sort(ids);
        for (String id : ids) {
            console.println(info.get(id));
        }
    }
}
