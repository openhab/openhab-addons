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

    public synchronized void setState(String channelId, State newValue) {
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
