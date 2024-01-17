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
package org.openhab.binding.panasonicbdp.internal;

import java.util.Map;
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.Fields;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PanaBlurayBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class PanaBlurayBindingConstants {
    public static final String BINDING_ID = "panasonicbdp";
    public static final String PROPERTY_UUID = "uuid";
    public static final String PROPERTY_HOST_NAME = "hostName";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BD_PLAYER = new ThingTypeUID(BINDING_ID, "bd-player");
    public static final ThingTypeUID THING_TYPE_UHD_PLAYER = new ThingTypeUID(BINDING_ID, "uhd-player");

    // List of all Channel id's
    public static final String POWER = "power";
    public static final String BUTTON = "button";
    public static final String CONTROL = "control";
    public static final String PLAYER_STATUS = "player-status";
    public static final String TIME_ELAPSED = "time-elapsed";
    public static final String TIME_TOTAL = "time-total";
    public static final String CHAPTER_CURRENT = "chapter-current";
    public static final String CHAPTER_TOTAL = "chapter-total";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BD_PLAYER,
            THING_TYPE_UHD_PLAYER);

    // Units of measurement of the data delivered by the API
    public static final Unit<Time> API_SECONDS_UNIT = Units.SECOND;

    public static final int DEFAULT_REFRESH_PERIOD_SEC = 5;
    public static final String USER_AGENT = "MEI-LAN-REMOTE-CALL";
    public static final String SHA_256_ALGORITHM = "SHA-256";
    public static final String CRLF = "\r\n";
    public static final String COMMA = ",";
    public static final String STOP_STATUS = "00";
    public static final String OPEN_STATUS = "01";
    public static final String OFF_STATUS = "07";
    public static final String PLAY_STATUS = "08";
    public static final String PAUSE_STATUS = "09";
    public static final String EMPTY = "";
    public static final String PLAYER_CMD_ERR = "52,\"";

    public static final String CMD_POWER = "POWER";
    public static final String CMD_PLAYBACK = "PLAYBACK";
    public static final String CMD_PAUSE = "PAUSE";
    public static final String CMD_SKIPFWD = "SKIPFWD";
    public static final String CMD_SKIPREV = "SKIPREV";
    public static final String CMD_CUE = "CUE";
    public static final String CMD_REV = "REV";

    // Map the player status numbers to the corresponding i18n translation file keys
    public static final Map<String, String> STATUS_MAP = Map.of(STOP_STATUS, "stop", OPEN_STATUS, "open", "02",
            "reverse", "05", "forward", "06", "slowfwd", OFF_STATUS, "off", PLAY_STATUS, "play", PAUSE_STATUS, "pause",
            "86", "slowrev");

    // pre-define the POST body for status update calls
    public static final Fields REVIEW_POST_CMD = new Fields();
    static {
        REVIEW_POST_CMD.add("cCMD_REVIEW.x", "100");
        REVIEW_POST_CMD.add("cCMD_REVIEW.y", "100");
    }

    public static final Fields PST_POST_CMD = new Fields();
    static {
        PST_POST_CMD.add("cCMD_PST.x", "100");
        PST_POST_CMD.add("cCMD_PST.y", "100");
    }

    public static final Fields GET_STATUS_POST_CMD = new Fields();
    static {
        GET_STATUS_POST_CMD.add("cCMD_GET_STATUS.x", "100");
        GET_STATUS_POST_CMD.add("cCMD_GET_STATUS.y", "100");
    }

    public static final Fields GET_NONCE_CMD = new Fields();
    static {
        GET_NONCE_CMD.add("SID", "1234ABCD");
    }
}
