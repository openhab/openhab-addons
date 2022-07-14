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
package org.openhab.binding.nobohub.internal.model;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.NoboHubBindingConstants;

/**
 * Helper class for converting data to/from Nobø Hub.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public final class ModelHelper {

    /**
     * Converts a String returned form Nobø hub to a normal Java string.
     *
     * @param noboString String where Char 160 (nobr space is used for space)
     * @return String with normal spaces.
     */
    static String toJavaString(final String noboString) {
        return noboString.replace((char) 160, ' ');
    }

    /**
     * Converts a String in java to a string the Nobø hub can understand (fix spaces).
     *
     * @param javaString String to send to Nobø hub
     * @return String with Nobø hub spaces
     */
    static String toHubString(final String javaString) {
        return javaString.replace(' ', (char) 160);
    }

    /**
     * Creates a Java date string from a date string returned from the Nobø Hub.
     *
     * @param noboDateString Date string from Nobø, like '202001221832' or '-1'
     * @return Java date for the returned string (or null if -1 is returned)
     */
    @Nullable
    static LocalDateTime toJavaDate(final String noboDateString) throws NoboDataException {
        if ("-1".equals(noboDateString)) {
            return null;
        }

        try {
            return LocalDateTime.parse(noboDateString, NoboHubBindingConstants.DATE_FORMAT_MINUTES);
        } catch (DateTimeParseException pe) {
            throw new NoboDataException(String.format("Failed parsing string %s", noboDateString), pe);
        }
    }

    static String toHubDateMinutes(final @Nullable LocalDateTime date) {
        if (null == date) {
            return "-1";
        }

        try {
            return date.format(NoboHubBindingConstants.DATE_FORMAT_MINUTES);
        } catch (DateTimeException dte) {
            return "-1";
        }
    }
}
