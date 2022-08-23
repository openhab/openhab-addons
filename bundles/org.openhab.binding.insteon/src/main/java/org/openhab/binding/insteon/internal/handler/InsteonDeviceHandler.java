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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonStateDescriptionProvider;
import org.openhab.binding.insteon.internal.config.InsteonDeviceConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceCache;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.DeviceType;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.binding.insteon.internal.utils.StringUtils;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateOption;

/**
 * The {@link InsteonDeviceHandler} is the handler for an insteon device thing.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class InsteonDeviceHandler extends InsteonThingHandler {
    private static final int HEARTBEAT_TIMEOUT = 24; // in hours
    private static final int RESET_DELAY = 1000; // in milliseconds

    private @Nullable InsteonDevice device;
    private @Nullable InsteonStateDescriptionProvider stateDescriptionProvider;
    private @Nullable ScheduledFuture<?> heartbeatJob;

    public InsteonDeviceHandler(Thing thing, @Nullable InsteonStateDescriptionProvider stateDescriptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public @Nullable InsteonDevice getDevice() {
        return device;
    }

    protected void setDevice(@Nullable InsteonDevice device) {
        this.device = device;
    }

    @Override
    public void initialize() {
        InsteonDeviceConfiguration config = getConfigAs(InsteonDeviceConfiguration.class);

        scheduler.execute(() -> {
            if (getBridge() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
                return;
            }

            if (!InsteonAddress.isValid(config.getAddress())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid device address, it must be formatted as 'AB.CD.EF'.");
                return;
            }

            InsteonAddress address = new InsteonAddress(config.getAddress());
            if (getInsteonBridgeHandler().getDevice(address) != null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Duplicate device.");
                return;
            }

            InsteonDevice device = createDevice(address, null);
            setDevice(device);
            initializeChannels(device);
            updateProperties(device);
            refresh();
        });
    }

    protected InsteonDevice createDevice(InsteonAddress address, @Nullable ProductData productData) {
        Driver driver = getInsteonBinding().getDriver();
        DeviceCache deviceCache = getInsteonBinding().getDeviceCache(address);
        int devicePollInterval = getInsteonBridgeConfig().getDevicePollInterval();
        boolean deviceSyncEnabled = getInsteonBridgeConfig().isDeviceSyncEnabled();
        InsteonDevice device = InsteonDevice.makeDevice(driver, address, productData, deviceCache);
        device.setHandler(this);
        device.setPollInterval(devicePollInterval);
        device.setIsDeviceSyncEnabled(deviceSyncEnabled);
        device.initialize();
        return device;
    }

    public void initializeChannels(InsteonDevice device) {
        DeviceType deviceType = device.getType();
        if (deviceType == null) {
            return;
        }

        String deviceTypeName = deviceType.getName();
        List<Channel> channels = new ArrayList<>();

        for (DeviceFeature feature : device.getFeatures()) {
            String featureName = feature.getName();
            if (feature.isGroupFeature()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("{} is a group feature for {}. It will not be added as a channel.", featureName,
                            deviceTypeName);
                }
            } else if (feature.isHiddenFeature()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("{} is a hidden feature for {}. It will not be added as a channel.", featureName,
                            deviceTypeName);
                }
            } else {
                // create channel using feature name as channel id
                Channel channel = createChannel(featureName);
                if (channel != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("adding channel {}", channel.getUID().getAsString());
                    }
                    channels.add(channel);
                } else {
                    logger.warn("unable to create channel {} for {}", featureName, deviceTypeName);
                }
                // add existing custom channels with the same feature name as channel type id
                for (Channel customChannel : getCustomChannels(featureName)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("adding custom channel {}", customChannel.getUID().getAsString());
                    }
                    channels.add(customChannel);
                }
            }
        }

        for (Channel channel : channels) {
            // set channel custom settings based on device type name
            setChannelCustomSettings(channel, deviceTypeName);
        }

        updateThing(editThing().withChannels(channels).build());
    }

    private @Nullable Channel createChannel(String featureName) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), featureName);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, featureName);
        Channel channel = getThing().getChannel(channelUID);
        ThingHandlerCallback callback = getCallback();
        // create channel if not already available
        if (channel == null && callback != null) {
            channel = callback.createChannelBuilder(channelUID, channelTypeUID).build();
        }
        return channel;
    }

    private List<Channel> getCustomChannels(String featureName) {
        return getThing().getChannels().stream().filter(channel -> {
            ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            return channelTypeUID != null && channelTypeUID.getId().equals(featureName)
                    && !channel.getUID().getId().equals(featureName);
        }).collect(Collectors.toList());
    }

    private void setChannelCustomSettings(Channel channel, String deviceTypeName) {
        ChannelUID channelUID = channel.getUID();
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            return;
        }
        // determine key based on device type name and channel type id
        String key = deviceTypeName + ":" + channelTypeUID.getId();
        // set channel custom state options if available
        String[] stateDescriptionOptions = CUSTOM_STATE_DESCRIPTION_OPTIONS.get(key);
        InsteonStateDescriptionProvider stateDescriptionProvider = this.stateDescriptionProvider;
        if (stateDescriptionOptions != null && stateDescriptionProvider != null) {
            List<StateOption> options = new ArrayList<>();
            for (String value : stateDescriptionOptions) {
                String label = StringUtils.capitalize(value.replace("_", " ").toLowerCase());
                options.add(new StateOption(value, label));
            }

            if (logger.isTraceEnabled()) {
                logger.trace("setting state options for {} to {}", channelUID.getAsString(), options);
            }

            stateDescriptionProvider.setStateOptions(channelUID, options);
        }
    }

    @Override
    public void dispose() {
        InsteonDevice device = getDevice();
        if (device != null && !device.getAddress().isX10()) {
            device.stopPolling();

            getInsteonBinding().storeDeviceCache(device.getAddress(), DeviceCache.create(device));
        }
        setDevice(null);
        stopHeartbeatMonitor();

        super.dispose();
    }

    @Override
    public void refresh() {
        if (getInsteonBinding().isModemDBComplete()) {
            InsteonDevice device = getDevice();
            if (device != null && device.hasFeature(FEATURE_HEARTBEAT_MONITOR)) {
                resetHeartbeatMonitor();
            }
        }

        super.refresh();
    }

    @Override
    public void bridgeThingUpdated() {
        Driver driver = getInsteonBinding().getDriver();
        int devicePollInterval = getInsteonBridgeConfig().getDevicePollInterval();
        boolean deviceSyncEnabled = getInsteonBridgeConfig().isDeviceSyncEnabled();
        InsteonDevice device = getDevice();
        if (device != null) {
            device.setDriver(driver);
            device.setPollInterval(devicePollInterval);
            device.setIsDeviceSyncEnabled(deviceSyncEnabled);
        }
    }

    @Override
    protected String getConfigInfo() {
        return getConfigAs(InsteonDeviceConfiguration.class).toString();
    }

    public void reset(InsteonDevice oldDevice) {
        scheduler.schedule(() -> {
            if (logger.isTraceEnabled()) {
                logger.trace("resetting device {}", oldDevice.getAddress());
            }

            dispose();
            initialize();
            // replay stored messages from old device
            InsteonDevice device = getDevice();
            if (device != null) {
                device.replayMessages(oldDevice.getStoredMessages());
            }
        }, RESET_DELAY, TimeUnit.MILLISECONDS);
    }

    public void resetHeartbeatMonitor() {
        if (logger.isTraceEnabled()) {
            logger.trace("resetting heartbeat timeout monitor for {}", getThing().getUID().getAsString());
        }

        if (stopHeartbeatMonitor()) {
            updateStatus();
        }

        heartbeatJob = scheduler.schedule(
                () -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Heartbeat timed out."),
                HEARTBEAT_TIMEOUT, TimeUnit.HOURS);
    }

    private boolean stopHeartbeatMonitor() {
        boolean timeout = false;
        ScheduledFuture<?> heartbeatJob = this.heartbeatJob;
        if (heartbeatJob != null) {
            timeout = heartbeatJob.isDone();
            heartbeatJob.cancel(true);
            this.heartbeatJob = null;
        }
        return timeout;
    }
}
