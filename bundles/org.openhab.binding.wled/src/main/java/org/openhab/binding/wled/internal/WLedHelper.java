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
package org.openhab.binding.wled.internal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;

/**
 * The {@link WLedHelper} Provides helper classes that are used from multiple classes in the binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WLedHelper {
    public static HSBType parseToHSBType(String message) {
        // example message rgb in array brackets [255.0, 255.0, 255.0]
        List<String> colors = Arrays.asList(message.replaceAll("\\[|\\]", "").split("\\s*,\\s*"));
        try {
            int r = new BigDecimal(colors.get(0)).intValue();
            int g = new BigDecimal(colors.get(1)).intValue();
            int b = new BigDecimal(colors.get(2)).intValue();
            return HSBType.fromRGB(r, g, b);
        } catch (NumberFormatException e) {
            return new HSBType();
        }
    }

    public static PercentType parseWhitePercent(String message) {
        // example message rgb in array brackets [255.0, 255.0, 255.0, 255.0]
        List<String> colors = Arrays.asList(message.replaceAll("\\[|\\]", "").split("\\s*,\\s*"));
        try {
            return new PercentType(new BigDecimal(colors.get(2)));
        } catch (IllegalArgumentException e) {
            return new PercentType();
        }
    }

    /**
     * @return A string that starts after finding the element and terminates when it finds the first occurrence of the
     *         end string after the element.
     */
    static String getValue(String message, String element, String end) {
        int startIndex = message.indexOf(element);
        if (startIndex != -1) // -1 means "not found"
        {
            int endIndex = message.indexOf(end, startIndex + element.length());
            if (endIndex != -1) {
                return message.substring(startIndex + element.length(), endIndex);
            }
        }
        return "";
    }
}
