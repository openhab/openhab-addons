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
package org.openhab.binding.oppo.internal.communication;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides mapping of various Oppo query status codes to the corresponding set codes
 *
 * @author Michael Lobstein - Initial contribution
 */

@NonNullByDefault
public class OppoStatusCodes {
    // map to lookup random mode
    public static final Map<String, String> repeatMode = new HashMap<>();
    static {
        repeatMode.put("00", "OFF");
        repeatMode.put("01", "ONE"); // maybe?"
        repeatMode.put("02", "CH");
        repeatMode.put("03", "ALL");
        repeatMode.put("04", "TT");
        repeatMode.put("05", "SHF");
        repeatMode.put("06", "RND");
    }
    
    // map to lookup zoom mode
    public static final Map<String, String> zoomMode = new HashMap<>();
    static {
        zoomMode.put("00", "1"); // Off (zoom 1x)
        zoomMode.put("01", "AR"); // Stretch
        zoomMode.put("02", "FS"); // Full screen
        zoomMode.put("03", "US"); // Underscan
        zoomMode.put("04", "1.2");
        zoomMode.put("05", "1.3");
        zoomMode.put("06", "1.5");
        zoomMode.put("07", "2");
        zoomMode.put("08", "3");
        zoomMode.put("09", "4");
        zoomMode.put("10", "1/2");
        zoomMode.put("11", "1/3");
        zoomMode.put("12", "1/4");
    }
}
