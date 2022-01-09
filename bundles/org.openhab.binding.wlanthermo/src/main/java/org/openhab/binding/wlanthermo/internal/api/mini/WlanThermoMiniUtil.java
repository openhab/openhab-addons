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
package org.openhab.binding.wlanthermo.internal.api.mini;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wlanthermo.internal.WlanThermoUtil;

/**
 * The {@link WlanThermoMiniUtil} class provides conversion functions for the WlanThermo Mini
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoMiniUtil extends WlanThermoUtil {
    private static final Map<String, String> COLOR_MAPPINGS = createColorMap();
    private static final String DEFAULT_HEX = "#ffffff";

    private WlanThermoMiniUtil() {
        // hidden
    }

    private static Map<String, String> createColorMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("green", "#008000");
        map.put("red", "#FF0000");
        map.put("blue", "#0000FF");
        map.put("olive", "#808000");
        map.put("magenta", "#FF00FF");
        map.put("yellow", "#FFFF00");
        map.put("violet", "#EE82EE");
        map.put("orange", "#FFA500");
        map.put("mediumpurple3", "#9370DB");
        map.put("aquamarine", "#7FFFD4");
        map.put("brown", "#A52A2A");
        map.put("plum", "#DDA0DD");
        map.put("skyblue", "#87CEEB");
        map.put("orange-red", "#FF4500");
        map.put("salmon", "#FA8072");
        map.put("black", "#000000");
        map.put("dark-grey", "#A9A9A9");
        map.put("purple", "800080");
        map.put("turquoise", "#40E0D0");
        map.put("khaki", "#F0E68C");
        map.put("dark-violet", "#9400D3");
        map.put("seagreen", "#2E8B57");
        map.put("web-blue", "#0080ff");
        map.put("steelblue", "#4682B4");
        map.put("gold", "#FFD700");
        map.put("dark-green", "#006400");
        map.put("midnight-blue", "#191970");
        map.put("dark-khaki", "#BDB76B");
        map.put("dark-olivegreen", "#556B2F");
        map.put("pink", "#FFC0CB");
        map.put("chartreuse", "#7FFF00");
        map.put("gray", "#808080");
        map.put("slategrey", "#708090");
        return map;
    }

    /**
     * Convert WlanThermo Color Name to Hex
     * 
     * @param colorName the WlanThermo color name
     * @return The color as Hex String
     */
    public static String toHex(String colorName) {
        return COLOR_MAPPINGS.getOrDefault(colorName, DEFAULT_HEX);
    }
}
