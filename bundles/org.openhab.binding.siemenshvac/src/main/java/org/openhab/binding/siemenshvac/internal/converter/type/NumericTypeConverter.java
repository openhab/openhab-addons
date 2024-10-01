/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.siemenshvac.internal.converter.type;

import java.util.Locale;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.converter.ConverterException;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDataPoint;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

import com.google.gson.JsonElement;

/**
 * Converts between a SiemensHvac datapoint value and an openHAB DecimalType.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class NumericTypeConverter extends AbstractTypeConverter {

    @Override
    protected boolean toBindingValidation(Type type) {
        return true;
    }

    @Override
    protected @Nullable Object toBinding(Type type, ChannelType tp) throws ConverterException {
        Object valUpdate = null;

        if (type instanceof QuantityType quantityType) {
            Number num = (quantityType);
            valUpdate = num.doubleValue();
        } else if (type instanceof DecimalType decimalValue) {
            valUpdate = decimalValue.toString();
        }

        return valUpdate;
    }

    @Override
    protected boolean fromBindingValidation(JsonElement value, String unit, String type) {
        return true;
    }

    @Override
    protected State fromBinding(JsonElement value, String unit, String type, ChannelType tp, Locale locale)
            throws ConverterException {
        if ("----".equals(value.getAsString())) {
            return new DecimalType(0);
        } else {
            double dValue = value.getAsDouble();

            String itemType = tp.getItemType();

            if (itemType != null) {
                if ("Number:Temperature".equals(itemType)) {
                    Unit<Temperature> targetUnit = null;

                    if ("°C".equals(unit)) {
                        targetUnit = SIUnits.CELSIUS;
                    } else if ("°F".equals(unit)) {
                        targetUnit = ImperialUnits.FAHRENHEIT;
                    }

                    if (targetUnit != null) {
                        return new QuantityType<>(dValue, targetUnit);
                    }
                } else if ("Number:ElectricPotential".equals(itemType)) {
                    Unit<ElectricPotential> targetUnit = null;

                    if ("V".equals(unit)) {
                        targetUnit = Units.VOLT;
                    }

                    if (targetUnit != null) {
                        return new QuantityType<>(dValue, targetUnit);
                    }
                } else if ("Number:Time".equals(itemType)) {
                    Unit<Time> targetUnit = null;

                    switch (unit) {
                        case "s":
                        case "sek":
                            targetUnit = Units.SECOND;
                            break;
                        case "m":
                        case "min":
                        case "perc":
                        case "dak":
                        case "мин":
                            targetUnit = Units.MINUTE;
                            break;
                        case "h":
                        case "sa":
                            targetUnit = Units.HOUR;
                            break;
                        case "Months":
                        case "Monate":
                        case "Mois":
                        case "Mesi":
                        case "Maanden":
                        case "mies.":
                        case "Měsíce":
                        case "hónap":
                        case "Meses":
                        case "mdr.":
                        case "Månader":
                        case "kk":
                        case "месяцы":
                        case "Aylar":
                        case "mesiac":
                            targetUnit = Units.MONTH;
                            break;
                        case "d":
                        case "Jours":
                        case "giorni":
                        case "Dny":
                        case "nap":
                        case "dage":
                        case "dag":
                        case "vrk":
                        case "д":
                            targetUnit = Units.DAY;
                            break;
                    }

                    if (targetUnit != null) {
                        return new QuantityType<>(dValue, targetUnit);
                    }
                } else if ("Number:Dimensionless".equals(itemType)) {
                    Unit<Dimensionless> targetUnit = null;

                    if ("%".equals(unit)) {
                        targetUnit = Units.PERCENT;
                    }

                    if (targetUnit != null) {
                        return new QuantityType<>(dValue, targetUnit);
                    }
                } else if ("Number".equals(itemType)) {
                    return new DecimalType(dValue);
                }
            } else {
                return new DecimalType(dValue);
            }

            return new DecimalType(dValue);
        }
    }

    @Override
    public String getChannelType(SiemensHvacMetadataDataPoint dpt) {
        return "number";
    }

    @Override
    public String getItemType(SiemensHvacMetadataDataPoint dpt) {
        String unit = dpt.getDptUnit();

        if (unit == null) {
            return CoreItemFactory.NUMBER;
        }

        if ("".equals(unit)) {
            return CoreItemFactory.NUMBER;
        }

        switch (unit) {
            case "°F":
            case "°C":
                return CoreItemFactory.NUMBER + ":Temperature";
            case "°F*min":
            case "°C*min":
            case "°Cmin":
            case "°Cdak":
                return CoreItemFactory.NUMBER;
            case "V":
                return CoreItemFactory.NUMBER + ":ElectricPotential";
            case "%":
                return CoreItemFactory.NUMBER + ":Dimensionless";
            case "d":
            case "Jours":
            case "giorni":
            case "Dny":
            case "nap":
            case "dage":
            case "dag":
            case "vrk":
            case "д":
            case "h":
            case "sa":
            case "m":
            case "s":
            case "сек":
            case "min":
            case "perc":
            case "мин":
            case "dak":
            case "Months":
            case "Monate":
            case "Mois":
            case "Mesi":
            case "Maanden":
            case "mies.":
            case "Měsíce":
            case "hónap":
            case "Meses":
            case "mdr.":
            case "Månader":
            case "kk":
            case "месяцы":
            case "Aylar":
            case "mesiac":
                return CoreItemFactory.NUMBER + ":Time";
            default:
                return CoreItemFactory.NUMBER;
        }
    }

    @Override
    public boolean hasVariant() {
        return true;
    }
}
