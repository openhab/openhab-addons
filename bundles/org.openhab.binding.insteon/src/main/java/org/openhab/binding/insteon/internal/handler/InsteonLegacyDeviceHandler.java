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

import static org.openhab.binding.insteon.internal.InsteonLegacyBindingConstants.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.InsteonLegacyBinding;
import org.openhab.binding.insteon.internal.config.InsteonLegacyChannelConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonLegacyDeviceConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceAddress;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.LegacyDevice;
import org.openhab.binding.insteon.internal.device.LegacyDeviceFeature;
import org.openhab.binding.insteon.internal.device.LegacyDeviceTypeLoader;
import org.openhab.binding.insteon.internal.device.X10Address;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link InsteonLegacyDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonLegacyDeviceHandler extends BaseThingHandler {

    private static final String CHANNEL_TYPE_ID_PREFIX = "legacy";

    private final Logger logger = LoggerFactory.getLogger(InsteonLegacyDeviceHandler.class);

    private @NonNullByDefault({}) InsteonLegacyDeviceConfiguration config;
    private boolean deviceLinked = true;

    public InsteonLegacyDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(InsteonLegacyDeviceConfiguration.class);
        deviceLinked = true;

        scheduler.execute(() -> {
            final Bridge bridge = getBridge();
            if (bridge == null) {
                String msg = "An Insteon network bridge has not been selected for this device.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            InsteonLegacyDeviceConfiguration config = this.config;
            if (config == null) {
                String msg = "Insteon device configuration is null.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            DeviceAddress address = getDeviceAddress();
            if (address == null) {
                String msg = "Unable to start Insteon device, the Insteon or X10 address '" + config.getAddress()
                        + "' is invalid. It must be in the format 'AB.CD.EF' or 'H.U' (X10).";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            String productKey = config.getProductKey();
            if (LegacyDeviceTypeLoader.instance().getDeviceType(productKey) == null) {
                String msg = "Unable to start Insteon device, invalid product key '" + productKey + "'.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            String deviceConfig = config.getDeviceConfig();
            Map<String, Object> deviceConfigMap;
            if (deviceConfig != null) {
                Type mapType = new TypeToken<Map<String, Object>>() {
                }.getType();
                try {
                    deviceConfigMap = Objects.requireNonNull(new Gson().fromJson(deviceConfig, mapType));
                } catch (JsonParseException e) {
                    String msg = "The device configuration parameter is not valid JSON.";
                    logger.warn("{} {}", thing.getUID().getAsString(), msg);

                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                    return;
                }
            } else {
                deviceConfigMap = Collections.emptyMap();
            }

            InsteonLegacyBinding insteonBinding = getInsteonBinding();
            if (insteonBinding.getDevice(address) != null) {
                String msg = "A device already exists with the address '" + address + "'.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            LegacyDevice device = insteonBinding.makeNewDevice(address, productKey, deviceConfigMap);
            if (device == null) {
                String msg = "Unable to create a device with the product key '" + productKey + "' with the address'"
                        + address + "'.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            ThingHandlerCallback callback = getCallback();
            if (callback == null) {
                String msg = "Unable to get thing handler callback.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            Map<String, Channel> channelMap = new HashMap<>();
            String thingId = getThing().getUID().getAsString();
            for (String channelId : ALL_CHANNEL_IDS) {
                String featureName = channelId.toLowerCase();

                if (productKey.equals(HIDDEN_DOOR_SENSOR_PRODUCT_KEY)) {
                    if (featureName.equalsIgnoreCase(BATTERY_LEVEL)
                            || featureName.equalsIgnoreCase(BATTERY_WATERMARK_LEVEL)) {
                        featureName = DATA;
                    }
                } else if (productKey.equals(MOTION_SENSOR_PRODUCT_KEY)) {
                    if (featureName.equalsIgnoreCase(BATTERY_LEVEL) || featureName.equalsIgnoreCase(LIGHT_LEVEL)) {
                        featureName = DATA;
                    }
                } else if (productKey.equals(MOTION_SENSOR_II_PRODUCT_KEY)) {
                    if (featureName.equalsIgnoreCase(BATTERY_LEVEL) || featureName.equalsIgnoreCase(BATTERY_PERCENT)
                            || featureName.equalsIgnoreCase(LIGHT_LEVEL)
                            || featureName.equalsIgnoreCase(TEMPERATURE_LEVEL)) {
                        featureName = DATA;
                    }
                } else if (productKey.equals(PLM_PRODUCT_KEY)) {
                    String[] parts = featureName.split("#");
                    if (parts.length == 2 && parts[0].equalsIgnoreCase(BROADCAST_ON_OFF)
                            && parts[1].matches("^\\d+$")) {
                        featureName = parts[0];
                    }
                } else if (productKey.equals(POWER_METER_PRODUCT_KEY)) {
                    if (featureName.equalsIgnoreCase(KWH) || featureName.equalsIgnoreCase(RESET)
                            || featureName.equalsIgnoreCase(UPDATE) || featureName.equalsIgnoreCase(WATTS)) {
                        featureName = METER;
                    }
                }

                LegacyDeviceFeature feature = device.getFeature(featureName);
                if (feature != null) {
                    if (!feature.isFeatureGroup()) {
                        if (channelId.equalsIgnoreCase(BROADCAST_ON_OFF)) {
                            Set<String> broadcastChannels = new HashSet<>();
                            for (Channel channel : thing.getChannels()) {
                                String id = channel.getUID().getId();
                                if (id.startsWith(BROADCAST_ON_OFF)) {
                                    channelMap.put(id, channel);
                                    broadcastChannels.add(id);
                                }
                            }

                            Object groups = deviceConfigMap.get(BROADCAST_GROUPS);
                            if (groups != null) {
                                boolean valid = false;
                                if (groups instanceof List<?> list) {
                                    valid = true;
                                    for (Object value : list) {
                                        if (value instanceof Double doubleValue && doubleValue % 1 == 0) {
                                            String id = BROADCAST_ON_OFF + "#" + doubleValue.intValue();
                                            if (!broadcastChannels.contains(id)) {
                                                channelMap.put(id, createChannel(id, BROADCAST_ON_OFF, callback));
                                                broadcastChannels.add(id);
                                            }
                                        } else {
                                            valid = false;
                                            break;
                                        }
                                    }
                                }

                                if (!valid) {
                                    String msg = "The value for key " + BROADCAST_GROUPS
                                            + " must be an array of integers in the device configuration parameter.";
                                    logger.warn("{} {}", thing.getUID().getAsString(), msg);

                                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                                    return;
                                }
                            }
                        } else {
                            channelMap.put(channelId, createChannel(channelId, channelId, callback));
                        }
                    } else {
                        logger.debug("{} is a feature group for {}. It will not be added as a channel.", featureName,
                                productKey);
                    }
                }
            }

            if (!channelMap.isEmpty() || device.isModem()) {
                List<Channel> channels = new ArrayList<>();
                StringBuilder channelList = new StringBuilder();
                if (!channelMap.isEmpty()) {
                    List<String> channelIds = new ArrayList<>(channelMap.keySet());
                    Collections.sort(channelIds);
                    channelIds.forEach(channelId -> {
                        Channel channel = channelMap.get(channelId);
                        if (channel != null) {
                            channels.add(channel);

                            if (channelList.length() > 0) {
                                channelList.append(", ");
                            }
                            channelList.append(channelId);
                        }
                    });

                    updateThing(editThing().withChannels(channels).build());
                }

                StringBuilder builder = new StringBuilder(thingId);
                builder.append(" address = ");
                builder.append(address);
                builder.append(" productKey = ");
                builder.append(productKey);
                builder.append(" channels = ");
                builder.append(channelList.toString());
                String msg = builder.toString();
                logger.debug("{}", msg);

                getInsteonNetworkHandler().initialized(getThing().getUID(), msg);

                channels.forEach(channel -> {
                    if (isLinked(channel.getUID())) {
                        channelLinked(channel.getUID());
                    }
                });

                if (ThingStatus.ONLINE == bridge.getStatus()) {
                    if (deviceLinked) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                String msg = "Product key '" + productKey
                        + "' does not have any features that match existing channels.";

                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            }
        });
    }

    @Override
    public void dispose() {
        InsteonLegacyDeviceConfiguration config = this.config;
        if (config != null) {
            DeviceAddress address = getDeviceAddress();
            if (getBridge() != null && address != null) {
                getInsteonBinding().removeDevice(address);

                logger.debug("removed {} address = {}", getThing().getUID().getAsString(), address);
            }

            InsteonLegacyNetworkHandler handler = null;
            try {
                handler = getInsteonNetworkHandler();
            } catch (IllegalArgumentException e) {
            }

            if (handler != null) {
                handler.disposed(getThing().getUID());
            }
        }

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (ThingStatus.ONLINE == getThing().getStatus()) {
            logger.debug("channel {} was triggered with the command {}", channelUID.getAsString(), command);

            getInsteonBinding().sendCommand(channelUID.getAsString(), command);
        } else {
            logger.debug("the command {} for channel {} was ignored because the thing is not ONLINE", command,
                    channelUID.getAsString());
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (getInsteonNetworkHandler().isChannelLinked(channelUID)) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("channel is null");
            return;
        }

        Map<String, Object> channelProperties = channel.getConfiguration().getProperties();
        for (String key : channelProperties.keySet()) {
            Object value = channelProperties.get(key);
            if (value instanceof String stringValue) {
                params.put(key, stringValue);
            } else if (value instanceof BigDecimal decimalValue) {
                String s = decimalValue.toPlainString();
                params.put(key, s);
            } else {
                logger.warn("not a string or big decimal value key '{}' value '{}' {}", key, value,
                        value != null ? value.getClass().getName() : "unknown");
            }
        }

        String feature = channelUID.getId().toLowerCase();
        InsteonLegacyDeviceConfiguration config = this.config;
        if (config == null) {
            logger.warn("insteon device config is null");
            return;
        }
        String productKey = config.getProductKey();
        if (productKey.equals(HIDDEN_DOOR_SENSOR_PRODUCT_KEY)) {
            if (feature.equalsIgnoreCase(BATTERY_LEVEL)) {
                params.put(FIELD, FIELD_BATTERY_LEVEL);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(BATTERY_WATERMARK_LEVEL)) {
                params.put(FIELD, FIELD_BATTERY_WATERMARK_LEVEL);
                feature = DATA;
            }
        } else if (productKey.equals(MOTION_SENSOR_PRODUCT_KEY)) {
            if (feature.equalsIgnoreCase(BATTERY_LEVEL)) {
                params.put(FIELD, FIELD_BATTERY_LEVEL);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(LIGHT_LEVEL)) {
                params.put(FIELD, FIELD_LIGHT_LEVEL);
                feature = DATA;
            }
        } else if (productKey.equals(MOTION_SENSOR_II_PRODUCT_KEY)) {
            if (feature.equalsIgnoreCase(BATTERY_LEVEL)) {
                params.put(FIELD, FIELD_BATTERY_LEVEL);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(BATTERY_PERCENT)) {
                params.put(FIELD, FIELD_BATTERY_PERCENTAGE);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(LIGHT_LEVEL)) {
                params.put(FIELD, FIELD_LIGHT_LEVEL);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(TEMPERATURE_LEVEL)) {
                params.put(FIELD, FIELD_TEMPERATURE_LEVEL);
                feature = DATA;
            }
        } else if (productKey.equals(PLM_PRODUCT_KEY)) {
            String[] parts = feature.split("#");
            if (parts.length == 2 && parts[0].equalsIgnoreCase(BROADCAST_ON_OFF) && parts[1].matches("^\\d+$")) {
                params.put(GROUP, parts[1]);
                feature = parts[0];
            }
        } else if (productKey.equals(POWER_METER_PRODUCT_KEY)) {
            if (feature.equalsIgnoreCase(KWH)) {
                params.put(FIELD, FIELD_KWH);
            } else if (feature.equalsIgnoreCase(WATTS)) {
                params.put(FIELD, FIELD_WATTS);
            } else if (feature.equalsIgnoreCase(RESET)) {
                params.put(CMD, CMD_RESET);
            } else if (feature.equalsIgnoreCase(UPDATE)) {
                params.put(CMD, CMD_UPDATE);
            }

            feature = METER;
        }

        DeviceAddress address = getDeviceAddress();
        if (address == null) {
            logger.warn("device address is null");
            return;
        }
        InsteonLegacyChannelConfiguration bindingConfig = new InsteonLegacyChannelConfiguration(channelUID, feature,
                address, productKey, params);
        getInsteonBinding().addFeatureListener(bindingConfig);

        StringBuilder builder = new StringBuilder(channelUID.getAsString());
        builder.append(" feature = ");
        builder.append(feature);
        builder.append(" parameters = ");
        builder.append(params);
        String msg = builder.toString();
        logger.debug("{}", msg);

        getInsteonNetworkHandler().linked(channelUID, msg);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        getInsteonBinding().removeFeatureListener(channelUID);
        getInsteonNetworkHandler().unlinked(channelUID);

        logger.debug("channel {} unlinked ", channelUID.getAsString());
    }

    public @Nullable DeviceAddress getDeviceAddress() {
        InsteonLegacyDeviceConfiguration config = this.config;
        if (config != null) {
            String address = config.getAddress();
            if (InsteonAddress.isValid(address)) {
                return new InsteonAddress(address);
            } else if (X10Address.isValid(address)) {
                return new X10Address(address);
            }
        }
        return null;
    }

    public void deviceNotLinked() {
        String msg = "device with the address '" + config.getAddress()
                + "' was not found in the modem database. Did you forget to link?";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);

        deviceLinked = false;
    }

    private Channel createChannel(String channelId, String channelTypeId, ThingHandlerCallback callback) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(InsteonBindingConstants.BINDING_ID,
                CHANNEL_TYPE_ID_PREFIX + StringUtils.capitalize(channelTypeId));
        Configuration channelConfig = getChannelConfig(channelUID);
        Channel channel = getThing().getChannel(channelUID);
        if (channel == null) {
            channel = callback.createChannelBuilder(channelUID, channelTypeUID).withConfiguration(channelConfig)
                    .build();
        }
        return channel;
    }

    private Configuration getChannelConfig(ChannelUID channelUID) {
        try {
            return getInsteonNetworkHandler().getChannelConfig(channelUID);
        } catch (IllegalArgumentException e) {
            return new Configuration();
        }
    }

    private InsteonLegacyNetworkHandler getInsteonNetworkHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            throw new IllegalArgumentException("insteon network bridge is null");
        }
        InsteonLegacyNetworkHandler handler = (InsteonLegacyNetworkHandler) bridge.getHandler();
        if (handler == null) {
            throw new IllegalArgumentException("insteon network handler is null");
        }
        return handler;
    }

    private InsteonLegacyBinding getInsteonBinding() {
        return getInsteonNetworkHandler().getInsteonBinding();
    }
}
