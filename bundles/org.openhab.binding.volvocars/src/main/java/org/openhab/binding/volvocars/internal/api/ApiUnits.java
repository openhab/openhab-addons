/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.volvocars.internal.api;

import java.util.Locale;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Maps the unit names used by the Volvo Cars APIs to units of measurement.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public final class ApiUnits {
    private static final Map<String, Unit<?>> UNITS = Map.ofEntries( //
            Map.entry("km", MetricPrefix.KILO(SIUnits.METRE)), //
            Map.entry("kilometers", MetricPrefix.KILO(SIUnits.METRE)), //
            Map.entry("mi", ImperialUnits.MILE), //
            Map.entry("miles", ImperialUnits.MILE), //
            Map.entry("l", Units.LITRE), //
            Map.entry("liters", Units.LITRE), //
            Map.entry("litres", Units.LITRE), //
            Map.entry("%", Units.PERCENT), //
            Map.entry("percentage", Units.PERCENT), //
            Map.entry("minutes", Units.MINUTE), //
            Map.entry("h", Units.HOUR), //
            Map.entry("hours", Units.HOUR), //
            Map.entry("days", Units.DAY), //
            Map.entry("weeks", Units.WEEK), //
            Map.entry("months", Units.MONTH), //
            Map.entry("km/h", SIUnits.KILOMETRE_PER_HOUR), //
            Map.entry("mph", ImperialUnits.MILES_PER_HOUR), //
            Map.entry("ampere", Units.AMPERE), //
            Map.entry("w", Units.WATT), //
            Map.entry("watts", Units.WATT), //
            Map.entry("kw", MetricPrefix.KILO(Units.WATT)), //
            Map.entry("kilowatts", MetricPrefix.KILO(Units.WATT)), //
            Map.entry("kwh", Units.KILOWATT_HOUR));

    private ApiUnits() {
    }

    public static @Nullable Unit<?> parse(@Nullable String unit) {
        return unit != null ? UNITS.get(unit.trim().toLowerCase(Locale.ROOT)) : null;
    }
}
