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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

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
 */
@NonNullByDefault
public class Constants {
    // For Vehicle Status
    public static final String OK = "Ok";
    public static final String ACTIVE = "Active";
    public static final String NOT_ACTIVE = "Not Active";
    public static final String NO_ENTRIES = "No Entries";
    public static final String OPEN = "Open";
    public static final String INVALID = "Invalid";
    public static final String CLOSED = "Closed";
    public static final String INTERMEDIATE = "Intermediate";
    public static final String UNDEF = UnDefType.UNDEF.toFullString();
    public static final String UTC_APPENDIX = "-01T12:00:00";
    public static final String NULL_DATE = "1900-01-01T00:00:00";
    public static final String NULL_TIME = "00:00";
    public static final int INT_UNDEF = -1;
    public static final Unit<Length> KILOMETRE_UNIT = MetricPrefix.KILO(SIUnits.METRE);

    // Services to query
    public static final String SERVICES_SUPPORTED = "servicesSupported";
    public static final String STATISTICS = "Statistics";
    public static final String LAST_DESTINATIONS = "LastDestinations";

    // Services in Discovery
    public static final String ACTIVATED = "ACTIVATED";
    public static final String SUPPORTED = "SUPPORTED";
    public static final String NOT_SUPPORTED = "NOT_SUPPORTED";

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

    public static final String ANONYMOUS = "Anonymous";
    public static final int MILES_TO_FEET_FACTOR = 5280;
    public static final String EMPTY_JSON = "{}";

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
}
