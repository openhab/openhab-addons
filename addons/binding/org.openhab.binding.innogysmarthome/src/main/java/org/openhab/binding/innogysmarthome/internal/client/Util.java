/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class with commonly used methods.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class Util {

    public static ZonedDateTime convertZuluTimeStringToDate(String timeString) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
        return ZonedDateTime.parse(timeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Compares two strings.
     *
     * @param string1
     * @param string2
     * @return true, if both strings are equal and not null
     */
    public static boolean equalsIfPresent(String string1, String string2) {
        return string1 == null ? false : string1.equals(string2);
    }
}
