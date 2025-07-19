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

import static org.openhab.binding.insteon.internal.InsteonBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.Device;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.DeviceType;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonBaseThingHandler} represents an insteon base thing handler.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class InsteonBaseThingHandler extends BaseThingHandler implements InsteonThingHandler {
    private static final Pattern CHANNEL_ID_PATTERN = Pattern.compile("-([a-z])");
    private static final Pattern FEATURE_NAME_PATTERN = Pattern.compile("(?!^)(?=[A-Z])");

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<ChannelUID, InsteonChannelHandler> channelHandlers = new ConcurrentHashMap<>();

    public InsteonBaseThingHandler(Thing thing) {
        super(thing);
    }

    public @Nullable InsteonModem getModem() {
        return Optional.ofNullable(getInsteonBridgeHandler()).map(InsteonBridgeHandler::getModem).orElse(null);
    }

    protected @Nullable InsteonBridgeHandler getInsteonBridgeHandler() {
        return Optional.ofNullable(getBridge()).map(Bridge::getHandler).filter(InsteonBridgeHandler.class::isInstance)
                .map(InsteonBridgeHandler.class::cast).orElse(null);
    }

    @Override
    public boolean isOnline() {
        return getThing().getStatus() == ThingStatus.ONLINE;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channel {} linked", channelUID);

        InsteonModem modem = getModem();
        if (modem == null || !modem.getDB().isComplete()) {
            logger.debug("channel {} linking skipped because modem database not complete yet.", channelUID);
            return;
        }

        if (channelHandlers.containsKey(channelUID)) {
            logger.debug("channel {} linking skipped because it is already configured", channelUID);
            return;
        }

        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("channel {} unknown, it will be ignored", channelUID);
            return;
        }

        Device device = getDevice();
        if (device == null || device.getFeatures().isEmpty()) {
            logger.debug("channel {} references uninitialized device, it will be ignored", channelUID);
            return;
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            logger.debug("channel {} references unknown channel type uid, it will be ignored", channelUID);
            return;
        }

        String featureName = channelIdToFeatureName(channelTypeUID.getId());
        DeviceFeature feature = device.getFeature(featureName);
        if (feature == null) {
            logger.warn("channel {} references unknown feature {} for device {}, it will be ignored", channelUID,
                    featureName, device.getAddress());
            return;
        }

        InsteonChannelHandler channelHandler = InsteonChannelHandler.makeHandler(channel, feature, this);
        channelHandlers.put(channelUID, channelHandler);

        if (logger.isDebugEnabled()) {
            logger.debug("{}", getChannelInfo(channelUID));
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.debug("channel {} unlinked", channelUID);

        InsteonChannelHandler channelHandler = channelHandlers.remove(channelUID);
        if (channelHandler != null) {
            channelHandler.dispose();
        }
    }

    @Override
    public void dispose() {
        channelHandlers.clear();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("channel {} received command {}", channelUID, command);

        if (!isOnline()) {
            logger.debug("thing {} not online, ignoring command", getThing().getUID());
            return;
        }

        InsteonChannelHandler channelHandler = channelHandlers.get(channelUID);
        if (channelHandler == null) {
            logger.warn("unable to find channel handler for {}", channelUID);
        } else {
            channelHandler.handleCommand(command);
        }
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        logger.debug("publishing state {} on {}", state, channelUID);

        super.updateState(channelUID, state);
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String event) {
        logger.debug("triggering event {} on {}", event, channelUID);

        super.triggerChannel(channelUID, event);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (getThing().getStatus() != ThingStatus.OFFLINE
                || getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            updateStatus();
        }
    }

    @Override
    public void bridgeThingDisposed() {
        // can be overridden by subclasses
    }

    @Override
    public void bridgeThingUpdated(InsteonBridgeConfiguration config, InsteonModem modem) {
        // can be overridden by subclasses
    }

    @Override
    public String getThingId() {
        return getThing().getUID().getId();
    }

    public String getThingInfo() {
        String thingId = getThingId();
        String config = getConfigInfo();
        String channels = getThing().getChannels().stream().map(Channel::getUID).map(ChannelUID::getId)
                .collect(Collectors.joining(","));

        StringBuilder builder = new StringBuilder(thingId);
        builder.append(":");
        builder.append(config);
        if (!channels.isEmpty()) {
            builder.append(" channels=");
            builder.append(channels);
        }
        builder.append(" status=");
        builder.append(getThing().getStatus());

        return builder.toString();
    }

    protected abstract String getConfigInfo();

    public Map<String, String> getChannelsInfo() {
        return getThing().getChannels().stream().map(Channel::getUID)
                .collect(Collectors.toMap(ChannelUID::getAsString, this::getChannelInfo));
    }

    private String getChannelInfo(ChannelUID channelUID) {
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            return "unknown channel " + channelUID;
        }
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            return "unknown channel type uid for " + channelUID;
        }
        InsteonChannelConfiguration config = channel.getConfiguration().as(InsteonChannelConfiguration.class);
        String featureName = channelIdToFeatureName(channelTypeUID.getId());

        StringBuilder builder = new StringBuilder(channelUID.getAsString());
        builder.append(config);
        builder.append(" feature=");
        builder.append(featureName);
        builder.append(" kind=");
        builder.append(channel.getKind());
        builder.append(" isLinked=");
        builder.append(isLinked(channelUID));

        return builder.toString();
    }

    protected String channelIdToFeatureName(String channelId) {
        return CHANNEL_ID_PATTERN.matcher(channelId).replaceAll(matchResult -> matchResult.group(1).toUpperCase());
    }

    protected String featureNameToChannelId(String featureName) {
        return FEATURE_NAME_PATTERN.matcher(featureName).replaceAll("-").toLowerCase();
    }

    protected void initializeChannels(Device device) {
        DeviceType deviceType = device.getType();
        ThingHandlerCallback callback = getCallback();
        if (deviceType == null || callback == null) {
            return;
        }

        String deviceTypeName = deviceType.getName();
        List<Channel> channels = new ArrayList<>();

        for (DeviceFeature feature : device.getFeatures()) {
            String featureName = feature.getName();
            if (feature.isGroupFeature()) {
                logger.trace("{} is a group feature for {}. It will not be added as a channel.", featureName,
                        deviceTypeName);
            } else if (feature.isHiddenFeature()) {
                logger.trace("{} is a hidden feature for {}. It will not be added as a channel.", featureName,
                        deviceTypeName);
            } else {
                String channelId = featureNameToChannelId(featureName);
                Channel channel = createChannel(channelId, callback);
                logger.trace("adding channel {}", channel.getUID());
                channels.add(channel);
                // add existing custom channels with the same channel type id but different channel id
                for (Channel customChannel : getCustomChannels(channelId)) {
                    logger.trace("adding custom channel {}", customChannel.getUID());
                    channels.add(customChannel);
                }
            }
        }

        updateThing(editThing().withChannels(channels).build());
    }

    private Channel createChannel(String channelId, ThingHandlerCallback callback) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
        ChannelBuilder channelBuilder = callback.createChannelBuilder(channelUID, channelTypeUID);
        Channel oldChannel = getThing().getChannel(channelUID);
        if (oldChannel != null) {
            channelBuilder.withConfiguration(oldChannel.getConfiguration());
            channelBuilder.withProperties(oldChannel.getProperties());
        }
        return channelBuilder.build();
    }

    private List<Channel> getCustomChannels(String channelId) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
        return getThing().getChannels().stream().filter(
                channel -> channelTypeUID.equals(channel.getChannelTypeUID()) && !channelUID.equals(channel.getUID()))
                .toList();
    }

    private void linkChannels() {
        getThing().getChannels().stream()
                .filter(channel -> isLinked(channel.getUID()) || channel.getKind() == ChannelKind.TRIGGER)
                .filter(channel -> !channelHandlers.containsKey(channel.getUID())).map(Channel::getUID)
                .forEach(this::channelLinked);
    }

    protected void unlinkChannels() {
        getThing().getChannels().stream().map(Channel::getUID).filter(channelHandlers::containsKey)
                .forEach(this::channelUnlinked);
    }

    @Override
    public void refresh() {
        InsteonModem modem = getModem();
        if (modem != null && modem.getDB().isComplete()) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}", getThingInfo());
            }
            linkChannels();
        }

        updateStatus();
    }

    public void updateProperties(Device device) {
        Map<String, String> properties = editProperties();

        String serialNumber = device.getAddress().toString().replace(".", "");
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);

        ProductData productData = device.getProductData();
        if (productData != null) {
            String vendor = productData.getVendor();
            if (vendor != null) {
                properties.put(Thing.PROPERTY_VENDOR, vendor);
            }
            String model = productData.getModel();
            if (model != null) {
                properties.put(Thing.PROPERTY_MODEL_ID, model);
            }
            int hardware = productData.getHardwareVersion();
            if (hardware != 0) {
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, HexUtils.getHexString(hardware));
            }
            int firmware = productData.getFirmwareVersion();
            if (firmware != 0) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, HexUtils.getHexString(firmware));
            }
            String productId = productData.getProductId();
            if (productId != null) {
                properties.put(PROPERTY_PRODUCT_ID, productId);
            }
            DeviceType deviceType = productData.getDeviceType();
            if (deviceType != null) {
                properties.put(PROPERTY_DEVICE_TYPE, deviceType.getName());
            }
        }

        logger.trace("updating properties for {} to {}", getThing().getUID(), properties);

        updateProperties(properties);
    }

    protected void cancelJob(@Nullable ScheduledFuture<?> job, boolean interrupt) {
        if (job != null && !job.isCancelled()) {
            job.cancel(interrupt);
        }
    }
}
