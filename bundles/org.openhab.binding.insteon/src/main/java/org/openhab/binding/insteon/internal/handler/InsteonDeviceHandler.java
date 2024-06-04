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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.config.InsteonChannelConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonDeviceConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.DeviceTypeLoader;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link InsteonDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
public class InsteonDeviceHandler extends BaseThingHandler {

    private static final Set<String> ALL_CHANNEL_IDS = Collections.unmodifiableSet(Stream.of(
            InsteonBindingConstants.AC_DELAY, InsteonBindingConstants.BACKLIGHT_DURATION,
            InsteonBindingConstants.BATTERY_LEVEL, InsteonBindingConstants.BATTERY_PERCENT,
            InsteonBindingConstants.BATTERY_WATERMARK_LEVEL, InsteonBindingConstants.BEEP,
            InsteonBindingConstants.BOTTOM_OUTLET, InsteonBindingConstants.BUTTON_A, InsteonBindingConstants.BUTTON_B,
            InsteonBindingConstants.BUTTON_C, InsteonBindingConstants.BUTTON_D, InsteonBindingConstants.BUTTON_E,
            InsteonBindingConstants.BUTTON_F, InsteonBindingConstants.BUTTON_G, InsteonBindingConstants.BUTTON_H,
            InsteonBindingConstants.BROADCAST_ON_OFF, InsteonBindingConstants.CONTACT,
            InsteonBindingConstants.COOL_SET_POINT, InsteonBindingConstants.DIMMER, InsteonBindingConstants.FAN,
            InsteonBindingConstants.FAN_MODE, InsteonBindingConstants.FAST_ON_OFF,
            InsteonBindingConstants.FAST_ON_OFF_BUTTON_A, InsteonBindingConstants.FAST_ON_OFF_BUTTON_B,
            InsteonBindingConstants.FAST_ON_OFF_BUTTON_C, InsteonBindingConstants.FAST_ON_OFF_BUTTON_D,
            InsteonBindingConstants.FAST_ON_OFF_BUTTON_E, InsteonBindingConstants.FAST_ON_OFF_BUTTON_F,
            InsteonBindingConstants.FAST_ON_OFF_BUTTON_G, InsteonBindingConstants.FAST_ON_OFF_BUTTON_H,
            InsteonBindingConstants.HEAT_SET_POINT, InsteonBindingConstants.HUMIDITY,
            InsteonBindingConstants.HUMIDITY_HIGH, InsteonBindingConstants.HUMIDITY_LOW,
            InsteonBindingConstants.IS_COOLING, InsteonBindingConstants.IS_HEATING,
            InsteonBindingConstants.KEYPAD_BUTTON_A, InsteonBindingConstants.KEYPAD_BUTTON_B,
            InsteonBindingConstants.KEYPAD_BUTTON_C, InsteonBindingConstants.KEYPAD_BUTTON_D,
            InsteonBindingConstants.KEYPAD_BUTTON_E, InsteonBindingConstants.KEYPAD_BUTTON_F,
            InsteonBindingConstants.KEYPAD_BUTTON_G, InsteonBindingConstants.KEYPAD_BUTTON_H,
            InsteonBindingConstants.KWH, InsteonBindingConstants.LAST_HEARD_FROM,
            InsteonBindingConstants.LED_BRIGHTNESS, InsteonBindingConstants.LED_ONOFF,
            InsteonBindingConstants.LIGHT_DIMMER, InsteonBindingConstants.LIGHT_LEVEL,
            InsteonBindingConstants.LIGHT_LEVEL_ABOVE_THRESHOLD, InsteonBindingConstants.LOAD_DIMMER,
            InsteonBindingConstants.LOAD_SWITCH, InsteonBindingConstants.LOAD_SWITCH_FAST_ON_OFF,
            InsteonBindingConstants.LOAD_SWITCH_MANUAL_CHANGE, InsteonBindingConstants.LOWBATTERY,
            InsteonBindingConstants.MANUAL_CHANGE, InsteonBindingConstants.MANUAL_CHANGE_BUTTON_A,
            InsteonBindingConstants.MANUAL_CHANGE_BUTTON_B, InsteonBindingConstants.MANUAL_CHANGE_BUTTON_C,
            InsteonBindingConstants.MANUAL_CHANGE_BUTTON_D, InsteonBindingConstants.MANUAL_CHANGE_BUTTON_E,
            InsteonBindingConstants.MANUAL_CHANGE_BUTTON_F, InsteonBindingConstants.MANUAL_CHANGE_BUTTON_G,
            InsteonBindingConstants.MANUAL_CHANGE_BUTTON_H, InsteonBindingConstants.NOTIFICATION,
            InsteonBindingConstants.ON_LEVEL, InsteonBindingConstants.RAMP_DIMMER, InsteonBindingConstants.RAMP_RATE,
            InsteonBindingConstants.RESET, InsteonBindingConstants.STAGE1_DURATION, InsteonBindingConstants.SWITCH,
            InsteonBindingConstants.SYSTEM_MODE, InsteonBindingConstants.TAMPER_SWITCH,
            InsteonBindingConstants.TEMPERATURE, InsteonBindingConstants.TEMPERATURE_LEVEL,
            InsteonBindingConstants.TOP_OUTLET, InsteonBindingConstants.UPDATE, InsteonBindingConstants.WATTS)
            .collect(Collectors.toSet()));

