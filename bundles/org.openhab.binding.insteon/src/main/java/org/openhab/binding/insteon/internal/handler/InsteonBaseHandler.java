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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.DeviceType;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonEngine;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InsteonBaseHandler} is the base abstract thing handler.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class InsteonBaseHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<ChannelUID, InsteonChannelHandler> channelHandlers = new ConcurrentHashMap<>();

    public InsteonBaseHandler(Thing thing) {
        super(thing);
    }

    public abstract InsteonBinding getInsteonBinding();

    public abstract @Nullable InsteonDevice getDevice();

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (logger.isDebugEnabled()) {
            logger.debug("channel {} linked", channelUID.getAsString());
        }

        if (!getInsteonBinding().isModemDBComplete()) {
            if (logger.isDebugEnabled()) {
                logger.debug("channel {} linking skipped because modem database not complete yet.",
                        channelUID.getAsString());
            }
            return;
        }

        if (channelHandlers.containsKey(channelUID)) {
            if (logger.isDebugEnabled()) {
                logger.debug("channel {} linking skipped because it is already configured", channelUID.getAsString());
            }
            return;
        }

        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("channel {} unknown, it will be ignored", channelUID.getAsString());
            return;
        }

        InsteonDevice device = getDevice();
        if (device == null) {
            logger.warn("channel {} references unknown device, it will be ignored", channelUID.getAsString());
            return;
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        Objects.requireNonNull(channelTypeUID);
        DeviceFeature feature = device.getFeature(channelTypeUID.getId());
        if (feature == null) {
            logger.warn("channel {} references unknown feature {} for device {}, it will be ignored",
                    channelUID.getAsString(), channelTypeUID.getId(), device.getAddress());
            return;
        }

        InsteonChannelHandler channelHandler = InsteonChannelHandler.makeHandler(getInsteonBinding(), channel, feature);
        channelHandlers.put(channelUID, channelHandler);

        if (logger.isDebugEnabled()) {
            logger.debug("{}", getChannelInfo(channelUID));
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        if (logger.isDebugEnabled()) {
            logger.debug("channel {} unlinked", channelUID.getAsString());
        }

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
        if (logger.isDebugEnabled()) {
            logger.debug("channel {} received command {}", channelUID.getAsString(), command);
        }

        ThingStatus status = getThing().getStatus();
        if (status != ThingStatus.ONLINE) {
            logger.debug("thing {} not ready to handle commands, it will be ignored",
                    getThing().getUID().getAsString());
            return;
        }

        InsteonChannelHandler channelHandler = channelHandlers.get(channelUID);
        if (channelHandler == null) {
            logger.warn("unable to find channel handler for {}", channelUID.getAsString());
        } else {
            channelHandler.handleCommand(command);
        }
    }

    public void refresh() {
        if (getInsteonBinding().isModemDBComplete()) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}", getThingInfo());
            }

            linkChannels();
        }

        updateStatus();
    }

    public String getThingId() {
        return getThing().getUID().getId();
    }

    public String getThingInfo() {
        String thingId = getThingId();
        String config = getConfigInfo();
        String channels = String.join(",", getChannelIds());

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

    public List<String> getChannelIds() {
        return getThing().getChannels().stream().map(channel -> channel.getUID().getId()).collect(Collectors.toList());
    }

    public Map<String, String> getChannelsInfo() {
        return getThing().getChannels().stream().map(channel -> channel.getUID())
                .collect(Collectors.toMap(ChannelUID::getAsString, this::getChannelInfo));
    }

    private String getChannelInfo(ChannelUID channelUID) {
        Channel channel = getThing().getChannel(channelUID.getId());
        Objects.requireNonNull(channel);
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        Objects.requireNonNull(channelTypeUID);
        InsteonChannelConfiguration config = channel.getConfiguration().as(InsteonChannelConfiguration.class);

        StringBuilder builder = new StringBuilder(channelUID.getAsString());
        builder.append(config);
        builder.append(" feature=");
        builder.append(channelTypeUID.getId());
        builder.append(" kind=");
        builder.append(channel.getKind());
        builder.append(" isLinked=");
        builder.append(isLinked(channelUID));

        return builder.toString();
    }

    /**
     * Links previously linked and trigger channels
     */
    private void linkChannels() {
        getThing().getChannels().stream()
                .filter(channel -> isLinked(channel.getUID()) || channel.getKind() == ChannelKind.TRIGGER)
                .filter(channel -> !channelHandlers.containsKey(channel.getUID()))
                .forEach(channel -> channelLinked(channel.getUID()));
    }

    public abstract void updateStatus();

    public void updateProperties(InsteonDevice device) {
        Map<String, String> properties = editProperties();

        InsteonAddress address = device.getAddress();
        if (!address.isX10()) {
            String serialNumber = address.toString().replace(".", "");
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        }

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
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, ByteUtils.getHexString(hardware));
            }
            int firmware = productData.getFirmwareVersion();
            if (firmware != 0) {
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, ByteUtils.getHexString(firmware));
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

        InsteonEngine engine = device.getInsteonEngine();
        if (engine != InsteonEngine.UNKNOWN) {
            properties.put(PROPERTY_ENGINE_VERSION, engine.name());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("updating properties for {} to {}", getThing().getUID().getAsString(), properties);
        }

        updateProperties(properties);
    }
}
