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

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_SOURCES_MAIN_ZONE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_SOURCES_ZONE_2;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_TUNER_BANDS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_TUNER_CHANNELS;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_am;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_fm;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.channel_1;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTagGroup;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
import org.openhab.core.types.State;

/**
 * Holds state for Emotiva Processor.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaProcessorState {

    private final Map<String, State> channelStateMap;

    private EnumMap<EmotivaControlCommands, String> sourcesMainZone;
    private EnumMap<EmotivaControlCommands, String> sourcesZone2;
    private final Set<EmotivaSubscriptionTagGroup> subscriptions = new HashSet<>();
    private final EnumMap<EmotivaSubscriptionTags, String> modes;
    private Instant lastSeen = Instant.EPOCH;

    private EnumMap<EmotivaControlCommands, String> tunerChannels = new EnumMap<>(
            Map.ofEntries(Map.entry(channel_1, channel_1.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_2, EmotivaControlCommands.channel_2.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_3, EmotivaControlCommands.channel_3.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_4, EmotivaControlCommands.channel_4.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_5, EmotivaControlCommands.channel_5.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_6, EmotivaControlCommands.channel_6.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_7, EmotivaControlCommands.channel_7.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_8, EmotivaControlCommands.channel_8.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_9, EmotivaControlCommands.channel_9.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_10, EmotivaControlCommands.channel_10.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_11, EmotivaControlCommands.channel_11.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_12, EmotivaControlCommands.channel_12.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_13, EmotivaControlCommands.channel_13.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_14, EmotivaControlCommands.channel_14.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_15, EmotivaControlCommands.channel_15.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_16, EmotivaControlCommands.channel_16.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_17, EmotivaControlCommands.channel_17.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_18, EmotivaControlCommands.channel_18.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_19, EmotivaControlCommands.channel_19.getLabel()),
                    Map.entry(EmotivaControlCommands.channel_20, EmotivaControlCommands.channel_20.getLabel())));

    private EnumMap<EmotivaControlCommands, String> tunerBands = new EnumMap<>(
            Map.of(band_am, band_am.getLabel(), band_fm, band_fm.getLabel()));

    public EmotivaProcessorState() {
        channelStateMap = Collections.synchronizedMap(new HashMap<>());
        sourcesMainZone = new EnumMap<>(EmotivaControlCommands.class);
        sourcesZone2 = new EnumMap<>(EmotivaControlCommands.class);
        modes = new EnumMap<>(EmotivaSubscriptionTags.class);
    }

    public Optional<State> getChannel(String channelName) {
        if (channelStateMap.containsKey(channelName)) {
            return Optional.ofNullable(channelStateMap.get(channelName));
        } else {
            return Optional.empty();
        }
    }

    public Optional<State> getChannel(EmotivaSubscriptionTags channelTagName) {
        if (channelStateMap.containsKey(channelTagName.name())) {
            return Optional.ofNullable(channelStateMap.get(channelTagName.name()));
        } else {
            return Optional.empty();
        }
    }

    public Map<EmotivaControlCommands, String> getCommandMap(String mapName) {
        return switch (mapName) {
            case MAP_SOURCES_MAIN_ZONE -> sourcesMainZone;
            case MAP_SOURCES_ZONE_2 -> sourcesZone2;
            case MAP_TUNER_CHANNELS -> tunerChannels;
            case MAP_TUNER_BANDS -> tunerBands;
            default -> new EnumMap<>(EmotivaControlCommands.class);
        };
    }

    public EnumMap<EmotivaControlCommands, String> getSourcesMainZone() {
        return sourcesMainZone;
    }

    public EnumMap<EmotivaControlCommands, String> getSourcesZone2() {
        return sourcesZone2;
    }

    public EnumMap<EmotivaSubscriptionTags, String> getModes() {
        return modes;
    }

    public void setChannels(EnumMap<EmotivaControlCommands, String> map) {
        tunerChannels = map;
    }

    public void setSourcesMainZone(EnumMap<EmotivaControlCommands, String> map) {
        sourcesMainZone = map;
    }

    public void setSourcesZone2(EnumMap<EmotivaControlCommands, String> map) {
        sourcesZone2 = map;
    }

    public void setTunerBands(EnumMap<EmotivaControlCommands, String> map) {
        tunerBands = map;
    }

    public void updateChannel(String channel, State state) {
        channelStateMap.put(channel, state);
    }

    public void updateSourcesMainZone(EmotivaControlCommands command, String label) {
        sourcesMainZone.put(command, label.trim());
    }

    public void updateModes(EmotivaSubscriptionTags tag, String value) {
        modes.put(tag, value);
    }

    public void removeChannel(String channel) {
        channelStateMap.remove(channel);
    }

    public void updateLastSeen(Instant instant) {
        lastSeen = instant;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public Set<EmotivaSubscriptionTagGroup> getSubscriptionsTagGroups() {
        return subscriptions;
    }

    public void updateSubscribedTagGroups(Set<EmotivaSubscriptionTagGroup> toAddSubscriptions) {
        subscriptions.addAll(toAddSubscriptions);
    }

    public void updateUnsubscribedTagGroups(Set<EmotivaSubscriptionTagGroup> toRemoveSubscriptions) {
        subscriptions.removeAll(toRemoveSubscriptions);
    }
}
