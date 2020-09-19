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
package org.openhab.binding.wlanthermo.internal.api.nano;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link UtilNano} class provides conversion functions for the WlanThermo Nano
 *
 * @author Christian Schlipp - Initial contribution
 */
public class UtilNano {

    private static final Map<String, String> COLOR_MAPPINGS = createColorMap();
    private static final String DEFAULT_HEX = "#ffffff";
    private static final String DEFAULT_COLORNAME = "niagara";

    private UtilNano() {
        // hidden
    }

    private static Map<String, String> createColorMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("niagara", "#5587A2");
        map.put("rosa", "#FFAEC9");
        map.put("lapis blue", "#0C4C88");
        map.put("orange", "#EF562D");
        map.put("lila", "#A349A4");
        map.put("red", "#ED1C24");
        map.put("green", "#22B14C");
        map.put("gold", "#FFC100");
        map.put("kale", "#5C7148");
        map.put("brown", "#804000");
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

    public static String toColorName(String colorHex) {
        String colorName = null;
        if (!colorHex.startsWith("#")) {
            colorHex = "#" + colorHex;
        }
        for (Map.Entry<String, String> entry : COLOR_MAPPINGS.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(colorHex)) {
                colorName = entry.getKey();
            }
        }
        if (colorName == null) {
            colorName = DEFAULT_COLORNAME;
        }
        return colorName;
    }
}
