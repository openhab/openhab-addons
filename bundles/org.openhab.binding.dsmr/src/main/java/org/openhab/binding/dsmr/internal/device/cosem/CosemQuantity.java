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
package org.openhab.binding.dsmr.internal.device.cosem;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * {@link CosemQuantity} represents a value with a unit.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 *
 * @param <Q> The {@link Quantity} type of the unit of this class
 */
@NonNullByDefault
class CosemQuantity<Q extends @Nullable Quantity<Q>> extends CosemValueDescriptor<QuantityType<Q>> {

    public static final CosemQuantity<ElectricCurrent> AMPERE = new CosemQuantity<>(Units.AMPERE);
    public static final CosemQuantity<Volume> CUBIC_METRE = new CosemQuantity<>(SIUnits.CUBIC_METRE);
    public static final CosemQuantity<Energy> GIGA_JOULE = new CosemQuantity<>(MetricPrefix.GIGA(Units.JOULE));
    public static final CosemQuantity<Power> KILO_WATT = new CosemQuantity<>(MetricPrefix.KILO(Units.WATT));
    public static final CosemQuantity<Energy> KILO_WATT_HOUR = new CosemQuantity<>(Units.KILOWATT_HOUR);
    public static final CosemQuantity<ElectricPotential> VOLT = new CosemQuantity<>(Units.VOLT);
    public static final CosemQuantity<Power> WATT = new CosemQuantity<>(Units.WATT);
    public static final CosemQuantity<Power> KILO_VAR = new CosemQuantity<>(Units.KILOVAR);
    public static final CosemQuantity<Energy> KILO_VAR_HOUR = new CosemQuantity<>(Units.KILOVAR_HOUR);
    public static final CosemQuantity<Power> KILO_VA = new CosemQuantity<>(MetricPrefix.KILO(Units.VOLT_AMPERE));

    /**
     * Pattern to convert a cosem value to a value that can be parsed by {@link QuantityType}.
     * The specification states that the delimiter between the value and the unit is a '*'-character.
     * We have seen on the Kaifa 0025 meter that both '*' and the '_' character are used.
     *
     * On the Kampstrup 162JxC in some CosemValues the separator is missing
     *
     * The above quirks are supported
     *
     * We also support unit that do not follow the exact case.
     */
    private static final Pattern COSEM_VALUE_WITH_UNIT_PATTERN = Pattern.compile("^([\\d\\.]+)[\\*_]?(.+)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Unit of this CosemValue
     */
    private final Unit<Q> unit;

    /**
     * Creates a new {@link CosemDouble}.
     *
     * @param unit the unit of the value
     */
    private CosemQuantity(Unit<Q> unit) {
        this(unit, "");
    }

    /**
     * Constructor.
     *
     * @param unit Unit of this CosemQuantity instance
     * @param channelId the channel for this CosemValueDescriptor
     */
    public CosemQuantity(Unit<Q> unit, String channelId) {
        super(channelId);
        this.unit = unit;
    }

    /**
     * Parses a String value (that represents a value with a unit) to a {@link QuantityType} object.
     *
     * @param cosemValue the value to parse
     * @return {@link QuanitytType} on success
     * @throws ParseException in case unit doesn't match.
     */
    @Override
    protected QuantityType<Q> getStateValue(String cosemValue) throws ParseException {
        try {
            final QuantityType<Q> it = new QuantityType<>(prepare(cosemValue));
            final @Nullable QuantityType<Q> qt = it.toUnit(unit);

            if (qt == null) {
                throw new ParseException("Failed to parse value '" + cosemValue + "' as unit " + unit, 0);
            }
            return qt;
        } catch (final IllegalArgumentException nfe) {
            throw new ParseException("Failed to parse value '" + cosemValue + "' as unit " + unit, 0);
        }
    }

    /**
     * Check if COSEM value has a unit, check and parse the value. We assume here numbers (float or integers)
     * The specification states that the delimiter between the value and the unit is a '*'-character.
     * We have seen on the Kaifa 0025 meter that both '*' and the '_' character are used.
     *
     * On the Kampstrup 162JxC in some CosemValues the separator is missing. This
     *
     * The above quirks are supported
     *
     * We also support unit that do not follow the exact case.
     */
    private String prepare(String cosemValue) {
        final Matcher matcher = COSEM_VALUE_WITH_UNIT_PATTERN.matcher(cosemValue.replace("m3", "m³"));
        if (!matcher.find()) {
            return cosemValue;
        }

        try {
            Integer.parseInt(matcher.group(2));
            return cosemValue;
        } catch (final NumberFormatException e) {
            return matcher.group(1) + ' ' + matcher.group(2);
        }
    }
}
