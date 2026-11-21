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
package org.openhab.binding.lgthinq.internal.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;

/**
 * Builds and caches {@link ChannelUID}s for a single channel group within a thing.
 * <p>
 * Replaces the pattern of declaring one {@link ChannelUID} field per channel in each handler.
 * Each handler creates one {@code DeviceChannelGroup} per channel group (e.g. dashboard,
 * extended-info); channels within the group are resolved on first access and cached.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public final class DeviceChannelGroup {

    private final ChannelGroupUID groupUID;
    private final Map<String, ChannelUID> channelCache = new ConcurrentHashMap<>();

    /**
     * Creates a new group for the given thing and group identifier.
     *
     * @param thingUID the UID of the owning thing
     * @param groupId the channel group identifier (matches the group defined in the thing XML)
     */
    public DeviceChannelGroup(ThingUID thingUID, String groupId) {
        this.groupUID = new ChannelGroupUID(thingUID, groupId);
    }

    /**
     * Returns the {@link ChannelGroupUID} for this group.
     *
     * @return group UID
     */
    public ChannelGroupUID uid() {
        return groupUID;
    }

    /**
     * Returns the {@link ChannelUID} for the given channel within this group, creating and caching
     * it on the first call.
     *
     * @param channelId the channel identifier (matches the channel defined in the thing XML)
     * @return the channel UID
     */
    public ChannelUID channel(String channelId) {
        ChannelUID uid = channelCache.get(channelId);
        if (uid == null) {
            uid = new ChannelUID(groupUID, channelId);
            channelCache.put(channelId, uid);
        }
        return uid;
    }
}
