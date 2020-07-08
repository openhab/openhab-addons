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
package org.openhab.binding.deconz.internal.handler;

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.*;
import static org.eclipse.smarthome.core.library.unit.SIUnits.*;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;
import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.deconz.internal.dto.SensorConfig;
import org.openhab.binding.deconz.internal.dto.SensorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This sensor Thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 *
 * A REST API call is made to get the initial sensor state.
 *
 * Every sensor and switch is supported by this Thing, because a unified state is kept
 * in {@link #sensorState}. Every field that got received by the REST API for this specific
 * sensor is published to the framework.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorThingHandler extends SensorBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_PRESENCE_SENSOR, THING_TYPE_DAYLIGHT_SENSOR, THING_TYPE_POWER_SENSOR,
                    THING_TYPE_CONSUMPTION_SENSOR, THING_TYPE_LIGHT_SENSOR, THING_TYPE_TEMPERATURE_SENSOR,
                    THING_TYPE_HUMIDITY_SENSOR, THING_TYPE_PRESSURE_SENSOR, THING_TYPE_SWITCH,
                    THING_TYPE_OPENCLOSE_SENSOR, THING_TYPE_WATERLEAKAGE_SENSOR, THING_TYPE_FIRE_SENSOR,
                    THING_TYPE_ALARM_SENSOR, THING_TYPE_VIBRATION_SENSOR, THING_TYPE_BATTERY_SENSOR,
                    THING_TYPE_CARBONMONOXIDE_SENSOR).collect(Collectors.toSet()));

    private static final List<String> CONFIG_CHANNELS = Arrays.asList(CHANNEL_BATTERY_LEVEL, CHANNEL_BATTERY_LOW,
            CHANNEL_TEMPERATURE);

    private final Logger logger = LoggerFactory.getLogger(SensorThingHandler.class);

    public SensorThingHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            return;
        }

        sensorState.buttonevent = null;
        valueUpdated(channelUID.getId(), sensorState, false);
    }

    @Override
    protected void valueUpdated(ChannelUID channelUID, SensorConfig newConfig) {
        super.valueUpdated(channelUID, newConfig);
        Float temperature = newConfig.temperature;

        switch (channelUID.getId()) {
            case CHANNEL_TEMPERATURE:
                if (temperature != null) {
                    updateState(channelUID, new QuantityType<>(temperature / 100, CELSIUS));
                }
                break;
        }
    }

    @Override
    protected void valueUpdated(String channelID, SensorState newState, boolean initializing) {
        super.valueUpdated(channelID, newState, initializing);
        switch (channelID) {
            case CHANNEL_BATTERY_LEVEL:
                updateDecimalTypeChannel(channelID, newState.battery);
                break;
            case CHANNEL_LIGHT:
                Boolean dark = newState.dark;
                if (dark != null) {
                    Boolean daylight = newState.daylight;
                    if (dark) { // if it's dark, it's dark ;)
                        updateState(channelID, new StringType("Dark"));
                    } else if (daylight != null) { // if its not dark, it might be between darkness and daylight
                        if (daylight) {
                            updateState(channelID, new StringType("Daylight"));
                        } else {
                            updateState(channelID, new StringType("Sunset"));
                        }
                    } else { // if no daylight value is known, we assume !dark means daylight
                        updateState(channelID, new StringType("Daylight"));
                    }
                }
                break;
            case CHANNEL_POWER:
                updateQuantityTypeChannel(channelID, newState.power, WATT);
                break;
            case CHANNEL_CONSUMPTION:
                updateQuantityTypeChannel(channelID, newState.consumption, WATT_HOUR);
                break;
            case CHANNEL_VOLTAGE:
                updateQuantityTypeChannel(channelID, newState.voltage, VOLT);
                break;
            case CHANNEL_CURRENT:
                updateQuantityTypeChannel(channelID, newState.current, MILLI(AMPERE));
                break;
            case CHANNEL_LIGHT_LUX:
                updateQuantityTypeChannel(channelID, newState.lux, LUX);
                break;
            case CHANNEL_LIGHT_LEVEL:
                updateDecimalTypeChannel(channelID, newState.lightlevel);
                break;
            case CHANNEL_DARK:
                updateSwitchChannel(channelID, newState.dark);
                break;
            case CHANNEL_DAYLIGHT:
                updateSwitchChannel(channelID, newState.daylight);
                break;
            case CHANNEL_TEMPERATURE:
                updateQuantityTypeChannel(channelID, newState.temperature, CELSIUS, 1.0 / 100);
                break;
            case CHANNEL_HUMIDITY:
                updateQuantityTypeChannel(channelID, newState.humidity, PERCENT, 1.0 / 100);
                break;
            case CHANNEL_PRESSURE:
                updateQuantityTypeChannel(channelID, newState.pressure, HECTO(PASCAL));
                break;
            case CHANNEL_PRESENCE:
                updateSwitchChannel(channelID, newState.presence);
                break;
            case CHANNEL_VALUE:
                updateDecimalTypeChannel(channelID, newState.status);
                break;
            case CHANNEL_OPENCLOSE:
                Boolean open = newState.open;
                if (open != null) {
                    updateState(channelID, open ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
                break;
            case CHANNEL_WATERLEAKAGE:
                updateSwitchChannel(channelID, newState.water);
                break;
            case CHANNEL_FIRE:
                updateSwitchChannel(channelID, newState.fire);
                break;
            case CHANNEL_ALARM:
                updateSwitchChannel(channelID, newState.alarm);
                break;
            case CHANNEL_TAMPERED:
                updateSwitchChannel(channelID, newState.tampered);
                break;
            case CHANNEL_VIBRATION:
                updateSwitchChannel(channelID, newState.vibration);
                break;
            case CHANNEL_CARBONMONOXIDE:
                updateSwitchChannel(channelID, newState.carbonmonoxide);
                break;
            case CHANNEL_BUTTON:
                updateDecimalTypeChannel(channelID, newState.buttonevent);
                break;
            case CHANNEL_BUTTONEVENT:
                Integer buttonevent = newState.buttonevent;
                if (buttonevent != null && !initializing) {
                    triggerChannel(channelID, String.valueOf(buttonevent));
                }
                break;
            case CHANNEL_GESTURE:
                updateDecimalTypeChannel(channelID, newState.gesture);
                break;
            case CHANNEL_GESTUREEVENT:
                Integer gesture = newState.gesture;
                if (gesture != null && !initializing) {
                    triggerChannel(channelID, String.valueOf(gesture));
                }
                break;
        }
    }

    @Override
    protected void createTypeSpecificChannels(SensorConfig sensorConfig, SensorState sensorState) {
        // some Xiaomi sensors
        if (sensorConfig.temperature != null) {
            createChannel(CHANNEL_TEMPERATURE, ChannelKind.STATE);
        }

        // ZHAPresence - e.g. IKEA TRÅDFRI motion sensor
        if (sensorState.dark != null) {
            createChannel(CHANNEL_DARK, ChannelKind.STATE);
        }

        // ZHAConsumption - e.g Bitron 902010/25 or Heiman SmartPlug
        if (sensorState.power != null) {
            createChannel(CHANNEL_POWER, ChannelKind.STATE);
        }

        // ZHAPower - e.g. Heiman SmartPlug
        if (sensorState.voltage != null) {
            createChannel(CHANNEL_VOLTAGE, ChannelKind.STATE);
        }
        if (sensorState.current != null) {
            createChannel(CHANNEL_CURRENT, ChannelKind.STATE);
        }

        // IAS Zone sensor - e.g. Heiman HS1MS motion sensor
        if (sensorState.tampered != null) {
            createChannel(CHANNEL_TAMPERED, ChannelKind.STATE);
        }

        // e.g. Aqara Cube
        if (sensorState.gesture != null) {
            createChannel(CHANNEL_GESTURE, ChannelKind.STATE);
            createChannel(CHANNEL_GESTUREEVENT, ChannelKind.TRIGGER);
        }
    }

    @Override
    protected List<String> getConfigChannels() {
        return CONFIG_CHANNELS;
    }
}
