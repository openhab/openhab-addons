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
package org.openhab.binding.mybmw.internal.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.UnDefType;

/**
 * The {@link Constants} General Constant Definitions
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - contributor
 * @author Martin Grassl - rename drivetrain options
 */
@NonNullByDefault
public class Constants {
    // For Vehicle Status
    public static final String NO_ENTRIES = "-";
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";
    public static final String LOCKED = "Locked";
    public static final String UNLOCKED = "Unlocked";
    public static final String CONNECTED = "Connected";
    public static final String UNCONNECTED = "Not connected";
    public static final String UNDEF = UnDefType.UNDEF.toFullString();
    public static final String NULL_TIME = "00:00";
    public static final String KILOMETERS_JSON = "KILOMETERS";
    public static final String KM_JSON = "km";
    public static final String MI_JSON = "mi";
    public static final String UNIT_PRECENT_JSON = "%";
    public static final String UNIT_LITER_JSON = "l";
    public static final Unit<Length> KILOMETRE_UNIT = MetricPrefix.KILO(SIUnits.METRE);
    public static final int INT_UNDEF = -1;

    // Services in Discovery
    public static final String ENABLED = "ENABLED";

    // General Constants for String concatenation
    public static final String NULL = "null";
    public static final String SPACE = " ";
    public static final String UNDERLINE = "_";
    public static final String HYPHEN = " - ";
    public static final String PLUS = "+";
    public static final String EMPTY = "";
    public static final String COMMA = ",";
    public static final String QUESTION = "?";
    public static final String COLON = ":";
    public static final String SEMICOLON = ";";
    public static final String TILDE = "~";

    public static final String ANONYMOUS = "anonymous";
    public static final String EMPTY_JSON = "{}";
    public static final String LANGUAGE_AUTODETECT = "AUTODETECT";

    // Time Constants for DateTime channels
    public static final LocalDate EPOCH_DAY = LocalDate.ofEpochDay(0);
    public static final DateTimeFormatter TIME_FORMATER = DateTimeFormatter.ofPattern("HH:mm");
    public static final LocalTime NULL_LOCAL_TIME = LocalTime.parse(NULL_TIME, TIME_FORMATER);

    @SuppressWarnings("serial")
    public static final Map<DayOfWeek, String> DAYS = new HashMap<>() {
        {
            put(DayOfWeek.MONDAY, "Mon");
            put(DayOfWeek.TUESDAY, "Tue");
            put(DayOfWeek.WEDNESDAY, "Wed");
            put(DayOfWeek.THURSDAY, "Thu");
            put(DayOfWeek.FRIDAY, "Fri");
            put(DayOfWeek.SATURDAY, "Sat");
            put(DayOfWeek.SUNDAY, "Sun");
        }
    };

    // Drive Train definitions from json
    public static final String DRIVETRAIN_BEV = "ELECTRIC";
    public static final String DRIVETRAIN_REX_EXTENSION = "(+ REX)";
    public static final String DRIVETRAIN_MILD_HYBRID = "MILD_HYBRID";
    public static final String DRIVETRAIN_CONV = "COMBUSTION";
    public static final String DRIVETRAIN_PHEV = "PLUGIN_HYBRID";

    // Carging States
    public static final String DEFAULT = "DEFAULT";
    public static final String NOT_CHARGING_STATE = "NOT_CHARGING";
    public static final String CHARGING_STATE = "CHARGING";
    public static final String PLUGGED_STATE = "PLUGGED_IN";
}
