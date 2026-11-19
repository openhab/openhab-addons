/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.hw;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper for HomeWorks device addresses.
 *
 * A processor accepts an address of 3 to 5 numeric elements written with any separator, with optional brackets, and
 * ignores letters and spaces, so {@code 1:1:3:2:4}, {@code 1.1.3.2.4} and {@code [proc 1 : link 1 : mi 3 : module 2 :
 * zone 4]} are the same address. It reports addresses as {@code [01:01:03:02:04]}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
final class HwAddress {
    private static final Pattern NUMBER = Pattern.compile("\\d{1,4}");
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final int MIN_FIELDS = 3;
    private static final int MAX_FIELDS = 5;

    // Links 4 to 6 are the configurable links, which can carry GRAFIK Eye, WPM, keypads and QED shades.
    private static final int FIRST_CONFIGURABLE_LINK = 4;
    private static final int LAST_CONFIGURABLE_LINK = 6;
    // GRAFIK Eye zones are outputs 1 to 8 and WPM zones are outputs 1 to 6.
    private static final int MAX_LIGHTING_OUTPUT_FOUR_FIELDS = 8;

    private HwAddress() {
    }

    /**
     * Returns the address in the form the processor reports it, such as {@code [01:01:03:02:04]}, or the input
     * unchanged if it is not an address of 3 to 5 elements.
     */
    static String normalize(String address) {
        List<Integer> fields = fields(address);
        if (fields.isEmpty()) {
            return address;
        }
        // The processor needs ASCII digits, which the default locale does not always produce
        return fields.stream().map(field -> String.format(Locale.ROOT, "%02d", field))
                .collect(Collectors.joining(":", "[", "]"));
    }

    /**
     * Returns whether a processor can report a lighting level for the address only for a QED shade.
     *
     * Four-field addresses on links 4 to 6 are GRAFIK Eye zones (outputs 1 to 8), WPM zones (outputs 1 to 6) or QED
     * shades (1 to 96 per router). A last element above 8 therefore cannot be a lighting zone. Lower elements are
     * ambiguous, and are not covered here.
     */
    static boolean isQedShade(String address) {
        List<Integer> fields = fields(address);
        if (fields.size() != 4) {
            return false;
        }
        int link = fields.get(1);
        return link >= FIRST_CONFIGURABLE_LINK && link <= LAST_CONFIGURABLE_LINK
                && fields.get(3) > MAX_LIGHTING_OUTPUT_FOUR_FIELDS;
    }

    private static List<Integer> fields(String address) {
        List<Integer> fields = new ArrayList<>();
        Matcher matcher = DIGITS.matcher(address);
        while (matcher.find()) {
            String digits = matcher.group();
            if (!NUMBER.matcher(digits).matches()) {
                return List.of();
            }
            fields.add(Integer.parseInt(digits));
        }
        return fields.size() >= MIN_FIELDS && fields.size() <= MAX_FIELDS ? fields : List.of();
    }
}
