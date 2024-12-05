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
package org.openhab.binding.mpd.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MPDBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public class MPDBindingConstants {

    public static final String BINDING_ID = "mpd";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MPD = new ThingTypeUID(BINDING_ID, "mpd");

    // List of all Channel ids
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_CURRENT_ALBUM = "currentalbum";
    public static final String CHANNEL_CURRENT_ARTIST = "currentartist";
    public static final String CHANNEL_CURRENT_NAME = "currentname";
    public static final String CHANNEL_CURRENT_SONG = "currentsong";
    public static final String CHANNEL_CURRENT_SONG_ID = "currentsongid";
    public static final String CHANNEL_CURRENT_TITLE = "currenttitle";
    public static final String CHANNEL_CURRENT_TRACK = "currenttrack";
    public static final String CHANNEL_STOP = "stop";
    public static final String CHANNEL_VOLUME = "volume";

    // Config Parameters
    public static final String PARAMETER_IPADDRESS = "ipAddress";
    public static final String PARAMETER_PORT = "port";
    public static final String UNIQUE_ID = "uniqueId";
}
