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
package org.openhab.binding.plugwise.internal.protocol;

import static java.time.ZoneOffset.UTC;
import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.REAL_TIME_CLOCK_SET_REQUEST;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Sets the real-time clock value of a Circle+. The Circle+ is the only device that holds a real-time clock value.
 *
 * @author Wouter Born - Initial contribution
 */
public class RealTimeClockSetRequestMessage extends Message {

    private ZonedDateTime utcDateTime;

    public RealTimeClockSetRequestMessage(MACAddress macAddress, LocalDateTime localDateTime) {
        super(REAL_TIME_CLOCK_SET_REQUEST, macAddress);
        this.utcDateTime = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(UTC);
    }

    @Override
    protected String payloadToHexString() {
        // Real-time clock values in the message are decimals and not hexadecimals
        String second = String.format("%02d", utcDateTime.getSecond());
        String minute = String.format("%02d", utcDateTime.getMinute());
        String hour = String.format("%02d", utcDateTime.getHour());
        // Monday = 0, ... , Sunday = 6
        String dayOfWeek = String.format("%02d", utcDateTime.getDayOfWeek().getValue() - 1);
        String day = String.format("%02d", utcDateTime.getDayOfMonth());
        String month = String.format("%02d", utcDateTime.getMonthValue());
        String year = String.format("%02d", utcDateTime.getYear() - 2000);

        return second + minute + hour + dayOfWeek + day + month + year;
    }
}
