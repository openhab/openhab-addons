/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Power;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.dto.SensorState;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.ValueUpdateListener;
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
 * in {@link #state}. Every field that got received by the REST API for this specific
 * sensor is published to the framework.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorThingHandler extends BaseThingHandler implements ValueUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(SensorThingHandler.class);
    private SensorThingConfig config = new SensorThingConfig();
    private DeconzBridgeConfig bridgeConfig = new DeconzBridgeConfig();
    private final Gson gson = new Gson();
    private @Nullable WebSocketConnection connection;
    /** The sensor state. Contains all possible fields for all supported sensors and switches */
    private SensorState state = new SensorState();
    /** Prevent a dispose/init cycle while this flag is set. Use for property updates */
    private boolean ignoreConfigurationUpdate;

    public SensorThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            return;
        }

        state.buttonevent = null;
        valueUpdated(channelUID, state, false);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (!ignoreConfigurationUpdate) {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    private @Nullable SensorMessage parseStateResponse(AsyncHttpClient.Result r) {
        if (r.getResponseCode() == 403) {
            return null;
        } else if (r.getResponseCode() == 200) {
            return gson.fromJson(r.getBody(), SensorMessage.class);
        } else {
            throw new IllegalStateException("Unknown status code for full state request");
        }
    }

    @Override
    public void bridgeStatusChanged(@NonNull ThingStatusInfo bridgeStatusInfo) {
        if (config.id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ID not set");
            return;
        }

        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            WebSocketConnection webSocketConnection = connection;
            if (webSocketConnection != null) {
                webSocketConnection.unregisterValueListener(config.id);
            }
            return;
        }

        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        DeconzBridgeHandler handler = (DeconzBridgeHandler) bridge.getHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        final WebSocketConnection webSocketConnection = handler.getWebsocketConnection();
        this.connection = webSocketConnection;
        this.bridgeConfig = handler.getBridgeConfig();

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);

        String url = url(bridgeConfig.host, bridgeConfig.apikey, "sensors", config.id);

        // Get initial data
        handler.getHttp().get(url.toString(), bridgeConfig.timeout).thenApply(this::parseStateResponse) //
                .exceptionally(e -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    logger.debug("Get state failed", e);
                    return null;
                }).thenAccept(newState -> {
                    // Auth failed
                    if (newState == null) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Auth failed");
                        return;
                    }

                    // Add some information about the sensor
                    if (!newState.config.reachable) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Not reachable");
                        return;
                    }

                    if (!newState.config.on) {
                        updateStatus(ThingStatus.OFFLINE);
                        return;
                    }

                    Map<String, String> editProperties = editProperties();
                    editProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, newState.swversion);
                    editProperties.put(Thing.PROPERTY_MODEL_ID, newState.modelid);
                    editProperties.put(UNIQUE_ID, newState.uniqueid);
                    ignoreConfigurationUpdate = true;
                    updateProperties(editProperties);

                    // Some sensors support optional channels
                    // (see https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Supported-Devices#sensors)
                    // any battery-powered sensor
                    Integer batteryLevel = newState.config.battery;
                    if (batteryLevel != null) {
                        createAndUpdateChannelIfExists(CHANNEL_BATTERY_LEVEL,
                                new DecimalType(batteryLevel.longValue()));
                        createAndUpdateChannelIfExists(CHANNEL_BATTERY_LOW,
                                batteryLevel <= 10 ? OnOffType.ON : OnOffType.OFF);
                    }

                    // some Xiaomi sensors
                    Float temperature = newState.config.temperature;
                    if (temperature != null) {
                        createAndUpdateChannelIfExists(CHANNEL_TEMPERATURE,
                                new QuantityType<Temperature>(temperature / 100, CELSIUS));
                    }

                    // ZHAPresence - e.g. IKEA TRÅDFRI motion sensor
                    if (newState.state.dark != null) {
                        createChannel(CHANNEL_DARK);
                    }

                    // ZHAConsumption - e.g Bitron 902010/25 or Heiman SmartPlug
                    if (newState.state.power != null) {
                        createChannel(CHANNEL_POWER);
                    }

                    // ZHAPower - e.g. Heiman SmartPlug
                    if (newState.state.voltage != null) {
                        createChannel(CHANNEL_VOLTAGE);
                    }
                    if (newState.state.current != null) {
                        createChannel(CHANNEL_CURRENT);
                    }

                    // IAS Zone sensor - e.g. Heiman HS1MS motion sensor
                    if (newState.state.tampered != null) {
                        createChannel(CHANNEL_TAMPERED);
                    }
                    ignoreConfigurationUpdate = false;

                    // Initial data
                    for (Channel channel : thing.getChannels()) {
                        valueUpdated(channel.getUID(), newState.state, true);
                    }

                    // Real-time data
                    webSocketConnection.registerValueListener(config.id, this);
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                });

    }

    private void createAndUpdateChannelIfExists(String channelId, State state) {
        Channel channel = thing.getChannel(channelId);
        if (channel == null) {
            channel = createChannel(channelId);
        }
        if (channel != null) {
            updateState(channel.getUID(), state);
        }
    }

    private @Nullable Channel createChannel(String channelId) {
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
            Channel channel = callback.createChannelBuilder(channelUID, channelTypeUID).build();
            updateThing(editThing().withoutChannel(channelUID).withChannel(channel).build());
            return channel;
        }
        return null;
    }

    @Override
    public void dispose() {
        WebSocketConnection webSocketConnection = connection;
        if (webSocketConnection != null) {
            webSocketConnection.unregisterValueListener(config.id);
        }
        super.dispose();
    }

    @Override
    public void initialize() {
        config = getConfigAs(SensorThingConfig.class);

        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    public void valueUpdated(ChannelUID channelUID, SensorState state, boolean initializing) {
        this.state = state;

        Integer buttonevent = state.buttonevent;
        String lastUpdated = state.lastupdated;
        Integer status = state.status;
        Boolean presence = state.presence;
        Boolean open = state.open;
        Float power = state.power;
        Float consumption = state.consumption;
        Float voltage = state.voltage;
        Float current = state.current;
        Integer lux = state.lux;
        Integer lightlevel = state.lightlevel;
        Float temperature = state.temperature;
        Float humidity = state.humidity;
        Integer pressure = state.pressure;

        switch (channelUID.getId()) {
            case CHANNEL_LIGHT:
                if (state.dark != null) {
                    boolean dark = state.dark;
                    if (dark) { // if it's dark, it's dark ;)
                        updateState(channelUID, new StringType("Dark"));
                    } else if (state.daylight != null) { // if its not dark, it might be between darkness and daylight
                        boolean daylight = state.daylight;
                        if (daylight) {
                            updateState(channelUID, new StringType("Daylight"));
                        } else if (!daylight) {
                            updateState(channelUID, new StringType("Sunset"));
                        }
                    } else { // if no daylight value is known, we assume !dark means daylight
                        updateState(channelUID, new StringType("Daylight"));
                    }
                }
                break;
            case CHANNEL_POWER:
                if (power != null) {
                    updateState(channelUID, new QuantityType<Power>(power, WATT));
                }
                break;
            case CHANNEL_CONSUMPTION:
                if (consumption != null) {
                    updateState(channelUID, new QuantityType<Energy>(consumption, WATT_HOUR));
                }
                break;
            case CHANNEL_VOLTAGE:
                if (voltage != null) {
                    updateState(channelUID, new QuantityType<ElectricPotential>(voltage, VOLT));
                }
                break;
            case CHANNEL_CURRENT:
                if (current != null) {
                    updateState(channelUID, new QuantityType<ElectricCurrent>(current, MILLI(AMPERE)));
                }
                break;
            case CHANNEL_LIGHT_LUX:
                if (lux != null) {
                    updateState(channelUID, new QuantityType<Illuminance>(lux, LUX));
                }
                break;
            case CHANNEL_LIGHT_LEVEL:
                if (lightlevel != null) {
                    updateState(channelUID, new DecimalType(lightlevel));
                }
                break;
            case CHANNEL_DARK:
                updateState(channelUID, Boolean.TRUE.equals(state.dark) ? OnOffType.ON : OnOffType.OFF);
                break;
            case CHANNEL_DAYLIGHT:
                updateState(channelUID, Boolean.TRUE.equals(state.daylight) ? OnOffType.ON : OnOffType.OFF);
                break;
            case CHANNEL_TEMPERATURE:
                if (temperature != null) {
                    updateState(channelUID, new QuantityType<Temperature>(temperature / 100, CELSIUS));
                }
                break;
            case CHANNEL_HUMIDITY:
                if (humidity != null) {
                    updateState(channelUID, new QuantityType<Dimensionless>(humidity / 100, PERCENT));
                }
                break;
            case CHANNEL_PRESSURE:
                if (pressure != null) {
                    updateState(channelUID, new QuantityType<Pressure>(pressure, HECTO(PASCAL)));
                }
                break;
            case CHANNEL_PRESENCE:
                if (presence != null) {
                    updateState(channelUID, OnOffType.from(presence));
                }
                break;
            case CHANNEL_VALUE:
                if (status != null) {
                    updateState(channelUID, new DecimalType(status));
                }
                break;
            case CHANNEL_OPENCLOSE:
                if (open != null) {
                    updateState(channelUID, open ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
                break;
            case CHANNEL_WATERLEAKAGE:
                updateState(channelUID, Boolean.TRUE.equals(state.water) ? OnOffType.ON : OnOffType.OFF);
                break;
            case CHANNEL_ALARM:
                updateState(channelUID, Boolean.TRUE.equals(state.alarm) ? OnOffType.ON : OnOffType.OFF);
                break;
            case CHANNEL_TAMPERED:
                updateState(channelUID, Boolean.TRUE.equals(state.tampered) ? OnOffType.ON : OnOffType.OFF);
                break;
            case CHANNEL_VIBRATION:
                updateState(channelUID, Boolean.TRUE.equals(state.vibration) ? OnOffType.ON : OnOffType.OFF);
                break;
            case CHANNEL_BUTTON:
                if (buttonevent != null) {
                    updateState(channelUID, new DecimalType(buttonevent));
                }
                break;
            case CHANNEL_BUTTONEVENT:
                if (buttonevent != null && !initializing) {
                    triggerChannel(channelUID, String.valueOf(buttonevent));
                }
                break;
            case CHANNEL_LAST_UPDATED:
                if (lastUpdated != null && !"none".equals(lastUpdated)) {
                    updateState(channelUID,
                            new DateTimeType(ZonedDateTime.ofInstant(
                                    LocalDateTime.parse(lastUpdated, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                    ZoneOffset.UTC, ZoneId.systemDefault())));
                }
                break;
        }
    }

    @Override
    public void websocketUpdate(String sensorID, SensorState newState) {
        for (Channel channel : thing.getChannels()) {
            valueUpdated(channel.getUID(), newState, false);
        }
    }
}
