/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.handler;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants;
import org.openhab.binding.gpstracker.internal.config.GPSTrackerBindingConfiguration;
import org.openhab.binding.gpstracker.internal.config.TrackerConfiguration;
import org.openhab.binding.gpstracker.internal.message.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.*;

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
     * Thing representing the tracker tracker.
     */
    private Thing thing;

    /**
     * Binding level configuration
     */
    private GPSTrackerBindingConfiguration bindingConfig;

    /**
     * Tracker level configuration
     */
    private TrackerConfiguration trackerConfig;

    /**
     * Translation helper
     */
    private TranslationUtil translationUtil;


    /**
     * Constructor.
     *
     * @param thing           The thing
     * @param bindingConfig   Binding level configuration
     * @param trackerConfig   Device level configuration
     * @param translationUtil Translation helper
     */
    ChannelUtil(Thing thing, GPSTrackerBindingConfiguration bindingConfig, TrackerConfiguration trackerConfig, TranslationUtil translationUtil) {
        this.thing = thing;
        this.bindingConfig = bindingConfig;
        this.trackerConfig = trackerConfig;
        this.translationUtil = translationUtil;
    }

    void setTrackerConfig(TrackerConfiguration trackerConfig) {
        this.trackerConfig = trackerConfig;
    }

    /**
     * Updates dynamic channels for the .
     * - A Switch type channel will be created for all binding and tracker level regions if the region triggerEvent
     * flag is FALSE. Channel state will be set to ON if the tracker is inside the region.
     * - RegionEnter and regionLeave trigger channels will be created if triggerEvent flag for region is TRUE. The
     * payload for these channels will be the region name.
     */
    Map<String, Channel> updateChannels() {
        ThingUID uid = thing.getUID();
        logger.debug("Update channel structure based on binding configuration for tracker {}", uid.getId());
        Map<String, Channel> channelMap = getChannelMap();
        Set<String> origChannels = new HashSet<>(channelMap.keySet());

        removeUnusedChannels(channelMap);

        createMissingChannels(channelMap);

        if (logger.isTraceEnabled()) {
            for (Channel c : channelMap.values()) {
                logger.trace("Channel after update: {}", c.getUID().getId());
            }
        }

        Set<String> newChannels = new HashSet<>(channelMap.keySet());
        return newChannels.equals(origChannels) ? null : channelMap;
    }

    /**
     * Create missing region switches and trigger channels.
     *
     * @param channelMap Channel map with required thing channels.
     */
    private void createMissingChannels(Map<String, Channel> channelMap) {
        ThingUID uid = thing.getUID();
        boolean triggerChannelNeeded = false;
        for (Region r : getAllRegions()) {
            String presenceChannelId = CHANNEL_REGION_PRESENCE + "_" + r.getId();
            if (!r.getTriggerEvent()) {
                if (!channelMap.containsKey(presenceChannelId)) {
                    Channel channel = ChannelBuilder.create(new ChannelUID(uid, presenceChannelId),
                            "Switch").withType(GPSTrackerBindingConstants.CHANNEL_TYPE_PRESENCE)
                            .withLabel(translationUtil.getText(TRANSLATION_PRESENCE, r.getName()))
                            .build();
                    channelMap.put(presenceChannelId, channel);
                    logger.trace("Creating channel {} for region {}", presenceChannelId, r.getName());
                }
            } else {
                triggerChannelNeeded = true;
                logger.trace("Trigger channel is required by region {}", r.getName());
            }
        }
        //if at least one region configured for the thing requires event triggering we need the trigger channel
        if (triggerChannelNeeded) {
            if (!channelMap.containsKey(CHANNEL_REGION_TRIGGER)) {
                Channel channel = ChannelBuilder.create(new ChannelUID(uid,
                        CHANNEL_REGION_TRIGGER), null)
                        .withType(CHANNEL_TYPE_TRIGGER)
                        .withKind(ChannelKind.TRIGGER)
                        .build();
                channelMap.put(CHANNEL_REGION_TRIGGER, channel);
                logger.trace("Creating trigger channel {} for region events", CHANNEL_REGION_TRIGGER);
            }
        }

        //create distance channels for regions with configured location
        for (Region r : getAllRegions()) {
            if (r.getLocation() != null) {
                String channelId = CHANNEL_DISTANCE + "_" + r.getId();
                if (!channelMap.containsKey(channelId)) {
                    Channel channel = ChannelBuilder.create(new ChannelUID(uid,
                            channelId), "Number")
                            .withType(CHANNEL_TYPE_DISTANCE)
                            .withLabel(translationUtil.getText(TRANSLATION_DISTANCE, r.getName()))
                            .build();
                    channelMap.put(channelId, channel);
                    logger.trace("Creating distance channel for region {}", r.getName());
                }
            }
        }
    }

    private Region getRegionFromChannelName(String channelId) {
        int idx = channelId.indexOf('_');
        if (idx > -1) {
            String regionName = channelId.substring(idx + 1);
            return getRegionByName(regionName);
        }
        return null;
    }

    /**
     * Removes regionPresence and regionTrigger channels if not needed by the configuration.
     *
     * @param channelMap Current channel map
     */
    private void removeUnusedChannels(Map<String, Channel> channelMap) {
        for (Channel c : thing.getChannels()) {
            String id = c.getUID().getId();
            if (id.startsWith(CHANNEL_REGION_PRESENCE)) {
                Region region = getRegionFromChannelName(id);
                if (region != null) {
                    if (region.getTriggerEvent()) {
                        channelMap.remove(id);
                        logger.trace("Removing presence channel as trigger channel is configured: {}", id);
                    }
                } else {
                    channelMap.remove(id);
                    logger.trace("Removing presence channel with missing region: {}", id);
                }
            } else if (id.startsWith(CHANNEL_DISTANCE)) {
                Region region = getRegionFromChannelName(id);
                if (region != null) {
                    if (region.getLocation() == null) {
                        channelMap.remove(id);
                        logger.trace("Removing distance channel without location: {}", id);
                    }
                } else {
                    channelMap.remove(id);
                    logger.trace("Removing distance channel with missing region: {}", id);
                }
            }
        }

        //handle trigger channels
        boolean needTriggerChannels = false;
        for (Region r : getAllRegions()) {
            needTriggerChannels |= r.getTriggerEvent();
        }
        if (!needTriggerChannels) {
            logger.trace("Triggers channels are not required by regions - removing.");
            channelMap.remove(CHANNEL_REGION_TRIGGER);
        } else {
            logger.trace("Triggers channels are required.");
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
     * Search tracker and binding level configurations for a region.
     *
     * @param regionName Name if the region to find.
     * @return Requested region or null.
     */
    @Nullable Region getRegionByName(String regionName) {
        Region region = trackerConfig.getRegionByName(regionName);
        return region == null ? bindingConfig.getRegionByName(regionName) : region;
    }

    /**
     * Collects all regions (tracker and binding level) into the same collection
     *
     * @return Joint collection of regions
     */
    private Collection<Region> getAllRegions() {
        Set<Region> allRegions = new HashSet<>(bindingConfig.getRegions());
        allRegions.addAll(trackerConfig.getRegions());
        return allRegions;
    }
}
