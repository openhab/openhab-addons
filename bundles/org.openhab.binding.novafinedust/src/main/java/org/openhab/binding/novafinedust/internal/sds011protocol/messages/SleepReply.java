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
package org.openhab.binding.novafinedust.internal.sds011protocol.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Reply from sensor to a set sleep command
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class SleepReply extends SensorReply {

    private final byte actionType;
    private final byte sleep;

    public SleepReply(byte[] bytes) {
        super(bytes);

        this.actionType = bytes[3];
        this.sleep = bytes[4];
    }

    /**
     * Get the type of action
     *
     * @return 0 = query 1 = set mode
     */
    public byte getActionType() {
        return actionType;
    }

    /**
     * Get the info whether this is a sleep or wakeup reply
     *
     * @return 0 = sleep 1 = work
     */
    public byte getSleep() {
        return sleep;
    }

    @Override
    public String toString() {
        return "SleepReply: [sleep=" + sleep + "]";
    }
}
