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
package org.openhab.binding.elerotransmitterstick.internal.stick;

/**
 * @author Volker Bier - Initial contribution
 */
public enum ResponseStatus {
    NO_INFORMATION((byte) 0x00),
    TOP((byte) 0x01),
    BOTTOM((byte) 0x02),
    INTERMEDIATE((byte) 0x03),
    VENTILATION((byte) 0x04),
    BLOCKING((byte) 0x05),
    OVERHEATED((byte) 0x06),
    TIMEOUT((byte) 0x07),
    START_MOVE_UP((byte) 0x08),
    START_MOVE_DOWN((byte) 0x09),
    MOVING_UP((byte) 0x0a),
    MOVING_DOWN((byte) 0x0b),
    STOPPED((byte) 0x0d),
    TOP_TILT((byte) 0x0e),
    BOTTOM_INTERMEDIATE((byte) 0x0f),
    SWITCHED_OFF((byte) 0x10),
    SWITCHED_ON((byte) 0x11);

    public static final byte EASY_CONFIRM = (byte) 0x4B;
    public static final byte EASY_ACK = (byte) 0x4D;

    private byte statusByte;

    private ResponseStatus(byte statusByte) {
        this.statusByte = statusByte;
    }

    public static ResponseStatus getFor(byte statusByte) {
        if (statusByte <= MOVING_DOWN.statusByte) {
            return ResponseStatus.values()[statusByte];
        }
        return ResponseStatus.values()[statusByte - 1];
    }

    public static int getPercentageFor(ResponseStatus status) {
        switch (status) {
            case BOTTOM:
                return 100;
            case NO_INFORMATION:
                return -1;
            case TOP:
                return 0;
            case INTERMEDIATE:
                return 25;
            case VENTILATION:
                return 75;
            default:
                return 50;
        }
    }
}
