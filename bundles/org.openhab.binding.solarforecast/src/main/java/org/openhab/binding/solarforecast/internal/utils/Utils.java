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
package org.openhab.binding.solarforecast.internal.utils;

import java.time.Instant;
import java.util.TreeMap;

import javax.measure.MetricPrefix;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.TimeSeries.Entry;

/**
 * The {@link Utils} Helpers for Solcast and ForecastSolar
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    public static QuantityType<Energy> getEnergyState(double d) {
        if (d < 0) {
            return QuantityType.valueOf(-1, Units.KILOWATT_HOUR);
        }
        return QuantityType.valueOf(Math.round(d * 1000) / 1000.0, Units.KILOWATT_HOUR);
    }

    public static QuantityType<Power> getPowerState(double d) {
        if (d < 0) {
            return QuantityType.valueOf(-1, MetricPrefix.KILO(Units.WATT));
        }
        return QuantityType.valueOf(Math.round(d * 1000) / 1000.0, MetricPrefix.KILO(Units.WATT));
    }

    public static void addState(TreeMap<Instant, QuantityType<?>> map, Entry entry) {
        Instant timestamp = entry.timestamp();
        QuantityType<?> qt1 = map.get(timestamp);
        if (qt1 != null) {
            QuantityType<?> qt2 = (QuantityType<?>) entry.state();
            map.put(timestamp, QuantityType.valueOf(qt1.doubleValue() + qt2.doubleValue(), qt2.getUnit()));
        } else {
            map.put(timestamp, (QuantityType<?>) entry.state());
        }
    }
}
