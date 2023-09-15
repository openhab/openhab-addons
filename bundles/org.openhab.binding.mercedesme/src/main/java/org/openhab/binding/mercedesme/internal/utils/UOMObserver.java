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
package org.openhab.binding.mercedesme.internal.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.handler.VehicleHandler;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UOMObserver} holds the necessary values to update a channel state
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
    public static final String CONSUMPTION_LIQUID_MI = "MPGE";
    public static final String CONSUMPTION_GAS_100KM = "KG_PER_100KM";

    private static final Map<String, String> patternMap = new HashMap<String, String>();
    private static final Map<String, Unit> unitMap = new HashMap<String, Unit>();

    private String label = Constants.NOT_SET;

    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    public UOMObserver(String l) {
        if (patternMap.isEmpty()) {
            patternMap.put(LENGTH_KM_UNIT, "%.1f km");
            patternMap.put(LENGTH_MILES_UNIT, "%.1f mi");
            patternMap.put(SPEED_KM_UNIT, "%.0f km/h");
            patternMap.put(SPEED_MILES_UNIT, "%.0f mph");
            patternMap.put(CELSIUS_UNIT, "%.1f °C");
            patternMap.put(FAHRENHEIT_UNIT, "%.1f °F");
            patternMap.put(BAR_UNIT, "%.1f bar");
            patternMap.put(KPA_UNIT, "%.1f kPa");
            patternMap.put(PSI_UNIT, "%.1f psi");
            patternMap.put(CONSUMPTION_ELECTRIC_KM, "km/kWh");
            patternMap.put(CONSUMPTION_ELECTRIC_MI, "m/kWh");
            patternMap.put(CONSUMPTION_ELECTRIC_100KM, "kWh/100km");
            patternMap.put(CONSUMPTION_ELECTRIC_100MI, "kWh/100mi");
            patternMap.put(CONSUMPTION_LIQUID_100KM, "l/100km");
            patternMap.put(CONSUMPTION_LIQUID_MI, "mpge");

            unitMap.put(LENGTH_KM_UNIT, Constants.KILOMETRE_UNIT);
            unitMap.put(LENGTH_MILES_UNIT, ImperialUnits.MILE);
            unitMap.put(SPEED_KM_UNIT, SIUnits.KILOMETRE_PER_HOUR);
            unitMap.put(SPEED_MILES_UNIT, ImperialUnits.MILES_PER_HOUR);
            unitMap.put(CELSIUS_UNIT, SIUnits.CELSIUS);
            unitMap.put(FAHRENHEIT_UNIT, ImperialUnits.FAHRENHEIT);
            unitMap.put(BAR_UNIT, Units.BAR);
            unitMap.put(KPA_UNIT, Constants.KPA_UNIT);
            unitMap.put(PSI_UNIT, ImperialUnits.POUND_FORCE_SQUARE_INCH);
            unitMap.put(CONSUMPTION_ELECTRIC_KM, Constants.KILOWATT_HOUR_UNIT);
            unitMap.put(CONSUMPTION_ELECTRIC_MI, Constants.KILOWATT_HOUR_UNIT);
            unitMap.put(CONSUMPTION_ELECTRIC_100KM, Constants.KILOWATT_HOUR_UNIT);
            unitMap.put(CONSUMPTION_ELECTRIC_100MI, Constants.KILOWATT_HOUR_UNIT);
            unitMap.put(CONSUMPTION_LIQUID_100KM, Units.LITRE);
            unitMap.put(CONSUMPTION_LIQUID_MI, ImperialUnits.GALLON_LIQUID_US);
        }
        if (!patternMap.containsKey(l)) {
            logger.info("No mapping found for {}", l);
        }
        label = l;
    }

    public String getLabel() {
        return label;
    }

    public Optional<Unit> getUnit() {
        Unit u = unitMap.get(label);
        if (u != null) {
            return Optional.of(u);
        } else {
            return Optional.empty();
        }
    }

    @Nullable
    public String getPattern(String group) {
        String pattern = patternMap.get(label);
        if (Constants.GROUP_RANGE.equals(group) && pattern != null) {
            return pattern.replace("1", "0");
        }
        return pattern;
    }

    public boolean equals(UOMObserver compare) {
        return label.equals(compare.getLabel());
    }
}
