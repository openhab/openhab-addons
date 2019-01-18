/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.onewire.internal.handler;

import static org.eclipse.smarthome.binding.onewire.internal.OwBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.eclipse.smarthome.binding.onewire.internal.OwException;
import org.eclipse.smarthome.binding.onewire.internal.SensorId;
import org.eclipse.smarthome.binding.onewire.internal.device.AbstractOwDevice;
import org.eclipse.smarthome.binding.onewire.internal.device.OwSensorType;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwBaseThingHandler} class defines a handler for simple OneWire devices
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class OwBaseThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(OwBaseThingHandler.class);

    protected static final int PROPERTY_UPDATE_INTERVAL = 5000; // in ms
    protected static final int PROPERTY_UPDATE_MAX_RETRY = 5;

    private static final Set<String> REQUIRED_PROPERTIES = Collections
            .unmodifiableSet(Stream.of(PROPERTY_MODELID, PROPERTY_VENDOR).collect(Collectors.toSet()));

    protected List<String> requiredProperties = new ArrayList<>(REQUIRED_PROPERTIES);
    protected Set<OwSensorType> supportedSensorTypes;

    protected final List<AbstractOwDevice> sensors = new ArrayList<AbstractOwDevice>();
    protected @NonNullByDefault({}) SensorId sensorId;
    protected @NonNullByDefault({}) OwSensorType sensorType;

    protected long lastRefresh = 0;
    protected long refreshInterval = 300 * 1000;

    protected boolean validConfig = false;
    protected boolean showPresence = false;

    protected OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    protected @Nullable ScheduledFuture<?> updateTask;

    public OwBaseThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            Set<OwSensorType> supportedSensorTypes) {
        super(thing);

        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
        this.supportedSensorTypes = supportedSensorTypes;
    }

    public OwBaseThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            Set<OwSensorType> supportedSensorTypes, Set<String> requiredProperties) {
        super(thing);

        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
        this.supportedSensorTypes = supportedSensorTypes;
        this.requiredProperties.addAll(requiredProperties);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            lastRefresh = 0;
            logger.trace("scheduled {} for refresh", this.thing.getUID());
        }
    }

    @Override
    public void initialize() {
        configure();
    }

    protected boolean configure() {
        Configuration configuration = getConfig();
        Map<String, String> properties = thing.getProperties();

        if (getBridge() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge missing");
            return false;
        }
        sensors.clear();

        if (configuration.get(CONFIG_ID) != null) {
            String sensorId = (String) configuration.get(CONFIG_ID);
            try {
                this.sensorId = new SensorId(sensorId);
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "sensor id format mismatch");
                return false;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "sensor id missing");
            return false;
        }

        if (configuration.get(CONFIG_REFRESH) != null) {
            refreshInterval = ((BigDecimal) configuration.get(CONFIG_REFRESH)).intValue() * 1000;
        } else {
            refreshInterval = 300 * 1000;
        }

        if (thing.getChannel(CHANNEL_PRESENT) != null) {
            showPresence = true;
        }

        // check if all required properties are present. update if not
        for (String property : requiredProperties) {
            if (!properties.containsKey(property)) {
                updateSensorProperties();
                return false;
            }
        }

        sensorType = OwSensorType.valueOf(properties.get(PROPERTY_MODELID));
        if (!supportedSensorTypes.contains(sensorType)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "sensor type not supported by this thing type");
            return false;
        }

        lastRefresh = 0;
        return true;
    }

    /**
     * check if thing can be refreshed from the bridge handler
     *
     * @return true if thing can be refreshed
     */
    public boolean isRefreshable() {
        return super.isInitialized()
                && this.thing.getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR
                && this.thing.getStatusInfo().getStatusDetail() != ThingStatusDetail.BRIDGE_OFFLINE;
    }

    /**
     * refresh this thing
     *
     * needs proper exception handling for refresh errors if overridden
     *
     * @param bridgeHandler bridge handler to use for communication with ow bus
     * @param now current time
     */
    public void refresh(OwBaseBridgeHandler bridgeHandler, long now) {
        try {
            Boolean forcedRefresh = lastRefresh == 0;
            if (now >= (lastRefresh + refreshInterval)) {
                logger.trace("refreshing {}", this.thing.getUID());

                lastRefresh = now;

                if (!sensors.get(0).checkPresence(bridgeHandler)) {
                    logger.trace("sensor not present");
                    return;
                }

                for (int i = 0; i < sensors.size(); i++) {
                    logger.trace("refreshing sensor {} ({})", i, sensors.get(i).getSensorId());
                    sensors.get(i).refresh(bridgeHandler, forcedRefresh);
                }
            }
        } catch (OwException e) {
            logger.debug("{}: refresh exception {}", this.thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "refresh exception");
        }

    }

    /**
     * update presence status to present state of slave
     *
     * @param presentState current present state
     */
    public void updatePresenceStatus(State presentState) {
        if (OnOffType.ON.equals(presentState)) {
            updateStatus(ThingStatus.ONLINE);
            if (showPresence) {
                updateState(CHANNEL_PRESENT, OnOffType.ON);
            }
        } else if (OnOffType.OFF.equals(presentState)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "slave missing");
            if (showPresence) {
                updateState(CHANNEL_PRESENT, OnOffType.OFF);
            }
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            if (showPresence) {
                updateState(CHANNEL_PRESENT, UnDefType.UNDEF);
            }
        }
    }

    /**
     * post update to channel
     *
     * @param channelId channel id
     * @param state new channel state
     */
    public void postUpdate(String channelId, State state) {
        if (this.thing.getChannel(channelId) != null) {
            updateState(channelId, state);
        } else {
            logger.warn("{} missing channel {} when posting update {}", this.thing.getUID(), channelId, state);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            if (validConfig) {
                updatePresenceStatus(UnDefType.UNDEF);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        dynamicStateDescriptionProvider.removeDescriptionsForThing(thing.getUID());
        super.dispose();
    }

    /**
     * add this sensor to the property update list of the bridge handler
     *
     */
    protected void updateSensorProperties() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge not found");
            return;
        }

        OwBaseBridgeHandler bridgeHandler = (OwBaseBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            logger.debug("bridgehandler for {} not available for scheduling property update, retrying in 5s",
                    thing.getUID());
            scheduler.schedule(() -> {
                updateSensorProperties();
            }, 5000, TimeUnit.MILLISECONDS);
            return;
        }

        bridgeHandler.scheduleForPropertiesUpdate(thing);
    }

    /**
     * thing specific update method for sensor properties
     *
     * called by the bridge handler
     *
     * @param bridgeHandler the bridge handler to be used
     * @return properties to be added to the properties map
     * @throws OwException
     */
    public Map<String, String> updateSensorProperties(OwBaseBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = new HashMap<String, String>();
        OwSensorType sensorType = bridgeHandler.getType(sensorId);
        properties.put(PROPERTY_MODELID, sensorType.toString());
        properties.put(PROPERTY_VENDOR, "Dallas/Maxim");
        logger.trace("updated modelid/vendor to {} / {}", sensorType.name(), "Dallas/Maxim");

        return properties;
    }

    /**
     * get the dynamic state description provider for this thing
     *
     * @return
     */
    public @Nullable OwDynamicStateDescriptionProvider getDynamicStateDescriptionProvider() {
        return dynamicStateDescriptionProvider;
    }

    /**
     * remove a channel during initialization if it exists
     *
     * @param thingBuilder ThingBuilder of the edited thing
     * @param channelId id of the channel
     */
    protected void removeChannelIfExisting(ThingBuilder thingBuilder, String channelId) {
        if (thing.getChannel(channelId) != null) {
            thingBuilder.withoutChannel(new ChannelUID(thing.getUID(), channelId));
        }
    }

    /**
     * add a channel during initialization
     *
     * @param thingBuilder ThingBuilder of the edited thing
     * @param channelId id of the channel
     * @param channelTypeUID ChannelTypeUID of the channel
     * @return existing or created channel
     */
    protected Channel addChannelIfMissing(ThingBuilder thingBuilder, String channelId, ChannelTypeUID channelTypeUID) {
        Channel channel = thing.getChannel(channelId);
        if (channel == null) {
            channel = ChannelBuilder
                    .create(new ChannelUID(thing.getUID(), channelId), ACCEPTED_ITEM_TYPES_MAP.get(channelId))
                    .withType(channelTypeUID).build();
            thingBuilder.withChannel(channel);

        }
        return channel;
    }

    /**
     * add a channel during initialization
     *
     * @param thingBuilder ThingBuilder of the edited thing
     * @param channelId id of the channel
     * @param channelTypeUID ChannelTypeUID of the channel
     * @param label label string if different from ChannelTypeUID
     * @return existing or created channel
     */
    protected Channel addChannelIfMissing(ThingBuilder thingBuilder, String channelId, ChannelTypeUID channelTypeUID,
            String label) {
        Channel channel = thing.getChannel(channelId);
        if (channel == null) {
            channel = ChannelBuilder
                    .create(new ChannelUID(thing.getUID(), channelId), ACCEPTED_ITEM_TYPES_MAP.get(channelId))
                    .withType(channelTypeUID).withLabel(label).build();
            thingBuilder.withChannel(channel);
        }
        return channel;
    }

    /**
     * add a channel during initialization
     *
     * @param thingBuilder ThingBuilder of the edited thing
     * @param channelId id of the channel
     * @param channelTypeUID ChannelTypeUID of the channel
     * @param configuration Configuration for the channel
     * @return existing or created channel
     */
    protected Channel addChannelIfMissing(ThingBuilder thingBuilder, String channelId, ChannelTypeUID channelTypeUID,
            Configuration configuration) {
        Channel channel = thing.getChannel(channelId);
        if (channel == null) {
            channel = ChannelBuilder
                    .create(new ChannelUID(thing.getUID(), channelId), ACCEPTED_ITEM_TYPES_MAP.get(channelId))
                    .withType(channelTypeUID).withConfiguration(configuration).build();
            thingBuilder.withChannel(channel);
        }
        return channel;
    }
}
