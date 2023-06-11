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
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.COMMAND_SET_ALARM_CLOCK;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusClockAlarm;

/**
 * The {@link VelbusSetLocalClockAlarmPacket} represents a Velbus packet that can be used to
 * set the value of the local clock alarm of the given Velbus module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusSetLocalClockAlarmPacket extends VelbusPacket {
    private byte alarmNumber;
    private VelbusClockAlarm alarmClock;

    public VelbusSetLocalClockAlarmPacket(byte address, byte alarmNumber, VelbusClockAlarm alarmClock) {
        super(address, PRIO_LOW);

        this.alarmNumber = alarmNumber;
        this.alarmClock = alarmClock;
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_SET_ALARM_CLOCK, alarmNumber, alarmClock.getWakeupHour(),
                alarmClock.getWakeupMinute(), alarmClock.getBedtimeHour(), alarmClock.getBedtimeMinute(),
                alarmClock.isEnabled() ? (byte) 0x01 : (byte) 0x00 };
    }
}
