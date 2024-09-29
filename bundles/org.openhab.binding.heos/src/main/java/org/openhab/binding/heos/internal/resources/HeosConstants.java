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
package org.openhab.binding.heos.internal.resources;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HeosConstants} provides the constants used within the HEOS
 * network
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosConstants {

    public static final String HEOS = "heos";

    public static final String CONNECTION_LOST = "connection_lost";
    public static final String EVENT_STREAM_TIMEOUT = "event_stream_timeout";
    public static final String CONNECTION_RESTORED = "connection_restored";

    public static final String PID = "pid";

    // Event Results
    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String REPEAT_ALL = "on_all";
    public static final String REPEAT_ONE = "on_one";

    // Event Types
    public static final String EVENT_TYPE_SYSTEM = "system";
    public static final String EVENT_TYPE_EVENT = "event";

    // Browse Command
    public static final String FAVORITE_SID = "1028";
    public static final String PLAYLISTS_SID = "1025";
    public static final int INPUT_SID = 1027;

    public static final String PLAY = "play";
    public static final String PAUSE = "pause";
    public static final String STOP = "stop";
    public static final String STATION = "station";
    public static final String SONG = "song";

    // UI Commands
    public static final String HEOS_UI_ALL = "All";
    public static final String HEOS_UI_ONE = "One";
    public static final String HEOS_UI_OFF = "Off";
}