    public static final String BROADCAST_GROUPS = "broadcastGroups";
    public static final String BROADCAST_ON_OFF = "broadcastonoff";
    public static final String CMD = "cmd";
    public static final String CMD_RESET = "reset";
    public static final String CMD_UPDATE = "update";
    public static final String DATA = "data";
    public static final String FIELD = "field";
    public static final String FIELD_BATTERY_LEVEL = "battery_level";
    public static final String FIELD_BATTERY_PERCENTAGE = "battery_percentage";
    public static final String FIELD_BATTERY_WATERMARK_LEVEL = "battery_watermark_level";
    public static final String FIELD_KWH = "kwh";
    public static final String FIELD_LIGHT_LEVEL = "light_level";
    public static final String FIELD_TEMPERATURE_LEVEL = "temperature_level";
    public static final String FIELD_WATTS = "watts";
    public static final String GROUP = "group";
    public static final String METER = "meter";

    public static final String HIDDEN_DOOR_SENSOR_PRODUCT_KEY = "F00.00.03";
    public static final String MOTION_SENSOR_II_PRODUCT_KEY = "F00.00.24";
    public static final String MOTION_SENSOR_PRODUCT_KEY = "0x00004A";
    public static final String PLM_PRODUCT_KEY = "0x000045";
    public static final String POWER_METER_PRODUCT_KEY = "F00.00.17";

    private final Logger logger = LoggerFactory.getLogger(InsteonDeviceHandler.class);

    private @NonNullByDefault({}) InsteonDeviceConfiguration config;
    private boolean deviceLinked = true;

    public InsteonDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(InsteonDeviceConfiguration.class);
        deviceLinked = true;

