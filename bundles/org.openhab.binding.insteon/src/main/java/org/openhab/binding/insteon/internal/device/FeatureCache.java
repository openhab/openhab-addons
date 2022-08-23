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
package org.openhab.binding.insteon.internal.device;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.core.types.UnDefType;

/**
 * Class that represents a device feature cache
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class FeatureCache {
    private static final String TYPE_SEPARATOR = "@@@";

    private @Nullable String state;
    private @Nullable Double lastMsgValue;

    public FeatureCache(@Nullable String state, @Nullable Double lastMsgValue) {
        this.state = state;
        this.lastMsgValue = lastMsgValue;
    }

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
     * Factory method for creating a FeatureCache from a DeviceFeature
     *
     * @param feature the device feature
     * @return the newly created FeatureCache
     */
    public static FeatureCache create(DeviceFeature feature) {
        String state = feature.getState() instanceof UnDefType ? null
                : feature.getState().getClass().getName() + TYPE_SEPARATOR + feature.getState().toFullString();
        Double lastMsgValue = feature.getLastMsgValue();

        return new FeatureCache(state, lastMsgValue);
    }

    /**
     * Factory method for loading a FeatureCache into a DeviceFeature
     *
     * @param cache the feature cache to load
     * @param feature the device feature
     */
    public static void load(FeatureCache cache, DeviceFeature feature) {
        // set feature state if defined
        State state = cache.getState();
        if (state != null) {
            feature.setState(state);
        }

        // set feature last message value if defined
        Double lastMsgValue = cache.getLastMsgValue();
        if (lastMsgValue != null) {
            feature.setLastMsgValue(lastMsgValue.doubleValue());
        }
    }
}
