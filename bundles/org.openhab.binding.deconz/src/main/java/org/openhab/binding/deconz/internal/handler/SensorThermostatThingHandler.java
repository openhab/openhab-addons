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

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.PERCENT;
import static org.openhab.binding.deconz.internal.BindingConstants.*;
import static org.openhab.binding.deconz.internal.Util.buildUrl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
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
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
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
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_THERMOSTAT).collect(Collectors.toSet()));

    private static final List<String> CONFIG_CHANNELS = Arrays.asList(CHANNEL_BATTERY_LEVEL, CHANNEL_BATTERY_LOW,
            CHANNEL_HEATSETPOINT, CHANNEL_TEMPERATURE_OFFSET, CHANNEL_THERMOSTAT_MODE);

    private final Logger logger = LoggerFactory.getLogger(SensorThermostatThingHandler.class);


    public SensorThermostatThingHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            sensorState.buttonevent = null;
            valueUpdated(channelUID.getId(), sensorState, false);
            return;
        }
        SensorConfig newConfig = new SensorConfig();
        String channelId = channelUID.getId();
        switch (channelId) {
            case CHANNEL_HEATSETPOINT:
                BigDecimal newTemperature;
                if (command instanceof DecimalType) {
                    newTemperature = ((DecimalType) command).toBigDecimal();
                } else if (command instanceof QuantityType) {
                    newTemperature = ((QuantityType) command).toUnit(CELSIUS).toBigDecimal();
                } else {
                    return;
                }
                newConfig.heatsetpoint = newTemperature.scaleByPowerOfTen(2).intValue();
                break;
            case CHANNEL_THERMOSTAT_MODE:
                if (command instanceof StringType) {
                    newConfig.mode = ((StringType) command).toString();
                } else {
                    return;
                }
                break;
            default:
                // no supported command
                return;

        }

        AsyncHttpClient asyncHttpClient = http;
        if (asyncHttpClient == null) {
            return;
        }
        String url = buildUrl(bridgeConfig.host, bridgeConfig.httpPort, bridgeConfig.apikey, "sensors", config.id,
                "config");

        String json = gson.toJson(newConfig);
        logger.trace("Sending {} to sensor {} via {}", json, config.id, url);
        asyncHttpClient.put(url, json, bridgeConfig.timeout).thenAccept(v -> {
            logger.trace("Result code={}, body={}", v.getResponseCode(), v.getBody());
        }).exceptionally(e -> {
            logger.debug("Sending command {} to channel {} failed:", command, channelUID, e);
            return null;
        });
    }

    @Override
    public void valueUpdated(ChannelUID channelUID, SensorConfig newConfig) {
        super.valueUpdated(channelUID, newConfig);
        String mode = newConfig.mode;
        String channelID = channelUID.getId();
        switch (channelID) {
            case CHANNEL_HEATSETPOINT:
                updateQuantityTypeChannel(channelID, newConfig.heatsetpoint, CELSIUS, 1.0 / 100);
                break;
            case CHANNEL_TEMPERATURE_OFFSET:
                updateDecimalTypeChannel(channelID, newConfig.offset);
                break;
            case CHANNEL_THERMOSTAT_MODE:
                if (mode != null) {
                    updateState(channelUID, new StringType(mode));
                }
                break;
        }
    }

    @Override
    public void valueUpdated(String channelID, SensorState newState, boolean initializing) {
        super.valueUpdated(channelID, newState, initializing);
        switch (channelID) {
            case CHANNEL_TEMPERATURE:
                updateQuantityTypeChannel(channelID, newState.temperature, CELSIUS, 1.0 / 100);
                break;
            case CHANNEL_VALVE_POSITION:
                updateQuantityTypeChannel(channelID, newState.valve, PERCENT, 100.0 / 255);
                break;
        }
    }

    @Override
    protected void createTypeSpecificChannels(SensorConfig sensorConfig, SensorState sensorState) {
        // some Xiaomi sensors
        if (sensorConfig.temperature != null || sensorState.temperature != null) {
            createChannel(CHANNEL_TEMPERATURE, ChannelKind.STATE);
        }

        // (Eurotronics) Thermostat
        if (sensorState.valve != null) {
            createChannel(CHANNEL_VALVE_POSITION, ChannelKind.STATE);
        }

        if (sensorConfig.heatsetpoint != null) {
            createChannel(CHANNEL_HEATSETPOINT, ChannelKind.STATE);
        }

        if (sensorConfig.mode != null) {
            createChannel(CHANNEL_THERMOSTAT_MODE, ChannelKind.STATE);
        }

        if (sensorConfig.offset != null) {
            createChannel(CHANNEL_TEMPERATURE_OFFSET, ChannelKind.STATE);
        }

    }

    @Override
    protected List<String> getConfigChannels() {
        return CONFIG_CHANNELS;
    }
}
