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
package org.openhab.binding.oppo.internal.communication;

import static org.openhab.binding.oppo.internal.OppoBindingConstants.*;

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
    public static final Map<String, String> REPEAT_MODE = new HashMap<>();
    static {
        REPEAT_MODE.put("00", "OFF");
        REPEAT_MODE.put("01", "ONE"); // maybe?"
        REPEAT_MODE.put("02", "CH");
        REPEAT_MODE.put("03", "ALL");
        REPEAT_MODE.put("04", "TT");
        REPEAT_MODE.put("05", "SHF");
        REPEAT_MODE.put("06", "RND");
    }

    // map to lookup zoom mode
    public static final Map<String, String> ZOOM_MODE = new HashMap<>();
    static {
        ZOOM_MODE.put("00", "1"); // Off (zoom 1x)
        ZOOM_MODE.put("01", "AR"); // Stretch
        ZOOM_MODE.put("02", "FS"); // Full screen
        ZOOM_MODE.put("03", "US"); // Underscan
        ZOOM_MODE.put("04", "1.2");
        ZOOM_MODE.put("05", "1.3");
        ZOOM_MODE.put("06", "1.5");
        ZOOM_MODE.put("07", "2");
        ZOOM_MODE.put("08", "3");
        ZOOM_MODE.put("09", "4");
        ZOOM_MODE.put("10", "1/2");
        ZOOM_MODE.put("11", "1/3");
        ZOOM_MODE.put("12", "1/4");
    }

    // map to lookup disc type
    public static final Map<String, String> DISC_TYPE = new HashMap<>();
    static {
        DISC_TYPE.put("BDMV", "BD-MV");
        DISC_TYPE.put("DVDV", "DVD-VIDEO");
        DISC_TYPE.put("DVDA", "DVD-AUDIO");
        DISC_TYPE.put("SACD", "SACD");
        DISC_TYPE.put("CDDA", "CDDA");
        DISC_TYPE.put("HDCD", "HDCD");
        DISC_TYPE.put("DATA", "DATA-DISC");
        DISC_TYPE.put("VCD2", "VCD2");
        DISC_TYPE.put("SVCD", "SVCD");
        DISC_TYPE.put("UHBD", "UHBD");
        DISC_TYPE.put("UNKN", UNKNOW_DISC);
    }

    // map to lookup playback status
    public static final Map<String, String> PLAYBACK_STATUS = new HashMap<>();
    static {
        PLAYBACK_STATUS.put("DISC", "NO DISC");
        PLAYBACK_STATUS.put("LOAD", "LOADING");
        PLAYBACK_STATUS.put("OPEN", "OPEN");
        PLAYBACK_STATUS.put("CLOS", "CLOSE");
        PLAYBACK_STATUS.put("PLAY", "PLAY");
        PLAYBACK_STATUS.put("PAUS", "PAUSE");
        PLAYBACK_STATUS.put("STOP", "STOP");
        PLAYBACK_STATUS.put("HOME", "HOME MENU");
        PLAYBACK_STATUS.put("MCTR", "MEDIA CENTER");
        PLAYBACK_STATUS.put("SCSV", "SCREEN SAVER");
        PLAYBACK_STATUS.put("MENU", "DISC MENU");
    }
}
