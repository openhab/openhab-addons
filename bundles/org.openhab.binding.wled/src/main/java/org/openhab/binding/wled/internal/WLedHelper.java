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

import java.util.LinkedList;
import java.util.List;

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

    /**
     * @return A List that holds the values from a heading/element that re-occurs in a message multiple times.
     *
     */
    static List<String> listOfResults(String message, String element, String end) {
        List<String> results = new LinkedList<>();
        String temp = "";
        for (int startLookingFromIndex = 0; startLookingFromIndex != -1;) {
            startLookingFromIndex = message.indexOf(element, startLookingFromIndex);
            if (startLookingFromIndex >= 0) {
                temp = getValue(message.substring(startLookingFromIndex), element, end);
                if (!temp.isEmpty()) {
                    results.add(temp);
                } else {
                    return results;// end string must not exist so stop looking.
                }
                startLookingFromIndex += temp.length();
            }
        }
        return results;
    }
}
