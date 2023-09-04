/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.dto.SensorConfig;
import org.openhab.binding.deconz.internal.dto.SensorState;
import org.openhab.binding.deconz.internal.dto.ThermostatUpdateConfig;
import org.openhab.binding.deconz.internal.types.ThermostatMode;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This sensor Thermostat Thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 *
 * A REST API call is made to get the initial sensor state.
 *
 * Only the Thermostat is supported by this Thing, because a unified state is kept
 * in {@link #sensorState}. Every field that got received by the REST API for this specific
 * sensor is published to the framework.
 *
 * @author Lukas Agethen - Initial contribution
 */
@NonNullByDefault
public class SensorThermostatThingHandler extends SensorBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_THERMOSTAT);

    private static final List<String> CONFIG_CHANNELS = List.of(CHANNEL_EXTERNAL_WINDOW_OPEN, CHANNEL_BATTERY_LEVEL,
            CHANNEL_BATTERY_LOW, CHANNEL_HEATSETPOINT, CHANNEL_TEMPERATURE_OFFSET, CHANNEL_THERMOSTAT_MODE,
            CHANNEL_THERMOSTAT_LOCKED);

    private final Logger logger = LoggerFactory.getLogger(SensorThermostatThingHandler.class);

    public SensorThermostatThingHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            sensorState.buttonevent = null;
            valueUpdated(channelUID, sensorState, false);
            return;
        }
        ThermostatUpdateConfig newConfig = new ThermostatUpdateConfig();
        switch (channelUID.getId()) {
            case CHANNEL_THERMOSTAT_LOCKED -> newConfig.locked = OnOffType.ON.equals(command);
            case CHANNEL_HEATSETPOINT -> {
                Integer newHeatsetpoint = getTemperatureFromCommand(command);
                if (newHeatsetpoint == null) {
                    logger.warn("Heatsetpoint must not be null.");
                    return;
                }
                newConfig.heatsetpoint = newHeatsetpoint;
            }
            case CHANNEL_TEMPERATURE_OFFSET -> {
                Integer newOffset = getTemperatureFromCommand(command);
                if (newOffset == null) {
                    logger.warn("Offset must not be null.");
                    return;
                }
                newConfig.offset = newOffset;
            }
            case CHANNEL_THERMOSTAT_MODE -> {
                if (command instanceof StringType stringCommand) {
                    String thermostatMode = stringCommand.toString();
                    try {
                        newConfig.mode = ThermostatMode.valueOf(thermostatMode);
                    } catch (IllegalArgumentException ex) {
                        logger.warn("Invalid thermostat mode: {}. Valid values: {}", thermostatMode,
                                ThermostatMode.values());
                        return;
                    }
                    if (newConfig.mode == ThermostatMode.UNKNOWN) {
                        logger.warn("Invalid thermostat mode: {}. Valid values: {}", thermostatMode,
                                ThermostatMode.values());
                        return;
                    }
                } else {
                    return;
                }
            }
            case CHANNEL_EXTERNAL_WINDOW_OPEN -> newConfig.externalwindowopen = OpenClosedType.OPEN.equals(command);
            default -> {
                // no supported command
                return;
            }
        }

        sendCommand(newConfig, command, channelUID, null);
    }

    @Override
    protected void valueUpdated(ChannelUID channelUID, SensorConfig newConfig) {
        super.valueUpdated(channelUID, newConfig);
        ThermostatMode thermostatMode = newConfig.mode;
        String mode = thermostatMode != null ? thermostatMode.name() : ThermostatMode.UNKNOWN.name();
        switch (channelUID.getId()) {
            case CHANNEL_THERMOSTAT_LOCKED -> updateSwitchChannel(channelUID, newConfig.locked);
            case CHANNEL_HEATSETPOINT ->
                updateQuantityTypeChannel(channelUID, newConfig.heatsetpoint, CELSIUS, 1.0 / 100);
            case CHANNEL_TEMPERATURE_OFFSET ->
                updateQuantityTypeChannel(channelUID, newConfig.offset, CELSIUS, 1.0 / 100);
            case CHANNEL_THERMOSTAT_MODE -> updateState(channelUID, new StringType(mode));
            case CHANNEL_EXTERNAL_WINDOW_OPEN -> {
                Boolean open = newConfig.externalwindowopen;
                if (open != null) {
                    updateState(channelUID, open ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
            }
        }
    }

    @Override
    protected void valueUpdated(ChannelUID channelUID, SensorState newState, boolean initializing) {
        super.valueUpdated(channelUID, newState, initializing);
        switch (channelUID.getId()) {
            case CHANNEL_TEMPERATURE -> updateQuantityTypeChannel(channelUID, newState.temperature, CELSIUS, 1.0 / 100);
            case CHANNEL_VALVE_POSITION -> {
                Integer valve = newState.valve;
                if (valve == null || valve < 0 || valve > 100) {
                    updateState(channelUID, UnDefType.UNDEF);
                } else {
                    updateQuantityTypeChannel(channelUID, valve, PERCENT, 1.0);
                }
            }
            case CHANNEL_WINDOW_OPEN -> {
                String open = newState.windowopen;
                if (open != null) {
                    updateState(channelUID, "Closed".equals(open) ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
                }
            }
            case CHANNEL_THERMOSTAT_ON -> updateSwitchChannel(channelUID, newState.on);
        }
    }

    @Override
    protected boolean createTypeSpecificChannels(ThingBuilder thingBuilder, SensorConfig sensorConfig,
            SensorState sensorState) {
        boolean thingEdited = false;
        if (sensorConfig.locked != null && createChannel(thingBuilder, CHANNEL_THERMOSTAT_LOCKED, ChannelKind.STATE)) {
            thingEdited = true;
        }
        if (sensorState.valve != null && createChannel(thingBuilder, CHANNEL_VALVE_POSITION, ChannelKind.STATE)) {
            thingEdited = true;
        }
        if (sensorState.on != null && createChannel(thingBuilder, CHANNEL_THERMOSTAT_ON, ChannelKind.STATE)) {
            thingEdited = true;
        }
        if (sensorState.windowopen != null && createChannel(thingBuilder, CHANNEL_WINDOW_OPEN, ChannelKind.STATE)) {
            thingEdited = true;
        }
        if (sensorConfig.externalwindowopen != null
                && createChannel(thingBuilder, CHANNEL_EXTERNAL_WINDOW_OPEN, ChannelKind.STATE)) {
            thingEdited = true;
        }

        return thingEdited;
    }

    @Override
    protected List<String> getConfigChannels() {
        return CONFIG_CHANNELS;
    }

    private @Nullable Integer getTemperatureFromCommand(Command command) {
        BigDecimal newTemperature;
        if (command instanceof DecimalType decimalCommand) {
            newTemperature = decimalCommand.toBigDecimal();
        } else if (command instanceof QuantityType) {
            @SuppressWarnings("unchecked")
            QuantityType<Temperature> temperatureCelsius = ((QuantityType<Temperature>) command).toUnit(CELSIUS);
            if (temperatureCelsius != null) {
                newTemperature = temperatureCelsius.toBigDecimal();
            } else {
                return null;
            }
        } else {
            return null;
        }
        return newTemperature.scaleByPowerOfTen(2).intValue();
    }
}
