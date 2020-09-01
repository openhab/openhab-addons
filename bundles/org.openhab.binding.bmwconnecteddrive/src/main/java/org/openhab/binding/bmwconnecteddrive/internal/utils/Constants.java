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
    public static final String APPENDIX_DAY = "-01"; // needed to complete Service Date
    public static final String MILES_SHORT = "mi";
    public static final String KM_SHORT = "km";

    // Services in Discovery
    public static final String ACTIVATED = "ACTIVATED";
    public static final String SUPPORTED = "SUPPORTED";
    public static final String NOT_SUPPORTED = "NOT_SUPPORTED";

    // General Constants for String concatenation
    public static final String SPACE = " ";
    public static final String UNDERLINE = "_";
    public static final String HYPHEN = " - ";
    public static final String EMPTY = "";

    public static final int MILES_TO_FEET_FACTOR = 5280;
}
