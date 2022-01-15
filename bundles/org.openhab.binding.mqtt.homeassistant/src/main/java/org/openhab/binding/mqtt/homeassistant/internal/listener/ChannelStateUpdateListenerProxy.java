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
package org.openhab.binding.mqtt.homeassistant.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * A proxy class for {@link ChannelStateUpdateListener} forwarding everything to the real listener.
 * <p>
 * This class is used to be able handle special cases like timeouts.
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public abstract class ChannelStateUpdateListenerProxy implements ChannelStateUpdateListener {

    private final ChannelStateUpdateListener original;

    public ChannelStateUpdateListenerProxy(ChannelStateUpdateListener original) {
        this.original = original;
    }

    @Override
    public void updateChannelState(ChannelUID channelUID, State value) {
        original.updateChannelState(channelUID, value);
    }

    @Override
    public void postChannelCommand(ChannelUID channelUID, Command value) {
        original.postChannelCommand(channelUID, value);
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String eventPayload) {
        original.triggerChannel(channelUID, eventPayload);
    }
}
