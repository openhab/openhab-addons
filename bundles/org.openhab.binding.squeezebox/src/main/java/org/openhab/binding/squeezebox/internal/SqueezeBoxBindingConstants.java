/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.squeezebox.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SqueezeBoxBindingConstants} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Mark Hilbush - Added duration channel
 */
@NonNullByDefault
public class SqueezeBoxBindingConstants {

    public static final String BINDING_ID = "squeezebox";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SQUEEZEBOXPLAYER_THING_TYPE = new ThingTypeUID(BINDING_ID, "squeezeboxplayer");
    public static final ThingTypeUID SQUEEZEBOXSERVER_THING_TYPE = new ThingTypeUID(BINDING_ID, "squeezeboxserver");

    // List of all Server Channel Ids
    public static final String CHANNEL_FAVORITES_LIST = "favoritesList";

    // List of all Player Channel Ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_STOP = "stop";
    public static final String CHANNEL_PLAY_PAUSE = "playPause";
    public static final String CHANNEL_NEXT = "next";
    public static final String CHANNEL_PREV = "prev";
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_STREAM = "stream";
    public static final String CHANNEL_SOURCE = "source";
    public static final String CHANNEL_SYNC = "sync";
    public static final String CHANNEL_UNSYNC = "unsync";
    public static final String CHANNEL_PLAYLIST_INDEX = "playListIndex";
    public static final String CHANNEL_CURRENT_PLAYING_TIME = "currentPlayingTime";
    public static final String CHANNEL_DURATION = "duration";
    public static final String CHANNEL_NUMBER_PLAYLIST_TRACKS = "numberPlaylistTracks";
    public static final String CHANNEL_CURRENT_PLAYLIST_SHUFFLE = "currentPlaylistShuffle";
    public static final String CHANNEL_CURRENT_PLAYLIST_REPEAT = "currentPlaylistRepeat";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_REMOTE_TITLE = "remotetitle";
    public static final String CHANNEL_ALBUM = "album";
    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_YEAR = "year";
    public static final String CHANNEL_GENRE = "genre";
    public static final String CHANNEL_ALBUM_ARTIST = "albumArtist";
    public static final String CHANNEL_TRACK_ARTIST = "trackArtist";
    public static final String CHANNEL_BAND = "band";
    public static final String CHANNEL_COMPOSER = "composer";
    public static final String CHANNEL_CONDUCTOR = "conductor";
    public static final String CHANNEL_COVERART_DATA = "coverartdata";
    public static final String CHANNEL_IRCODE = "ircode";
    public static final String CHANNEL_IP = "ip";
    public static final String CHANNEL_UID = "uid";
    public static final String CHANNEL_TYPEID = "typeId";
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_MODEL = "model";
    public static final String CHANNEL_FAVORITES_PLAY = "playFavorite";
    public static final String CHANNEL_RATE = "rate";
    public static final String CHANNEL_SLEEP = "sleep";

    // List of all properties
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_IP = "ip";
}
