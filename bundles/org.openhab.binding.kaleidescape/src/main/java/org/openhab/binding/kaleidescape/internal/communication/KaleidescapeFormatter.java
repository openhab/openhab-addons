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
package org.openhab.binding.kaleidescape.internal.communication;

import static org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KaleidescapeFormatter} is a utility class with formatting methods for Kaleidescape strings
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class KaleidescapeFormatter {
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

                // fix the encoding for k mangled extended ascii characters (chars coming in as \dnnn)
                // I.e. characters with accent, umlaut, etc., they need to be restored to the correct character
                // example: Noel (with umlaut 'o') comes in as N\d246el
                input = input.replaceAll("(?i)\\\\d([0-9]{3})", "\\&#$1;"); // first convert to html escaped codes
                // then convert with unescapeHtml4, not sure how to do this without the Apache libraries :(
                return StringEscapeUtils.unescapeHtml4(input);
            }
        }
        return input;
    }
}
