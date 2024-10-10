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
package org.openhab.binding.insteon.internal.device.feature;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.core.types.UnDefType;

/**
 * The {@link FeatureCache} represents a device feature cache
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class FeatureCache {
    private static final String TYPE_SEPARATOR = "@@@";

    private @Nullable String state;
    private @Nullable Double lastMsgValue;

    public @Nullable State getState() {
        String state = this.state;
        if (state == null) {
            return null;
        }
        String[] parts = state.split(TYPE_SEPARATOR, 2);
        if (parts.length != 2) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<? extends State> type = (Class<? extends State>) Class.forName(parts[0]);
            return TypeParser.parseState(List.of(type), parts[1]);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public @Nullable Double getLastMsgValue() {
        return lastMsgValue;
    }

    /**
     * Loads this feature cache into a device feature
     *
     * @param feature the device feature to use
     */
    public void load(DeviceFeature feature) {
        // set feature state if defined
        State state = getState();
        if (state != null) {
            feature.setState(state);
        }

        // set feature last message value if defined
        Double lastMsgValue = getLastMsgValue();
        if (lastMsgValue != null) {
            feature.setLastMsgValue(lastMsgValue.doubleValue());
        }
    }

    /**
     * Class that represents a feature cache builder
     */
    public static class Builder {
        private final FeatureCache cache = new FeatureCache();

        private Builder() {
        }

        public Builder withState(State state) {
            cache.state = state instanceof UnDefType ? null
                    : state.getClass().getName() + TYPE_SEPARATOR + state.toFullString();
            return this;
        }

        public Builder withLastMsgValue(@Nullable Double lastMsgValue) {
            cache.lastMsgValue = lastMsgValue;
            return this;
        }

        public FeatureCache build() {
            return cache;
        }
    }

    /**
     * Factory method for creating a feature cache builder
     *
     * @return the newly created feature cache builder
     */
    public static Builder builder() {
        return new Builder();
    }
}
