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
package org.openhab.binding.panamaxfurman.internal.protocol.event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.protocol.PowerConditionerChannel;
import org.openhab.core.types.State;

/**
 * The event fired when status information is received from the Power Conditioner.
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public class PanamaxFurmanInformationReceivedEvent {
    private final PowerConditionerChannel channel;
    private final @Nullable Integer outletNumber;
    private final @Nullable State state;

    public PanamaxFurmanInformationReceivedEvent(PowerConditionerChannel channel, @Nullable Integer outletNumber,
            @Nullable State state) {
        super();
        this.channel = channel;
        this.outletNumber = outletNumber;
        this.state = state;

        if (state != null && channel.getStateClass() != null && !channel.getStateClass().isInstance(state)) {
            throw new IllegalStateException("Expected state for channel " + getChannelString() + " to be of class "
                    + channel.getStateClass() + " but was " + state.getClass());
        }
    }

    public String getChannelString() {
        return channel.getChannelName(outletNumber);
    }

    public @Nullable State getState() {
        return state;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(channel);
        if (outletNumber != null) {
            buf.append(" #").append(outletNumber);
        }
        buf.append(" ").append(state);
        return buf.toString();
    }
}
