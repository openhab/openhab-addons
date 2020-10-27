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
package org.openhab.binding.mpd.internal.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for parsing a response from a Music Player Daemon.
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public class MPDResponseParser {

    private final static Logger logger = LoggerFactory.getLogger(MPDResponseParser.class);

    static Map<String, String> responseToMap(MPDResponse response) {
        Map<String, String> map = new HashMap<String, String>();

        for (String line : response.getLines()) {
            int offset = line.indexOf(':');
            if (offset >= 0) {
                String key = line.substring(0, offset);
                String value = line.substring(offset + 1).trim();

                map.put(key, value);
            }
        }

        return map;
    }

    static int parseInteger(String value, int aDefault) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.debug("parseInt of {} failed", value);
        }
        return aDefault;
    }

    static Optional<Integer> parseInteger(String value) {
       if( value == null){
           return Optional.empty();
       }
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            logger.debug("parseInt of {} failed", value);
            return Optional.empty();
        }
    }

    public static Optional<Float> parseFloat(String value) {
        if (value == null){
            return Optional.empty();
        }
        try {
            return Optional.of(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            logger.debug("parseFloat of {} failed", value);
            return Optional.empty();
        }
    }
}
