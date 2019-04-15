/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openmuc.jsml.EUnit;

/**
 * Converts a {@link EUnit} to an {@link Unit}.
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
                javaUnit = SmartHomeUnits.AMPERE;
                break;
            case AMPERE_HOUR:
                javaUnit = SmartHomeUnits.AMPERE.divide(SmartHomeUnits.HOUR);
                break;
            case AMPERE_PER_METRE:
                javaUnit = SmartHomeUnits.AMPERE.multiply(SIUnits.METRE);
                break;
            case AMPERE_SQUARED_HOURS:
                javaUnit = SmartHomeUnits.AMPERE.pow(2).multiply(SmartHomeUnits.HOUR);
                break;
            case BAR:
                javaUnit = SIUnits.PASCAL.multiply(100000);
                break;
            case COULOMB:
                javaUnit = SmartHomeUnits.COULOMB;
                break;

            case CUBIC_METRE:
            case CUBIC_METRE_CORRECTED:
                javaUnit = SIUnits.CUBIC_METRE;
                break;
            case CUBIC_METRE_PER_DAY:
            case CUBIC_METRE_PER_DAY_CORRECTED:
                javaUnit = SIUnits.CUBIC_METRE.divide(SmartHomeUnits.DAY);
                break;
            case CUBIC_METRE_PER_HOUR:
            case CUBIC_METRE_PER_HOUR_CORRECTED:
                javaUnit = SIUnits.CUBIC_METRE.divide(SmartHomeUnits.HOUR);
                break;

            case DAY:
                javaUnit = SmartHomeUnits.DAY;
                break;
            case DEGREE:
                javaUnit = SmartHomeUnits.DEGREE_ANGLE;
                break;
            case DEGREE_CELSIUS:
                javaUnit = SIUnits.CELSIUS;
                break;

            case FARAD:
                javaUnit = SmartHomeUnits.FARAD;
                break;
            case HENRY:
                javaUnit = SmartHomeUnits.HENRY;
                break;
            case HERTZ:
                javaUnit = SmartHomeUnits.HERTZ;
                break;
            case HOUR:
                javaUnit = SmartHomeUnits.HOUR;
                break;
            case JOULE:
                javaUnit = SmartHomeUnits.JOULE;
                break;
            case JOULE_PER_HOUR:
                javaUnit = SmartHomeUnits.JOULE.divide(SmartHomeUnits.HOUR);
                break;
            case KELVIN:
                javaUnit = SmartHomeUnits.KELVIN;
                break;
            case KILOGRAM:
                javaUnit = SIUnits.KILOGRAM;
            case KILOGRAM_PER_SECOND:
                javaUnit = SIUnits.KILOGRAM.divide(SmartHomeUnits.SECOND);
                break;
            case LITRE:
                javaUnit = SmartHomeUnits.LITRE;
                break;
            case MASS_DENSITY:
                break;
            case METER_CONSTANT_OR_PULSE_VALUE:
                break;
            case METRE:
                javaUnit = SIUnits.METRE;
                break;
            case METRE_PER_SECOND:
                javaUnit = SmartHomeUnits.METRE_PER_SECOND;
                break;
            case MOLE_PERCENT:
                javaUnit = SmartHomeUnits.MOLE;
                break;
            case MONTH:
                javaUnit = SmartHomeUnits.YEAR.divide(12);
                break;
            case NEWTON:
                javaUnit = SmartHomeUnits.NEWTON;
            case NEWTONMETER:
                javaUnit = SmartHomeUnits.NEWTON.multiply(SIUnits.METRE);
                break;
            case OHM:
                javaUnit = SmartHomeUnits.OHM;
                break;
            case OHM_METRE:
                javaUnit = SmartHomeUnits.OHM.multiply(SIUnits.METRE);
                break;
            case PASCAL:
                javaUnit = SIUnits.PASCAL;
                break;
            case PASCAL_SECOND:
                javaUnit = SIUnits.PASCAL.multiply(SmartHomeUnits.SECOND);
                break;
            case PERCENTAGE:
                javaUnit = SmartHomeUnits.PERCENT;
                break;
            case SECOND:
                javaUnit = SmartHomeUnits.SECOND;
                break;
            case TESLA:
                javaUnit = SmartHomeUnits.TESLA;
                break;
            case VAR:
                javaUnit = SmartHomeUnits.WATT.alternate("Var");
                break;
            case VAR_HOUR:
                javaUnit = SmartHomeUnits.WATT.alternate("Var").multiply(SmartHomeUnits.HOUR);
                break;
            case VOLT:
                javaUnit = SmartHomeUnits.VOLT;
                break;
            case VOLT_AMPERE:
                javaUnit = SmartHomeUnits.VOLT.multiply(SmartHomeUnits.AMPERE);
                break;
            case VOLT_AMPERE_HOUR:
                javaUnit = SmartHomeUnits.VOLT.multiply(SmartHomeUnits.AMPERE).multiply(SmartHomeUnits.HOUR);
                break;
            case VOLT_PER_METRE:
                javaUnit = SmartHomeUnits.WATT.divide(SIUnits.METRE);
                break;
            case VOLT_SQUARED_HOURS:
                javaUnit = SmartHomeUnits.VOLT.pow(2).multiply(SmartHomeUnits.HOUR);
                break;
            case WATT:
                javaUnit = SmartHomeUnits.WATT;
                break;
            case WATT_HOUR:
                javaUnit = SmartHomeUnits.WATT.multiply(SmartHomeUnits.HOUR);
                break;
            case WEBER:
                javaUnit = SmartHomeUnits.WEBER;
                break;
            case WEEK:
                javaUnit = SmartHomeUnits.WEEK;
                break;
            case YEAR:
                javaUnit = SmartHomeUnits.YEAR;
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
