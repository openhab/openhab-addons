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
 * The {@link VelbusClockAlarm} represents a class that contains the state representation of a velbus clock alarm.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusClockAlarm {
    private boolean enabled;
    private boolean isLocal;
    private byte wakeupHour;
    private byte wakeupMinute;
    private byte bedtimeHour;
    private byte bedtimeMinute;

    public VelbusClockAlarm() {
        this.enabled = true;
        this.isLocal = true;
        this.wakeupHour = 7;
        this.wakeupMinute = 0;
        this.bedtimeHour = 23;
        this.bedtimeMinute = 0;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isLocal() {
        return this.isLocal;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    public void setLocal(boolean value) {
        this.isLocal = value;
    }

    public byte getWakeupHour() {
        return this.wakeupHour;
    }

    public void setWakeupHour(byte value) {
        this.wakeupHour = value;
    }

    public byte getWakeupMinute() {
        return this.wakeupMinute;
    }

    public void setWakeupMinute(byte value) {
        this.wakeupMinute = value;
    }

    public byte getBedtimeHour() {
        return this.bedtimeHour;
    }

    public void setBedtimeHour(byte value) {
        this.bedtimeHour = value;
    }

    public byte getBedtimeMinute() {
        return this.bedtimeMinute;
    }

    public void setBedtimeMinute(byte value) {
        this.bedtimeMinute = value;
    }
}
