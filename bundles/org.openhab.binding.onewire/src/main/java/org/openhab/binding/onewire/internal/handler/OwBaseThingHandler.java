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
package org.openhab.binding.onewire.internal.handler;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.config.BaseHandlerConfiguration;
import org.openhab.binding.onewire.internal.device.AbstractOwDevice;
import org.openhab.binding.onewire.internal.device.OwChannelConfig;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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

    private static final Set<String> REQUIRED_PROPERTIES = Set.of(PROPERTY_MODELID, PROPERTY_VENDOR);

    protected List<String> requiredProperties = new ArrayList<>(REQUIRED_PROPERTIES);
    protected Set<OwSensorType> supportedSensorTypes;

    protected final List<AbstractOwDevice> sensors = new ArrayList<>();
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
        configureThingHandler();
    }

    protected boolean configureThingHandler() {
        BaseHandlerConfiguration configuration = getConfig().as(BaseHandlerConfiguration.class);
        Map<String, String> properties = thing.getProperties();

        if (getBridge() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge missing");
            return false;
        }
        sensors.clear();

        final String id = configuration.id;
        if (id != null) {
            try {
                this.sensorId = new SensorId(id);
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "sensor id format mismatch");
                return false;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "sensor id missing");
            return false;
        }

        refreshInterval = configuration.refresh * 1000L;

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

    protected void configureThingChannels() {
        ThingBuilder thingBuilder = editThing();

        logger.debug("configuring sensors for {}", thing.getUID());

        // remove unwanted channels
        Set<String> existingChannelIds = thing.getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toSet());
        Set<String> wantedChannelIds = SENSOR_TYPE_CHANNEL_MAP.getOrDefault(sensorType, Set.of()).stream()
                .map(channelConfig -> channelConfig.channelId).collect(Collectors.toSet());
        existingChannelIds.stream().filter(channelId -> !wantedChannelIds.contains(channelId))
                .forEach(channelId -> removeChannelIfExisting(thingBuilder, channelId));

        // add or update wanted channels
        SENSOR_TYPE_CHANNEL_MAP.getOrDefault(sensorType, Set.of()).stream().forEach(channelConfig -> {
            addChannelIfMissingAndEnable(thingBuilder, channelConfig);
        });

        updateThing(thingBuilder.build());

        try {
            sensors.get(0).configureChannels();
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        if (thing.getChannel(CHANNEL_PRESENT) != null) {
            showPresence = true;
        }

        validConfig = true;
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
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
    public void refresh(OwserverBridgeHandler bridgeHandler, long now) {
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

        OwserverBridgeHandler bridgeHandler = (OwserverBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            logger.debug("bridgehandler for {} not available for scheduling property update, retrying in 5s",
                    thing.getUID());
            scheduler.schedule(() -> {
                updateSensorProperties();
            }, 5000, TimeUnit.MILLISECONDS);
            return;
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "required properties missing");
        bridgeHandler.scheduleForPropertiesUpdate(thing);
    }

    /**
     * thing specific update method for sensor properties
     *
     * called by the bridge handler
     *
     * @param bridgeHandler the bridge handler to be used
     * @throws OwException in case an error occurs
     */
    public void updateSensorProperties(OwserverBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = editProperties();
        OwSensorType sensorType = bridgeHandler.getType(sensorId);
        properties.put(PROPERTY_MODELID, sensorType.toString());
        properties.put(PROPERTY_VENDOR, "Dallas/Maxim");

        updateProperties(properties);

        logger.trace("updated modelid/vendor to {} / {}", sensorType.name(), "Dallas/Maxim");
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
     * adds (or replaces) a channel and enables it within the sensor (configuration preserved, default sensor)
     *
     * @param thingBuilder ThingBuilder of the edited thing
     * @param channelConfig a OwChannelConfig for the new channel
     */
    protected void addChannelIfMissingAndEnable(ThingBuilder thingBuilder, OwChannelConfig channelConfig) {
        addChannelIfMissingAndEnable(thingBuilder, channelConfig, null, 0);
    }

    /**
     * adds (or replaces) a channel and enables it within the sensor (configuration overridden, default sensor)
     *
     * @param thingBuilder ThingBuilder of the edited thing
     * @param channelConfig a OwChannelConfig for the new channel
     * @param configuration the new Configuration for this channel
     */
    protected void addChannelIfMissingAndEnable(ThingBuilder thingBuilder, OwChannelConfig channelConfig,
            Configuration configuration) {
        addChannelIfMissingAndEnable(thingBuilder, channelConfig, configuration, 0);
    }

    /**
     * adds (or replaces) a channel and enables it within the sensor (configuration preserved)
     *
     * @param thingBuilder ThingBuilder of the edited thing
     * @param channelConfig a OwChannelConfig for the new channel
     * @param sensorNo number of sensor that provides this channel
     */
    protected void addChannelIfMissingAndEnable(ThingBuilder thingBuilder, OwChannelConfig channelConfig,
            int sensorNo) {
        addChannelIfMissingAndEnable(thingBuilder, channelConfig, null, sensorNo);
    }

    /**
     * adds (or replaces) a channel and enables it within the sensor (configuration overridden)
     *
     * @param thingBuilder ThingBuilder of the edited thing
     * @param channelConfig a OwChannelConfig for the new channel
     * @param configuration the new Configuration for this channel
     * @param sensorNo number of sensor that provides this channel
     */
    protected void addChannelIfMissingAndEnable(ThingBuilder thingBuilder, OwChannelConfig channelConfig,
            @Nullable Configuration configuration, int sensorNo) {
        Channel channel = thing.getChannel(channelConfig.channelId);
        Configuration config = configuration;
        String label = channelConfig.label;

        // remove channel if wrong type uid and preserve config if not overridden
        if (channel != null && !channelConfig.channelTypeUID.equals(channel.getChannelTypeUID())) {
            removeChannelIfExisting(thingBuilder, channelConfig.channelId);
            if (config == null) {
                config = channel.getConfiguration();
            }
            channel = null;
        }

        // create channel if missing
        if (channel == null) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelConfig.channelId);

            ThingHandlerCallback callback = getCallback();
            if (callback == null) {
                logger.warn("Could not get callback, adding '{}' failed.", channelUID);
                return;
            }

            ChannelBuilder channelBuilder = callback.createChannelBuilder(channelUID, channelConfig.channelTypeUID);

            if (label != null) {
                channelBuilder.withLabel(label);
            }
            if (config != null) {
                channelBuilder.withConfiguration(config);
            }

            channel = channelBuilder.build();
            thingBuilder.withChannel(channel);
        }

        // enable channel in sensor
        sensors.get(sensorNo).enableChannel(channelConfig.channelId);
    }
}
