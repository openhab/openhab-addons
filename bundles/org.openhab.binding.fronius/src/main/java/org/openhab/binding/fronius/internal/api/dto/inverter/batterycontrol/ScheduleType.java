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
package org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol;

import java.util.Arrays;
import java.util.Locale;

/**
 * Enum for the schedule type of the battery control.
 *
 * @author Florian Hotze - Initial contribution
 */
public enum ScheduleType {
    CHARGE_MIN,
    CHARGE_MAX,
    DISCHARGE_MIN,
    DISCHARGE_MAX;

    /**
     * Like {@link #valueOf(String)}, but case-insensitive and with an error message listing the valid values.
     *
     * @param value the name of the schedule type
     * @return the schedule type
     * @throws IllegalArgumentException when the value is not a valid schedule type
     */
    public static ScheduleType parse(String value) {
        try {
            return valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid schedule type '" + value + "', valid values are: " + Arrays.toString(values()));
        }
    }
}
