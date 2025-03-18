/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.InsteonLegacyBinding;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonLegacyNetworkConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceAddress;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.discovery.InsteonLegacyDiscoveryService;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.console.Console;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingManager;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonLegacyNetworkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonLegacyNetworkHandler extends BaseBridgeHandler {
    private static final int DRIVER_INITIALIZED_TIME_IN_SECONDS = 1;
    private static final int LOG_DEVICE_STATISTICS_DELAY_IN_SECONDS = 600;
    private static final int RETRY_DELAY_IN_SECONDS = 30;
    private static final int SETTLE_TIME_IN_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(InsteonLegacyNetworkHandler.class);

    private final ScheduledExecutorService insteonScheduler = ThreadPoolManager
            .getScheduledPool(InsteonBindingConstants.BINDING_ID + "-" + getThing().getUID().getId());

    private @Nullable InsteonLegacyBinding insteonBinding;
    private @Nullable InsteonLegacyDiscoveryService insteonDiscoveryService;
    private @Nullable ScheduledFuture<?> driverInitializedJob = null;
    private @Nullable ScheduledFuture<?> pollingJob = null;
    private @Nullable ScheduledFuture<?> reconnectJob = null;
    private @Nullable ScheduledFuture<?> settleJob = null;
    private long lastInsteonDeviceCreatedTimestamp = 0;
    private HttpClient httpClient;
    private SerialPortManager serialPortManager;
    private ThingManager thingManager;
    private ThingRegistry thingRegistry;
    private Map<String, String> deviceInfo = new ConcurrentHashMap<>();
    private Map<String, String> channelInfo = new ConcurrentHashMap<>();
    private Map<ChannelUID, Channel> channelCache = new ConcurrentHashMap<>();

    public InsteonLegacyNetworkHandler(Bridge bridge, HttpClient httpClient, SerialPortManager serialPortManager,
            ThingManager thingManager, ThingRegistry thingRegistry) {
        super(bridge);
        this.httpClient = httpClient;
        this.serialPortManager = serialPortManager;
        this.thingManager = thingManager;
        this.thingRegistry = thingRegistry;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("Starting Insteon bridge");

        InsteonLegacyNetworkConfiguration config = getConfigAs(InsteonLegacyNetworkConfiguration.class);
        if (!config.isParsable()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to parse port configuration.");
            return;
        }

        InsteonBridgeHandler handler = getBridgeHandler(config.parse());
        if (handler != null) {
            logger.info("Disabling Insteon legacy network bridge {} in favor of bridge {}", getThing().getUID(),
                    handler.getThing().getUID());
            disable();
            return;
        }

        insteonBinding = new InsteonLegacyBinding(this, config, httpClient, insteonScheduler, serialPortManager);
        updateStatus(ThingStatus.UNKNOWN);

        // hold off on starting to poll until devices that already are defined as things are added.
        // wait SETTLE_TIME_IN_SECONDS to start then check every second afterwards until it has been at
        // least SETTLE_TIME_IN_SECONDS since last device was created.
        settleJob = scheduler.scheduleWithFixedDelay(() -> {
            // check to see if it has been at least SETTLE_TIME_IN_SECONDS since last device was created
            if (System.currentTimeMillis() - lastInsteonDeviceCreatedTimestamp > SETTLE_TIME_IN_SECONDS * 1000) {
                // settle time has expired start polling
                InsteonLegacyBinding insteonBinding = this.insteonBinding;
                if (insteonBinding != null && insteonBinding.startPolling()) {
                    pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                        insteonBinding.logDeviceStatistics();
                    }, 0, LOG_DEVICE_STATISTICS_DELAY_IN_SECONDS, TimeUnit.SECONDS);

                    // wait until driver is initialized before setting network to ONLINE
                    driverInitializedJob = scheduler.scheduleWithFixedDelay(() -> {
                        if (insteonBinding.isDriverInitialized()) {
                            logger.debug("driver is initialized");

                            insteonBinding.setIsActive(true);

                            updateStatus(ThingStatus.ONLINE);

                            ScheduledFuture<?> driverInitializedJob = this.driverInitializedJob;
                            if (driverInitializedJob != null) {
                                driverInitializedJob.cancel(false);
                                this.driverInitializedJob = null;
                            }
                        } else {
                            logger.trace("driver is not initialized yet");
                        }
                    }, 0, DRIVER_INITIALIZED_TIME_IN_SECONDS, TimeUnit.SECONDS);
                } else {
                    String msg = "Initialization failed, unable to start the Insteon bridge with the port '"
                            + config.getRedactedPort() + "'.";
                    logger.warn(msg);

                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                }

                ScheduledFuture<?> settleJob = this.settleJob;
                if (settleJob != null) {
                    settleJob.cancel(false);
                    this.settleJob = null;
                }
            }
        }, SETTLE_TIME_IN_SECONDS, 1, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Insteon bridge");

        ScheduledFuture<?> driverInitializedJob = this.driverInitializedJob;
        if (driverInitializedJob != null) {
            driverInitializedJob.cancel(true);
            this.driverInitializedJob = null;
        }

        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }

        ScheduledFuture<?> reconnectJob = this.reconnectJob;
        if (reconnectJob != null) {
            reconnectJob.cancel(true);
            this.reconnectJob = null;
        }

        ScheduledFuture<?> settleJob = this.settleJob;
        if (settleJob != null) {
            settleJob.cancel(true);
            this.settleJob = null;
        }

        InsteonLegacyBinding insteonBinding = this.insteonBinding;
        if (insteonBinding != null) {
            insteonBinding.shutdown();
            this.insteonBinding = null;
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
            InsteonLegacyBinding insteonBinding = this.insteonBinding;
            if (insteonBinding != null && insteonBinding.reconnect()) {
                updateStatus(ThingStatus.ONLINE);
                ScheduledFuture<?> reconnectJob = this.reconnectJob;
                if (reconnectJob != null) {
                    reconnectJob.cancel(false);
                    this.reconnectJob = null;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Port disconnected.");
            }
        }, 0, RETRY_DELAY_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void disable() {
        scheduler.execute(() -> {
            InsteonLegacyDiscoveryService insteonDiscoveryService = this.insteonDiscoveryService;
            if (insteonDiscoveryService != null) {
                insteonDiscoveryService.removeAllResults();
            }

            thingManager.setEnabled(getThing().getUID(), false);
        });
    }

    public void insteonDeviceWasCreated() {
        lastInsteonDeviceCreatedTimestamp = System.currentTimeMillis();
    }

    public @Nullable InsteonBridgeConfiguration getBridgeConfig() {
        try {
            return getConfigAs(InsteonLegacyNetworkConfiguration.class).parse();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private @Nullable InsteonBridgeHandler getBridgeHandler(InsteonBridgeConfiguration config) {
        return thingRegistry.getAll().stream().filter(Thing::isEnabled).map(Thing::getHandler)
                .filter(InsteonBridgeHandler.class::isInstance).map(InsteonBridgeHandler.class::cast)
                .filter(handler -> config.equals(handler.getBridgeConfig())).findFirst().orElse(null);
    }

    public InsteonLegacyBinding getInsteonBinding() {
        InsteonLegacyBinding insteonBinding = this.insteonBinding;
        if (insteonBinding != null) {
            return insteonBinding;
        } else {
            throw new IllegalArgumentException("insteon binding is null");
        }
    }

    public void setInsteonDiscoveryService(InsteonLegacyDiscoveryService insteonDiscoveryService) {
        this.insteonDiscoveryService = insteonDiscoveryService;
    }

    public void addMissingDevices(List<InsteonAddress> missing) {
        scheduler.execute(() -> {
            InsteonLegacyDiscoveryService insteonDiscoveryService = this.insteonDiscoveryService;
            if (insteonDiscoveryService != null) {
                insteonDiscoveryService.addInsteonDevices(missing);
            }
        });
    }

    public void deviceNotLinked(DeviceAddress addr) {
        getThing().getThings().stream().forEach((thing) -> {
            InsteonLegacyDeviceHandler handler = (InsteonLegacyDeviceHandler) thing.getHandler();
            if (handler != null && addr.equals(handler.getDeviceAddress())) {
                handler.deviceNotLinked();
                return;
            }
        });
    }

    public void displayDevices(Console console) {
        display(console, deviceInfo);
    }

    public void displayChannels(Console console) {
        display(console, channelInfo);
    }

    public void displayLocalDatabase(Console console) {
        InsteonLegacyBinding insteonBinding = this.insteonBinding;
        if (insteonBinding != null) {
            Map<String, String> databaseInfo = insteonBinding.getDatabaseInfo();
            console.println("local database contains " + databaseInfo.size() + " entries");
            display(console, databaseInfo);
        }
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

    public List<Channel> getCachedChannels(ThingUID thingUID) {
        return channelCache.values().stream().filter(channel -> channel.getUID().getThingUID().equals(thingUID))
                .toList();
    }

    public @Nullable Channel pollCachedChannel(ChannelUID channelUID) {
        return channelCache.remove(channelUID);
    }

    public void cacheChannel(Channel channel) {
        channelCache.put(channel.getUID(), channel);
    }

    private void display(Console console, Map<String, String> info) {
        info.entrySet().stream().sorted(Entry.comparingByKey()).map(Entry::getValue).forEach(console::println);
    }
}