        scheduler.execute(() -> {
            final Bridge bridge = getBridge();
            if (bridge == null) {
                String msg = "An Insteon network bridge has not been selected for this device.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            InsteonDeviceConfiguration config = this.config;
            if (config == null) {
                String msg = "Insteon device configuration is null.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }
            String address = config.getAddress();
            if (!InsteonAddress.isValid(address)) {
                String msg = "Unable to start Insteon device, the insteon or X10 address '" + address
                        + "' is invalid. It must be in the format 'AB.CD.EF' or 'H.U' (X10).";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            DeviceTypeLoader instance = DeviceTypeLoader.instance();
            if (instance == null) {
                String msg = "Device type loader is null.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            String productKey = config.getProductKey();
            if (instance.getDeviceType(productKey) == null) {
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

            InsteonBinding insteonBinding = getInsteonBinding();
            InsteonAddress insteonAddress = new InsteonAddress(address);
            if (insteonBinding.getDevice(insteonAddress) != null) {
                String msg = "A device already exists with the address '" + address + "'.";
                logger.warn("{} {}", thing.getUID().getAsString(), msg);

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                return;
            }

            InsteonDevice device = insteonBinding.makeNewDevice(insteonAddress, productKey, deviceConfigMap);
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
                String feature = channelId.toLowerCase();

                if (productKey.equals(HIDDEN_DOOR_SENSOR_PRODUCT_KEY)) {
                    if (feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_LEVEL)
                            || feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_WATERMARK_LEVEL)) {
                        feature = DATA;
                    }
                } else if (productKey.equals(MOTION_SENSOR_PRODUCT_KEY)) {
                    if (feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_LEVEL)
                            || feature.equalsIgnoreCase(InsteonBindingConstants.LIGHT_LEVEL)) {
                        feature = DATA;
                    }
                } else if (productKey.equals(MOTION_SENSOR_II_PRODUCT_KEY)) {
                    if (feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_LEVEL)
                            || feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_PERCENT)
                            || feature.equalsIgnoreCase(InsteonBindingConstants.LIGHT_LEVEL)
                            || feature.equalsIgnoreCase(InsteonBindingConstants.TEMPERATURE_LEVEL)) {
                        feature = DATA;
                    }
                } else if (productKey.equals(PLM_PRODUCT_KEY)) {
                    String[] parts = feature.split("#");
                    if (parts.length == 2 && parts[0].equalsIgnoreCase(InsteonBindingConstants.BROADCAST_ON_OFF)
                            && parts[1].matches("^\\d+$")) {
                        feature = BROADCAST_ON_OFF;
                    }
                } else if (productKey.equals(POWER_METER_PRODUCT_KEY)) {
                    if (feature.equalsIgnoreCase(InsteonBindingConstants.KWH)
                            || feature.equalsIgnoreCase(InsteonBindingConstants.RESET)
                            || feature.equalsIgnoreCase(InsteonBindingConstants.UPDATE)
                            || feature.equalsIgnoreCase(InsteonBindingConstants.WATTS)) {
                        feature = METER;
                    }
                }

                DeviceFeature f = device.getFeature(feature);
                if (f != null) {
                    if (!f.isFeatureGroup()) {
                        if (channelId.equals(InsteonBindingConstants.BROADCAST_ON_OFF)) {
                            Set<String> broadcastChannels = new HashSet<>();
                            for (Channel channel : thing.getChannels()) {
                                String id = channel.getUID().getId();
                                if (id.startsWith(InsteonBindingConstants.BROADCAST_ON_OFF)) {
                                    channelMap.put(id, channel);
                                    broadcastChannels.add(id);
                                }
                            }

                            Object groups = deviceConfigMap.get(BROADCAST_GROUPS);
                            if (groups != null) {
                                boolean valid = false;
                                if (groups instanceof List<?> list) {
                                    valid = true;
                                    for (Object o : list) {
                                        if (o instanceof Double && (Double) o % 1 == 0) {
                                            String id = InsteonBindingConstants.BROADCAST_ON_OFF + "#"
                                                    + ((Double) o).intValue();
                                            if (!broadcastChannels.contains(id)) {
                                                ChannelUID channelUID = new ChannelUID(thing.getUID(), id);
                                                ChannelTypeUID channelTypeUID = new ChannelTypeUID(
                                                        InsteonBindingConstants.BINDING_ID,
                                                        InsteonBindingConstants.SWITCH);
                                                Channel channel = callback
                                                        .createChannelBuilder(channelUID, channelTypeUID).withLabel(id)
                                                        .build();

                                                channelMap.put(id, channel);
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
                            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
                            ChannelTypeUID channelTypeUID = new ChannelTypeUID(InsteonBindingConstants.BINDING_ID,
                                    channelId);
                            Channel channel = thing.getChannel(channelUID);
                            if (channel == null) {
                                channel = callback.createChannelBuilder(channelUID, channelTypeUID).build();
                            }

                            channelMap.put(channelId, channel);
                        }
                    } else {
                        logger.debug("{} is a feature group for {}. It will not be added as a channel.", feature,
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
        InsteonDeviceConfiguration config = this.config;
        if (config != null) {
            String address = config.getAddress();
            if (getBridge() != null && InsteonAddress.isValid(address)) {
                getInsteonBinding().removeDevice(new InsteonAddress(address));

                logger.debug("removed {} address = {}", getThing().getUID().getAsString(), address);
            }

            InsteonNetworkHandler handler = null;
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
        InsteonDeviceConfiguration config = this.config;
        if (config == null) {
            logger.warn("insteon device config is null");
            return;
        }
        String productKey = config.getProductKey();
        if (productKey.equals(HIDDEN_DOOR_SENSOR_PRODUCT_KEY)) {
            if (feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_LEVEL)) {
                params.put(FIELD, FIELD_BATTERY_LEVEL);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_WATERMARK_LEVEL)) {
                params.put(FIELD, FIELD_BATTERY_WATERMARK_LEVEL);
                feature = DATA;
            }
        } else if (productKey.equals(MOTION_SENSOR_PRODUCT_KEY)) {
            if (feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_LEVEL)) {
                params.put(FIELD, FIELD_BATTERY_LEVEL);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(InsteonBindingConstants.LIGHT_LEVEL)) {
                params.put(FIELD, FIELD_LIGHT_LEVEL);
                feature = DATA;
            }
        } else if (productKey.equals(MOTION_SENSOR_II_PRODUCT_KEY)) {
            if (feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_LEVEL)) {
                params.put(FIELD, FIELD_BATTERY_LEVEL);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(InsteonBindingConstants.BATTERY_PERCENT)) {
                params.put(FIELD, FIELD_BATTERY_PERCENTAGE);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(InsteonBindingConstants.LIGHT_LEVEL)) {
                params.put(FIELD, FIELD_LIGHT_LEVEL);
                feature = DATA;
            } else if (feature.equalsIgnoreCase(InsteonBindingConstants.TEMPERATURE_LEVEL)) {
                params.put(FIELD, FIELD_TEMPERATURE_LEVEL);
                feature = DATA;
            }
        } else if (productKey.equals(PLM_PRODUCT_KEY)) {
            String[] parts = feature.split("#");
            if (parts.length == 2 && parts[0].equalsIgnoreCase(InsteonBindingConstants.BROADCAST_ON_OFF)
                    && parts[1].matches("^\\d+$")) {
                params.put(GROUP, parts[1]);
                feature = BROADCAST_ON_OFF;
            }
        } else if (productKey.equals(POWER_METER_PRODUCT_KEY)) {
            if (feature.equalsIgnoreCase(InsteonBindingConstants.KWH)) {
                params.put(FIELD, FIELD_KWH);
            } else if (feature.equalsIgnoreCase(InsteonBindingConstants.WATTS)) {
                params.put(FIELD, FIELD_WATTS);
            } else if (feature.equalsIgnoreCase(InsteonBindingConstants.RESET)) {
                params.put(CMD, CMD_RESET);
            } else if (feature.equalsIgnoreCase(InsteonBindingConstants.UPDATE)) {
                params.put(CMD, CMD_UPDATE);
            }

            feature = METER;
        }

        InsteonChannelConfiguration bindingConfig = new InsteonChannelConfiguration(channelUID, feature,
                new InsteonAddress(config.getAddress()), productKey, params);
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

    public InsteonAddress getInsteonAddress() {
        return new InsteonAddress(config.getAddress());
    }

    public void deviceNotLinked() {
        String msg = "device with the address '" + config.getAddress()
                + "' was not found in the modem database. Did you forget to link?";
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);

        deviceLinked = false;
    }

    private InsteonNetworkHandler getInsteonNetworkHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            throw new IllegalArgumentException("insteon network bridge is null");
        }
        InsteonNetworkHandler handler = (InsteonNetworkHandler) bridge.getHandler();
        if (handler == null) {
            throw new IllegalArgumentException("insteon network handler is null");
        }
        return handler;
    }

    private InsteonBinding getInsteonBinding() {
        return getInsteonNetworkHandler().getInsteonBinding();
    }
}
