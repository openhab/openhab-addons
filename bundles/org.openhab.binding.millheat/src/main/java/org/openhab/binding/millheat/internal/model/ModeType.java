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
package org.openhab.binding.millheat.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A mode a room or house can be in. The cloud API reports these as lower case strings; the old
 * service used small integers, so the numeric mapping is gone.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Map to the cloud API's string values
 */
@NonNullByDefault
public enum ModeType {
    WEEKLY_PROGRAM("weekly_program"),
    COMFORT("comfort"),
    SLEEP("sleep"),
    AWAY("away"),
    VACATION("vacation"),
    NORMAL("normal"),
    ALWAYS_HEATING("always_heating"),
    OFF("off"),
    /** Reported by neither the API nor the binding; used when a mode string is absent or unknown. */
    UNKNOWN("unknown");

    private final String apiValue;

    ModeType(final String apiValue) {
        this.apiValue = apiValue;
    }

    /** The string this mode is called in the cloud API. */
    public String getApiValue() {
        return apiValue;
    }

    /**
     * Resolves an API mode string. Unrecognised and missing values map to {@link #UNKNOWN} rather
     * than failing, so a new mode added by Mill does not break the binding.
     */
    public static ModeType fromApiValue(final @Nullable String value) {
        if (value != null) {
            for (final ModeType mode : values()) {
                if (mode.apiValue.equals(value)) {
                    return mode;
                }
            }
        }
        return UNKNOWN;
    }
}
