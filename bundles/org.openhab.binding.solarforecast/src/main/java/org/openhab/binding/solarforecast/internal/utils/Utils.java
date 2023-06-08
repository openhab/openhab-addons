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
package org.openhab.binding.solarforecast.internal.utils;

import java.time.Instant;
import java.time.ZonedDateTime;

import javax.measure.MetricPrefix;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link Utils} Helpers for Solcast and ForecastSolar
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    public static State getEnergyState(double d) {
        if (d < 0) {
            return UnDefType.UNDEF;
        } else {
            return QuantityType.valueOf(Math.round(d * 1000) / 1000.0, Units.KILOWATT_HOUR);
        }
    }

    public static State getPowerState(double d) {
        if (d < 0) {
            return UnDefType.UNDEF;
        } else {
            return QuantityType.valueOf(Math.round(d * 1000) / 1000.0, MetricPrefix.KILO(Units.WATT));
        }
    }

    /**
     * Get time frames in 15 minutes intervals
     *
     * @return
     */
    public static Instant getNextTimeframe(Instant timeStamp, TimeZoneProvider tzp) {
        ZonedDateTime now = timeStamp.atZone(tzp.getTimeZone());
        ZonedDateTime nextTime;
        int quarter = now.getMinute() / 15;
        switch (quarter) {
            case 0:
                nextTime = now.withMinute(15).withSecond(0).withNano(0);
                break;
            case 1:
                nextTime = now.withMinute(30).withSecond(0).withNano(0);
                break;
            case 2:
                nextTime = now.withMinute(45).withSecond(0).withNano(0);
                break;
            case 3:
                nextTime = now.withMinute(0).withSecond(0).withNano(0).plusHours(1);
                break;
            default:
                nextTime = now;
                break;
        }
        return nextTime.toInstant();
    }

    public static ZonedDateTime getZdtFromUTC(String utc, TimeZoneProvider tzp) {
        Instant timestamp = Instant.parse(utc);
        return timestamp.atZone(tzp.getTimeZone());
    }
}
