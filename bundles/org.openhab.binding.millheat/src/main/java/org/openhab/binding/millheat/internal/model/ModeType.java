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
 * A mode a room or house can be in, as reported by the cloud API's lower case mode strings.
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
    UNKNOWN("unknown");

    private final String apiValue;

    ModeType(final String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    /** Unrecognised values map to {@link #UNKNOWN}, so a mode Mill adds later cannot break this. */
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
