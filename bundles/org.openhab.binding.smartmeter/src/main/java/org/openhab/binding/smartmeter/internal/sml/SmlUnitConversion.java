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
package org.openhab.binding.smartmeter.internal.sml;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openmuc.jsml.EUnit;

/**
 * Converts an {@link EUnit} to a {@link Unit}.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class SmlUnitConversion {

    @SuppressWarnings("unchecked")
    public static @Nullable <Q extends Quantity<Q>> Unit<Q> getUnit(EUnit unit) {
        Unit<?> javaUnit = null;
        switch (unit) {
            case AMPERE:
                javaUnit = Units.AMPERE;
                break;
            case AMPERE_HOUR:
                javaUnit = Units.AMPERE.divide(Units.HOUR);
                break;
            case AMPERE_PER_METRE:
                javaUnit = Units.AMPERE.multiply(SIUnits.METRE);
                break;
            case AMPERE_SQUARED_HOURS:
                javaUnit = Units.AMPERE.pow(2).multiply(Units.HOUR);
                break;
            case BAR:
                javaUnit = SIUnits.PASCAL.multiply(100000);
                break;
            case COULOMB:
                javaUnit = Units.COULOMB;
                break;

            case CUBIC_METRE:
            case CUBIC_METRE_CORRECTED:
                javaUnit = SIUnits.CUBIC_METRE;
                break;
            case CUBIC_METRE_PER_DAY:
            case CUBIC_METRE_PER_DAY_CORRECTED:
                javaUnit = SIUnits.CUBIC_METRE.divide(Units.DAY);
                break;
            case CUBIC_METRE_PER_HOUR:
            case CUBIC_METRE_PER_HOUR_CORRECTED:
                javaUnit = SIUnits.CUBIC_METRE.divide(Units.HOUR);
                break;

            case DAY:
                javaUnit = Units.DAY;
                break;
            case DEGREE:
                javaUnit = Units.DEGREE_ANGLE;
                break;
            case DEGREE_CELSIUS:
                javaUnit = SIUnits.CELSIUS;
                break;

            case FARAD:
                javaUnit = Units.FARAD;
                break;
            case HENRY:
                javaUnit = Units.HENRY;
                break;
            case HERTZ:
                javaUnit = Units.HERTZ;
                break;
            case HOUR:
                javaUnit = Units.HOUR;
                break;
            case JOULE:
                javaUnit = Units.JOULE;
                break;
            case JOULE_PER_HOUR:
                javaUnit = Units.JOULE.divide(Units.HOUR);
                break;
            case KELVIN:
                javaUnit = Units.KELVIN;
                break;
            case KILOGRAM:
                javaUnit = SIUnits.KILOGRAM;
            case KILOGRAM_PER_SECOND:
                javaUnit = SIUnits.KILOGRAM.divide(Units.SECOND);
                break;
            case LITRE:
                javaUnit = Units.LITRE;
                break;
            case MASS_DENSITY:
                break;
            case METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case METRE:
                javaUnit = SIUnits.METRE;
                break;
            case METRE_PER_SECOND:
                javaUnit = Units.METRE_PER_SECOND;
                break;
            case MOLE_PERCENT:
                javaUnit = Units.MOLE;
                break;
            case MONTH:
                javaUnit = Units.YEAR.divide(12);
                break;
            case NEWTON:
                javaUnit = Units.NEWTON;
            case NEWTONMETER:
                javaUnit = Units.NEWTON.multiply(SIUnits.METRE);
                break;
            case OHM:
                javaUnit = Units.OHM;
                break;
            case OHM_METRE:
                javaUnit = Units.OHM.multiply(SIUnits.METRE);
                break;
            case PASCAL:
                javaUnit = SIUnits.PASCAL;
                break;
            case PASCAL_SECOND:
                javaUnit = SIUnits.PASCAL.multiply(Units.SECOND);
                break;
            case PERCENTAGE:
                javaUnit = Units.PERCENT;
                break;
            case SECOND:
                javaUnit = Units.SECOND;
                break;
            case TESLA:
                javaUnit = Units.TESLA;
                break;
            case VAR:
                javaUnit = Units.WATT.alternate("Var");
                break;
            case VAR_HOUR:
                javaUnit = Units.WATT.alternate("Var").multiply(Units.HOUR);
                break;
            case VOLT:
                javaUnit = Units.VOLT;
                break;
            case VOLT_AMPERE:
                javaUnit = Units.VOLT.multiply(Units.AMPERE);
                break;
            case VOLT_AMPERE_HOUR:
                javaUnit = Units.VOLT.multiply(Units.AMPERE).multiply(Units.HOUR);
                break;
            case VOLT_PER_METRE:
                javaUnit = Units.WATT.divide(SIUnits.METRE);
                break;
            case VOLT_SQUARED_HOURS:
                javaUnit = Units.VOLT.pow(2).multiply(Units.HOUR);
                break;
            case WATT:
                javaUnit = Units.WATT;
                break;
            case WATT_HOUR:
                javaUnit = Units.WATT.multiply(Units.HOUR);
                break;
            case WEBER:
                javaUnit = Units.WEBER;
                break;
            case WEEK:
                javaUnit = Units.WEEK;
                break;
            case YEAR:
                javaUnit = Units.YEAR;
                break;

            // not clearly defined yet:
            case VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case ACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case ENERGY_PER_VOLUME:
                break;
            case CALORIFIC_VALUE:
                break;

            // no unit possible:
            case MIN:
            case OTHER_UNIT:
            case RESERVED:
            case COUNT:
            case CURRENCY:
            case EMPTY:
                break;
            default:
                break;
        }
        return (Unit<Q>) javaUnit;
    }
}
