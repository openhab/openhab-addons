/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.resources;

/**
 * The {@link HeosConstants} provides the constants used within the HEOS
 * network
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosConstants {

    public static final String PLAYERS_CHANGED = "players_changed";
    public static final String GROUPS_CHANGED = "groups_changed";
    public static final String PLAYER_STATE_CHANGED = "player_state_changed";
    public static final String CONNECTION_LOST = "connection_lost";
    public static final String CONNECTION_RESTORED = "connection_restored";

    public static final String PID = "pid";
    public static final String GID = "gid";

    // Event Results
    public static final String FAIL = "fail";
    public static final String SUCCESS = "success";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String COM_UNDER_PROCESS = "command under process";

    // Event Types
    public static final String EVENT_SYSTEM = "system";
    public static final String EVENT_EVENT = "event";
    public static final String EVENT_THING = "thing";

    // Event Commands
    public static final String COM_SING_IN = "sign_in";
    public static final String COM_USER_CHANGED = "user_changed";

    // Browse Command

    public static final String FAVORIT_SID = "1028";
    public static final String PLAYLISTS_SID = "1025";
    public static final String INPUT_SID = "1027";

    public static final String CID = "cid";
    public static final String MID = "mid";
    public static final String SID = "sid";
    public static final String QID = "qid";

}
