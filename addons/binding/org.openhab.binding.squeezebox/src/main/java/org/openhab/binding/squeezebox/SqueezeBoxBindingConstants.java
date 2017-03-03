/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SqueezeBoxBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SqueezeBoxBindingConstants {

    public static final String BINDING_ID = "squeezebox";

    // List of all Thing Type UIDs
    public final static ThingTypeUID SQUEEZEBOXPLAYER_THING_TYPE = new ThingTypeUID(BINDING_ID, "squeezeboxplayer");
    public final static ThingTypeUID SQUEEZEBOXSERVER_THING_TYPE = new ThingTypeUID(BINDING_ID, "squeezeboxserver");

    // List of all Channel ids
    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_MUTE = "mute";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_STOP = "stop";
    public final static String CHANNEL_PLAY_PAUSE = "playPause";
    public final static String CHANNEL_NEXT = "next";
    public final static String CHANNEL_PREV = "prev";
    public final static String CHANNEL_CONTROL = "control";
    public final static String CHANNEL_STREAM = "stream";
    public final static String CHANNEL_SYNC = "sync";
    public final static String CHANNEL_UNSYNC = "unsync";
    public final static String CHANNEL_PLAYLIST_INDEX = "playListIndex";
    public final static String CHANNEL_CURRENT_PLAYING_TIME = "currentPlayingTime";
    public final static String CHANNEL_NUMBER_PLAYLIST_TRACKS = "numberPlaylistTracks";
    public final static String CHANNEL_CURRENT_PLAYLIST_SHUFFLE = "currentPlaylistShuffle";
    public final static String CHANNEL_CURRENT_PLAYLIST_REPEAT = "currentPlaylistRepeat";
    public final static String CHANNEL_TITLE = "title";
    public final static String CHANNEL_REMOTE_TITLE = "remotetitle";
    public final static String CHANNEL_ALBUM = "album";
    public final static String CHANNEL_ARTIST = "artist";
    public final static String CHANNEL_YEAR = "year";
    public final static String CHANNEL_GENRE = "genre";
    public final static String CHANNEL_COVERART_DATA = "coverartdata";
    public final static String CHANNEL_IRCODE = "ircode";
    public final static String CHANNEL_IP = "ip";
    public final static String CHANNEL_UID = "uid";
    public final static String CHANNEL_TYPEID = "typeId";
    public final static String CHANNEL_NAME = "name";
    public final static String CHANNEL_MODEL = "model";
    public final static String CHANNEL_NOTIFICATION_SOUND_VOLUME = "notificationSoundVolume";
}
