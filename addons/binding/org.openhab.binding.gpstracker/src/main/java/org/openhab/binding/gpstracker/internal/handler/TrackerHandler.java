/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.handler;


import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.gpstracker.internal.config.ConfigHelper;
import org.openhab.binding.gpstracker.internal.message.LocationMessage;
import org.openhab.binding.gpstracker.internal.message.NotificationBroker;
import org.openhab.binding.gpstracker.internal.message.NotificationHandler;
import org.openhab.binding.gpstracker.internal.message.TransitionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.*;

/**
 * The {@link TrackerHandler} class is a tracker thing handler.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class TrackerHandler extends BaseThingHandler {
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
     * Set of all regions referenced by distance channels and extended by the received transition messages.
     */
    private Set<String> regions;

    /**
     * Constructor.
     *
     * @param thing Thing.
     * @param notificationBroker Notification broker
     * @param regions Global region set
     */
    public TrackerHandler(Thing thing, NotificationBroker notificationBroker, Set<String> regions) {
        super(thing);

        this.notificationBroker = notificationBroker;
        this.notificationHandler = new NotificationHandler();
        this.regions = regions;

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
        mapDistanceChannels();
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Create a map of all configured distance channels to handle channel updates easily.
     */
    private void mapDistanceChannels() {
        distanceChannelMap = thing.getChannels().stream()
                .filter(c -> CHANNEL_TYPE_DISTANCE.equals(c.getChannelTypeUID()))
                .collect(Collectors.toMap(c -> ConfigHelper.getRegionName(c.getConfiguration()), Function.identity()));
        //register the collected regions
        regions.addAll(distanceChannelMap.keySet());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        //no command handling
    }

    /**
     * Handle transition messages by firing the trigger channel with regionName/event payload.
     *
     * @param message TransitionMessage message.
     */
    private void updateTriggerChannelsWithTransition(TransitionMessage message) {
        String regionName = message.getRegionName();
        String payload = regionName + "/" + message.getEvent();
        triggerChannel(CHANNEL_REGION_TRIGGER, payload);
        logger.trace("Triggering {} for {}/{}", regionName, trackerId, payload);
    }

    /**
     * Update state channels from location message. This includes basic channel updates and recalculations of all distances.
     *
     * @param message Message.
     */
    private void updateChannelsWithLocation(LocationMessage message) {
        String trackerId = message.getTrackerId();
        updateBaseChannels(message);

        logger.debug("Updating distance channels tracker {}", trackerId);
        distanceChannelMap.values()
                .forEach(c -> {
                    PointType center = ConfigHelper.getRegionCenterLocation(c.getConfiguration());
                    PointType newLocation = message.getTrackerLocation();
                    if (center != null) {
                        double newDistance = newLocation.distanceFrom(center).doubleValue();
                        updateState(c.getUID(), new QuantityType<>(newDistance / 1000, MetricPrefix.KILO(SIUnits.METRE)));
                        logger.trace("Region center distance from tracker location {} is {}m", newLocation.toString(), newDistance);
                    }
                });
    }

    /**
     * Update basic channels: batteryLevel, lastLocation, lastReport
     *
     * @param message Received message.
     */
    private void updateBaseChannels(LocationMessage message) {
        logger.debug("Update base channels for tracker {} from message: {}", trackerId, message);
        DateTimeType timestamp = message.getTimestamp();
        if (timestamp != null) {
            updateState(CHANNEL_LAST_REPORT, timestamp);
            logger.trace("{} -> {}", CHANNEL_LAST_REPORT, timestamp);
        }

        PointType newLocation = message.getTrackerLocation();
        if (newLocation != null) {
            updateState(CHANNEL_LAST_LOCATION, newLocation);
            logger.trace("{} -> {}", CHANNEL_LAST_LOCATION, newLocation);
        }

        DecimalType batteryLevel = message.getBatteryLevel();
        if (batteryLevel != null) {
            updateState(CHANNEL_BATTERY_LEVEL, batteryLevel);
            logger.trace("{} -> {}", CHANNEL_BATTERY_LEVEL, batteryLevel);
        }
    }

    /**
     * Location message handling.
     *
     * @param lm Location message
     */
    public void updateLocation(LocationMessage lm) {
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
