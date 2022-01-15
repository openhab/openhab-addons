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
package org.openhab.binding.miio.internal;

import static org.openhab.core.library.unit.MetricPrefix.*;

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

    CELSIUS(SIUnits.CELSIUS, "C", "celcius"),
    FAHRENHEIT(ImperialUnits.FAHRENHEIT),
    KELVIN(Units.KELVIN, "K"),
    PASCAL(SIUnits.PASCAL, "pa"),
    HPA(HECTO(SIUnits.PASCAL)),
    SECOND(Units.SECOND, "seconds"),
    MINUTE(Units.MINUTE, "minutes"),
    HOUR(Units.HOUR, "hours"),
    DAY(Units.DAY, "days"),
    AMPERE(Units.AMPERE),
    MILLI_AMPERE(MILLI(Units.AMPERE), "mA"),
    VOLT(Units.VOLT),
    MILLI_VOLT(MILLI(Units.VOLT), "mV"),
    WATT(Units.WATT, "W", "w"),
    LITRE(Units.LITRE, "liter"),
    LUX(Units.LUX),
    RADIANS(Units.RADIAN, "radians"),
    DEGREE(Units.DEGREE_ANGLE, "degree"),
    KILOWATT_HOUR(Units.KILOWATT_HOUR, "kwh", "kWH"),
    SQUARE_METRE(SIUnits.SQUARE_METRE, "square_meter", "squaremeter"),
    PERCENT(Units.PERCENT, "percentage"),
    KGM3(Units.KILOGRAM_PER_CUBICMETRE, "kilogram_per_cubicmeter"),
    UGM3(Units.MICROGRAM_PER_CUBICMETRE, "microgram_per_cubicmeter", "μg/m3"),
    M3(SIUnits.CUBIC_METRE, "cubic_meter", "cubic_metre"),
    LITER(Units.LITRE, "L", "litre"),
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
