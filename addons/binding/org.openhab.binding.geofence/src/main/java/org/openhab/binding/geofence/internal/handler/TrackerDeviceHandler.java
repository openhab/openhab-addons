/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.handler;


import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.geofence.internal.config.BindingConfiguration;
import org.openhab.binding.geofence.internal.config.DeviceConfiguration;
import org.openhab.binding.geofence.internal.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.geofence.internal.BindingConstants.*;

/**
 * Device thing handler.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class TrackerDeviceHandler extends BaseThingHandler implements TrackerRecorder {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(TrackerDeviceHandler.class);

    /**
     * Binding configuration
     */
    private BindingConfiguration bindingConfig;

    /**
     * Device configuration
     */
    private DeviceConfiguration deviceConfig;

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
     * Jpb ping the device periodically
     */
    private ScheduledFuture<?> networkPingJob;

    /**
     * Constructor.
     *
     * @param thing              Thing.
     * @param bindingConfig      Binding level configuration.
     * @param notificationBroker Notification broker
     */
    public TrackerDeviceHandler(Thing thing, BindingConfiguration bindingConfig, NotificationBroker notificationBroker) {
        super(thing);
        this.bindingConfig = bindingConfig;

        this.notificationBroker = notificationBroker;
        notificationHandler = new NotificationHandler();
        String trackerId = thing.getUID().getId();
        notificationBroker.registerHandler(trackerId, notificationHandler);

        channelUtil = new ChannelUtil(thing, bindingConfig, deviceConfig);
        loadDeviceConfig();
        logger.debug("Tracker handler created: {}", trackerId);
    }

    private void loadDeviceConfig() {
        deviceConfig = getConfigAs(DeviceConfiguration.class);
        deviceConfig.parseRegions();
        channelUtil.setDeviceConfig(deviceConfig);
    }

    @Override
    public void initialize() {
        updateThingChannels();
        initNetworkTimer();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        //no command handling
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        stopNetworkPingJob();
        super.handleConfigurationUpdate(configurationParameters);

        updateThingChannels();
        initNetworkTimer();
    }

    /**
     * Update the thing with dynamic channels.
     */
    private void updateThingChannels() {
        Map<String, Channel> updatedChannels = channelUtil.updateChannels();
        String thingId = thing.getUID().getId();
        if (updatedChannels != null) {
            logger.debug("Update the thing after all channel modifications {}.", thingId);
            ThingBuilder thingBuilder = editThing();
            Thing newThing = thingBuilder.withChannels((Channel[]) updatedChannels.values().toArray(new Channel[0])).withLabel(thing.getLabel()).build();
            updateThing(newThing);
            logger.debug("Thing update executed: {}", thingId);
        } else {
            logger.debug("Skip thing channel update. No change.");
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        loadDeviceConfig();
        super.updateConfiguration(configuration);
    }

    /**
     * If enabled by the device configuration it starts a background timer to check the device presence on local
     * network. If the device is reachable the networkPresence channel will bet updated to ON.
     */
    private void initNetworkTimer() {
        final String trackerId = thing.getUID().getId();
        //check the configuration and start the new ping timer
        if (deviceConfig.getIpAddressStatic()
                && deviceConfig.getIpAddress() != null
                && deviceConfig.getPingInterval() > 0) {
            logger.debug("Starting new network ping job for device {}.", trackerId);
            if (networkPingJob == null || networkPingJob.isCancelled() || networkPingJob.isDone()) {
                networkPingJob = scheduler.scheduleWithFixedDelay(() ->
                        pingDevice(trackerId), 0, deviceConfig.getPingInterval(), TimeUnit.SECONDS);
            }
        }
    }

    private void pingDevice(String trackerId) {
        try {
            InetAddress inetAddress = InetAddress.getByName(deviceConfig.getIpAddress());
            if (inetAddress.isReachable(3000)) {
                updateState(CHANNEL_NETWORK_PRESENCE, OnOffType.ON);
                logger.trace("Device {} with IP {} is reachable.", trackerId, deviceConfig.getIpAddress());
            } else {
                updateState(CHANNEL_NETWORK_PRESENCE, OnOffType.OFF);
                logger.trace("Device {} with IP {} is not reachable.", trackerId, deviceConfig.getIpAddress());
            }
        } catch (Exception e) {
            logger.error("Error checking the device on the network.", e);
        }
    }

    /**
     * Checks the existence of a region mentioned by transition message. If the region is unknown it will be created and
     * this method will also update the thing configuration containing all device specific external regions.
     *
     * @param regionName Region name.
     */
    @Override
    public void maintainExternalRegion(String regionName) {
        if (channelUtil.getRegionByName(regionName) == null) {
            logger.debug("Creating new external region: {}", regionName);
            Region externalRegion = new Region();
            externalRegion.setName(regionName);
            externalRegion.setTriggerEvent(true);
            deviceConfig.addRegion(externalRegion);

            //update the configuration
            Configuration configuration = editConfiguration();
            configuration.put(DeviceConfiguration.PARAM_EXTERNAL_REGION, deviceConfig.toJSON());
            updateConfiguration(configuration);
        } else {
            logger.debug("External region exist: {}", regionName);
        }
    }

    @Override
    public void updateDeviceIPAddress(String ipAddress) {
        //update the configuration
        if (ipAddress != null && !deviceConfig.getIpAddressStatic()) {
            Configuration configuration = editConfiguration();
            configuration.put(DeviceConfiguration.PARAM_IP_ADDRESS, ipAddress);
            updateConfiguration(configuration);
        }
    }

    /**
     * If the message is a transition report update the affected region and trigger channels
     * .
     *
     * @param message Transition message.
     * @return Name of the updated region;
     */
    @Nullable private String updateRegionChannelsWithTransition(Transition message) {
        Region affectedRegion = channelUtil.getRegionByName(message.getRegionName());

        if (affectedRegion != null) {
            String trackerId = message.getTrackerId();
            if (message.isEntering()) {
                regionEnter(trackerId, affectedRegion, true);
            } else if (message.isLeaving()) {
                regionLeave(trackerId, affectedRegion, true);
            }
        }
        return affectedRegion != null ? affectedRegion.getName() : null;
    }

    /**
     * Update presence channels on internal regions by new location.
     *
     * @param message    Message.
     * @param skipRegion Region name to skip with update
     */
    private void updateRegionChannelsWithLocation(AbstractBaseMessage message, String skipRegion) {
        String trackerId = message.getTrackerId();
        logger.debug("Updating channel for device {} by new received location.", trackerId);
        PointType newLocation = updateBaseChannels(message);

        if (oldLocation != null) {
            logger.debug("Updating region channel states for device {}", trackerId);
            for (Region r : bindingConfig.getRegions()) {
                String regionName = r.getName();
                if (skipRegion != null && skipRegion.equals(regionName)) {
                    //this region was already updated with transition
                    continue;
                }

                logger.trace("Updating region channel states: {}", regionName);

                if (newLocation != null) {
                    PointType center = r.getLocation();
                    Integer radius = r.getRadius();

                    logger.trace("Existing new location: {}", newLocation.toString());
                    int newDistance = newLocation.distanceFrom(center).intValue();
                    if (r.isPrimary()) {
                        updateState(CHANNEL_DISTANCE, new DecimalType(newDistance / 1000));
                    }
                    logger.trace("Region center distance from new {} is {} with radius {}", newLocation.toString(), newDistance, radius);

                    logger.trace("Existing old location: {}", oldLocation.toString());
                    int oldDistance = oldLocation.distanceFrom(center).intValue();
                    logger.trace("Region center distance from old {} is {} with radius {}", oldLocation.toString(), oldDistance, radius);

                    if (newDistance < radius) {
                        regionEnter(trackerId, r, oldDistance > radius);
                    } else if (newDistance > radius) {
                        regionLeave(trackerId, r, oldDistance < radius);
                    }
                }
            }
        }
        oldLocation = newLocation;
    }

    /**
     * Update channels with leave event.
     *
     * @param trackerId   Tracker id
     * @param region      Left region
     * @param fireTrigger True to fire the trigger event as well
     */
    private void regionLeave(String trackerId, Region region, boolean fireTrigger) {
        regionAction(trackerId, region, fireTrigger ? CHANNEL_REGION_LEAVE_TRIGGER : null, OnOffType.OFF);
    }

    /**
     * Update channels with enter event.
     *
     * @param trackerId   Tracker id
     * @param region      Left region
     * @param fireTrigger True to fire the trigger event as well
     */
    private void regionEnter(String trackerId, Region region, boolean fireTrigger) {
        regionAction(trackerId, region, fireTrigger ? CHANNEL_REGION_ENTER_TRIGGER : null, OnOffType.ON);
    }

    private void regionAction(String trackerId, Region region, @Nullable String triggerChannel, OnOffType value) {
        String regionName = region.getName();
        if (region.getTriggerEvent()) {
            if (triggerChannel != null) {
                triggerChannel(triggerChannel, regionName);
                logger.trace("Triggering {} for {}/{}", triggerChannel, trackerId, regionName);
            }
        } else {
            logger.trace("Turning ON region switch for {}/{}", trackerId, regionName);
            String switchChannelId = "regionPresence_" + regionName;
            updateState(switchChannelId, value);
        }
    }

    /**
     * Update base channels: battery, location, last report
     *
     * @param message Received message.
     * @return New location extracted from the message.
     */
    private PointType updateBaseChannels(AbstractBaseMessage message) {
        logger.debug("Update channels for device from message: {}", thing.getUID().getId(), message);
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
        updateRegionChannelsWithLocation(lm, null);
        notificationBroker.sendNotification(lm);
    }

    @Override
    public void doTransition(Transition tm) {
        updateStatus(ThingStatus.ONLINE);
        String regionName = tm.getRegionName();
        logger.debug("External region event received: {}", regionName);

        String updatedRegion = updateRegionChannelsWithTransition(tm);
        updateRegionChannelsWithLocation(tm, updatedRegion);
        notificationBroker.sendNotification(tm);
    }

    @Override
    public List<AbstractBaseMessage> getNotifications() {
        return notificationHandler.getNotifications();
    }

    @Override
    public void dispose() {
        stopNetworkPingJob();
        super.dispose();
    }

    private void stopNetworkPingJob() {
        if (networkPingJob != null && !networkPingJob.isCancelled()) {
            logger.debug("Cancelling existing network ping job for device {}.", thing.getUID().getId());
            networkPingJob.cancel(true);
        }
    }
}
