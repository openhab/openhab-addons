/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.velbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusClockAlarmConfiguration} represents a class that contains the configuration of a velbus clock alarm.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusClockAlarmConfiguration {
    private VelbusClockAlarm alarmClock1;
    private VelbusClockAlarm alarmClock2;

    public VelbusClockAlarmConfiguration() {
        this.alarmClock1 = new VelbusClockAlarm();
        this.alarmClock2 = new VelbusClockAlarm();
    }

    public VelbusClockAlarm getAlarmClock1() {
        return alarmClock1;
    }

    public VelbusClockAlarm getAlarmClock2() {
        return alarmClock2;
    }

    public VelbusClockAlarm getAlarmClock(int alarmNumber) {
        if (alarmNumber == 1) {
            return getAlarmClock1();
        } else if (alarmNumber == 2) {
            return getAlarmClock2();
        } else {
            throw new IllegalArgumentException("The alarm clock '" + alarmNumber + "' does not exist.");
        }
    }
}
