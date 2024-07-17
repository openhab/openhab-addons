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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private Optional<UOMObserver> uomObserver = Optional.empty();

    public ChannelStateMap(String ch, String grp, State st) {
        channel = ch;
        group = grp;
        state = st;
    }

    public ChannelStateMap(String ch, String grp, State st, @Nullable UOMObserver uom) {
        channel = ch;
        group = grp;
        state = st;
        if (uom != null) {
            uomObserver = Optional.of(uom);
        }
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

    public boolean hasUomObserver() {
        return !uomObserver.isEmpty();
    }

    public UOMObserver getUomObserver() {
        return uomObserver.get();
    }

    @Override
    public String toString() {
        return group + "#" + channel + " " + state;
    }

    public boolean isValid() {
        return !channel.isEmpty();
    }
}
