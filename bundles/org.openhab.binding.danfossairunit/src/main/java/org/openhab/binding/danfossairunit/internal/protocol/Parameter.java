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
    CCM_SERIAL_NUMBER(Endpoint.ENDPOINT_0, Flag.READ, (short) 22),
    OPERATION_TIME(Endpoint.ENDPOINT_0, Flag.READ, (short) 992),
    ROOM_TEMPERATURE_CALCULATED(Endpoint.ENDPOINT_0, Flag.READ, (short) 5270),
    ROOM_TEMPERATURE(Endpoint.ENDPOINT_1, Flag.READ, (short) 768),
    BATTERY_LIFE(Endpoint.ENDPOINT_1, Flag.READ, (short) 783),
    OUTDOOR_TEMPERATURE(Endpoint.ENDPOINT_1, Flag.READ, (short) 820),
    MODE(Endpoint.ENDPOINT_1, Flag.READ_WRITE, (short) 5138),
    BYPASS(Endpoint.ENDPOINT_1, Flag.READ_WRITE, (short) 5216),
    FILTER_PERIOD(Endpoint.ENDPOINT_1, Flag.READ_WRITE, (short) 5225),
    FILTER_LIFE(Endpoint.ENDPOINT_1, Flag.READ, (short) 5226),
    HUMIDITY(Endpoint.ENDPOINT_1, Flag.READ, (short) 5232),
    BOOST(Endpoint.ENDPOINT_1, Flag.READ_WRITE, (short) 5424),
    MANUAL_FAN_SPEED_STEP(Endpoint.ENDPOINT_1, Flag.READ_WRITE, (short) 5473),
    NIGHT_COOLING(Endpoint.ENDPOINT_1, Flag.READ_WRITE, (short) 5489),
    CURRENT_TIME(Endpoint.ENDPOINT_1, Flag.READ, (short) 5600),
    UNIT_NAME(Endpoint.ENDPOINT_1, Flag.READ, (short) 5605),
    DEFROST_STATUS(Endpoint.ENDPOINT_1, Flag.READ, (short) 5617),
    UNIT_HARDWARE_REVISION(Endpoint.ENDPOINT_4, Flag.READ, (short) 34),
    UNIT_SOFTWARE_REVISION(Endpoint.ENDPOINT_4, Flag.READ, (short) 35),
    UNIT_SERIAL(Endpoint.ENDPOINT_4, Flag.READ, (short) 37),
    POWER_CYCLE_COUNTER(Endpoint.ENDPOINT_4, Flag.READ, (short) 5125),
    SUPPLY_FAN_STEP(Endpoint.ENDPOINT_4, Flag.READ, (short) 5160),
    EXTRACT_FAN_STEP(Endpoint.ENDPOINT_4, Flag.READ, (short) 5161),
    SUPPLY_FAN_SPEED(Endpoint.ENDPOINT_4, Flag.READ, (short) 5200),
    EXTRACT_FAN_SPEED(Endpoint.ENDPOINT_4, Flag.READ, (short) 5201),
    SUPPLY_TEMPERATURE(Endpoint.ENDPOINT_4, Flag.READ, (short) 5235),
    EXTRACT_TEMPERATURE(Endpoint.ENDPOINT_4, Flag.READ, (short) 5236),
    EXHAUST_TEMPERATURE(Endpoint.ENDPOINT_4, Flag.READ, (short) 5237);

    private final Endpoint endpoint;
    private final Flag flag;
    private final byte valueHighByte;
    private final byte valueLowByte;

    Parameter(Endpoint endpoint, Flag flag, short number) {
        this.endpoint = endpoint;
        this.flag = flag;
        this.valueHighByte = (byte) (number >> 8);
        this.valueLowByte = (byte) (number & 0xff);
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
        request[0] = endpoint.getValue();
        request[1] = (isWriteOperation ? Flag.READ_WRITE : Flag.READ).getValue();
        request[2] = valueHighByte;
        request[3] = valueLowByte;
        System.arraycopy(value, 0, request, 4, value.length);

        return request;
    }
}
