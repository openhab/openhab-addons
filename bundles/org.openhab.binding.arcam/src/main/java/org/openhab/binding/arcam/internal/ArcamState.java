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
package org.openhab.binding.arcam.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * The {@link ArcamState} class contains the device state as seen by the binding.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamState {

    private Map<String, State> states = new ConcurrentHashMap<>();

    private ArcamStateChangedListener handler;

    public ArcamState(ArcamStateChangedListener handler) {
        this.handler = handler;
    }

    public void setState(String channelId, @Nullable String value) {
        StringType newValue = new StringType(value);
        setState(channelId, newValue);
    }

    public void setState(String channelId, int value) {
        DecimalType newValue = new DecimalType(value);
        setState(channelId, newValue);
    }

    public void setPercentageState(String channelId, int value) {
        PercentType newValue = new PercentType(value);
        setState(channelId, newValue);
    }

    public void setState(String channelId, boolean value) {
        OnOffType newValue = value ? OnOffType.ON : OnOffType.OFF;
        setState(channelId, newValue);
    }

    private synchronized void setState(String channelId, State newValue) {
        State oldValue = states.get(channelId);
        if (newValue.equals(oldValue)) {
            return;
        }

        states.put(channelId, newValue);
        handler.stateChanged(channelId, newValue);
    }

    public @Nullable State getState(String channel) {
        return states.get(channel);
    }
}
