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
package org.openhab.binding.enturno.internal.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * EnturNo date utility methods.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DateUtil {
    /**
     * Converts a zoned date time string that lacks a colon in the zone to an ISO-8601 formatted string.
     * 
     * @param dateTimeWithoutColonInZone
     * @return ISO-8601 formatted string
     */
    public static String getIsoDateTime(String dateTimeWithoutColonInZone) {
        ZonedDateTime zonedDateTime = null;
        try {
            zonedDateTime = ZonedDateTime.parse(dateTimeWithoutColonInZone);
        } catch (DateTimeParseException e) {
            // Skip
        }
        try {
            zonedDateTime = ZonedDateTime.parse(dateTimeWithoutColonInZone.replaceAll("(\\d{2})(\\d{2})$", "$1:$2"));
        } catch (DateTimeParseException e) {
            // Skip
        }
        if (zonedDateTime != null) {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime);
        }
        return dateTimeWithoutColonInZone;
    }
}
