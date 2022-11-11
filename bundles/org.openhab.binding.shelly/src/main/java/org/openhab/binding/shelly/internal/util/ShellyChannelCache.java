/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.util;

import static org.openhab.binding.shelly.internal.util.ShellyUtils.mkChannelId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyChannelCache} implements a caching layer for channel updates.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyChannelCache {
    private final Logger logger = LoggerFactory.getLogger(ShellyChannelCache.class);

    private final ShellyThingInterface thingHandler;
    private final Map<String, State> channelData = new ConcurrentHashMap<>();
    private String thingName = "";
    private boolean enabled = false;

    public ShellyChannelCache(ShellyThingInterface thingHandler) {
        this.thingHandler = thingHandler;
        setThingName(thingHandler.getThingName());
    }

    public void setThingName(String thingName) {
        this.thingName = thingName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public synchronized void disable() {
        clear();
        enabled = false;
    }

    /**
     * Update one channel. Use Channel Cache to avoid unnecessary updates (and avoid
     * messing up the log with those updates)
     *
     * @param channelId Channel id
     * @param value Value (State)
     * @param forceUpdate true: ignore cached data, force update; false check cache of changed data
     * @return true, if successful
     */
    public boolean updateChannel(String channelId, State newValue, Boolean forceUpdate) {
        try {
            State current = null;
            if (channelData.containsKey(channelId)) {
                current = channelData.get(channelId);
            }
            if (!enabled || forceUpdate || (current == null) || !current.equals(newValue)) {
                if ((current != null) && current.getClass().isEnum() && (current.equals(newValue))) {
                    return false; // special case for OnOffType
                }
                // For channels that support multiple types (like brightness) a suffix is added
                // this gets removed to get the channelId for updateState
                thingHandler.publishState(channelId, newValue);
                if (current == null) {
                    channelData.put(channelId, newValue);
                } else {
                    channelData.replace(channelId, newValue);
                }
                return true;
            }
        } catch (IllegalArgumentException e) {
            logger.debug("{}: Unable to update channel {} with {} (type {}): {} ({})", thingName, channelId, newValue,
                    newValue.getClass(), ShellyUtils.getMessage(e), e.getClass(), e);
        }
        return false;
    }

    public boolean updateChannel(String group, String channel, State value) {
        return updateChannel(mkChannelId(group, channel), value, false);
    }

    public boolean updateChannel(String channelId, State value) {
        return updateChannel(channelId, value, false);
    }

    /**
     * Get a value from the Channel Cache
     *
     * @param group Channel Group
     * @param channel Channel Name
     * @return the data from that channel
     */

    public State getValue(String group, String channel) {
        return getValue(mkChannelId(group, channel));
    }

    public State getValue(String channelId) {
        State st = channelData.get(channelId);
        return st != null ? st : UnDefType.NULL;
    }

    public void resetChannel(String channelId) {
        channelData.remove(channelId);
    }

    public void clear() {
        channelData.clear();
    }
}
