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
package org.openhab.binding.fineoffsetweatherstation.internal.domain.response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.MeasureType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 * A certain measured value.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class MeasuredValue {
    private final MeasureType measureType;
    private final String channelPrefix;
    private final @Nullable Integer channelNumber;
    private final @Nullable ChannelTypeUID channelTypeUID;
    private final State state;
    private final String debugName;

    public MeasuredValue(MeasureType measureType, String channelPrefix, @Nullable Integer channelNumber,
            @Nullable ChannelTypeUID channelTypeUID, State state, String debugName) {
        this.measureType = measureType;
        this.channelPrefix = channelPrefix;
        this.channelNumber = channelNumber;
        this.channelTypeUID = channelTypeUID;
        this.state = state;
        this.debugName = debugName;
    }

    public String getChannelId() {
        return channelNumber == null ? channelPrefix : (channelPrefix + "-" + channelNumber);
    }

    public String getChannelPrefix() {
        return channelPrefix;
    }

    public @Nullable Integer getChannelNumber() {
        return channelNumber;
    }

    public @Nullable ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }

    public State getState() {
        return state;
    }

    public String getDebugName() {
        return debugName;
    }

    @Override
    public String toString() {
        return "MeasuredValue{" + "measureType=" + measureType + ", channelId='" + getChannelId() + '\''
                + ", channelTypeUID=" + channelTypeUID + ", state=" + state + ", debugName='" + debugName + '\'' + '}';
    }
}
