/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.novafinedust.internal.sds011protocol.WorkMode;

/**
 * Reply from sensor to a set mode command
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class ModeReply extends SensorReply {

    private final byte actionType;
    private final WorkMode mode;

    public ModeReply(byte[] bytes) {
        super(bytes);

        this.actionType = bytes[3];
        if (bytes[4] == (byte) 1) {
            this.mode = WorkMode.POLLING;
        } else {
            this.mode = WorkMode.REPORTING;
        }
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
     * Get the set work mode
     *
     * @return work mode set on the sensor
     */
    public WorkMode getMode() {
        return mode;
    }

    @Override
    public String toString() {
        return "ModeReply: [mode=" + mode + "]";
    }
}
