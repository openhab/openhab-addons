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
package org.openhab.binding.loqed.internal.api;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Bolt states supported by the LOQED APIs.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public enum BoltState {
    @SerializedName("unknown")
    UNKNOWN("unknown", 0),
    @SerializedName("open")
    OPEN("open", 1),
    @SerializedName("day_lock")
    DAY_LOCK("day_lock", 2),
    @SerializedName("night_lock")
    NIGHT_LOCK("night_lock", 3);

    private final String apiValue;
    private final int localAction;

    BoltState(String apiValue, int localAction) {
        this.apiValue = apiValue;
        this.localAction = localAction;
    }

    /** Returns the value used by the LOQED APIs. */
    public String apiValue() {
        return apiValue;
    }

    /** Returns the numeric action used by signed local commands. */
    public int localAction() {
        return localAction;
    }

    /** Returns the state matching an API value, ignoring case. */
    public static Optional<BoltState> fromApiValue(String value) {
        if ("latch".equalsIgnoreCase(value)) {
            return Optional.of(DAY_LOCK);
        }
        return Arrays.stream(values()).filter(state -> state.apiValue.equalsIgnoreCase(value)).findFirst();
    }

    /** Returns the state matching an API value, or {@link #UNKNOWN} if the value is not recognized. */
    public static BoltState fromApiValueOrUnknown(String value) {
        if ("latch".equalsIgnoreCase(value)) {
            return DAY_LOCK;
        }
        for (BoltState state : values()) {
            if (state.apiValue.equalsIgnoreCase(value)) {
                return state;
            }
        }
        return UNKNOWN;
    }
}
