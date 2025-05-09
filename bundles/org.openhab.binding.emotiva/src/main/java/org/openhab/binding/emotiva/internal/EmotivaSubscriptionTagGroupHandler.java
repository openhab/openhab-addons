/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal;

import static org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup.UI_DEVICE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles subscription tags against av given Emotiva device. Devices have limited processing power, so the device do
 * not expect an integration to subscribe to all possible tags at the same time. Tags are divided into groups that
 * correspond with source/input the device is using or configuration values set in the binding.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaSubscriptionTagGroupHandler {

    private final Logger logger = LoggerFactory.getLogger(EmotivaSubscriptionTagGroupHandler.class);

    private final EmotivaConfiguration config;
    private final EmotivaProcessorState state;
    private final List<EmotivaSubscriptionTagGroup> basicSubscriptions = List.of(EmotivaSubscriptionTagGroup.GENERAL,
            EmotivaSubscriptionTagGroup.AUDIO_ADJUSTMENT);

    public EmotivaSubscriptionTagGroupHandler(EmotivaConfiguration config, EmotivaProcessorState state) {
        this.config = config;
        this.state = state;
    }

    public List<EmotivaSubscriptionTags> init() {
        Set<EmotivaSubscriptionTagGroup> unsubscribe = new HashSet<>();
        Set<EmotivaSubscriptionTagGroup> subscribe = new HashSet<>(basicSubscriptions);

        if (config.activateFrontBar) {
            subscribe.add(UI_DEVICE);
        } else {
            unsubscribe.add(UI_DEVICE);
        }
        if (config.activateOSDMenu) {
            subscribe.add(EmotivaSubscriptionTagGroup.UI_MENU);
        } else {
            unsubscribe.add(EmotivaSubscriptionTagGroup.UI_MENU);
        }
        if (config.activateZone2) {
            subscribe.add(EmotivaSubscriptionTagGroup.ZONE2_GENERAL);
        } else {
            unsubscribe.add(EmotivaSubscriptionTagGroup.ZONE2_GENERAL);
        }

        state.updateUnsubscribedTagGroups(unsubscribe);
        state.updateSubscribedTagGroups(subscribe);
        Set<EmotivaSubscriptionTagGroup> tagGroups = state.getSubscriptionsTagGroups();
        List<EmotivaSubscriptionTags> tags = EmotivaSubscriptionTags.getBySubscriptionTagGroups(tagGroups);
        logger.trace("Subscribing to tag groups '{}' and tags '{}' on binding startup.", tagGroups, tags);
        return tags;
    }

    /**
     * Updates a BiConsumer with subscribe and unsubscribe tag groups based on provided source.
     *
     * @param source Provided source.
     * @param tagGroups Consumer for subscribe and unsubscribe tag groups.
     */
    public void tagGroupsFromSource(EmotivaControlCommands source,
            BiConsumer<Set<EmotivaSubscriptionTagGroup>, Set<EmotivaSubscriptionTagGroup>> tagGroups) {
        Set<EmotivaSubscriptionTagGroup> unsubscribeSet = new HashSet<>();
        Set<EmotivaSubscriptionTagGroup> subscribeSet = new HashSet<>();

        switch (source) {
            case analog1, analog2, analog3, analog4, analog5, analog71, ARC, coax1, coax2, coax3, coax4, front_in,
                    optical1, optical2, optical3, optical4, usb_stream -> {
                subscribeSet.add(EmotivaSubscriptionTagGroup.AUDIO_INFO);
                unsubscribeSet.add(EmotivaSubscriptionTagGroup.TUNER);
                unsubscribeSet.add(EmotivaSubscriptionTagGroup.VIDEO_INFO);
            }
            case hdmi1, hdmi2, hdmi3, hdmi4, hdmi5, hdmi6, hdmi7, hdmi8, source_1, source_2, source_3, source_4,
                    source_5, source_6, source_7, source_8 -> {
                subscribeSet.add(EmotivaSubscriptionTagGroup.AUDIO_INFO);
                subscribeSet.add(EmotivaSubscriptionTagGroup.VIDEO_INFO);
                unsubscribeSet.add(EmotivaSubscriptionTagGroup.TUNER);
            }
            case tuner -> {
                subscribeSet.add(EmotivaSubscriptionTagGroup.TUNER);
                subscribeSet.add(EmotivaSubscriptionTagGroup.AUDIO_INFO);
                unsubscribeSet.add(EmotivaSubscriptionTagGroup.VIDEO_INFO);
            }
            default -> subscribeSet.add(EmotivaSubscriptionTagGroup.VIDEO_INFO);
        }
        tagGroups.accept(subscribeSet, unsubscribeSet);
    }
}
