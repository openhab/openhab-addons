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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.Day;
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

    public static final String ANONYMOUS = "Anonymous";
    public static final int MILES_TO_FEET_FACTOR = 5280;
    public static final String EMPTY_VEHICLES = "{}";

    @SuppressWarnings("serial")
    public static final Map<String, String> DAYS = new HashMap<String, String>() {
        {
            put(Day.MONDAY.name(), "Mon");
            put(Day.TUESDAY.name(), "Tue");
            put(Day.WEDNESDAY.name(), "Wed");
            put(Day.THURSDAY.name(), "Thu");
            put(Day.FRIDAY.name(), "Fri");
            put(Day.SATURDAY.name(), "Sat");
            put(Day.SUNDAY.name(), "Sun");
        }
    };
}
