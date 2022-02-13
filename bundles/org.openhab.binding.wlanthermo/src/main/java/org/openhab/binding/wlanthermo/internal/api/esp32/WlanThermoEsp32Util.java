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
package org.openhab.binding.wlanthermo.internal.api.esp32;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wlanthermo.internal.WlanThermoUtil;

/**
 * The {@link WlanThermoEsp32Util} class provides conversion functions for the WlanThermo Nano V3
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoEsp32Util extends WlanThermoUtil {

    private static final Map<String, String> COLOR_MAPPINGS = createColorMap();
    private static final String DEFAULT_HEX = "#FFFFFF";
    private static final String DEFAULT_COLORNAME = "undefined";

    private WlanThermoEsp32Util() {
        // hidden
    }

    private static Map<String, String> createColorMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("yellow", "#FFFF00");
        map.put("dark yellow", "#FFC002");
        map.put("green", "#00FF00");
        map.put("white", "#FFFFFF");
        map.put("pink", "#FF1DC4");
        map.put("orange", "#E46C0A");
        map.put("olive", "#C3D69B");
        map.put("light blue", "#0FE6F1");
        map.put("blue", "#0000FF");
        map.put("dark green", "#03A923");
        map.put("brown", "#C84B32");
        map.put("light brown", "#FF9B69");
        map.put("dark blue", "#5082BE");
        map.put("light pink", "#FFB1D0");
        map.put("light green", "#A6EF03");
        map.put("dark pink", "#D42A6B");
        map.put("beige", "#FFDA8F");
        map.put("azure", "#00B0F0");
        map.put("dark olive", "#948A54");
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
        return toColorName(colorHex, COLOR_MAPPINGS, DEFAULT_COLORNAME);
    }
}
