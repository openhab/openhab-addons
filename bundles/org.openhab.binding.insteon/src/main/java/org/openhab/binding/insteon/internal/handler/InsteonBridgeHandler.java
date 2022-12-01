/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonHubConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonHubLegacyConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonPLMConfiguration;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonScene;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.discovery.InsteonDiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.types.State;

/**
 * The {@link InsteonBridgeHandler} is the base handler for insteon bridges.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class InsteonBridgeHandler extends InsteonBaseHandler implements BridgeHandler {
    private static final int DEVICE_STATISTICS_INTERVAL = 600; // seconds
    private static final int RETRY_INTERVAL = 30; // seconds
    private static final int START_DELAY = 5; // seconds

    private @Nullable InsteonBinding binding;
    private @Nullable InsteonDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> connectJob;
    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> statisticsJob;
    private @Nullable SerialPortManager serialPortManager;
    private StorageService storageService;

    public InsteonBridgeHandler(Bridge bridge, StorageService storageService) {
        super(bridge);
        this.storageService = storageService;
    }

    public InsteonBridgeHandler(Bridge bridge, SerialPortManager serialPortManager, StorageService storageService) {
        super(bridge);
        this.serialPortManager = serialPortManager;
        this.storageService = storageService;
    }

    @Override
    public Bridge getThing() {
        return (Bridge) super.getThing();
    }

    @Override
    public InsteonBinding getInsteonBinding() {
        InsteonBinding binding = this.binding;
        Objects.requireNonNull(binding);
        return binding;
    }

    @Override
    public @Nullable InsteonDevice getDevice() {
        return getInsteonBinding().getModemDevice();
    }

    private InsteonDiscoveryService getInsteonDiscoveryService() {
        InsteonDiscoveryService discoveryService = this.discoveryService;
        Objects.requireNonNull(discoveryService);
        return discoveryService;
    }

    public boolean isDeviceDiscoveryEnabled() {
        return getInsteonBridgeConfig().isDeviceDiscoveryEnabled();
    }

    public boolean isSceneDiscoveryEnabled() {
        return getInsteonBridgeConfig().isSceneDiscoveryEnabled();
    }

    public void setInsteonDiscoveryService(InsteonDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public void initialize() {
        if (logger.isDebugEnabled()) {
            logger.debug("starting bridge {}", getThing().getUID().getAsString());
        }

        InsteonBridgeConfiguration config = getInsteonBridgeConfig();
        InsteonBinding binding = new InsteonBinding(this, config, scheduler, serialPortManager, storageService);
        this.binding = binding;

        if (isInitialized()) {
            getChildHandlers().forEach(InsteonThingHandler::bridgeThingUpdated);
        }

        scheduler.execute(() -> {
            connectJob = scheduler.scheduleWithFixedDelay(() -> {
                if (!binding.startPolling()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Unable to connect to modem.");
                    return;
                }

                statisticsJob = scheduler.scheduleWithFixedDelay(() -> binding.logDeviceStatistics(), 0,
                        DEVICE_STATISTICS_INTERVAL, TimeUnit.SECONDS);

                cancelJob(connectJob, false);
                refresh();
            }, START_DELAY, RETRY_INTERVAL, TimeUnit.SECONDS);
        });
    }

    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("shutting down bridge {}", getThing().getUID().getAsString());
        }

        cancelJob(connectJob, true);
        cancelJob(reconnectJob, true);
        cancelJob(statisticsJob, true);

        getDevices().forEach(InsteonDevice::stopPolling);
        getInsteonBinding().stopPolling();

        super.dispose();
    }

    @Override
    protected BridgeBuilder editThing() {
        return BridgeBuilder.create(thing.getThingTypeUID(), thing.getUID()).withBridge(thing.getBridgeUID())
                .withChannels(thing.getChannels()).withConfiguration(thing.getConfiguration())
                .withLabel(thing.getLabel()).withLocation(thing.getLocation()).withProperties(thing.getProperties());
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (logger.isDebugEnabled()) {
            logger.debug("added thing {}", childThing.getUID().getAsString());
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (logger.isDebugEnabled()) {
            logger.debug("removed thing {}", childThing.getUID().getAsString());
        }
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String event) {
        super.triggerChannel(channelUID, event);
    }

    @Override
    public void updateStatus() {
        if (!getInsteonBinding().isModemDBComplete()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Loading modem database.");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected String getConfigInfo() {
        return getInsteonBridgeConfig().toString();
    }

    public InsteonBridgeConfiguration getInsteonBridgeConfig() {
        ThingTypeUID thingTypeUID = getThing().getThingTypeUID();
        if (THING_TYPE_HUB1.equals(thingTypeUID)) {
            return getConfigAs(InsteonHubLegacyConfiguration.class);
        } else if (THING_TYPE_HUB2.equals(thingTypeUID)) {
            return getConfigAs(InsteonHubConfiguration.class);
        } else if (THING_TYPE_PLM.equals(thingTypeUID)) {
            return getConfigAs(InsteonPLMConfiguration.class);
        } else {
            throw new UnsupportedOperationException("Unsupported bridge configuration");
        }
    }

    public Stream<InsteonThingHandler> getChildHandlers() {
        return getThing().getThings().stream().filter(Thing::isEnabled).map(Thing::getHandler)
                .filter(InsteonThingHandler.class::isInstance).map(InsteonThingHandler.class::cast);
    }

    public @Nullable InsteonDevice getDevice(InsteonAddress address) {
        return getDevices().filter(device -> device.getAddress().equals(address)).findFirst().orElse(null);
    }

    public Stream<InsteonDevice> getDevices() {
        return getDeviceHandlers().map(InsteonDeviceHandler::getDevice).filter(Objects::nonNull)
                .map(InsteonDevice.class::cast);
    }

    public Stream<InsteonDeviceHandler> getDeviceHandlers() {
        return getChildHandlers().filter(InsteonDeviceHandler.class::isInstance).map(InsteonDeviceHandler.class::cast);
    }

    public @Nullable InsteonScene getScene(int group) {
        return getScenes().filter(scene -> scene.getGroup() == group).findFirst().orElse(null);
    }

    public Stream<InsteonScene> getScenes() {
        return getSceneHandlers().map(InsteonSceneHandler::getScene).filter(Objects::nonNull)
                .map(InsteonScene.class::cast);
    }

    public Stream<InsteonSceneHandler> getSceneHandlers() {
        return getChildHandlers().filter(InsteonSceneHandler.class::isInstance).map(InsteonSceneHandler.class::cast);
    }

    public void discoverInsteonDevice(InsteonAddress address, ProductData productData) {
        scheduler.execute(() -> getInsteonDiscoveryService().addInsteonDevice(address.toString(), productData));
    }

    public void discoverMissingThings() {
        scheduler.execute(() -> getInsteonDiscoveryService().discoverMissingThings());
    }

    public void disconnected() {
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            if (!getInsteonBinding().reconnect()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to reconnect to modem.");
                return;
            }

            cancelJob(reconnectJob, false);
            updateStatus();
        }, 0, RETRY_INTERVAL, TimeUnit.SECONDS);
    }

    private void cancelJob(@Nullable ScheduledFuture<?> job, boolean interrupt) {
        if (job != null) {
            job.cancel(interrupt);
            job = null;
        }
    }
}
