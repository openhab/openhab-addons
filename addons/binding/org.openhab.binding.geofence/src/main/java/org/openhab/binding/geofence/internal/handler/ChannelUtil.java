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
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.openhab.binding.geofence.internal.BindingConstants;
import org.openhab.binding.geofence.internal.config.BindingConfiguration;
import org.openhab.binding.geofence.internal.config.DeviceConfiguration;
import org.openhab.binding.geofence.internal.message.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openhab.binding.geofence.internal.BindingConstants.*;

/**
 * Dynamic channel handling helper.
 *
 * @author Gabor Bicskei - Initial contribution
 */
class ChannelUtil {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);

    /**
     * Thing representing the tracker device.
     */
    private Thing thing;

    /**
     * Binding level configuration
     */
    private BindingConfiguration bindingConfig;

    /**
     * Device level configuration
     */
    private DeviceConfiguration deviceConfig;

    /**
     * Constructor.
     *
     * @param thing         The thing
     * @param bindingConfig Binding level configuration
     * @param deviceConfig  Device level configuration
     */
    ChannelUtil(Thing thing, BindingConfiguration bindingConfig, DeviceConfiguration deviceConfig) {
        this.thing = thing;
        this.bindingConfig = bindingConfig;
        this.deviceConfig = deviceConfig;
    }

    public void setDeviceConfig(DeviceConfiguration deviceConfig) {
        this.deviceConfig = deviceConfig;
    }

    /**
     * Updates dynamic channels for the .
     * - A Switch type channel will be created for all binding and device level regions if the region triggerEvent
     * flag is FALSE. Channel state will be set to ON if the device is inside the region.
     * - RegionEnter and regionLeave trigger channels will be created if triggerEvent flag for region is TRUE. The
     * payload for these channels will be the region name.
     * - Network presence channel is ON if the tracker device is on the same network as the openHAB server and the device
     * is not in sleep mode.
     */
    Map<String, Channel> updateChannels() {
        ThingUID uid = thing.getUID();
        logger.debug("Update channel structure based on binding configuration for device {}", uid.getId());
        Map<String, Channel> channelMap = getChannelMap();
        Set<String> origChannels = new HashSet<>(channelMap.keySet());

        removeUnusedChannels(channelMap);

        createMissingChannels(channelMap);

        addNetworkChannel(channelMap);

        if (logger.isTraceEnabled()) {
            for (Channel c : channelMap.values()) {
                logger.trace("Channel after update: {}", c.getUID().getId());
            }
        }

        Set<String> newChannels = new HashSet<>(channelMap.keySet());
        return newChannels.equals(origChannels) ? null : channelMap;
    }

    /**
     * Adds network channel to the thing channel map if tracker device has static address.
     *
     * @param channelMap Channel map.
     */
    private void addNetworkChannel(Map<String, Channel> channelMap) {
        ThingUID uid = thing.getUID();
        if (deviceConfig.getIpAddressStatic() && !channelMap.containsKey(CHANNEL_NETWORK_PRESENCE)) {
            Channel channel = ChannelBuilder.create(new ChannelUID(uid,
                    CHANNEL_NETWORK_PRESENCE), "Switch")
                    .withType(CHANNEL_TYPE_NETWORK_PRESENCE)
                    .build();
            channelMap.put(CHANNEL_NETWORK_PRESENCE, channel);
            logger.trace("Creating network presence channel.");
        }
    }

    /**
     * Create missing region switches and trigger channels.
     *
     * @param channelMap Channel map with required thing channels.
     */
    private void createMissingChannels(Map<String, Channel> channelMap) {
        ThingUID uid = thing.getUID();
        boolean triggerChannelsNeeded = false;
        boolean distanceChannelNeeded = false;
        for (Region r : getAllRegions()) {
            String presenceChannelId = "regionPresence_" + r.getName();
            if (!r.getTriggerEvent()) {
                if (!channelMap.containsKey(presenceChannelId)) {
                    Channel channel = ChannelBuilder.create(new ChannelUID(uid, presenceChannelId),
                            "Switch").withType(BindingConstants.CHANNEL_TYPE_PRESENCE)
                            .withLabel(uid.getId() + " @ " + r.getName()).build();
                    channelMap.put(presenceChannelId, channel);
                    logger.trace("Creating channel {} for region {}", presenceChannelId, r.getName());
                }
            } else {
                triggerChannelsNeeded = true;
                logger.trace("Trigger channel is required by region {}", r.getName());
            }
            if (r.isPrimary()) {
                distanceChannelNeeded = true;
                logger.trace("Distance channel is required by primary region {}", r.getName());
            }
        }
        //if at least one region configured for the thing requires event triggering we need the trigger channels
        if (triggerChannelsNeeded) {
            if (!channelMap.containsKey(CHANNEL_REGION_LEAVE_TRIGGER)) {
                Channel channel = ChannelBuilder.create(new ChannelUID(uid,
                        CHANNEL_REGION_LEAVE_TRIGGER), null)
                        .withType(CHANNEL_TYPE_LEAVE_TRIGGER)
                        .withKind(ChannelKind.TRIGGER)
                        .build();
                channelMap.put(CHANNEL_REGION_LEAVE_TRIGGER, channel);
                logger.trace("Creating trigger channel {} for region leave events", CHANNEL_REGION_LEAVE_TRIGGER);
            }
            if (!channelMap.containsKey(CHANNEL_REGION_ENTER_TRIGGER)) {
                Channel channel = ChannelBuilder.create(new ChannelUID(uid,
                        CHANNEL_REGION_ENTER_TRIGGER), null)
                        .withType(CHANNEL_TYPE_ENTER_TRIGGER)
                        .withKind(ChannelKind.TRIGGER)
                        .build();
                channelMap.put(CHANNEL_REGION_ENTER_TRIGGER, channel);
                logger.trace("Creating trigger channel {} for region enter events", CHANNEL_REGION_ENTER_TRIGGER);
            }
        }

        //create distance channel if the primary region is configured
        if (distanceChannelNeeded) {
            Channel channel = ChannelBuilder.create(new ChannelUID(uid,
                    CHANNEL_DISTANCE), "Number")
                    .withType(CHANNEL_TYPE_DISTANCE)
                    .build();
            channelMap.put(CHANNEL_DISTANCE, channel);
            logger.trace("Primary location is set - creating distance channel.");
        }
    }

    /**
     * Removes regionPresence, regionTrigger and networkPresence channels if not needed by the configuration.
     *
     * @param channelMap Current channel map
     */
    private void removeUnusedChannels(Map<String, Channel> channelMap) {
        for (Channel c : thing.getChannels()) {
            String id = c.getUID().getId();
            if (id.startsWith(CHANNEL_REGION_PRESENCE)) {
                int idx = id.indexOf('_');
                if (idx > -1) {
                    String regionName = id.substring(idx + 1);
                    Region region = getRegionByName(regionName);
                    if (region == null) {
                        channelMap.remove(id);
                        logger.trace("Removing channel for missing region: {}", id);
                    } else if (region.getTriggerEvent()) {
                        channelMap.remove(id);
                        logger.trace("Removing presence channel as trigger channel is configured: {}", id);
                    }
                } else {
                    channelMap.remove(id);
                    logger.trace("Removing channel with invalid name: {}", id);
                }
            } else if (id.startsWith(CHANNEL_NETWORK_PRESENCE)) {
                if (!deviceConfig.getIpAddressStatic()) {
                    logger.trace("Removing channel for network presence monitoring as no static IP address is set for device: {}", id);
                    channelMap.remove(id);
                }
            }
        }

        //handle trigger channels
        boolean needTriggerChannels = false;
        boolean distanceChannelNeeded = false;
        for (Region r : getAllRegions()) {
            needTriggerChannels |= r.getTriggerEvent();
            distanceChannelNeeded |= r.isPrimary();
        }
        if (!needTriggerChannels) {
            logger.trace("Triggers channels are not required by regions - removing.");
            channelMap.remove(CHANNEL_REGION_ENTER_TRIGGER);
            channelMap.remove(CHANNEL_REGION_LEAVE_TRIGGER);
        } else {
            logger.trace("Triggers channels are required.");
        }

        //handle distance channel
        if (distanceChannelNeeded) {
            logger.trace("Primary location is not set - removing distance channel.");
            channelMap.remove(CHANNEL_DISTANCE);
        }
    }

    /**
     * Create a map from thing channels.
     *
     * @return Map of channels.
     */
    private Map<String, Channel> getChannelMap() {
        Map<String, Channel> channelMap = thing.getChannels()
                .stream()
                .collect(Collectors.toMap(c -> c.getUID().getId(), Function.identity()));
        if (logger.isTraceEnabled()) {
            channelMap.values().forEach(c -> logger.trace("Existing channel: {}", c.getUID().getId()));
        }
        return channelMap;
    }

    /**
     * Search device and binding level configurations for a region.
     *
     * @param regionName Name if the region to find.
     * @return Requested region or null.
     */
    @Nullable Region getRegionByName(String regionName) {
        Region region = deviceConfig.getRegionByName(regionName);
        return region == null ? bindingConfig.getRegionByName(regionName) : region;
    }

    /**
     * Collects all regions (device and binding level) into the same collection
     *
     * @return Joint collection of regions
     */
    private Collection<Region> getAllRegions() {
        Set<Region> allRegions = new HashSet<>(bindingConfig.getRegions());
        allRegions.addAll(deviceConfig.getRegions());
        return allRegions;
    }
}
