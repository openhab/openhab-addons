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
package org.openhab.binding.novafinedust.internal.sds011protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.ModeReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorFirmwareReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorMeasuredDataReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SensorReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.SleepReply;
import org.openhab.binding.novafinedust.internal.sds011protocol.messages.WorkingPeriodReply;

/**
 * Factory for creating the specific reply instances for data received from the sensor
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class ReplyFactory {

    private static final byte COMMAND_REPLY = (byte) 0xC5;
    private static final byte DATA_REPLY = (byte) 0xC0;

    private ReplyFactory() {
    }

    /**
     * Creates the specific reply message according to the commandID and first data byte
     *
     * @param bytes the received message
     * @return a specific instance of a sensor reply message
     */
    public static @Nullable SensorReply create(byte[] bytes) {
        if (bytes.length != 10) {
            return null;
        }

        byte commandID = bytes[1];
        byte firstDataByte = bytes[2];

        if (commandID == COMMAND_REPLY) {
            switch (firstDataByte) {
                case Command.FIRMWARE:
                    return new SensorFirmwareReply(bytes);
                case Command.WORKING_PERIOD:
                    return new WorkingPeriodReply(bytes);
                case Command.MODE:
                    return new ModeReply(bytes);
                case Command.SLEEP:
                    return new SleepReply(bytes);
                default:
                    return new SensorReply(bytes);
            }
        } else if (commandID == DATA_REPLY) {
            return new SensorMeasuredDataReply(bytes);
        }
        return null;
    }
}
