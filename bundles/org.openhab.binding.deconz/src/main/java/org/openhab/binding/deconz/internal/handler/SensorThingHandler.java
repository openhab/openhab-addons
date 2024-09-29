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
package org.openhab.binding.deconz.internal.handler;

import static org.openhab.binding.deconz.internal.BindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.*;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.dto.SensorConfig;
import org.openhab.binding.deconz.internal.dto.SensorState;
import org.openhab.binding.deconz.internal.dto.SensorUpdateConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.ColorUtil;

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
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_PRESENCE_SENSOR,
            THING_TYPE_DAYLIGHT_SENSOR, THING_TYPE_POWER_SENSOR, THING_TYPE_CONSUMPTION_SENSOR, THING_TYPE_LIGHT_SENSOR,
            THING_TYPE_TEMPERATURE_SENSOR, THING_TYPE_HUMIDITY_SENSOR, THING_TYPE_PRESSURE_SENSOR, THING_TYPE_SWITCH,
            THING_TYPE_OPENCLOSE_SENSOR, THING_TYPE_WATERLEAKAGE_SENSOR, THING_TYPE_FIRE_SENSOR,
            THING_TYPE_ALARM_SENSOR, THING_TYPE_VIBRATION_SENSOR, THING_TYPE_BATTERY_SENSOR,
            THING_TYPE_CARBONMONOXIDE_SENSOR, THING_TYPE_AIRQUALITY_SENSOR, THING_TYPE_COLOR_CONTROL,
            THING_TYPE_MOISTURE_SENSOR);

    private static final List<String> CONFIG_CHANNELS = List.of(CHANNEL_BATTERY_LEVEL, CHANNEL_BATTERY_LOW,
            CHANNEL_ENABLED, CHANNEL_TEMPERATURE);

    public SensorThingHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            sensorState.buttonevent = null;
            valueUpdated(channelUID, sensorState, false);
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_ENABLED:
                if (command instanceof OnOffType) {
                    SensorUpdateConfig newConfig = new SensorUpdateConfig();
                    newConfig.on = OnOffType.ON.equals(command);
                    sendCommand(newConfig, command, channelUID, null);
                }
                break;
        }
    }

    @Override
    protected void valueUpdated(ChannelUID channelUID, SensorConfig newConfig) {
        super.valueUpdated(channelUID, newConfig);
        switch (channelUID.getId()) {
            case CHANNEL_ENABLED -> updateState(channelUID, OnOffType.from(newConfig.on));
            case CHANNEL_TEMPERATURE -> {
                Float temperature = newConfig.temperature;
                if (temperature != null) {
                    updateState(channelUID, new QuantityType<>(temperature / 100, CELSIUS));
                }
            }
        }
    }

    @Override
    protected void valueUpdated(ChannelUID channelUID, SensorState newState, boolean initializing) {
        super.valueUpdated(channelUID, newState, initializing);
        switch (channelUID.getId()) {
            case CHANNEL_AIRQUALITY -> updateStringChannel(channelUID, newState.airquality);
            case CHANNEL_AIRQUALITYPPB ->
                updateQuantityTypeChannel(channelUID, newState.airqualityppb, PARTS_PER_BILLION);
            case CHANNEL_ALARM -> updateSwitchChannel(channelUID, newState.alarm);
            case CHANNEL_BATTERY_LEVEL -> updateDecimalTypeChannel(channelUID, newState.battery);
            case CHANNEL_BUTTON -> updateDecimalTypeChannel(channelUID, newState.buttonevent);
            case CHANNEL_BUTTONEVENT -> triggerChannel(channelUID, newState.buttonevent, initializing);
            case CHANNEL_CARBONMONOXIDE -> updateSwitchChannel(channelUID, newState.carbonmonoxide);
            case CHANNEL_COLOR -> {
                final double @Nullable [] xy = newState.xy;
                if (xy != null && xy.length == 2) {
                    updateState(channelUID, ColorUtil.xyToHsb(xy));
                }
            }
            case CHANNEL_CONSUMPTION -> updateQuantityTypeChannel(channelUID, newState.consumption, WATT_HOUR);
            case CHANNEL_CURRENT -> updateQuantityTypeChannel(channelUID, newState.current, MILLI(AMPERE));
            case CHANNEL_DARK -> updateSwitchChannel(channelUID, newState.dark);
            case CHANNEL_DAYLIGHT -> updateSwitchChannel(channelUID, newState.daylight);
            case CHANNEL_FIRE -> updateSwitchChannel(channelUID, newState.fire);
            case CHANNEL_GESTURE -> updateDecimalTypeChannel(channelUID, newState.gesture);
            case CHANNEL_GESTUREEVENT -> triggerChannel(channelUID, newState.gesture, initializing);
            case CHANNEL_HUMIDITY -> updateQuantityTypeChannel(channelUID, newState.humidity, PERCENT, 1.0 / 100);
            case CHANNEL_LIGHT -> updateStringChannel(channelUID, getLightState(newState));
            case CHANNEL_LIGHT_LEVEL -> updateDecimalTypeChannel(channelUID, newState.lightlevel);
            case CHANNEL_LIGHT_LUX -> updateQuantityTypeChannel(channelUID, newState.lux, LUX);
            case CHANNEL_MOISTURE -> updateQuantityTypeChannel(channelUID, newState.moisture, PERCENT);
            case CHANNEL_OPENCLOSE -> {
                Boolean open = newState.open;
                if (open != null) {
                    updateState(channelUID, open ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
            }
            case CHANNEL_ORIENTATION_X ->
                updateDecimalTypeChannel(channelUID, newState.orientation != null ? newState.orientation[0] : null);
            case CHANNEL_ORIENTATION_Y ->
                updateDecimalTypeChannel(channelUID, newState.orientation != null ? newState.orientation[1] : null);
            case CHANNEL_ORIENTATION_Z ->
                updateDecimalTypeChannel(channelUID, newState.orientation != null ? newState.orientation[2] : null);
            case CHANNEL_POWER -> updateQuantityTypeChannel(channelUID, newState.power, WATT);
            case CHANNEL_PRESENCE -> updateSwitchChannel(channelUID, newState.presence);
            case CHANNEL_PRESSURE -> updateQuantityTypeChannel(channelUID, newState.pressure, HECTO(PASCAL));
            case CHANNEL_TAMPERED -> updateSwitchChannel(channelUID, newState.tampered);
            case CHANNEL_TEMPERATURE -> updateQuantityTypeChannel(channelUID, newState.temperature, CELSIUS, 1.0 / 100);
            case CHANNEL_TILTANGLE -> updateQuantityTypeChannel(channelUID, newState.tiltangle, DEGREE_ANGLE);
            case CHANNEL_VALUE -> updateDecimalTypeChannel(channelUID, newState.status);
            case CHANNEL_VIBRATION -> updateSwitchChannel(channelUID, newState.vibration);
            case CHANNEL_VIBRATION_STRENGTH -> updateDecimalTypeChannel(channelUID, newState.vibrationstrength);
            case CHANNEL_VOLTAGE -> updateQuantityTypeChannel(channelUID, newState.voltage, VOLT);
            case CHANNEL_WATERLEAKAGE -> updateSwitchChannel(channelUID, newState.water);
        }
    }

    @Override
    protected boolean createTypeSpecificChannels(ThingBuilder thingBuilder, SensorConfig sensorConfig,
            SensorState sensorState) {
        boolean thingEdited = false;

        // some Xiaomi sensors
        if (sensorConfig.temperature != null && createChannel(thingBuilder, CHANNEL_TEMPERATURE, ChannelKind.STATE)) {
            thingEdited = true;
        }

        // ZHAPresence - e.g. IKEA TRÅDFRI motion sensor
        if (sensorState.dark != null && createChannel(thingBuilder, CHANNEL_DARK, ChannelKind.STATE)) {
            thingEdited = true;
        }

        // ZHAConsumption - e.g Bitron 902010/25 or Heiman SmartPlug
        if (sensorState.power != null && createChannel(thingBuilder, CHANNEL_POWER, ChannelKind.STATE)) {
            thingEdited = true;
        }
        // ZHAConsumption - e.g. Linky devices second channel
        if (sensorState.consumption2 != null && createChannel(thingBuilder, CHANNEL_CONSUMPTION_2, ChannelKind.STATE)) {
            thingEdited = true;
        }

        // ZHAPower - e.g. Heiman SmartPlug
        if (sensorState.voltage != null && createChannel(thingBuilder, CHANNEL_VOLTAGE, ChannelKind.STATE)) {
            thingEdited = true;
        }
        if (sensorState.current != null && createChannel(thingBuilder, CHANNEL_CURRENT, ChannelKind.STATE)) {
            thingEdited = true;
        }

        // IAS Zone sensor - e.g. Heiman HS1MS motion sensor
        if (sensorState.tampered != null && createChannel(thingBuilder, CHANNEL_TAMPERED, ChannelKind.STATE)) {
            thingEdited = true;
        }

        // e.g. Aqara Cube
        if (sensorState.gesture != null && (createChannel(thingBuilder, CHANNEL_GESTURE, ChannelKind.STATE)
                || createChannel(thingBuilder, CHANNEL_GESTUREEVENT, ChannelKind.TRIGGER))) {
            thingEdited = true;
        }

        // vibration sensors
        if (sensorState.tiltangle != null && createChannel(thingBuilder, CHANNEL_TILTANGLE, ChannelKind.STATE)) {
            thingEdited = true;
        }
        if (sensorState.vibrationstrength != null
                && createChannel(thingBuilder, CHANNEL_VIBRATION_STRENGTH, ChannelKind.STATE)) {
            thingEdited = true;
        }
        if (sensorState.orientation != null) {
            if (createChannel(thingBuilder, CHANNEL_ORIENTATION_X, ChannelKind.STATE)) {
                thingEdited = true;
            }
            if (createChannel(thingBuilder, CHANNEL_ORIENTATION_Y, ChannelKind.STATE)) {
                thingEdited = true;
            }
            if (createChannel(thingBuilder, CHANNEL_ORIENTATION_Z, ChannelKind.STATE)) {
                thingEdited = true;
            }
        }

        return thingEdited;
    }

    @Override
    protected List<String> getConfigChannels() {
        return CONFIG_CHANNELS;
    }

    /**
     * Determine the light state from a state message
     *
     * @param newState the {@link SensorState} message
     * @return <code>Dark</code>, <code>Daylight</code>, <code>Sunset</code>
     */
    private @Nullable String getLightState(SensorState newState) {
        Boolean dark = newState.dark;
        if (dark == null) {
            return null;
        }
        Boolean daylight = newState.daylight;
        if (dark) { // if it's dark, it's dark ;)
            return "Dark";
        } else if (daylight != null) { // if its not dark, it might be between darkness and daylight
            if (daylight) {
                return "Daylight";
            } else {
                return "Sunset";
            }
        } else { // if no daylight value is known, we assume !dark means daylight
            return "Daylight";
        }
    }

    private void triggerChannel(ChannelUID channelUID, @Nullable Integer value, boolean initializing) {
        if (value == null || initializing) {
            return;
        }
        triggerChannel(channelUID, String.valueOf(value));
    }
}
