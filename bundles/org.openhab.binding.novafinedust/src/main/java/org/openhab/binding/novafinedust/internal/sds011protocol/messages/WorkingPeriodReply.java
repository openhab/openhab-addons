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
package org.openhab.binding.novafinedust.internal.sds011protocol.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Reply from sensor to a set working period command
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class WorkingPeriodReply extends SensorReply {

    private final byte actionType;
    private final byte period;

    public WorkingPeriodReply(byte[] bytes) {
        super(bytes);

        this.actionType = bytes[3];
        this.period = bytes[4];
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
     * Get the set working period
     *
     * @return working period set on the sensor
     */
    public byte getPeriod() {
        return period;
    }

    @Override
    public String toString() {
        return "WorkingPeriodReply: [Period=" + this.period + "]";
    }
}
