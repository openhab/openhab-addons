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
import static org.openhab.binding.plugwise.internal.protocol.field.MessageType.CLOCK_SET_REQUEST;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;

/**
 * Sets the clock of the Circle+. Based on what is known about the Plugwise protocol, only the clock of the Circle+ has
 * to be set. The Circle+ sets the clock of all other network nodes.
 *
 * @author Wouter Born, Karel Goderis - Initial contribution
 */
public class ClockSetRequestMessage extends Message {

    private ZonedDateTime utcDateTime;

    public ClockSetRequestMessage(MACAddress macAddress, LocalDateTime localDateTime) {
        super(CLOCK_SET_REQUEST, macAddress);
        // Nodes expect clock info to be in the UTC timezone
        this.utcDateTime = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(UTC);
    }

    @Override
    protected String payloadToHexString() {
        String year = String.format("%02X", utcDateTime.getYear() - 2000);
        String month = String.format("%02X", utcDateTime.getMonthValue());
        String minutes = String.format("%04X",
                (utcDateTime.getDayOfMonth() - 1) * 24 * 60 + (utcDateTime.getHour() * 60) + utcDateTime.getMinute());
        // If we set logaddress to FFFFFFFFF then previous buffered data will be kept by the Circle+
        String logaddress = "FFFFFFFF";
        String hour = String.format("%02X", utcDateTime.getHour());
        String minute = String.format("%02X", utcDateTime.getMinute());
        String second = String.format("%02X", utcDateTime.getSecond());
        // Monday = 0, ... , Sunday = 6
        String dayOfWeek = String.format("%02X", utcDateTime.getDayOfWeek().getValue() - 1);

        return year + month + minutes + logaddress + hour + minute + second + dayOfWeek;
    }
}
