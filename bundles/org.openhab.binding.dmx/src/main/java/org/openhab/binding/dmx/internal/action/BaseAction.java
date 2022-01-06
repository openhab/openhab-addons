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
package org.openhab.binding.dmx.internal.action;

import org.openhab.binding.dmx.internal.multiverse.DmxChannel;

/**
 * The {@link BaseAction} is the base class for Actions like faders, chasers, etc..
 *
 * @author Davy Vanherbergen - Initial contribution
 * @author Jan N. Klug - Refactoring for ESH
 */
public abstract class BaseAction {

    protected ActionState state = ActionState.WAITING;
    protected long startTime = 0;

    /**
     * Calculate the new output value of the channel.
     *
     * @param channel
     * @param currentTime UNIX timestamp to use as current time
     * @return value as float between 0 - 65535
     */
    public abstract int getNewValue(DmxChannel channel, long currentTime);

    /**
     * @return the action's state
     */
    public final ActionState getState() {
        return state;
    }

    /**
     * Reset the action to start from the beginning.
     */
    public void reset() {
        startTime = 0;
        state = ActionState.WAITING;
    }
}
