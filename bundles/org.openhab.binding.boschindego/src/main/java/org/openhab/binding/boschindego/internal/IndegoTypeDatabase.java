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
package org.openhab.binding.boschindego.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Translates from tool number to model names.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoTypeDatabase {

    /**
     * Return tool name from tool type number.
     *
     * @see <a href=
     *      "https://www.boschtoolservice.com/gb/en/boschdiy/spareparts/search-results?q=Indego">
     *      https://www.boschtoolservice.com/gb/en/boschdiy/spareparts/search-results?q=Indego</a>
     *
     * @param toolTypeNumber condensed tool type number, e.g. "3600HA2200" rather than "3 600 HA2 200".
     * @return tool type name
     */
    public static String nameFromTypeNumber(String toolTypeNumber) {
        String name = switch (toolTypeNumber) {
            case "3600HA2103" -> "800";
            case "3600HA2104" -> "850";
            case "3600HA2200", "3600HA2201" -> "1300";
            case "3600HA2300" -> "1000 Connect";
            case "3600HA2301" -> "1200 Connect";
            case "3600HA2302" -> "1100 Connect";
            case "3600HA2303" -> "13C";
            case "3600HA2304" -> "10C";
            case "3600HB0000" -> "350";
            case "3600HB0001" -> "400";
            case "3600HB0004" -> "XS 300";
            case "3600HB0006" -> "350";
            case "3600HB0007" -> "400";
            case "3600HB0100" -> "350 Connect";
            case "3600HB0101" -> "400 Connect";
            case "3600HB0102" -> "S+ 350";
            case "3600HB0103" -> "S+ 400";
            case "3600HB0105" -> "S+ 350";
            case "3600HB0106" -> "S+ 400";
            case "3600HB0201" -> "M";
            case "3600HB0202" -> "S 500";
            case "3600HB0203" -> "M 700";
            case "3600HB0301" -> "M+";
            case "3600HB0302" -> "S+ 500";
            case "3600HB0303" -> "M+ 700";
            default -> "";
        };

        return (name.isEmpty() ? "Indego" : "Indego " + name);
    }
}
