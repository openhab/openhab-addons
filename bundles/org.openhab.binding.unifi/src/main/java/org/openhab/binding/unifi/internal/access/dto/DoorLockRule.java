/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.access.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Lock rule payload and value object used for both setting and reading lock rules.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DoorLockRule {
    @SerializedName(value = "type", alternate = { "temp_lock_type" })
    public DoorState.DoorLockRuleType type;
    /** minutes, only for type=custom and for setting the rule */
    public Integer interval = 0;
    @SerializedName(value = "until", alternate = { "ended_time", "endtime" })
    public Long until = 0L; // milliseconds since epoch
    /**
     * End of the temporary unlock window, in epoch <em>seconds</em>, as reported by the device's
     * {@code lock_rule} read-back ({@code unlock_period} / {@code end_time_interval}). The setter-side
     * {@link #until} is epoch milliseconds; {@link #endInstantMillis()} reconciles the two.
     */
    @SerializedName(value = "unlock_period", alternate = { "end_time_interval" })
    public Long unlockPeriod = 0L; // seconds since epoch

    public DoorLockRule(DoorState.DoorLockRuleType type, Integer minutes) {
        this.type = type;
        this.interval = minutes;
        this.until = System.currentTimeMillis() + minutes * 60 * 1000;
    }

    public DoorLockRule(DoorState.DoorLockRuleType type) {
        this.type = type;
    }

    public static DoorLockRule keepUnlock() {
        return new DoorLockRule(DoorState.DoorLockRuleType.KEEP_UNLOCK);
    }

    public static DoorLockRule keepLock() {
        return new DoorLockRule(DoorState.DoorLockRuleType.KEEP_LOCK);
    }

    public static DoorLockRule customMinutes(int minutes) {
        return new DoorLockRule(DoorState.DoorLockRuleType.CUSTOM, minutes);
    }

    public static DoorLockRule reset() {
        return new DoorLockRule(DoorState.DoorLockRuleType.RESET);
    }

    public static DoorLockRule lockEarly() {
        return new DoorLockRule(DoorState.DoorLockRuleType.LOCK_EARLY);
    }

    public static DoorLockRule lockNow() {
        return new DoorLockRule(DoorState.DoorLockRuleType.LOCK_NOW);
    }

    /**
     * The end of the unlock window in epoch milliseconds, preferring the device read-back
     * {@code unlock_period} (epoch seconds) and falling back to the setter-side {@code until}
     * (epoch milliseconds). Returns 0 if neither is set.
     */
    public long endInstantMillis() {
        Long period = unlockPeriod;
        if (period != null && period > 0) {
            return period * 1000L;
        }
        Long u = until;
        return u != null ? u : 0L;
    }
}
