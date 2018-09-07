/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.handler;


import org.eclipse.smarthome.core.library.types.*;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.gpstracker.internal.config.GPSTrackerBindingConfiguration;
import org.openhab.binding.gpstracker.internal.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.*;

/**
 * Tracker thing handler.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class TrackerHandler extends BaseThingHandler implements TrackerRecorder {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(TrackerHandler.class);

    /**
     * Binding configuration
     */
    private GPSTrackerBindingConfiguration bindingConfig;

    /**
     * Previous location of the tracker. It is initialized with zeros so the first location report from a tracker
     * will trigger events if the newly received location is inside a region.
     */
    private PointType oldLocation = new PointType();

    /**
     * Channel helper
     */
    private ChannelUtil channelUtil;

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
     * Constructor.
     *
     * @param thing Thing.
     * @param bindingConfig Binding level configuration.
     * @param notificationBroker Notification broker
     * @param translationUtil Tranlation helper
     */
    public TrackerHandler(Thing thing, GPSTrackerBindingConfiguration bindingConfig, NotificationBroker notificationBroker, TranslationUtil translationUtil) {
        super(thing);
        this.bindingConfig = bindingConfig;

        this.notificationBroker = notificationBroker;
        notificationHandler = new NotificationHandler();
        trackerId = (String) thing.getConfiguration().get(CONFIG_TRACKER_ID);
        notificationBroker.registerHandler(trackerId, notificationHandler);

        channelUtil = new ChannelUtil(thing, bindingConfig, translationUtil);
        logger.debug("Tracker handler created: {}", trackerId);
    }

    public String getTrackerId() {
        return trackerId;
    }

    @Override
    public void initialize() {
        updateThingChannels();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        //no command handling
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        updateThingChannels();
    }

    /**
     * Update the thing with dynamic channels.
     */
    private void updateThingChannels() {
        Map<String, Channel> updatedChannels = channelUtil.updateChannels();
        if (updatedChannels != null) {
            logger.debug("Update the thing after all channel modifications {}.", trackerId);
            ThingBuilder thingBuilder = editThing();
            Thing newThing = thingBuilder.withChannels((Channel[]) updatedChannels.values().toArray(new Channel[0])).withLabel(thing.getLabel()).build();
            updateThing(newThing);
            logger.debug("Thing update executed: {}", trackerId);
        } else {
            logger.debug("Skip thing channel update. No change.");
        }
    }

    /**
     * If the message is a transition report update the affected trigger channels
     *
     * @param message Transition message.
     */
    private void updateTriggerChannelsWithTransition(Transition message) {
        String regionName = message.getRegionName();
        Region affectedRegion = bindingConfig.getRegionByName(regionName);
        if (affectedRegion != null) {
            updateRegionSwitchChannel(message.isEntering(), affectedRegion);
        } else {
            String payload = ">";
            if (message.isLeaving()) {
                payload = "<";
            }
            payload += regionName;
            triggerChannel(CHANNEL_REGION_TRIGGER, payload);
            logger.trace("Triggering {} for {}/{}", regionName, trackerId, payload);
        }
    }

    private void updateRegionSwitchChannel(boolean isInside, Region region) {
        OnOffType value = OnOffType.OFF;
        if (isInside) {
            value = OnOffType.ON;
        }
        logger.trace("Turning {} region switch for {}/{}", value, trackerId, region.getName());
        String switchChannelId = CHANNEL_REGION_PRESENCE + "_" + region.getId();
        updateState(switchChannelId, value);
    }

    /**
     * Update presence channels on internal regions by new location.
     *
     * @param message Message.
     */
    private void updateRegionChannelsWithLocation(AbstractBaseMessage message) {
        String trackerId = message.getTrackerId();
        logger.debug("Updating channel for tracker {} by new received location.", trackerId);
        PointType newLocation = updateBaseChannels(message);

        if (oldLocation != null) {
            logger.debug("Updating region channel states for tracker {}", trackerId);
            for (Region r : bindingConfig.getRegions()) {
                String regionName = r.getName();
                logger.trace("Updating region channel states: {}", regionName);

                PointType center = r.getLocation();
                if (newLocation != null && center != null) {
                    Integer radius = r.getRadius();

                    logger.trace("Existing new location: {}", newLocation.toString());
                    int newDistance = newLocation.distanceFrom(center).intValue();
                    updateState(CHANNEL_DISTANCE + "_" + r.getId(), new QuantityType<>(newDistance/1000, MetricPrefix.KILO(SIUnits.METRE)));
                    logger.trace("Region center distance from new {} is {} with radius {}", newLocation.toString(), newDistance, radius);

                    logger.trace("Existing old location: {}", oldLocation.toString());
                    int oldDistance = oldLocation.distanceFrom(center).intValue();
                    logger.trace("Region center distance from old {} is {} with radius {}", oldLocation.toString(), oldDistance, radius);

                    if (newDistance < radius) {
                        updateRegionSwitchChannel(true, r);
                    } else if (newDistance > radius) {
                        updateRegionSwitchChannel(false, r);
                    }
                }
            }
        }
        oldLocation = newLocation;
    }

    /**
     * Update base channels: battery, location, last report
     *
     * @param message Received message.
     * @return New location extracted from the message.
     */
    private PointType updateBaseChannels(AbstractBaseMessage message) {
        logger.debug("Update channels for tracker from message: {}", trackerId, message);
        DateTimeType timestamp = message.getTimestamp();
        if (timestamp != null) {
            updateState(CHANNEL_LAST_REPORT, timestamp);
            logger.trace("{} -> {}", CHANNEL_LAST_REPORT, timestamp);
        }

        PointType newLocation = message.getPoint();
        if (newLocation != null) {
            updateState(CHANNEL_LOCATION, newLocation);
            logger.trace("{} -> {}", CHANNEL_LOCATION, newLocation);
        }

        DecimalType batteryLevel = message.getBatteryLevel();
        if (batteryLevel != null) {
            updateState(CHANNEL_BATTERY_LEVEL, batteryLevel);
            logger.trace("{} -> {}", CHANNEL_BATTERY_LEVEL, batteryLevel);
        }

        return newLocation;
    }

    @Override
    public void updateLocation(Location lm) {
        updateStatus(ThingStatus.ONLINE);
        updateRegionChannelsWithLocation(lm);
        notificationBroker.sendNotification(lm);
    }

    @Override
    public void doTransition(Transition tm) {
        updateStatus(ThingStatus.ONLINE);
        String regionName = tm.getRegionName();
        logger.debug("External region event received: {}", regionName);

        updateTriggerChannelsWithTransition(tm);
        updateBaseChannels(tm);
        notificationBroker.sendNotification(tm);
    }

    @Override
    public List<AbstractBaseMessage> getNotifications() {
        return notificationHandler.getNotifications();
    }
}
