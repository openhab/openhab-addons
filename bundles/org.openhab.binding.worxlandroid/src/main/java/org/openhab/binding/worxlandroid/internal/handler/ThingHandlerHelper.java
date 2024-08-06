/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.worxlandroid.internal.handler;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * {@link ThingHandlerHelper} provides utility function for thing handlers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public interface ThingHandlerHelper {
    public boolean isLinked(ChannelUID channelUID);

    public Thing getThing();

    public void updateState(ChannelUID channelUID, State state);

    public default @Nullable <T extends BaseBridgeHandler> T getBridgeHandler(@Nullable Bridge bridge,
            Class<T> expected) {
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                try {
                    T expectedBridge = expected.cast(handler);
                    if (expectedBridge.getThing().getStatus() == ThingStatus.ONLINE) {
                        return expectedBridge;
                    }
                } catch (ClassCastException exc) {
                }
            }
        }
        return null;
    }

    public default boolean isOnline() {
        return getThing().getStatus() == ThingStatus.ONLINE;
    }

    public default boolean firstLaunch() {
        return getThing().getProperties().isEmpty();
    }

    public default ChannelGroupUID getGroupUID(String group) {
        return new ChannelGroupUID(getThing().getUID(), group);
    }

    public default ChannelUID getChannelUID(String group, String channelId) {
        return new ChannelUID(getThing().getUID(), group, channelId);
    }

    public default Set<ChannelUID> getChannelUIDs(String groupName, Set<String> channelIds) {
        Set<ChannelUID> result = new HashSet<>();
        ChannelGroupUID groupUID = getGroupUID(groupName);
        channelIds.forEach(id -> result.add(new ChannelUID(groupUID, id)));
        return result;
    }

    public default void updateIfActive(String group, String channelId, State state) {
        ChannelUID id = getChannelUID(group, channelId);
        if (isLinked(id)) {
            updateState(id, state);
        }
    }

    public default void updateChannelOnOff(String group, String channelId, boolean value) {
        updateIfActive(group, channelId, OnOffType.from(value));
    }

    public default void updateChannelDateTime(String group, String channelId, @Nullable ZonedDateTime timestamp) {
        updateIfActive(group, channelId, timestamp == null ? UnDefType.NULL : new DateTimeType(timestamp));
    }

    public default void updateChannelString(String group, String channelId, @Nullable String value) {
        updateIfActive(group, channelId, value == null || value.isEmpty() ? UnDefType.NULL : new StringType(value));
    }

    public default void updateChannelEnum(String group, String channelId, @Nullable Enum<?> value) {
        String name = value != null ? value.name() : null;
        updateChannelString(group, channelId, name == null || "UNKNOWN".equals(name) ? null : name);
    }

    public default void updateChannelDecimal(String group, String channelId, @Nullable Number value) {
        updateIfActive(group, channelId, value == null || value.equals(-1) ? UnDefType.NULL : new DecimalType(value));
    }

    public default void updateChannelQuantity(String group, String channelId, @Nullable QuantityType<?> quantity) {
        updateIfActive(group, channelId, quantity != null ? quantity : UnDefType.NULL);
    }

    public default void updateChannelQuantity(String group, String channelId, @Nullable Number d, Unit<?> unit) {
        if (d == null) {
            updateIfActive(group, channelId, UnDefType.NULL);
        } else {
            updateChannelQuantity(group, channelId, new QuantityType<>(d, unit));
        }
    }
}
