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
package org.openhab.binding.unifiaccess.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Lock rule payload and value object used for both setting and reading lock rules.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class DoorLockRule {
    public DoorState.DoorLockRuleType type;
    /** minutes, only for type=custom and for setting the rule */
    public Integer interval = 0;
    @SerializedName(value = "until", alternate = { "ended_time", "endtime" })
    public Long until = 0L; // milliseconds since epoch

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
}
