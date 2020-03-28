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
package org.openhab.binding.sensibo.internal;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;

/**
 * The {@link SensiboTemperatureUnitParser} parses from Sensibo temperature symbols to Unit<Temperature>
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public final class SensiboTemperatureUnitParser {
    public static Unit<Temperature> parse(@Nullable String symbol) {
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
}
