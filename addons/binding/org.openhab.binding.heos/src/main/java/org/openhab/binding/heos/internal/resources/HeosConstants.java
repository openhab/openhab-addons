/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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

    public final static String PLAYERS_CHANGED = "players_changed";
    public final static String GROUPS_CHANGED = "groups_changed";
    public final static String PLAYER_STATE_CHANGED = "player_state_changed";
    public final static String CONNECTION_LOST = "connection_lost";
    public final static String CONNECTION_RESTORED = "connection_restored";

    public final static String PID = "pid";
    public final static String GID = "gid";

    // Event Results
    public final static String FAIL = "fail";
    public final static String SUCCESS = "success";
    public final static String TRUE = "true";
    public final static String FALSE = "false";
    public final static String COM_UNDER_PROCESS = "command under process";

    // Event Types
    public final static String EVENT_SYSTEM = "system";
    public final static String EVENT_EVENT = "event";
    public final static String EVENT_THING = "thing";

    // Event Commands
    public final static String COM_SING_IN = "sign_in";
    public final static String COM_USER_CHANGED = "user_changed";

    // Browse Command

    public final static String FAVORIT_SID = "1028";
    public final static String PLAYLISTS_SID = "1025";
    public final static String INPUT_SID = "1027";

    public final static String CID = "cid";
    public final static String MID = "mid";
    public final static String SID = "sid";
    public final static String QID = "qid";

}
