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
package org.openhab.binding.wled.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WLedHelper} Provides helper classes that are used from multiple classes in the binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WLedHelper {

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
