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
package org.openhab.binding.mercedesme.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * The {@link ChannelStateMap} holds the necessary values to update a channel state
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ChannelStateMap {
    private String channel;
    private String group;
    private State state;
    private long timestamp;

    public ChannelStateMap(String ch, String grp, State st, long ts) {
        channel = ch;
        group = grp;
        state = st;
        timestamp = ts;
    }

    public String getChannel() {
        return channel;
    }

    public String getGroup() {
        return group;
    }

    public State getState() {
        return state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return group + ":" + channel + " " + state;
    }

    public boolean isValid() {
        return !channel.isEmpty();
    }
}
