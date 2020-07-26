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
package org.openhab.binding.wlanthermo.internal.api.mini.builtin;

/**
 * The {@link UntilMini} class provides conversion functions for the WlanThermo Mini
 *
 * @author Christian Schlipp - Initial contribution
 */
public class UtilMini {
    private UtilMini() {
        //hidden
    }
    
    /**
     * Convert WlanThermo Color Name to Hex
     * @param colorName the WlanThermo color name
     * @return The color as Hey String
     */
    public static String toHex(String colorName) {
        switch (colorName) {
            case "green":
                return "#008000";
            case "red":
                return "#FF0000";
            case "blue":
                return "#0000FF";
            case "olive":
                return "#808000";
            case "magenta":
                return "#FF00FF";
            case "yellow":
                return "#FFFF00";
            case "violet":
                return "#EE82EE";
            case "orange":
                return "#FFA500";
            case "mediumpurple3":
                return "#9370DB";
            case "aquamarine":
                return "#7FFFD4";
            case "brown":
                return "#A52A2A";
            case "plum":
                return "#DDA0DD";
            case "skyblue":
                return "#87CEEB";
            case "orange-red":
                return "#FF4500";
            case "salmon":
                return "#FA8072";
            case "black":
                return "#000000";
            case "dark-grey":
                return "#A9A9A9";
            case "purple":
                return "800080";
            case "turquoise":
                return "#40E0D0";
            case "khaki":
                return "#F0E68C";
            case "dark-violet":
                return "#9400D3";
            case "seagreen":
                return "#2E8B57";
            case "web-blue":
                return "#0080ff";
            case "steelblue":
                return "#4682B4";
            case "gold":
                return "#FFD700";
            case "dark-green":
                return "#006400";
            case "midnight-blue":
                return "#191970";
            case "dark-khaki":
                return "#BDB76B";
            case "dark-olivegreen":
                return "#556B2F";
            case "pink":
                return "#FFC0CB";
            case "chartreuse":
                return "#7FFF00";
            case "gray":
                return "#808080";
            case "slategrey":
                return "#708090";
            default:
                return "#ffffff";
        }
    }
}