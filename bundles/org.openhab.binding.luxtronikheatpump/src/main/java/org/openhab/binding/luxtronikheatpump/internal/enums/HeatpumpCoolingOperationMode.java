/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.luxtronikheatpump.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.luxtronikheatpump.internal.exceptions.InvalidOperationModeException;

/**
 * Represents all heat pump cooling operation modes
 *
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
public enum HeatpumpCoolingOperationMode {
    AUTOMATIC(1),
    OFF(0);

    private int value;

    private HeatpumpCoolingOperationMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HeatpumpCoolingOperationMode fromValue(int value) throws InvalidOperationModeException {
        for (HeatpumpCoolingOperationMode mode : HeatpumpCoolingOperationMode.values()) {
            if (mode.value == value) {
                return mode;
            }
        }

        throw new InvalidOperationModeException("Invalid heat pump cooling operation mode: '" + value + "'");
    }
}
