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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The {@link Converter} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Converter {
    public static final DateTimeFormatter serviceDateInputPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter serviceDateOutputPattern = DateTimeFormatter.ofPattern("MMM yyyy");

    public static final String SPACE = " ";
    public static final String UNDERLINE = "_";

    private static final DateTimeFormatter inputPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter outputPattern = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static double round(double value) {
        double scale = Math.pow(10, 1);
        return Math.round(value * scale) / scale;
    }

    public static String getLocalDateTime(String input) {
        LocalDateTime ldt = LocalDateTime.parse(input, Converter.inputPattern);
        ZonedDateTime zdtUTC = ldt.atZone(ZoneId.of("UTC"));
        ZonedDateTime zdtLZ = zdtUTC.withZoneSameInstant(ZoneId.systemDefault());
        return zdtLZ.format(Converter.outputPattern);
    }

    public static String toTitleCase(String input) {
        String lower = input.replaceAll(UNDERLINE, SPACE).toLowerCase();
        String[] arr = lower.split(SPACE);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0))).append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
