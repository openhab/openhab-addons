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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.dto.SensorConfig;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.dto.SensorState;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
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
public abstract class SensorBaseThingHandler extends DeconzBaseThingHandler {
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
        super(thing, gson, ResourceType.SENSORS);
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    protected abstract boolean createTypeSpecificChannels(ThingBuilder thingBuilder, SensorConfig sensorState,
            SensorState sensorConfig);

    protected abstract List<String> getConfigChannels();

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (!ignoreConfigurationUpdate) {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    @Override
    protected void processStateResponse(DeconzBaseMessage stateResponse) {
        if (!(stateResponse instanceof SensorMessage sensorMessage)) {
            return;
        }

        sensorConfig = Objects.requireNonNullElse(sensorMessage.config, new SensorConfig());
        sensorState = Objects.requireNonNullElse(sensorMessage.state, new SensorState());

        // Add some information about the sensor
        if (!sensorConfig.reachable) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.sensor-not-reachable");
            return;
        }

        Map<String, String> editProperties = editProperties();
        editProperties.put(UNIQUE_ID, sensorMessage.uniqueid);
        editProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, sensorMessage.swversion);
        editProperties.put(Thing.PROPERTY_VENDOR, sensorMessage.manufacturername);
        editProperties.put(Thing.PROPERTY_MODEL_ID, sensorMessage.modelid);

        ignoreConfigurationUpdate = true;

        updateProperties(editProperties);

        // Some sensors support optional channels
        // (see https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Supported-Devices#sensors)
        // any battery-powered sensor
        ThingBuilder thingBuilder = editThing();
        boolean thingEdited = false;

        if (sensorConfig.battery != null) {
            if (createChannel(thingBuilder, CHANNEL_BATTERY_LEVEL, ChannelKind.STATE)) {
                thingEdited = true;
            }
            if (createChannel(thingBuilder, CHANNEL_BATTERY_LOW, ChannelKind.STATE)) {
                thingEdited = true;
            }
        } else if (sensorState.lowbattery != null) {
            // if sensorConfig.battery != null the channel is already added
            if (createChannel(thingBuilder, CHANNEL_BATTERY_LOW, ChannelKind.STATE)) {
                thingEdited = true;
            }
        }

        if (createTypeSpecificChannels(thingBuilder, sensorConfig, sensorState)) {
            thingEdited = true;
        }

        if (checkLastSeen(thingBuilder, sensorMessage.lastseen)) {
            thingEdited = true;
        }

        // if the thing was edited, we update it now
        if (thingEdited) {
            logger.debug("Thing configuration changed, updating thing.");
            updateThing(thingBuilder.build());
        }
        ignoreConfigurationUpdate = false;

        // Initial data
        updateChannels(sensorConfig);
        updateChannels(sensorState, true);

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Update channel value from {@link SensorConfig} object - override to include further channels
     *
     * @param channelUID
     * @param newConfig
     */
    protected void valueUpdated(ChannelUID channelUID, SensorConfig newConfig) {
        Integer batteryLevel = newConfig.battery;
        if (batteryLevel != null) {
            switch (channelUID.getId()) {
                case CHANNEL_BATTERY_LEVEL -> updateDecimalTypeChannel(channelUID, batteryLevel.longValue());
                case CHANNEL_BATTERY_LOW -> updateSwitchChannel(channelUID, batteryLevel <= 10);
                // other cases covered by subclass
            }
        }
    }

    /**
     * Update channel value from {@link SensorState} object - override to include further channels
     *
     * @param channelUID
     * @param newState
     * @param initializing
     */
    protected void valueUpdated(ChannelUID channelUID, SensorState newState, boolean initializing) {
        switch (channelUID.getId()) {
            case CHANNEL_LAST_UPDATED -> {
                String lastUpdated = newState.lastupdated;
                if (lastUpdated != null && !"none".equals(lastUpdated)) {
                    updateState(channelUID, Util.convertTimestampToDateTime(lastUpdated));
                }
            }
            case CHANNEL_BATTERY_LOW -> updateSwitchChannel(channelUID, newState.lowbattery);
            // other cases covered by subclass
        }
    }

    @Override
    public void messageReceived(DeconzBaseMessage message) {
        logger.trace("{} received {}", thing.getUID(), message);
        if (message instanceof SensorMessage sensorMessage) {
            SensorConfig sensorConfig = sensorMessage.config;
            if (sensorConfig != null) {
                if (sensorConfig.reachable) {
                    updateStatus(ThingStatus.ONLINE);
                    updateChannels(sensorConfig);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.sensor-not-reachable");
                }
            }
            SensorState sensorState = sensorMessage.state;
            if (sensorState != null) {
                updateChannels(sensorState, false);
            }
        }
    }

    private void updateChannels(SensorConfig newConfig) {
        this.sensorConfig = newConfig;
        List<String> configChannels = getConfigChannels();
        thing.getChannels().stream().map(Channel::getUID)
                .filter(channelUID -> configChannels.contains(channelUID.getId()))
                .forEach((channelUID) -> valueUpdated(channelUID, newConfig));
    }

    protected void updateChannels(SensorState newState, boolean initializing) {
        sensorState = newState;
        thing.getChannels().forEach(channel -> valueUpdated(channel.getUID(), newState, initializing));
    }
}
