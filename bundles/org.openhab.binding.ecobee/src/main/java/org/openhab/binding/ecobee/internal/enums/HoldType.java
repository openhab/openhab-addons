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
package org.openhab.binding.ecobee.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HoldType} represents the possible hold types.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapt for OH2/3
 */
@NonNullByDefault
public enum HoldType {

    /**
     * Use the provided startDate, startTime, endDate and endTime for the event.
     * If start date/time is not provided, it will be assumed to be right now.
     * End date/time is required.
     */
    @SerializedName("dateTime")
    DATE_TIME("dateTime"),

    /**
     * The end date/time will be set to the next climate transition in the program.
     */
    @SerializedName("nextTransition")
    NEXT_TRANSITION("nextTransition"),

    /**
     * The hold will not end and require to be cancelled explicitly.
     */
    @SerializedName("indefinite")
    INDEFINITE("indefinite"),

    /**
     * Use the value in the holdHours parameter to set the end date/time for the event.
     */
    @SerializedName("holdHours")
    HOLD_HOURS("holdHours");

    private final String type;

    private HoldType(final String type) {
        this.type = type;
    }

    public static HoldType forValue(String v) {
        for (HoldType ht : HoldType.values()) {
            if (ht.type.equals(v)) {
                return ht;
            }
        }
        throw new IllegalArgumentException("Invalid hold type: " + v);
    }

    @Override
    public String toString() {
        return this.type;
    }
}
