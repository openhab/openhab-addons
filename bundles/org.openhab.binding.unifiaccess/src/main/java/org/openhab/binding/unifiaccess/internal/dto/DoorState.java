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
 * Shared door-related enums used across DTOs.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public final class DoorState {
    private DoorState() {
    }

    /**
     * Lock state. Accepts both "locked"/"unlocked" and "lock"/"unlock" during
     * deserialization.
     */
    public enum LockState {
        @SerializedName(value = "lock", alternate = { "locked" })
        LOCKED,
        @SerializedName(value = "unlock", alternate = { "unlocked" })
        UNLOCKED
    }

    /** Door position status. */
    public enum DoorPosition {
        @SerializedName("open")
        OPEN,
        @SerializedName("close")
        CLOSE
    }

    /**
     * Remain unlock / lock rule type used by both notifications and lock rule APIs.
     */
    public enum DoorLockRuleType {
        @SerializedName("custom")
        CUSTOM,
        @SerializedName("keep_unlock")
        KEEP_UNLOCK,
        @SerializedName("keep_lock")
        KEEP_LOCK,
        @SerializedName("reset")
        RESET,
        @SerializedName("lock_early")
        LOCK_EARLY,
        @SerializedName("lock_now")
        LOCK_NOW,
        @SerializedName("schedule")
        SCHEDULE,
        @SerializedName("unknown")
        UNKNOWN
    }
}
