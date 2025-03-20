/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This defines the protocol parameters that can be read and/or written.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum Parameter {
    ROOM_TEMPERATURE_CALCULATED((byte) 0x00, Flag.READ, new byte[] { 0x14, (byte) 0x96 }),
    ROOM_TEMPERATURE((byte) 0x01, Flag.READ, new byte[] { 0x03, 0x00 }),
    BATTERY_LIFE((byte) 0x01, Flag.READ, new byte[] { 0x03, 0x0f }),
    OUTDOOR_TEMPERATURE((byte) 0x01, Flag.READ, new byte[] { 0x03, 0x34 }),
    MODE((byte) 0x01, Flag.READ_WRITE, new byte[] { 0x14, 0x12 }),
    BYPASS((byte) 0x01, Flag.READ_WRITE, new byte[] { 0x14, 0x60 }),
    FILTER_PERIOD((byte) 0x01, Flag.READ_WRITE, new byte[] { 0x14, 0x69 }),
    FILTER_LIFE((byte) 0x01, Flag.READ, new byte[] { 0x14, 0x6a }),
    HUMIDITY((byte) 0x01, Flag.READ, new byte[] { 0x14, 0x70 }),
    BOOST((byte) 0x01, Flag.READ_WRITE, new byte[] { 0x15, 0x30 }),
    MANUAL_FAN_SPEED_STEP((byte) 0x01, Flag.READ_WRITE, new byte[] { 0x15, 0x61 }),
    NIGHT_COOLING((byte) 0x01, Flag.READ_WRITE, new byte[] { 0x15, 0x71 }),
    CURRENT_TIME((byte) 0x01, Flag.READ, new byte[] { 0x15, (byte) 0xe0 }),
    UNIT_NAME((byte) 0x01, Flag.READ, new byte[] { 0x15, (byte) 0xe5 }),
    UNIT_SERIAL((byte) 0x04, Flag.READ, new byte[] { 0x00, 0x25 }),
    SUPPLY_FAN_SPEED((byte) 0x04, Flag.READ, new byte[] { 0x14, 0x50 }),
    EXTRACT_FAN_SPEED((byte) 0x04, Flag.READ, new byte[] { 0x14, 0x51 }),
    SUPPLY_FAN_STEP((byte) 0x04, Flag.READ, new byte[] { 0x14, 0x28 }),
    EXTRACT_FAN_STEP((byte) 0x04, Flag.READ, new byte[] { 0x14, 0x29 }),
    SUPPLY_TEMPERATURE((byte) 0x04, Flag.READ, new byte[] { 0x14, 0x73 }),
    EXTRACT_TEMPERATURE((byte) 0x04, Flag.READ, new byte[] { 0x14, 0x74 }),
    EXHAUST_TEMPERATURE((byte) 0x04, Flag.READ, new byte[] { 0x14, 0x75 });

    private final byte endpoint;
    private final Flag flag;
    private final byte[] register;

    Parameter(byte endpoint, Flag flag, byte[] register) {
        this.endpoint = endpoint;
        this.flag = flag;
        this.register = register;
    }

    public byte[] getRequest() {
        return getRequest(new byte[] {});
    }

    public byte[] getRequest(byte[] value) {
        boolean isWriteOperation = value.length > 0;
        if (isWriteOperation && flag == Flag.READ) {
            throw new IllegalArgumentException("Attempt to write to read-only register");
        }
        byte[] request = new byte[4 + value.length];
        request[0] = endpoint;
        request[1] = (isWriteOperation ? Flag.READ_WRITE : Flag.READ).getValue();
        System.arraycopy(register, 0, request, 2, 2);
        System.arraycopy(value, 0, request, 4, value.length);

        return request;
    }
}
