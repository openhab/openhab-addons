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

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
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
 * @author Lukas Agethen - Refactored to provide better extensibility
 */
@NonNullByDefault
public abstract class SensorBaseThingHandler extends DeconzBaseThingHandler<SensorMessage> {
    private final Logger logger = LoggerFactory.getLogger(SensorBaseThingHandler.class);
    /**
     * The sensor state. Contains all possible fields for all supported sensors and switches
     */
    protected SensorConfig sensorConfig = new SensorConfig();
    protected SensorState sensorState = new SensorState();
    /**
     * Prevent a dispose/init cycle while this flag is set. Use for property updates
     */
    private boolean ignoreConfigurationUpdate;

    public SensorBaseThingHandler(Thing thing, Gson gson) {
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
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    protected abstract void createTypeSpecificChannels(SensorConfig sensorState, SensorState sensorConfig);

    protected abstract List<String> getConfigChannels();

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

        createTypeSpecificChannels(sensorConfig, sensorState);

        ignoreConfigurationUpdate = false;

        // Initial data
        updateChannels(sensorConfig);
        updateChannels(sensorState, true);

        // "Last seen" is the last "ping" from the device, whereas "last update" is the last status changed.
        // For example, for a fire sensor, the device pings regularly, without necessarily updating channels.
        // So to monitor a sensor is still alive, the "last seen" is necessary.
        if (stateResponse.lastseen != null) {
            updateState(CHANNEL_LAST_SEEN,
                    new DateTimeType(ZonedDateTime.ofInstant(
                            LocalDateTime.parse(stateResponse.lastseen, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            ZoneOffset.UTC, ZoneId.systemDefault())));
            // Because "last seen" is never updated by the WebSocket API - if this is supported, then we have to
            // manually poll it time to time (every 5 minutes by default)
            super.scheduledFuture = scheduler.schedule((Runnable) this::requestState, 5, TimeUnit.MINUTES);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    protected void createChannel(String channelId, ChannelKind kind) {
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

    /**
     * Update channel value from {@link SensorConfig} object - override to include further channels
     *
     * @param channelUID
     * @param newConfig
     */
    protected void valueUpdated(ChannelUID channelUID, SensorConfig newConfig) {
        Integer batteryLevel = newConfig.battery;
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
            default:
                // other cases covered by sub-class
        }
    }

    /**
     * Update channel value from {@link SensorState} object - override to include further channels
     *
     * @param channelID
     * @param newState
     * @param initializing
     */
    protected void valueUpdated(String channelID, SensorState newState, boolean initializing) {
        switch (channelID) {
            case CHANNEL_LAST_UPDATED:
                String lastUpdated = newState.lastupdated;
                if (lastUpdated != null && !"none".equals(lastUpdated)) {
                    updateState(channelID,
                            new DateTimeType(ZonedDateTime.ofInstant(
                                    LocalDateTime.parse(lastUpdated, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                    ZoneOffset.UTC, ZoneId.systemDefault())));
                }
                break;
            default:
                // other cases covered by sub-class
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
        List<String> configChannels = getConfigChannels();
        thing.getChannels().stream().map(Channel::getUID)
                .filter(channelUID -> configChannels.contains(channelUID.getId()))
                .forEach((channelUID) -> valueUpdated(channelUID, newConfig));
    }

    protected void updateChannels(SensorState newState, boolean initializing) {
        logger.trace("{} received {}", thing.getUID(), newState);
        sensorState = newState;
        thing.getChannels().forEach(channel -> valueUpdated(channel.getUID().getId(), newState, initializing));
    }

    protected void updateSwitchChannel(String channelID, @Nullable Boolean value) {
        if (value == null) {
            return;
        }
        updateState(channelID, OnOffType.from(value));
    }

    protected void updateDecimalTypeChannel(String channelID, @Nullable Number value) {
        if (value == null) {
            return;
        }
        updateState(channelID, new DecimalType(value.longValue()));
    }

    protected void updateQuantityTypeChannel(String channelID, @Nullable Number value, Unit<?> unit) {
        updateQuantityTypeChannel(channelID, value, unit, 1.0);
    }

    protected void updateQuantityTypeChannel(String channelID, @Nullable Number value, Unit<?> unit, double scaling) {
        if (value == null) {
            return;
        }
        updateState(channelID, new QuantityType<>(value.doubleValue() * scaling, unit));
    }
}
