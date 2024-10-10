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
package org.openhab.binding.insteon.internal.handler;

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonStateDescriptionProvider;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonDeviceConfiguration;
import org.openhab.binding.insteon.internal.device.Device;
import org.openhab.binding.insteon.internal.device.DeviceCache;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonEngine;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.StringUtils;

/**
 * The {@link InsteonDeviceHandler} represents an Insteon device handler.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonDeviceHandler extends InsteonBaseThingHandler {
    private static final int HEARTBEAT_TIMEOUT_BUFFER = 5; // in minutes
    private static final int INIT_DELAY = 100; // in milliseconds
    private static final int RESET_DELAY = 1000; // in milliseconds

    private @Nullable InsteonDevice device;
    private @Nullable ScheduledFuture<?> heartbeatJob;
    private InsteonStateDescriptionProvider stateDescriptionProvider;

    public InsteonDeviceHandler(Thing thing, InsteonStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public @Nullable InsteonDevice getDevice() {
        return device;
    }

    @Override
    public void initialize() {
        InsteonDeviceConfiguration config = getConfigAs(InsteonDeviceConfiguration.class);

        scheduler.execute(() -> {
            Bridge bridge = getBridge();
            if (bridge == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
                return;
            }

            if (bridge.getThingTypeUID().equals(THING_TYPE_LEGACY_NETWORK)) {
                changeThingType(THING_TYPE_LEGACY_DEVICE, bridge.getHandler());
                return;
            }

            if (!InsteonAddress.isValid(config.getAddress())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid device address, it must be formatted as 'AB.CD.EF'.");
                return;
            }

            InsteonModem modem = getModem();
            InsteonAddress address = new InsteonAddress(config.getAddress());
            if (modem != null && modem.hasDevice(address)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Duplicate device.");
                return;
            }

            InsteonDevice device = createDevice(address, modem);
            this.device = device;

            if (modem != null) {
                modem.addDevice(device);
            }

            initializeChannels(device);
            updateProperties(device);
            refresh();
        });
    }

    private void changeThingType(ThingTypeUID thingTypeUID, @Nullable BridgeHandler bridgeHandler) {
        if (bridgeHandler instanceof InsteonLegacyNetworkHandler legacyNetworkHandler) {
            Map<ChannelUID, Configuration> channelConfigs = getThing().getChannels().stream()
                    .collect(Collectors.toMap(Channel::getUID, Channel::getConfiguration));

            legacyNetworkHandler.addChannelConfigs(channelConfigs);
        }

        changeThingType(thingTypeUID, getConfig());
    }

    private InsteonDevice createDevice(InsteonAddress address, @Nullable InsteonModem modem) {
        InsteonDevice device;
        InsteonBridgeHandler handler = getInsteonBridgeHandler();
        if (handler != null) {
            device = InsteonDevice.makeDevice(address, modem, handler.getProductData(address));
            device.setPollInterval(handler.getDevicePollInterval());
            device.setIsDeviceSyncEnabled(handler.isDeviceSyncEnabled());
            handler.loadDeviceCache(device);
        } else {
            device = InsteonDevice.makeDevice(address, modem, null);
        }
        device.setHandler(this);
        device.initialize();
        return device;
    }

    @Override
    protected void initializeChannels(Device device) {
        super.initializeChannels(device);

        getThing().getChannels().forEach(channel -> setChannelCustomSettings(channel, device));
    }

    private void setChannelCustomSettings(Channel channel, Device device) {
        ChannelUID channelUID = channel.getUID();
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            return;
        }

        String featureName = channelIdToFeatureName(channelTypeUID.getId());
        DeviceFeature feature = device.getFeature(featureName);
        if (feature == null) {
            return;
        }

        List<String> stateDescriptionOptions = CUSTOM_STATE_DESCRIPTION_OPTIONS.get(feature.getType());
        if (stateDescriptionOptions == null) {
            return;
        }

        List<StateOption> options = stateDescriptionOptions.stream().map(value -> new StateOption(value,
                StringUtils.capitalizeByWhitespace(value.replace("_", " ").toLowerCase()))).toList();

        logger.trace("setting state options for {} to {}", channelUID, options);

        stateDescriptionProvider.setStateOptions(channelUID, options);
    }

    @Override
    public void dispose() {
        InsteonDevice device = getDevice();
        if (device != null) {
            device.stopPolling();

            InsteonModem modem = getModem();
            if (modem != null) {
                modem.deleteSceneEntries(device);
                modem.removeDevice(device);
            }

            InsteonBridgeHandler handler = getInsteonBridgeHandler();
            if (handler != null && device.hasModemDBEntry()) {
                handler.storeDeviceCache(device.getAddress(),
                        DeviceCache.builder().withProductData(device.getProductData())
                                .withInsteonEngine(device.getInsteonEngine()).withDatabase(device.getLinkDB())
                                .withFeatures(device.getFeatures()).build());
            }
        }
        this.device = null;

        stopHeartbeatMonitor();

        super.dispose();
    }

    @Override
    public void refresh() {
        resetHeartbeatMonitor();

        super.refresh();
    }

    @Override
    public void bridgeThingDisposed() {
        InsteonDevice device = getDevice();
        if (device != null) {
            device.stopPolling();
            device.setModem(null);
        }
    }

    @Override
    public void bridgeThingUpdated(InsteonBridgeConfiguration config, InsteonModem modem) {
        InsteonDevice device = getDevice();
        if (device != null) {
            device.setPollInterval(config.getDevicePollInterval());
            device.setIsDeviceSyncEnabled(config.isDeviceSyncEnabled());
            device.setModem(modem);

            modem.addDevice(device);
        }
    }

    public void deviceLinkDBUpdated(InsteonDevice device) {
        if (device.getLinkDB().isComplete()) {
            resetHeartbeatMonitor();

            InsteonModem modem = getModem();
            if (modem != null) {
                modem.updateSceneEntries(device);
            }
        }
        updateStatus();
    }

    @Override
    protected String getConfigInfo() {
        return getConfigAs(InsteonDeviceConfiguration.class).toString();
    }

    @Override
    public void updateStatus() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
            return;
        }

        if (bridge.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        InsteonModem modem = getModem();
        if (modem == null || !modem.getDB().isComplete()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Waiting for modem database.");
            return;
        }

        InsteonDevice device = getDevice();
        if (device == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unable to determine device.");
            return;
        }

        if (!device.hasModemDBEntry()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Device not found in modem database.");
            return;
        }

        if (!device.isResponding() && !device.isBatteryPowered()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device not responding.");
            return;
        }

        if (device.getProductData() == null) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Waiting for product data.");
            return;
        }

        if (device.getType() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unsupported device.");
            return;
        }

        if (!device.getLinkDB().isComplete()) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for link database.");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void updateProperties(InsteonDevice device) {
        InsteonEngine engine = device.getInsteonEngine();
        if (engine != InsteonEngine.UNKNOWN) {
            updateProperty(PROPERTY_ENGINE_VERSION, engine.name());
        }

        super.updateProperties(device);
    }

    public void reset(InsteonDevice oldDevice) {
        scheduler.schedule(() -> {
            logger.debug("resetting thing {}", getThing().getUID());

            dispose();
            initialize();

            scheduler.schedule(() -> {
                InsteonDevice device = getDevice();
                if (device != null) {
                    device.replayMessages(oldDevice.getStoredMessages());
                }
            }, INIT_DELAY, TimeUnit.MILLISECONDS);

        }, RESET_DELAY, TimeUnit.MILLISECONDS);
    }

    public void resetHeartbeatMonitor() {
        if (stopHeartbeatMonitor()) {
            updateStatus();
        }

        InsteonDevice device = getDevice();
        if (device == null || !device.hasModemDBEntry() || !device.hasHeartbeat()) {
            return;
        }

        if (device.getMissingLinks().contains(FEATURE_HEARTBEAT)) {
            logger.warn("heartbeat link missing, timeout monitor disabled for {}", getThing().getUID());
            return;
        }

        int timeout = device.getHeartbeatTimeout();
        if (timeout > 0) {
            logger.debug("setting heartbeat timeout monitor to {} min for {}", timeout, getThing().getUID());

            heartbeatJob = scheduler.schedule(() -> {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Heartbeat timed out.");
            }, timeout + HEARTBEAT_TIMEOUT_BUFFER, TimeUnit.MINUTES);
        }
    }

    private boolean stopHeartbeatMonitor() {
        boolean hasTimedOut = false;
        ScheduledFuture<?> heartbeatJob = this.heartbeatJob;
        if (heartbeatJob != null) {
            hasTimedOut = heartbeatJob.isDone();
            heartbeatJob.cancel(true);
            this.heartbeatJob = null;
        }
        return hasTimedOut;
    }
}
