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
package org.openhab.binding.panamaxfurman.internal.protocol;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.PanamaxFurmanAbstractHandler;
import org.openhab.binding.panamaxfurman.internal.PanamaxFurmanConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum for channel that is exposed by the device
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public enum PowerConditionerChannel {
    POWER("power", OnOffType.class, true),
    ID("id", null, false),;

    private final static Logger logger = LoggerFactory.getLogger(PowerConditionerChannel.class);

    private final static Map<String, PowerConditionerChannel> CHANNEL_STRING_TO_ENUM_TABLE = new ConcurrentHashMap<>();

    private final String name;
    private final @Nullable Class<? extends State> stateClass;
    private final boolean outletSpecific;

    private PowerConditionerChannel(String name, @Nullable Class<? extends State> stateClass, boolean outletSpecific) {
        this.name = name;
        this.stateClass = stateClass;
        this.outletSpecific = outletSpecific;
    }

    public String getChannelName(@Nullable Integer outletNumber) {
        if (outletNumber == null) {
            return getName();
        }
        if (isOutletSpecific()) {
            return PanamaxFurmanAbstractHandler.getChannelUID(name, outletNumber);
        } else {
            logger.warn("{} was called with outlet #{} but is not outlet specific.  Please raise a Github issue.", this,
                    outletNumber);
            return name;
        }
    }

    public String getName() {
        if (isOutletSpecific()) {
            throw new IllegalStateException(this.name()
                    + " was called without an outlet # but is outlet specific.  Please raise a Github issue.");
        }
        return name;
    }

    public @Nullable Class<? extends State> getStateClass() {
        return stateClass;
    }

    public @Nullable State buildState(String stateValueParam) {
        String stateValue = stateValueParam.trim();
        if (getStateClass() == null) {
            return null;
        } else if (OnOffType.class.equals(getStateClass())) {
            return OnOffType.from(stateValue);
        } else {
            logger.warn("Can't map state class {}, returning null. Please raise a Github issue", stateClass);
            return null;
        }
    }

    public boolean isOutletSpecific() {
        return outletSpecific;
    }

    public static PowerConditionerChannel from(ChannelUID channelUID) {
        return from(channelUID.getId());
    }

    public static PowerConditionerChannel from(String channelString) {
        initLookupTable();
        Matcher matcher = PanamaxFurmanConstants.GROUP_CHANNEL_OUTLET_PATTERN.matcher(channelString);
        boolean channelHasOutlet = matcher.find();
        String toFind = channelString;
        if (channelHasOutlet) {
            toFind = channelString.substring(channelString.indexOf('#') + 1);
        }
        PowerConditionerChannel channel = CHANNEL_STRING_TO_ENUM_TABLE.get(toFind);
        if (channel == null) {
            throw new IllegalArgumentException("Could not map channelString " + channelString + " by searching for "
                    + toFind + " from cache table: " + CHANNEL_STRING_TO_ENUM_TABLE.keySet());
        }
        return channel;
    }

    private static void initLookupTable() {
        if (CHANNEL_STRING_TO_ENUM_TABLE.isEmpty()) {
            Arrays.stream(PowerConditionerChannel.values())
                    .forEach(pmc -> CHANNEL_STRING_TO_ENUM_TABLE.put(pmc.name, pmc));
        }
    }
}
