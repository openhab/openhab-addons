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
package org.openhab.binding.sensibo.internal;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;

/**
 * The {@link SensiboTemperatureUnitConverter} converts to/from Sensibo temperature symbols to Unit<Temperature>
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public final class SensiboTemperatureUnitConverter {
    public static Unit<Temperature> parseFromSensiboFormat(@Nullable String symbol) {
        if (symbol == null) {
            symbol = "C";
        }
        switch (symbol) {
            case "C":
                return SIUnits.CELSIUS;
            case "F":
                return ImperialUnits.FAHRENHEIT;
            default:
                throw new IllegalArgumentException("Do not understand temperature unit " + symbol);
        }
    }

    public static String toSensiboFormat(@Nullable Unit<Temperature> unit) {
        if (SIUnits.CELSIUS.equals(unit)) {
            return "C";
        } else if (ImperialUnits.FAHRENHEIT.equals(unit)) {
            return "F";
        } else {
            throw new IllegalArgumentException("Do not understand temperature unit " + unit);
        }
    }
}
