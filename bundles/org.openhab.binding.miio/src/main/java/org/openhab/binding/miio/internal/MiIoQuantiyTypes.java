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
package org.openhab.binding.miio.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Enum of the units used in the miio protocol
 * Used to find the right {@link javax.measure.Unit} given the string of the unit
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum MiIoQuantiyTypes {

    CELCIUS(SIUnits.CELSIUS, "C"),
    FAHRENHEIT(ImperialUnits.FAHRENHEIT),
    KELVIN(Units.KELVIN, "K"),
    PASCAL(SIUnits.PASCAL),
    SECOND(Units.SECOND, "seconds"),
    MINUTE(Units.MINUTE, "minutes"),
    HOUR(Units.HOUR, "hours"),
    DAY(Units.DAY, "days"),
    AMPERE(Units.AMPERE),
    VOLT(Units.VOLT),
    WATT(Units.WATT),
    LITRE(Units.LITRE, "liter"),
    LUX(Units.LUX),
    RADIANS(Units.RADIAN, "radians"),
    KILOWATT_HOUR(Units.KILOWATT_HOUR, "kwh"),
    SQUARE_METRE(SIUnits.SQUARE_METRE, "square_meter", "squaremeter"),
    PERCENT(Units.PERCENT),
    KGM3(Units.KILOGRAM_PER_CUBICMETRE, "kilogram_per_cubicmeter"),
    UGM3(Units.MICROGRAM_PER_CUBICMETRE, "microgram_per_cubicmeter"),
    PPM(Units.PARTS_PER_MILLION, "parts_per_million");

    private final Unit<?> unit;
    private final String[] aliasses;

    private static Map<String, Unit<?>> stringMap = Arrays.stream(values())
            .collect(Collectors.toMap(Enum::toString, MiIoQuantiyTypes::getUnit));

    private static Map<String, Unit<?>> aliasMap() {
        Map<String, Unit<?>> aliassesMap = new HashMap<>();
        for (MiIoQuantiyTypes miIoQuantiyType : values()) {
            for (String alias : miIoQuantiyType.getAliasses()) {
                aliassesMap.put(alias.toLowerCase(), miIoQuantiyType.getUnit());
            }
        }
        return aliassesMap;
    }

    private MiIoQuantiyTypes(Unit<?> unit, String... aliasses) {
        this.unit = unit;
        this.aliasses = aliasses;
    }

    public Unit<?> getUnit() {
        return unit;
    }

    public String[] getAliasses() {
        return aliasses;
    }

    public static @Nullable Unit<?> get(String unitName) {
        Unit<?> unit = stringMap.get(unitName.toUpperCase());
        if (unit == null) {
            unit = aliasMap().get(unitName.toLowerCase());
        }
        return unit;
    }
}
