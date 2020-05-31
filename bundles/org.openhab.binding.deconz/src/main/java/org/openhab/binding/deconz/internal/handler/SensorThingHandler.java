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

import static org.eclipse.smarthome.core.library.unit.MetricPrefix.HECTO;
import static org.eclipse.smarthome.core.library.unit.MetricPrefix.MILLI;
import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.library.unit.SIUnits.PASCAL;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;
import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.dto.SensorConfig;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.dto.SensorState;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
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
public class SensorThingHandler extends DeconzBaseThingHandler<SensorMessage> {
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
    /**
     * The sensor state. Contains all possible fields for all supported sensors and switches
     */
    private SensorConfig sensorConfig = new SensorConfig();
    private SensorState sensorState = new SensorState();
    /**
     * Prevent a dispose/init cycle while this flag is set. Use for property updates
     */
    private boolean ignoreConfigurationUpdate;

    public SensorThingHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    protected void requestState() {
        requestState("sensors");
    }

    @Override
    protected void registerListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.registerSensorListener(config.id, this);
        }
    }

    @Override
    protected void unregisterListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.unregisterSensorListener(config.id);
        }
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
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (!ignoreConfigurationUpdate) {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    @Override
    protected @Nullable SensorMessage parseStateResponse(AsyncHttpClient.Result r) {
        if (r.getResponseCode() == 403) {
            return null;
        } else if (r.getResponseCode() == 200) {
            return gson.fromJson(r.getBody(), SensorMessage.class);
        } else {
            throw new IllegalStateException("Unknown status code " + r.getResponseCode() + " for full state request");
        }
    }

    @Override
    protected void processStateResponse(@Nullable SensorMessage stateResponse) {
        if (stateResponse == null) {
            return;
        }
        SensorConfig newSensorConfig = stateResponse.config;
        sensorConfig = newSensorConfig != null ? newSensorConfig : new SensorConfig();
        SensorState newSensorState = stateResponse.state;
        sensorState = newSensorState != null ? newSensorState : new SensorState();

        // Add some information about the sensor
        if (!sensorConfig.reachable) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Not reachable");
            return;
        }

        if (!sensorConfig.on) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        Map<String, String> editProperties = editProperties();
        editProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, stateResponse.swversion);
        editProperties.put(Thing.PROPERTY_MODEL_ID, stateResponse.modelid);
        editProperties.put(UNIQUE_ID, stateResponse.uniqueid);
        ignoreConfigurationUpdate = true;
        updateProperties(editProperties);

        // Some sensors support optional channels
        // (see https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Supported-Devices#sensors)
        // any battery-powered sensor
        if (sensorConfig.battery != null) {
            createChannel(CHANNEL_BATTERY_LEVEL, ChannelKind.STATE);
            createChannel(CHANNEL_BATTERY_LOW, ChannelKind.STATE);
        }

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
        ignoreConfigurationUpdate = false;

        // Initial data
        updateChannels(sensorConfig);
        updateChannels(sensorState, true);

        updateStatus(ThingStatus.ONLINE);
    }

    private void createChannel(String channelId, ChannelKind kind) {
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
            ChannelTypeUID channelTypeUID;
            switch (channelId) {
                case CHANNEL_BATTERY_LEVEL:
                    channelTypeUID = new ChannelTypeUID("system:battery-level");
                    break;
                case CHANNEL_BATTERY_LOW:
                    channelTypeUID = new ChannelTypeUID("system:low-battery");
                    break;
                default:
                    channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
                    break;
            }
            Channel channel = callback.createChannelBuilder(channelUID, channelTypeUID).withKind(kind).build();
            updateThing(editThing().withoutChannel(channelUID).withChannel(channel).build());
        }
    }

    public void valueUpdated(ChannelUID channelUID, SensorConfig newConfig) {
        Integer batteryLevel = newConfig.battery;
        Float temperature = newConfig.temperature;

        switch (channelUID.getId()) {
            case CHANNEL_BATTERY_LEVEL:
                if (batteryLevel != null) {
                    updateState(channelUID, new DecimalType(batteryLevel.longValue()));
                }
                break;
            case CHANNEL_BATTERY_LOW:
                if (batteryLevel != null) {
                    updateState(channelUID, OnOffType.from(batteryLevel <= 10));
                }
                break;
            case CHANNEL_TEMPERATURE:
                if (temperature != null) {
                    updateState(channelUID, new QuantityType<>(temperature / 100, CELSIUS));
                }
                break;
        }
    }

    public void valueUpdated(String channelID, SensorState newState, boolean initializing) {
        switch (channelID) {
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
            case CHANNEL_BATTERY_LEVEL:
                updateDecimalTypeChannel(channelID, newState.battery);
                break;
            case CHANNEL_LAST_UPDATED:
                String lastUpdated = newState.lastupdated;
                if (lastUpdated != null && !"none".equals(lastUpdated)) {
                    updateState(channelID,
                            new DateTimeType(ZonedDateTime.ofInstant(
                                    LocalDateTime.parse(lastUpdated, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                    ZoneOffset.UTC, ZoneId.systemDefault())));
                }
                break;
        }
    }

    @Override
    public void messageReceived(String sensorID, DeconzBaseMessage message) {
        if (message instanceof SensorMessage) {
            SensorMessage sensorMessage = (SensorMessage) message;
            SensorConfig sensorConfig = sensorMessage.config;
            if (sensorConfig != null) {
                this.sensorConfig = sensorConfig;
                updateChannels(sensorConfig);
            }
            SensorState sensorState = sensorMessage.state;
            if (sensorState != null) {
                updateChannels(sensorState, false);
            }
        }
    }

    private void updateChannels(SensorConfig newConfig) {
        thing.getChannels().stream().map(Channel::getUID)
                .filter(channelUID -> CONFIG_CHANNELS.contains(channelUID.getId()))
                .forEach((channelUID) -> valueUpdated(channelUID, newConfig));
    }

    private void updateChannels(SensorState newState, boolean initializing) {
        logger.trace("{} received {}", thing.getUID(), newState);
        sensorState = newState;
        thing.getChannels().forEach(channel -> valueUpdated(channel.getUID().getId(), newState, initializing));
    }

    private void updateSwitchChannel(String channelID, @Nullable Boolean value) {
        if (value == null) {
            return;
        }
        updateState(channelID, OnOffType.from(value));
    }

    private void updateDecimalTypeChannel(String channelID, @Nullable Number value) {
        if (value == null) {
            return;
        }
        updateState(channelID, new DecimalType(value.longValue()));
    }

    private void updateQuantityTypeChannel(String channelID, @Nullable Number value, Unit<?> unit) {
        updateQuantityTypeChannel(channelID, value, unit, 1.0);
    }

    private void updateQuantityTypeChannel(String channelID, @Nullable Number value, Unit<?> unit, double scaling) {
        if (value == null) {
            return;
        }
        updateState(channelID, new QuantityType<>(value.doubleValue() * scaling, unit));
    }
}
