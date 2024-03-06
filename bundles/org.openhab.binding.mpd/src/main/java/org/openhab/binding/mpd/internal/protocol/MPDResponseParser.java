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
package org.openhab.binding.mpd.internal.protocol;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class for parsing a response from a Music Player Daemon.
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public class MPDResponseParser {

    static Map<String, String> responseToMap(MPDResponse response) {
        Map<String, String> map = new HashMap<>();

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
}
