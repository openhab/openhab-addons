/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.domain;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Aggregation of the interesting parts to write into a channel.
 *
 * From this the {@link ChannelType}s are created.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SolarwattChannel {
    private final String channelName;
    private final @Nullable Unit<?> unit;
    private final String category;
    private final Boolean advanced;

    public SolarwattChannel(String channelName, String category) {
        this(channelName, category, false);
    }

    public SolarwattChannel(String channelName, Unit<?> unit, String category) {
        this(channelName, unit, category, false);
    }

    public SolarwattChannel(String channelName, String category, Boolean advanced) {
        this(channelName, null, category, advanced);
    }

    public SolarwattChannel(String channelName, @Nullable Unit<?> unit, String category, Boolean advanced) {
        this.channelName = channelName;
        this.unit = unit;
        this.category = category;
        this.advanced = advanced;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public @Nullable Unit<?> getUnit() {
        return this.unit;
    }

    public String getCategory() {
        return this.category;
    }

    public Boolean getAdvanced() {
        return this.advanced;
    }
}
