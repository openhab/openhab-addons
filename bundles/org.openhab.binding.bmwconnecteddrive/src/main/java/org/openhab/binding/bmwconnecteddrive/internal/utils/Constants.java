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

/**
 * The {@link Constants} General Constant Definitions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Constants {
    // For Vehicle Status
    public static final String OK = "Ok";
    public static final String OPEN = "OPEN";
    public static final String INVALID = "INVALID";
    public static final String CLOSED = "CLOSED";
    public static final String UNKNOWN = "UNKOWN";
    public static final String NO_SERVICE_REQUEST = "No Service Requests";
    public static final String UTC_APPENDIX = "-01T12:00:00";
    public static final String NULL_DATE = "1900-01-01T00:00:00";

    // Services to query
    public static final String SERVICES_SUPPORTED = "Services Supported";
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

    public static final String ANONYMOUS = "ANONYMOUS";
    public static final int MILES_TO_FEET_FACTOR = 5280;
    public static final String EMPTY_VEHICLES = "{}";

    @SuppressWarnings("serial")
    public static final Map<String, String> DAYS = new HashMap<String, String>() {
        {
            put("MONDAY", "Mon");
            put("TUESDAY", "Tue");
            put("WEDNESDAY", "Wed");
            put("THURSDAY", "Thu");
            put("FRIDAY", "Fri");
            put("SATURDAY", "Sat");
            put("SUNDAY", "Sun");
        }
    };
}
