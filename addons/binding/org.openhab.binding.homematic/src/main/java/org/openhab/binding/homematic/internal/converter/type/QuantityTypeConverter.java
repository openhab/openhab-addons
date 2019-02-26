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
package org.openhab.binding.homematic.internal.converter.type;

import java.math.BigDecimal;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Volume;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.homematic.internal.converter.ConverterException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Converts between a Homematic datapoint value and a {@link DecimalType}.
 *
 * @author Michael Reitler - Initial contribution
 */
public class QuantityTypeConverter extends AbstractTypeConverter<QuantityType<? extends Quantity<?>>> {

    // this literal is required because some gateway types are mixing up encodings in their XML-RPC responses
    private final String UNCORRECT_ENCODED_CELSIUS = "Â°C";

    // "100%" is a commonly used "unit" in datapoints. Generated channel-type is of DecimalType,
    // but clients may define a QuantityType if preferred
    private final String HUNDRED_PERCENT = "100%";

    @Override
    protected boolean toBindingValidation(HmDatapoint dp, Class<? extends Type> typeClass) {
        return dp.isNumberType() && typeClass.isAssignableFrom(QuantityType.class);
    }

    @Override
    protected Object toBinding(QuantityType<? extends Quantity<?>> type, HmDatapoint dp) throws ConverterException {

        if (dp.isIntegerType()) {
            return toUnitFromDatapoint(type, dp).intValue();
        }
        return round(toUnitFromDatapoint(type, dp).doubleValue()).doubleValue();

    }

    private QuantityType<? extends Quantity<?>> toUnitFromDatapoint(QuantityType<? extends Quantity<?>> type,
            HmDatapoint dp) {
        if (dp == null || dp.getUnit() == null || dp.getUnit().isEmpty()) {
            // datapoint is dimensionless, nothing to convert
            return type;
        }

        // convert the given QuantityType to a QuantityType with the unit of the target datapoint
        switch (dp.getUnit()) {
            case "Lux":
                return type.toUnit(SmartHomeUnits.LUX);
            case "degree":
                return type.toUnit(SmartHomeUnits.DEGREE_ANGLE);
            case HUNDRED_PERCENT:
                return type.toUnit(SmartHomeUnits.ONE);
            case UNCORRECT_ENCODED_CELSIUS:
                return type.toUnit(SIUnits.CELSIUS);
            case "dBm":
            case "minutes":
            case "day":
            case "month":
            case "year":
            case "":
                return type;
            default:
                // According to datapoint documentation, the following values are remaining
                // °C, V, %, s, min, mHz, Hz, hPa, km/h, mm, W, m3
                return type.toUnit(dp.getUnit());
        }
    }

    @Override
    protected boolean fromBindingValidation(HmDatapoint dp) {
        return dp.isNumberType() && dp.getValue() instanceof Number;
    }

    @Override
    protected QuantityType<? extends Quantity<?>> fromBinding(HmDatapoint dp) throws ConverterException {
        Number number = null;
        if (dp.isIntegerType()) {
            number = new BigDecimal(((Number) dp.getValue()).intValue());
        } else {
            number = round(((Number) dp.getValue()).doubleValue());
        }

        // create a QuantityType from the datapoint's value based on the datapoint's unit
        String unit = dp.getUnit() != null ? dp.getUnit() : "";
        switch (unit) {
            case UNCORRECT_ENCODED_CELSIUS:
            case "°C":
                return new QuantityType<Temperature>(number, SIUnits.CELSIUS);
            case "V":
                return new QuantityType<ElectricPotential>(number, SmartHomeUnits.VOLT);
            case "%":
                return new QuantityType<Dimensionless>(number, SmartHomeUnits.PERCENT);
            case "mHz":
                return new QuantityType<Frequency>(number, MetricPrefix.MILLI(SmartHomeUnits.HERTZ));
            case "Hz":
                return new QuantityType<Frequency>(number, SmartHomeUnits.HERTZ);
            case "hPa":
                return new QuantityType<Pressure>(number, SIUnits.PASCAL.multiply(2));
            case "Lux":
                return new QuantityType<Illuminance>(number, SmartHomeUnits.LUX);
            case "degree":
                return new QuantityType<Angle>(number, SmartHomeUnits.DEGREE_ANGLE);
            case "km/h":
                return new QuantityType<Speed>(number, SIUnits.KILOMETRE_PER_HOUR);
            case "mm":
                return new QuantityType<Length>(number, MetricPrefix.MILLI(SIUnits.METRE));
            case "W":
                return new QuantityType<Power>(number, SmartHomeUnits.WATT);
            case "Wh":
                return new QuantityType<Energy>(number, SmartHomeUnits.WATT_HOUR);
            case "m3":
                return new QuantityType<Volume>(number, SIUnits.CUBIC_METRE);
            case HUNDRED_PERCENT:
                return new QuantityType<Dimensionless>(number.doubleValue() * 100.0, SmartHomeUnits.PERCENT);
            case "dBm":
            case "s":
            case "min":
            case "minutes":
            case "day":
            case "month":
            case "year":
            case "":
            default:
                return new QuantityType<Dimensionless>(number, SmartHomeUnits.ONE);
        }
    }

    @Override
    protected LogLevel getDefaultLogLevelForTypeConverter() {
        // increase logging verbosity for this type of converter
        return LogLevel.DEBUG;
    }

}
