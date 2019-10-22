/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class with commonly used methods.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@NonNullByDefault
public final class Util {

    private Util() {
        // Util class.
    }

    public static ZonedDateTime convertZuluTimeStringToDate(String timeString) {
        return ZonedDateTime.parse(timeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Compares two strings.
     *
     * @param string1
     * @param string2
     * @return true, if both strings are equal and not null
     */
    public static boolean equalsIfPresent(@Nullable String string1, @Nullable String string2) {
        return string1 == null ? false : string1.equals(string2);
    }
}
