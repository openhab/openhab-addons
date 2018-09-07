/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.handler;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants;
import org.openhab.binding.gpstracker.internal.config.GPSTrackerBindingConfiguration;
import org.openhab.binding.gpstracker.internal.message.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Translation helper
     */
    private TranslationUtil translationUtil;


    /**
     * Constructor.
     *
     * @param thing The thing
     * @param bindingConfig Binding level configuration
     * @param translationUtil Translation helper
     */
    ChannelUtil(Thing thing, GPSTrackerBindingConfiguration bindingConfig, TranslationUtil translationUtil) {
        this.thing = thing;
        this.bindingConfig = bindingConfig;
        this.translationUtil = translationUtil;
    }

    /**
     * Updates dynamic channels for internal regions:
     * - regionPresence_%regionId%.
     * - distance_%regionId%.
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
     * Create missing region switch and distance channels.
     *
     * @param channelMap Channel map with required thing channels.
     */
    private void createMissingChannels(Map<String, Channel> channelMap) {
        ThingUID uid = thing.getUID();
        for (Region r : bindingConfig.getRegions()) {
            String presenceChannelId = CHANNEL_REGION_PRESENCE + "_" + r.getId();
            if (!channelMap.containsKey(presenceChannelId)) {
                Channel channel = ChannelBuilder.create(new ChannelUID(uid, presenceChannelId),
                        "Switch").withType(GPSTrackerBindingConstants.CHANNEL_TYPE_PRESENCE)
                        .withLabel(translationUtil.getText(TRANSLATION_PRESENCE, r.getName()))
                        .build();
                channelMap.put(presenceChannelId, channel);
                logger.trace("Creating channel {} for region {}", presenceChannelId, r.getName());
            }
        }

        //create distance channels for regions
        for (Region r : bindingConfig.getRegions()) {
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

    private Region getRegionFromChannelId(String channelId) {
        int idx = channelId.indexOf('_');
        if (idx > -1) {
            String regionName = channelId.substring(idx + 1);
            return bindingConfig.getRegionByName(regionName);
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
                Region region = getRegionFromChannelId(id);
                if (region == null) {
                    channelMap.remove(id);
                    logger.trace("Removing presence channel with missing region: {}", id);
                }
            } else if (id.startsWith(CHANNEL_DISTANCE)) {
                Region region = getRegionFromChannelId(id);
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
}
