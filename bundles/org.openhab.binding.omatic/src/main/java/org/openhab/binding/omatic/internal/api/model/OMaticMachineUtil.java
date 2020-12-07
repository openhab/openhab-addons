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
package org.openhab.binding.omatic.internal.api.model;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OMaticMachineUtil} Utilities used by OMatic binding
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
@NonNullByDefault
public class OMaticMachineUtil {
    private static final Logger logger = LoggerFactory.getLogger(OMaticMachineUtil.class);

    @SuppressWarnings("null")
    public static long loadPropertyAsLong(Thing thing, String property) {
        String value = thing.getProperties().get(property);
        long lValue = 0;
        try {
            lValue = value == null || value.isEmpty() ? 0 : Long.parseLong(value);
        } catch (Exception x) {
            logger.debug("Exception while loading property: {}", property, x);
        }
        return lValue;
    }

    @SuppressWarnings("null")
    public static double loadPropertyAsDouble(Thing thing, String property) {
        String value = thing.getProperties().get(property);
        double dValue = 0.0;
        try {
            dValue = value == null || value.isEmpty() ? 0.0 : Double.parseDouble(value);
        } catch (Exception x) {
            logger.debug("Exception while restoring property: {}", property, x);
        }
        return dValue;
    }

    public static State getStateFromTimeStr(@Nullable String timeStr) {
        State state = UnDefType.UNDEF;
        if (timeStr != null) {
            state = new StringType(timeStr);
        }
        return state;
    }

    public static State getStateFromDecimalType(long lValue) {
        State state = UnDefType.UNDEF;
        try {
            if (lValue != -1) {
                state = new DecimalType(lValue);
            }
        } catch (Exception x) {
            logger.error("Failed to convert value to decimalType: {}", lValue, x);
        }
        return state;
    }

    public static State getStateFromDecimalType(@Nullable Double dValue) {
        State state = UnDefType.NULL;
        try {
            if (dValue != null) {
                state = new DecimalType(dValue.doubleValue());
            }
        } catch (Exception x) {
            logger.error("Failed to convert value to decimalType: {}", dValue, x);
        }
        return state;
    }

    public static String convertSecondsToTimeString(double d) {
        final long seconds = Math.round(d);
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        String timeString = "";
        if (day > 0) {
            timeString = "" + day + "d";
        }

        if (hours > 0) {
            timeString += " " + hours + "h";
        }

        if (minute > 0) {
            timeString += " " + minute + "m";
        }

        if (second > 0) {
            timeString += " " + second + "s";
        }
        return timeString;
    }
}
