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
package org.openhab.binding.alarmdecoder.internal.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link IntCommandMap} class contains an integer to command map used by the keypad intcommand channel.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class IntCommandMap {
    private static final Pattern VALID_COMMAND_PATTERN = Pattern.compile(ADCommand.KEYPAD_COMMAND_REGEX);

    private final Map<Integer, String> commandMap;

    public IntCommandMap(String mappingString) throws IllegalArgumentException {
        commandMap = new HashMap<>();

        String mstring = mappingString.replace("POUND", "#");
        String[] elements = mstring.split(",");
        for (String element : elements) {
            String[] kvPair = element.split("=");
            if (kvPair.length != 2) {
                throw new IllegalArgumentException("Invalid key-value pair format");
            }

            Matcher matcher = VALID_COMMAND_PATTERN.matcher(kvPair[1]);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid command characters in mapping");
            }

            try {
                commandMap.put(Integer.parseInt(kvPair[0]), kvPair[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unable to parse integer in mapping", e);
            }
        }
    }

    @Nullable
    public String getCommand(int key) {
        return commandMap.get(key);
    }

    public int size() {
        return commandMap.size();
    }
}
