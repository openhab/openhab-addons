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
package org.openhab.binding.mercedesme.internal.utils;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UOMObserver} responsible to identify Unit and StatePattern for a Mercedes VehicleAttribute
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class UOMObserver {
    // Values delivered by Mercedes API
    public static final String LENGTH_KM_UNIT = "KILOMETERS";
    public static final String LENGTH_MILES_UNIT = "MILES";
    public static final String SPEED_KM_UNIT = "KM_PER_HOUR";
    public static final String SPEED_MILES_UNIT = "M_PER_HOUR";
    public static final String CELSIUS_UNIT = "CELSIUS";
    public static final String FAHRENHEIT_UNIT = "FAHRENHEIT";
    public static final String BAR_UNIT = "BAR";
    public static final String PSI_UNIT = "PSI";
    public static final String KPA_UNIT = "KPA";
    public static final String CONSUMPTION_ELECTRIC_KM = "KM_PER_KWH";
    public static final String CONSUMPTION_ELECTRIC_MI = "M_PER_KWH";
    public static final String CONSUMPTION_ELECTRIC_100KM = "KWH_PER_100KM";
    public static final String CONSUMPTION_ELECTRIC_100MI = "KWH_PER_100MI";
    public static final String CONSUMPTION_LIQUID_100KM = "LITER_PER_100KM";
    public static final String CONSUMPTION_LIQUID_KM_PER_LITER = "KM_PER_LITER";
    public static final String CONSUMPTION_LIQUID_MPG_UK = "MPG_UK";
    public static final String CONSUMPTION_LIQUID_MPG_US = "MPG_US";
    public static final String CONSUMPTION_LIQUID_MI = "MPGE";
    public static final String CONSUMPTION_GAS_100KM = "KG_PER_100KM";
    public static final String TIME_US = "TIME_US";
    public static final String TIME_ROW = "TIME_ROW";

    private static final Map<String, String> PATTERN_MAP = new HashMap<>();
    private static final Map<String, Unit<?>> UNIT_MAP = new HashMap<>();

    private String label = Constants.NOT_SET;

    private final Logger logger = LoggerFactory.getLogger(UOMObserver.class);

    public UOMObserver(String unitLabel) {
        if (PATTERN_MAP.isEmpty()) {
            PATTERN_MAP.put(LENGTH_KM_UNIT, "%.1f km");
            PATTERN_MAP.put(LENGTH_MILES_UNIT, "%.1f mi");
            PATTERN_MAP.put(SPEED_KM_UNIT, "%.0f km/h");
            PATTERN_MAP.put(SPEED_MILES_UNIT, "%.0f mph");
            PATTERN_MAP.put(CELSIUS_UNIT, "%.1f °C");
            PATTERN_MAP.put(FAHRENHEIT_UNIT, "%.0f °F");
            PATTERN_MAP.put(BAR_UNIT, "%.1f bar");
            PATTERN_MAP.put(KPA_UNIT, "%.1f kPa");
            PATTERN_MAP.put(PSI_UNIT, "%.1f psi");
            PATTERN_MAP.put(CONSUMPTION_ELECTRIC_KM, "km/kWh");
            PATTERN_MAP.put(CONSUMPTION_ELECTRIC_MI, "m/kWh");
            PATTERN_MAP.put(CONSUMPTION_ELECTRIC_100KM, "kWh/100km");
            PATTERN_MAP.put(CONSUMPTION_ELECTRIC_100MI, "kWh/100mi");
            PATTERN_MAP.put(CONSUMPTION_LIQUID_100KM, "l/100km");
            PATTERN_MAP.put(CONSUMPTION_LIQUID_KM_PER_LITER, "km/l");
            PATTERN_MAP.put(CONSUMPTION_LIQUID_MPG_UK, "mi/g");
            PATTERN_MAP.put(CONSUMPTION_LIQUID_MPG_US, "mi/g");
            PATTERN_MAP.put(CONSUMPTION_LIQUID_MI, "mpge");
            PATTERN_MAP.put(TIME_US, "%1$tA, %1$td.%1$tm. %1$tI:%1$tM %1$Tp");
            PATTERN_MAP.put(TIME_ROW, "%1$tA, %1$td.%1$tm. %1$tH:%1$tM");

            UNIT_MAP.put(LENGTH_KM_UNIT, Constants.KILOMETRE_UNIT);
            UNIT_MAP.put(LENGTH_MILES_UNIT, ImperialUnits.MILE);
            UNIT_MAP.put(SPEED_KM_UNIT, SIUnits.KILOMETRE_PER_HOUR);
            UNIT_MAP.put(SPEED_MILES_UNIT, ImperialUnits.MILES_PER_HOUR);
            UNIT_MAP.put(CELSIUS_UNIT, SIUnits.CELSIUS);
            UNIT_MAP.put(FAHRENHEIT_UNIT, ImperialUnits.FAHRENHEIT);
            UNIT_MAP.put(BAR_UNIT, Units.BAR);
            UNIT_MAP.put(KPA_UNIT, Constants.KPA_UNIT);
            UNIT_MAP.put(PSI_UNIT, ImperialUnits.POUND_FORCE_SQUARE_INCH);
            UNIT_MAP.put(CONSUMPTION_ELECTRIC_KM, Constants.KILOWATT_HOUR_UNIT);
            UNIT_MAP.put(CONSUMPTION_ELECTRIC_MI, Constants.KILOWATT_HOUR_UNIT);
            UNIT_MAP.put(CONSUMPTION_ELECTRIC_100KM, Constants.KILOWATT_HOUR_UNIT);
            UNIT_MAP.put(CONSUMPTION_ELECTRIC_100MI, Constants.KILOWATT_HOUR_UNIT);
            UNIT_MAP.put(CONSUMPTION_LIQUID_100KM, Units.LITRE);
            UNIT_MAP.put(CONSUMPTION_LIQUID_MI, ImperialUnits.GALLON_LIQUID_US);
        }
        if (!PATTERN_MAP.containsKey(unitLabel)) {
            logger.trace("No mapping found for {}", unitLabel);
        }
        label = unitLabel;
    }

    public String getLabel() {
        return label;
    }

    @Nullable
    public Unit<?> getUnit() {
        return UNIT_MAP.get(label);
    }

    @Nullable
    public String getPattern(String group, String channel) {
        String pattern = PATTERN_MAP.get(label);
        if (Constants.GROUP_RANGE.equals(group) && pattern != null) {
            if ("home-distance".equals(channel)) {
                return pattern.replace("1", "3");
            }
            return pattern.replace("1", "0");
        }
        return pattern;
    }

    public boolean equals(UOMObserver compare) {
        return label.equals(compare.getLabel());
    }
}
