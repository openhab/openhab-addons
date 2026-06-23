/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.fineoffsetweatherstation.internal.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * Reconciles a batch of {@link MeasuredValue}s against a Thing's current channels: it decides which dynamic
 * channels to create, which states to post, and which channels to remove once their value has been missing for
 * {@link #MISSING_VALUE_REMOVAL_THRESHOLD} consecutive reconcile passes. It is a pure planner - it never mutates a
 * Thing or posts state itself; the calling handler applies the returned {@link Plan} with its own builder.
 *
 * <p>
 * Removal-eligibility is derived from a fixed set of protected channel IDs: every channel except those is removable.
 * Basing it on a static set rather than on which channels this instance created keeps it correct across restarts,
 * where dynamic channels persisted on the Thing were never seen being created.
 *
 * <p>
 * Used by both {@code FineOffsetGatewayHandler} (gateway channels - an empty protected set, so every channel is
 * removal-eligible) and {@code FineOffsetSensorHandler} (sensor Thing - the static signal/battery channels are
 * protected and never removed, while every other - dynamic measurement - channel is removal-eligible).
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
class DynamicChannelReconciler {

    /**
     * Number of consecutive reconcile passes a value may be missing before its channel is removed. Debounces
     * transient gaps so dynamically created channels do not flip-flop.
     */
    static final int MISSING_VALUE_REMOVAL_THRESHOLD = 10;

    /** Builds the {@link Channel} for a value whose channel does not exist yet, or {@code null} if it cannot. */
    @FunctionalInterface
    interface ChannelFactory {
        @Nullable
        Channel create(MeasuredValue value);
    }

    /** The changes one reconcile pass calls for. The handler applies these with its own Thing builder. */
    static final class Plan {
        final List<Channel> channelsToAdd;
        final List<Channel> channelsToRemove;
        final Map<ChannelUID, State> statesToPost;

        Plan(List<Channel> channelsToAdd, List<Channel> channelsToRemove, Map<ChannelUID, State> statesToPost) {
            this.channelsToAdd = channelsToAdd;
            this.channelsToRemove = channelsToRemove;
            this.statesToPost = statesToPost;
        }

        boolean hasChannelChanges() {
            return !channelsToAdd.isEmpty() || !channelsToRemove.isEmpty();
        }
    }

    private final Set<String> protectedChannelIds;
    private final Map<String, Integer> missingCounts = new HashMap<>();

    /**
     * @param protectedChannelIds channel IDs (by {@link ChannelUID#getId()}) that must never be removed - e.g. a
     *            sensor Thing's static signal/battery channels. Pass an empty set to make every channel
     *            removal-eligible (gateway semantics).
     */
    DynamicChannelReconciler(Set<String> protectedChannelIds) {
        this.protectedChannelIds = protectedChannelIds;
    }

    /** Clears the accumulated missing-value debounce state, e.g. when the owning handler re-initializes. */
    void reset() {
        missingCounts.clear();
    }

    Plan reconcile(Collection<MeasuredValue> values, Collection<Channel> currentChannels,
            Function<MeasuredValue, String> channelIdResolver, ChannelFactory channelFactory) {
        Map<String, Channel> currentById = new HashMap<>();
        for (Channel channel : currentChannels) {
            currentById.put(channel.getUID().getId(), channel);
        }

        Set<String> reported = new HashSet<>();
        List<Channel> toAdd = new ArrayList<>();
        Map<String, ChannelUID> addedThisPass = new HashMap<>();
        Map<ChannelUID, State> states = new LinkedHashMap<>();

        for (MeasuredValue value : values) {
            String id = channelIdResolver.apply(value);
            reported.add(id);
            Channel existing = currentById.get(id);
            ChannelUID uid;
            if (existing != null) {
                uid = existing.getUID();
            } else {
                ChannelUID queued = addedThisPass.get(id);
                if (queued != null) {
                    uid = queued;
                } else {
                    Channel created = channelFactory.create(value);
                    if (created == null) {
                        continue;
                    }
                    toAdd.add(created);
                    uid = created.getUID();
                    addedThisPass.put(id, uid);
                }
            }
            states.put(uid, value.getState());
        }

        List<Channel> toRemove = new ArrayList<>();
        for (Channel channel : currentChannels) {
            String id = channel.getUID().getId();
            if (protectedChannelIds.contains(id)) {
                continue;
            }
            if (reported.contains(id)) {
                missingCounts.remove(id);
            } else {
                Integer count = missingCounts.merge(id, 1, Integer::sum);
                if (count != null && count >= MISSING_VALUE_REMOVAL_THRESHOLD) {
                    toRemove.add(channel);
                    missingCounts.remove(id);
                }
            }
        }

        return new Plan(toAdd, toRemove, states);
    }
}
