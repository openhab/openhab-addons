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
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.COMMAND_SET_REALTIME_CLOCK;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusSetRealtimeClockPacket} represents a Velbus packet that can be used to
 * set the clock of the given Velbus module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusSetRealtimeClockPacket extends VelbusPacket {
    private ZonedDateTime zonedDateTime;

    public VelbusSetRealtimeClockPacket(byte address, ZonedDateTime zonedDateTime) {
        super(address, PRIO_LOW);

        this.zonedDateTime = zonedDateTime;
    }

    public byte getHour() {
        return (byte) this.zonedDateTime.getHour();
    }

    public byte getMinute() {
        return (byte) this.zonedDateTime.getMinute();
    }

    public byte getWeekDay() {
        DayOfWeek dayOfWeek = zonedDateTime.getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY:
                return 0x00;
            case TUESDAY:
                return 0x01;
            case WEDNESDAY:
                return 0x02;
            case THURSDAY:
                return 0x03;
            case FRIDAY:
                return 0x04;
            case SATURDAY:
                return 0x05;
            case SUNDAY:
                return 0x06;
            default:
                throw new IllegalArgumentException("Day " + dayOfWeek + " is not a valid weekday.");
        }
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_SET_REALTIME_CLOCK, this.getWeekDay(), getHour(), getMinute() };
    }
}
