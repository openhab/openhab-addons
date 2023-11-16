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
package org.openhab.binding.volumio.internal.mapping;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @see <a href="https://github.com/volumio/Volumio2-UI/blob/master/src/app/services/player.service.js">
 *      https://github.com/volumio/Volumio2-UI/blob/master/src/app/services/player.service.js</a>
 * @see <a href="https://github.com/volumio/Volumio2/blob/master/app/plugins/user_interface/websocket/index.js">
 *      https://github.com/volumio/Volumio2/blob/master/app/plugins/user_interface/websocket/index.js</a>
 *
 * @author Patrick Sernetz - Initial Contribution
 * @author Chris Wohlbrecht - Adaption for openHAB 3
 * @author Michael Loercher - Adaption for openHAB 3
 *
 */
@NonNullByDefault
public class VolumioCommands {

    /* Player Status */

    public static final String GET_STATE = "get-state";

    /* Player Controls */

    public static final String PLAY = "play";

    public static final String PAUSE = "pause";

    public static final String STOP = "stop";

    public static final String PREVIOUS = "prev";

    public static final String NEXT = "next";

    public static final String SEEK = "seek";

    public static final String RANDOM = "set-random";

    public static final String REPEAT = "set-repeat";

    /* Search */

    public static final String SEARCH = "search";

    /* Volume */

    public static final String VOLUME = "volume";

    public static final String MUTE = "mute";

    public static final String UNMUTE = "unmute";

    /* MultiRoom */

    public static final String GET_MULTIROOM_DEVICES = "get-multi-room-devices";

    /* Queue */

    /**
     * Replace the complete queue and play add/play the delivered entry.
     */
    public static final String REPLACE_AND_PLAY = "replace-and-play";

    public static final String ADD_PLAY = "addPlay";

    public static final String CLEAR_QUEUE = "clear-queue";

    /* ... */
    public static final String SHUTDOWN = "shutdown";

    public static final String REBOOT = "reboot";

    public static final String PLAY_PLAYLIST = "play-playlist";

    public static final String PLAY_FAVOURITES = "play-favourites";

    public static final String PLAY_RADIO_FAVOURITES = "play-radio-favourites";
}
