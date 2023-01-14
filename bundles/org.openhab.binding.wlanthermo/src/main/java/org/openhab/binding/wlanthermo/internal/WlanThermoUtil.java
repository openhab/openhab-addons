/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.wlanthermo.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.HSBType;

/**
 * The {@link WlanThermoUtil} class provides conversion functions for the WlanThermo
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoUtil {

    public static String toColorName(String colorHex, Map<String, String> colorMappings, String defaultColorName) {
        if (!colorHex.startsWith("#")) {
            colorHex = "#" + colorHex;
        }

        for (Map.Entry<String, String> entry : colorMappings.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(colorHex)) {
                return entry.getKey();
            }
        }

        return defaultColorName;
    }

    public static String toHex(HSBType hsb) {
        return "#" + String.format("%02X", hsb.getRed().intValue()) + String.format("%02X", hsb.getGreen().intValue())
                + String.format("%02X", hsb.getBlue().intValue());
    }

    public static <T> T requireNonNull(@Nullable T obj) throws WlanThermoInputException {
        if (obj == null) {
            throw new WlanThermoInputException();
        }
        return obj;
    }
}
