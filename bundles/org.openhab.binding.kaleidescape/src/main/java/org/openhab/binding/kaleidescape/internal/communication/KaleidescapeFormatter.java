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
package org.openhab.binding.kaleidescape.internal.communication;

import static org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KaleidescapeFormatter} is a utility class with formatting methods for Kaleidescape strings
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class KaleidescapeFormatter {
    private static final String WITH_DELIMITER = "((?<=\\\\d[0-9]{3})|(?=\\\\d[0-9]{3}))";

    public static String formatString(String input) {
        if (!EMPTY.equals(input)) {
            // convert || back to :
            input = input.replace("||", ":");

            // if input does not have any escaped characters, bypass all the replace()'s
            if (input.contains("\\")) {
                // fix escaped :
                input = input.replace("\\:", ":");

                // fix escaped /
                input = input.replace("\\/", "/");

                // convert \r into comma space
                input = input.replace("\\r", ", ");

                // convert \d146 from review text into apostrophe
                input = input.replace("\\d146", "'");
                // convert \d147 & \d148 from review text into double quote
                input = input.replace("\\d147", "\"");
                input = input.replace("\\d148", "\"");
            }

            // fix the encoding for k mangled extended ascii characters (chars coming in as \dnnn)
            // I.e. characters with accent, umlaut, etc., they need to be restored to the correct character
            // example: Noel (with umlaut 'o') comes in as N\d246el
            if (input.contains("\\d")) {
                StringBuilder fixedOutput = new StringBuilder();
                String[] arr = input.split(WITH_DELIMITER);

                for (String s : arr) {
                    if (s.startsWith("\\d") && s.length() == 5) {
                        try {
                            fixedOutput.append((char) Integer.parseInt(s.substring(2, 5)));
                        } catch (NumberFormatException e) {
                            fixedOutput.append(s);
                        }
                    } else {
                        fixedOutput.append(s);
                    }
                }
                return fixedOutput.toString();
            }
        }
        return input;
    }
}
