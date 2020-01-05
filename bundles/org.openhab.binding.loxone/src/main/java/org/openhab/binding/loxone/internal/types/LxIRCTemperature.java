/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.types;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Intelligent Room Controller Temperature.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
@NonNullByDefault
public enum LxIRCTemperature {
    ECONOMY(0),
    COMFORT_HEATING(1),
    COMFORT_COOLING(2),
    EMPTY_HOUSE(3),
    HEAT_PROTECTION(4),
    INCREASED_HEAT(5),
    PARTY(6),
    MANUAL(7),
    UNKNOWN(8);

    private Integer index;

    private LxIRCTemperature(Integer index) {
        this.index = index;
    }

    public static LxIRCTemperature fromIndex(Integer index) {
        for (LxIRCTemperature t : LxIRCTemperature.values()) {
            if (t.index.equals(index)) {
                return t;
            }
        }
        return UNKNOWN;
    }

    public String getLabel() {
        switch (index) {
            case 0:
                return "Economy";
            case 1:
                return "Comfort Heating";
            case 2:
                return "Comfort Cooling";
            case 3:
                return "Empty House";
            case 4:
                return "Heat Protection";
            case 5:
                return "Increased Heat";
            case 6:
                return "Party";
            case 7:
                return "Manual";
            default:
                return "Unknown";
        }
    }

    public State getIndexState() {
        if (this == UNKNOWN) {
            return UnDefType.UNDEF;
        }
        return new DecimalType(index);
    }
}
