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
package org.openhab.binding.zwavejs.internal.type.capabilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;

/**
 * A class encapsulating the color capability of a light endpoint.
 * Mutable light state is guarded by this instance.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * @author Leo Siepel - Track reported colors per channel
 */
@NonNullByDefault
public class ColorCapability {
    public final Map<ChannelUID, HSBType> cachedColors = new HashMap<>();
    public final Set<ChannelUID> confirmedRgbChannels = new HashSet<>();
    public PercentType cachedBrightness = PercentType.HUNDRED;
    public Number cachedWarmWhite = DecimalType.ZERO;
    public Number cachedColdWhite = DecimalType.ZERO;
    public Set<ChannelUID> colorChannels = new HashSet<>();
    public @Nullable ChannelUID dimmerChannel = null;
    public @Nullable ChannelUID colorTempChannel = null;
    public @Nullable ChannelUID warmWhiteChannel = null;
    public @Nullable ChannelUID coldWhiteChannel = null;

    @Override
    public synchronized String toString() {
        return "ColorCapability [cachedColors=" + cachedColors + ", cachedBrightness=" + cachedBrightness
                + ", cachedWarmWhite=" + cachedWarmWhite + ", cachedColdWhite=" + cachedColdWhite + ", colorChannels="
                + colorChannels + ", dimmerChannel=" + dimmerChannel + ", colorTempChannel=" + colorTempChannel
                + ", warmWhiteChannel=" + warmWhiteChannel + ", coldWhiteChannel=" + coldWhiteChannel + "]";
    }
}
