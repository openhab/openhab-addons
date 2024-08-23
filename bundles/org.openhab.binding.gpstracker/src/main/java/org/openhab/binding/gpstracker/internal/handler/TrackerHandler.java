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
package org.openhab.binding.gpstracker.internal.handler;

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.*;
import static org.openhab.binding.gpstracker.internal.config.ConfigHelper.CONFIG_REGION_CENTER_LOCATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpstracker.internal.config.ConfigHelper;
import org.openhab.binding.gpstracker.internal.message.NotificationBroker;
import org.openhab.binding.gpstracker.internal.message.NotificationHandler;
import org.openhab.binding.gpstracker.internal.message.dto.LocationMessage;
import org.openhab.binding.gpstracker.internal.message.dto.TransitionMessage;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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
 * The {@link TrackerHandler} class is a tracker thing handler.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class TrackerHandler extends BaseThingHandler {
    /**
     * Trigger events
     */
    private static final String EVENT_ENTER = "enter";
    private static final String EVENT_LEAVE = "leave";

    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(TrackerHandler.class);

    /**
     * Notification handler
     */
    private NotificationHandler notificationHandler;

    /**
     * Notification broker
     */
    private NotificationBroker notificationBroker;

    /**
     * Id of the tracker represented by the thing
     */
    private String trackerId;

    /**
     * Map of regionName/distance channels
     */
    private Map<String, Channel> distanceChannelMap = new HashMap<>();

    /**
     * Map of last trigger events per region
     */
    private Map<String, Boolean> lastTriggeredStates = new HashMap<>();

    /**
     * Set of all regions referenced by distance channels and extended by the received transition messages.
     */
    private Set<String> regions;

    /**
     * System location
     */
    private @Nullable PointType sysLocation;

    /**
     * Unit provider
     */
    private @Nullable UnitProvider unitProvider;

    /**
     * Last message received from the tracker
     */
    private @Nullable LocationMessage lastMessage;

    /**
     * Constructor.
     *
     * @param thing Thing.
     * @param notificationBroker Notification broker
     * @param regions Global region set
     * @param sysLocation Location of the system
     * @param unitProvider Unit provider
     */
    public TrackerHandler(Thing thing, NotificationBroker notificationBroker, Set<String> regions,
            @Nullable PointType sysLocation, @Nullable UnitProvider unitProvider) {
        super(thing);

        this.notificationBroker = notificationBroker;
        this.notificationHandler = new NotificationHandler();
        this.regions = regions;
        this.sysLocation = sysLocation;
        this.unitProvider = unitProvider;

        trackerId = ConfigHelper.getTrackerId(thing.getConfiguration());
        notificationBroker.registerHandler(trackerId, notificationHandler);

        logger.debug("Tracker handler created: {}", trackerId);
    }

    /**
     * Returns tracker id configuration of the thing.
     *
     * @return Tracker id
     */
    public String getTrackerId() {
        return trackerId;
    }

    @Override
    public void initialize() {
        if (sysLocation != null) {
            createBasicDistanceChannel();
        } else {
            logger.debug("System location is not set. Skipping system distance channel setup.");
        }

        mapDistanceChannels();
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Create distance channel for measuring the distance between the tracker and the szstem.
     */
    private void createBasicDistanceChannel() {
        @Nullable
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            // find the system distance channel
            ChannelUID systemDistanceChannelUID = new ChannelUID(thing.getUID(), CHANNEL_DISTANCE_SYSTEM_ID);
            Channel systemDistance = thing.getChannel(CHANNEL_DISTANCE_SYSTEM_ID);
            ChannelBuilder channelBuilder = null;
            String sysLocationString = sysLocation == null ? "unknown" : sysLocation.toFullString();
            if (systemDistance != null) {
                if (!systemDistance.getConfiguration().get(CONFIG_REGION_CENTER_LOCATION).equals(sysLocationString)) {
                    logger.trace("Existing distance channel for system. Changing system location config parameter: {}",
                            sysLocationString);

                    channelBuilder = callback.editChannel(thing, systemDistanceChannelUID);
                    Configuration configToUpdate = systemDistance.getConfiguration();
                    configToUpdate.put(CONFIG_REGION_CENTER_LOCATION, sysLocationString);
                    channelBuilder.withConfiguration(configToUpdate);
                } else {
                    logger.trace("Existing distance channel for system. No change.");
                }
            } else {
                logger.trace("Creating missing distance channel for system.");

                Configuration config = new Configuration();
                config.put(ConfigHelper.CONFIG_REGION_NAME, CHANNEL_DISTANCE_SYSTEM_NAME);
                config.put(CONFIG_REGION_CENTER_LOCATION, sysLocationString);
                config.put(ConfigHelper.CONFIG_REGION_RADIUS, CHANNEL_DISTANCE_SYSTEM_RADIUS);
                config.put(ConfigHelper.CONFIG_ACCURACY_THRESHOLD, 0);

                channelBuilder = callback.createChannelBuilder(systemDistanceChannelUID, CHANNEL_TYPE_DISTANCE)
                        .withLabel("System Distance").withConfiguration(config);
            }

            // update the thing with system distance channel
            if (channelBuilder != null) {
                List<Channel> channels = new ArrayList<>(thing.getChannels());
                if (systemDistance != null) {
                    channels.remove(systemDistance);
                }
                channels.add(channelBuilder.build());

                ThingBuilder thingBuilder = editThing();
                thingBuilder.withChannels(channels);
                updateThing(thingBuilder.build());

                logger.debug("Distance channel created for system: {}", systemDistanceChannelUID);
            }
        }
    }

    /**
     * Create a map of all configured distance channels to handle channel updates easily.
     */
    private void mapDistanceChannels() {
        distanceChannelMap = thing.getChannels().stream()
                .filter(c -> CHANNEL_TYPE_DISTANCE.equals(c.getChannelTypeUID()))
                .collect(Collectors.toMap(c -> ConfigHelper.getRegionName(c.getConfiguration()), Function.identity()));
        // register the collected regions
        regions.addAll(distanceChannelMap.keySet());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        LocationMessage lastMessageLocal = lastMessage;
        if (command instanceof RefreshType && lastMessageLocal != null) {
            String channelId = channelUID.getId();
            switch (channelId) {
                case CHANNEL_LAST_REPORT:
                    updateBaseChannels(lastMessageLocal, CHANNEL_LAST_REPORT);
                    break;
                case CHANNEL_LAST_LOCATION:
                    updateBaseChannels(lastMessageLocal, CHANNEL_LAST_LOCATION);
                    break;
                case CHANNEL_BATTERY_LEVEL:
                    updateBaseChannels(lastMessageLocal, CHANNEL_BATTERY_LEVEL);
                    break;
                case CHANNEL_GPS_ACCURACY:
                    updateBaseChannels(lastMessageLocal, CHANNEL_GPS_ACCURACY);
                    break;
                default: // distance channels
                    @Nullable
                    Channel channel = thing.getChannel(channelId);
                    if (channel != null) {
                        updateDistanceChannelFromMessage(lastMessageLocal, channel);
                    }
            }
        }
    }

    /**
     * Handle transition messages by firing the trigger channel with regionName/event payload.
     *
     * @param message TransitionMessage message.
     */
    private void updateTriggerChannelsWithTransition(TransitionMessage message) {
        String regionName = message.getRegionName();
        triggerRegionChannel(regionName, message.getEvent(), true);
    }

    /**
     * Fire trigger event with regionName/enter|leave payload but only if the event differs from the last event.
     *
     * @param regionName Region name
     * @param event Occurred event
     * @param forced Force channel triggering in case the transition event is received from the mobile application.
     */
    private void triggerRegionChannel(String regionName, String event, boolean forced) {
        Boolean lastState = lastTriggeredStates.get(regionName);
        Boolean newState = EVENT_ENTER.equals(event);
        if (!newState.equals(lastState) || forced) {
            String payload = regionName + "/" + event;
            triggerChannel(CHANNEL_REGION_TRIGGER, payload);
            lastTriggeredStates.put(regionName, newState);
            logger.trace("Triggering {} for {}/{}", regionName, trackerId, payload);
        }
        lastTriggeredStates.put(regionName, newState);
    }

    /**
     * Update state channels from location message. This includes basic channel updates and recalculations of all
     * distances.
     *
     * @param message Message.
     */
    private void updateChannelsWithLocation(LocationMessage message) {
        updateBaseChannels(message, CHANNEL_BATTERY_LEVEL, CHANNEL_LAST_LOCATION, CHANNEL_LAST_REPORT,
                CHANNEL_GPS_ACCURACY);

        String trackerId = message.getTrackerId();
        logger.debug("Updating distance channels tracker {}", trackerId);
        distanceChannelMap.values().forEach(c -> updateDistanceChannelFromMessage(message, c));
    }

    private void updateDistanceChannelFromMessage(LocationMessage message, Channel c) {
        Configuration currentConfig = c.getConfiguration();
        // convert into meters which is the unit of the threshold
        Double accuracyThreshold = convertToMeters(ConfigHelper.getAccuracyThreshold(currentConfig));
        State messageAccuracy = message.getGpsAccuracy();
        Double accuracy = messageAccuracy != UnDefType.UNDEF ? ((QuantityType<?>) messageAccuracy).doubleValue()
                : accuracyThreshold;

        if (accuracyThreshold >= accuracy || accuracyThreshold.intValue() == 0) {
            if (accuracyThreshold > 0) {
                logger.debug("Location accuracy is below required threshold: {}<={}", accuracy, accuracyThreshold);
            } else {
                logger.debug("Location accuracy threshold check is disabled.");
            }

            String regionName = ConfigHelper.getRegionName(currentConfig);
            PointType center = ConfigHelper.getRegionCenterLocation(currentConfig);
            State newLocation = message.getTrackerLocation();
            if (center != null && newLocation != UnDefType.UNDEF) {
                double newDistance = center.distanceFrom((PointType) newLocation).doubleValue();
                updateState(c.getUID(), new QuantityType<>(newDistance / 1000, MetricPrefix.KILO(SIUnits.METRE)));
                logger.trace("Region {} center distance from tracker location {} is {}m", regionName, newLocation,
                        newDistance);

                // fire trigger based on distance calculation only in case of pure location message
                if (!(message instanceof TransitionMessage)) {
                    // convert into meters which is the unit of the calculated distance
                    double radiusMeter = convertToMeters(ConfigHelper.getRegionRadius(c.getConfiguration()));
                    if (radiusMeter > newDistance) {
                        triggerRegionChannel(regionName, EVENT_ENTER, false);
                    } else {
                        triggerRegionChannel(regionName, EVENT_LEAVE, false);
                    }
                }
            }
        } else {
            logger.debug("Skip update as location accuracy is above required threshold: {}>{}", accuracy,
                    accuracyThreshold);
        }
    }

    private double convertToMeters(double valueToConvert) {
        UnitProvider unitProviderLocal = unitProvider;
        if (unitProviderLocal != null) {
            @Nullable
            Unit<Length> unit = unitProviderLocal.getUnit(Length.class);
            if (unit != null && !SIUnits.METRE.equals(unit)) {
                double value = ImperialUnits.YARD.getConverterTo(SIUnits.METRE).convert(valueToConvert);
                logger.trace("Value converted: {}yd->{}m", valueToConvert, value);
                return value;
            } else {
                logger.trace("System uses SI measurement units. No conversion is needed.");
            }
        } else {
            logger.trace("No unit provider. Considering region radius {} in meters.", valueToConvert);
        }
        return valueToConvert;
    }

    /**
     * Update basic channels: batteryLevel, lastLocation, lastReport
     *
     * @param message Received message.
     */
    private void updateBaseChannels(LocationMessage message, String... channels) {
        logger.debug("Update base channels for tracker {} from message: {}", trackerId, message);

        for (String channel : channels) {
            switch (channel) {
                case CHANNEL_LAST_REPORT:
                    State timestamp = message.getTimestamp();
                    updateState(CHANNEL_LAST_REPORT, timestamp);
                    logger.trace("{} -> {}", CHANNEL_LAST_REPORT, timestamp);
                    break;
                case CHANNEL_LAST_LOCATION:
                    State newLocation = message.getTrackerLocation();
                    updateState(CHANNEL_LAST_LOCATION, newLocation);
                    logger.trace("{} -> {}", CHANNEL_LAST_LOCATION, newLocation);
                    break;
                case CHANNEL_BATTERY_LEVEL:
                    State batteryLevel = message.getBatteryLevel();
                    updateState(CHANNEL_BATTERY_LEVEL, batteryLevel);
                    logger.trace("{} -> {}", CHANNEL_BATTERY_LEVEL, batteryLevel);
                    break;
                case CHANNEL_GPS_ACCURACY:
                    State accuracy = message.getGpsAccuracy();
                    updateState(CHANNEL_GPS_ACCURACY, accuracy);
                    logger.trace("{} -> {}", CHANNEL_GPS_ACCURACY, accuracy);
                    break;
            }
        }
    }

    /**
     * Location message handling.
     *
     * @param lm Location message
     */
    public void updateLocation(LocationMessage lm) {
        this.lastMessage = lm;
        updateStatus(ThingStatus.ONLINE);
        updateChannelsWithLocation(lm);
        notificationBroker.sendNotification(lm);
    }

    /**
     * Transition message handling
     *
     * @param tm Transition message
     */
    public void doTransition(TransitionMessage tm) {
        this.lastMessage = tm;
        updateStatus(ThingStatus.ONLINE);
        String regionName = tm.getRegionName();
        logger.debug("ConfigHelper transition event received: {}", regionName);
        regions.add(regionName);

        updateChannelsWithLocation(tm);
        updateTriggerChannelsWithTransition(tm);

        notificationBroker.sendNotification(tm);
    }

    /**
     * Get notification to return to the tracker (supported by OwnTracks only)
     *
     * @return List of notifications received from other trackers
     */
    public List<LocationMessage> getNotifications() {
        return notificationHandler.getNotifications();
    }
}
