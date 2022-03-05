/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.dto.SensorConfig;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.dto.SensorState;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
    private @Nullable ScheduledFuture<?> lastSeenPollingJob;

    public SensorBaseThingHandler(Thing thing, Gson gson) {
        super(thing, gson, ResourceType.SENSORS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> lastSeenPollingJob = this.lastSeenPollingJob;
        if (lastSeenPollingJob != null) {
            lastSeenPollingJob.cancel(true);
            this.lastSeenPollingJob = null;
        }

        super.dispose();
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
    protected void processStateResponse(DeconzBaseMessage stateResponse) {
        if (!(stateResponse instanceof SensorMessage)) {
            return;
        }

        SensorMessage sensorMessage = (SensorMessage) stateResponse;
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
        if (sensorConfig.battery != null) {
            createChannel(CHANNEL_BATTERY_LEVEL, ChannelKind.STATE);
            createChannel(CHANNEL_BATTERY_LOW, ChannelKind.STATE);
        }

        if (sensorState.lowbattery != null) {
            createChannel(CHANNEL_BATTERY_LOW, ChannelKind.STATE);
        }

        createTypeSpecificChannels(sensorConfig, sensorState);

        ignoreConfigurationUpdate = false;

        // "Last seen" is the last "ping" from the device, whereas "last update" is the last status changed.
        // For example, for a fire sensor, the device pings regularly, without necessarily updating channels.
        // So to monitor a sensor is still alive, the "last seen" is necessary.
        // Because "last seen" is never updated by the WebSocket API - if this is supported, then we have to
        // manually poll it after the defined time
        String lastSeen = sensorMessage.lastseen;
        if (lastSeen != null && config.lastSeenPolling > 0) {
            createChannel(CHANNEL_LAST_SEEN, ChannelKind.STATE);
            updateState(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime(lastSeen));
            lastSeenPollingJob = scheduler.schedule(() -> requestState(this::processLastSeen), config.lastSeenPolling,
                    TimeUnit.MINUTES);
            logger.trace("lastSeen polling enabled for thing {} with interval of {} minutes", thing.getUID(),
                    config.lastSeenPolling);
        }

        // Initial data
        updateChannels(sensorConfig);
        updateChannels(sensorState, true);

        updateStatus(ThingStatus.ONLINE);
    }

    private void processLastSeen(DeconzBaseMessage stateResponse) {
        String lastSeen = stateResponse.lastseen;
        if (lastSeen != null) {
            updateState(CHANNEL_LAST_SEEN, Util.convertTimestampToDateTime(lastSeen));
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
     * @param channelUID
     * @param newState
     * @param initializing
     */
    protected void valueUpdated(ChannelUID channelUID, SensorState newState, boolean initializing) {
        switch (channelUID.getId()) {
            case CHANNEL_LAST_UPDATED:
                String lastUpdated = newState.lastupdated;
                if (lastUpdated != null && !"none".equals(lastUpdated)) {
                    updateState(channelUID, Util.convertTimestampToDateTime(lastUpdated));
                }
                break;
            case CHANNEL_BATTERY_LOW:
                Boolean lowBattery = newState.lowbattery;
                if (lowBattery != null) {
                    updateState(channelUID, OnOffType.from(lowBattery));
                }
                break;
            default:
                // other cases covered by sub-class
        }
    }

    @Override
    public void messageReceived(String sensorID, DeconzBaseMessage message) {
        logger.trace("{} received {}", thing.getUID(), message);
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
        sensorState = newState;
        thing.getChannels().forEach(channel -> valueUpdated(channel.getUID(), newState, initializing));
    }

    protected void updateSwitchChannel(ChannelUID channelUID, @Nullable Boolean value) {
        if (value == null) {
            return;
        }
        updateState(channelUID, OnOffType.from(value));
    }

    protected void updateStringChannel(ChannelUID channelUID, @Nullable String value) {
        updateState(channelUID, new StringType(value));
    }

    protected void updateDecimalTypeChannel(ChannelUID channelUID, @Nullable Number value) {
        if (value == null) {
            return;
        }
        updateState(channelUID, new DecimalType(value.longValue()));
    }

    protected void updateQuantityTypeChannel(ChannelUID channelUID, @Nullable Number value, Unit<?> unit) {
        updateQuantityTypeChannel(channelUID, value, unit, 1.0);
    }

    protected void updateQuantityTypeChannel(ChannelUID channelUID, @Nullable Number value, Unit<?> unit,
            double scaling) {
        if (value == null) {
            return;
        }
        updateState(channelUID, new QuantityType<>(value.doubleValue() * scaling, unit));
    }
}
