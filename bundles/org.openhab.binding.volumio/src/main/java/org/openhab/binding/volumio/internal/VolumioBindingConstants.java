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
package org.openhab.binding.volumio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VolumioBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Sernetz - Initial Contribution
 * @author Chris Wohlbrecht - Adaption for openHAB 3
 * @author Michael Loercher - Adaption for openHAB 3
 */
@NonNullByDefault
public class VolumioBindingConstants {

    private static final String BINDING_ID = "volumio";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VOLUMIO = new ThingTypeUID(BINDING_ID, "player");

    // List of all Channel ids
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_ALBUM = "album";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_PLAYER = "player";
    public static final String CHANNEL_COVER_ART = "album-art";
    public static final String CHANNEL_TRACK_TYPE = "track-type";
    public static final String CHANNEL_PLAY_RADIO_STREAM = "play-radiostream";
    public static final String CHANNEL_PLAY_PLAYLIST = "play-playlist";
    public static final String CHANNEL_CLEAR_QUEUE = "clear-queue";
    public static final String CHANNEL_PLAY_RANDOM = "random";
    public static final String CHANNEL_PLAY_REPEAT = "repeat";
    public static final String CHANNEL_PLAY_URI = "play-uri";
    public static final String CHANNEL_PLAY_FILE = "play-file";
    public static final String CHANNEL_SYSTEM_COMMAND = "system-command";
    public static final String CHANNEL_STOP = "stop-command";

    // discovery properties
    public static final String DISCOVERY_SERVICE_TYPE = "_Volumio._tcp.local.";
    public static final String DISCOVERY_NAME_PROPERTY = "volumioName";
    public static final String DISCOVERY_UUID_PROPERTY = "UUID";

    // config
    public static final String CONFIG_PROPERTY_HOSTNAME = "hostname";
    public static final String CONFIG_PROPERTY_PORT = "port";
    public static final String CONFIG_PROPERTY_PROTOCOL = "protocol";
    public static final String CONFIG_PROPERTY_TIMEOUT = "timeout";
}
