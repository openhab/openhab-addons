/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.providers.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the thing type channel definition that will be used to serialize/deserialize channel
 * information.
 * 
 * Note that this can be either created via the constructor (which will enforce null/empty checks) or via
 * deserialization (which can provide null/empty variables)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyThingChannelDefinition {
    /** The channel identifier */
    private @Nullable String channelId;

    /** The channel type identifier */
    private @Nullable String channelType;

    /** The channel properties */
    private @Nullable Map<@Nullable String, @Nullable String> properties;

    /** The channel options */
    private @Nullable SonyThingStateDefinition state;

    /**
     * Empty constructor used for deserialization
     */
    public SonyThingChannelDefinition() {
    }

    /**
     * Constructs the definition from the passed arguments.
     *
     * @param channelId the non-null, non-empty channel identifier
     * @param channelType the non-null, non-empty channel type
     * @param state the non-null thing state definition
     * @param properties the non-null, possibly empty properties
     */
    public SonyThingChannelDefinition(final String channelId, final String channelType,
            final SonyThingStateDefinition state, final Map<String, String> properties) {
        Validate.notEmpty(channelId, "channelId must not be empty");
        Validate.notEmpty(channelType, "channelType must not be empty");
        Objects.requireNonNull(state, "state cannot be null");
        Objects.requireNonNull(properties, "properties cannot be null");

        this.channelId = channelId;
        this.channelType = channelType;
        this.properties = new HashMap<>(properties);
        this.state = state;
    }

    /**
     * Returns the channel identifier
     *
     * @return a possibly null, possibly empty channel identifier
     */
    public @Nullable String getChannelId() {
        return channelId;
    }

    /**
     * Returns the channel type identifier
     *
     * @return a possibly null, possibly empty channel type identifier
     */
    public @Nullable String getChannelType() {
        return channelType;
    }

    /**
     * Returns a new properties map
     *
     * @return a non-null, possibly empty map of properties
     */
    public Map<@Nullable String, @Nullable String> getProperties() {
        final Map<@Nullable String, @Nullable String> localProp = properties;
        return Collections.unmodifiableMap(localProp == null ? Collections.emptyMap() : localProp);
    }

    /**
     * Returns a new thing state definition
     *
     * @return a possibly null thing state definition
     */
    public @Nullable SonyThingStateDefinition getState() {
        return state;
    }

    @Override
    public String toString() {
        return "SonyThingChannelDefinition [channelId=" + channelId + ", channelType=" + channelType + ", properties="
                + properties + ", state=" + state + "]";
    }
}
