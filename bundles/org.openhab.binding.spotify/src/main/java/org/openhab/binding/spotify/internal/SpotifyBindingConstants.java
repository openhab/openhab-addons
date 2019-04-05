/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.spotify.internal;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SpotifyBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Added more constants
 */
public class SpotifyBindingConstants {

    // List of Spotify services related urls, information
    public static final String SPOTIFY_ACCOUNT_URL = "https://accounts.spotify.com";
    public static final String SPOTIFY_AUTHORIZE_URL = SPOTIFY_ACCOUNT_URL + "/authorize";
    public static final String SPOTIFY_API_TOKEN_URL = SPOTIFY_ACCOUNT_URL + "/api/token";
    /**
     * Spotify scopes needed by this binding to work.
     */
    public static final String SPOTIFY_SCOPES = Stream.of("user-read-playback-state", "user-modify-playback-state",
            "playlist-read-private", "playlist-read-collaborative").collect(Collectors.joining(" "));
    public static final String SPOTIFY_API_URL = "https://api.spotify.com/v1/me";
    public static final String SPOTIFY_API_PLAYER_URL = SPOTIFY_API_URL + "/player";

    // Authorization related Servlet and resources aliases.
    public static final String SPOTIFY_ALIAS = "/connectspotify";
    public static final String SPOTIFY_IMG_ALIAS = "/img";

    // List of all Thing Type UIDs
    private static final String BINDING_ID = "spotify";
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel ids
    public static final String CHANNEL_ACCESSTOKEN = "accessToken";

    public static final String CHANNEL_TRACKPLAY = "trackPlay";
    public static final String CHANNEL_TRACKPLAYER = "trackPlayer";
    public static final String CHANNEL_TRACKREPEAT = "trackRepeat";

    public static final String CHANNEL_PLAYLIST = "playlist";

    public static final String CHANNEL_PLAYED_TRACKID = "trackId";
    public static final String CHANNEL_PLAYED_TRACKURI = "trackUri";
    public static final String CHANNEL_PLAYED_TRACKHREF = "trackHref";
    public static final String CHANNEL_PLAYED_TRACKNAME = "trackName";
    public static final String CHANNEL_PLAYED_TRACKTYPE = "trackType";
    public static final String CHANNEL_PLAYED_TRACKNUMBER = "trackNumber";
    public static final String CHANNEL_PLAYED_TRACKDISCNUMBER = "trackDiscNumber";
    public static final String CHANNEL_PLAYED_TRACKPOPULARITY = "trackPopularity";
    public static final String CHANNEL_PLAYED_TRACKDURATION_MS = "trackDurationMs";
    public static final String CHANNEL_PLAYED_TRACKPROGRESS_MS = "trackProgressMs";
    public static final String CHANNEL_PLAYED_TRACKDURATION_FMT = "trackDuration";
    public static final String CHANNEL_PLAYED_TRACKPROGRESS_FMT = "trackProgress";

    public static final String CHANNEL_PLAYED_ALBUMID = "albumId";
    public static final String CHANNEL_PLAYED_ALBUMURI = "albumUri";
    public static final String CHANNEL_PLAYED_ALBUMHREF = "albumHref";
    public static final String CHANNEL_PLAYED_ALBUMNAME = "albumName";
    public static final String CHANNEL_PLAYED_ALBUMTYPE = "albumType";
    public static final String CHANNEL_PLAYED_ALBUMIMAGE = "albumImage";

    public static final String CHANNEL_PLAYED_ARTISTID = "artistId";
    public static final String CHANNEL_PLAYED_ARTISTURI = "artistUri";
    public static final String CHANNEL_PLAYED_ARTISTHREF = "artistHref";
    public static final String CHANNEL_PLAYED_ARTISTNAME = "artistName";
    public static final String CHANNEL_PLAYED_ARTISTTYPE = "artistType";

    public static final String CHANNEL_DEVICEID = "deviceId";
    public static final String CHANNEL_TYPE_ACTIVE_DEVICENAME = "activeDeviceName";
    public static final String CHANNEL_DEVICENAME = "deviceName";
    public static final String CHANNEL_DEVICETYPE = "deviceType";
    public static final String CHANNEL_DEVICEACTIVE = "deviceActive";
    public static final String CHANNEL_DEVICERESTRICTED = "deviceRestricted";
    public static final String CHANNEL_DEVICEVOLUME = "deviceVolume";
    public static final String CHANNEL_DEVICESHUFFLE = "deviceShuffle";
    public static final String CHANNEL_DEVICEPLAYER = "devicePlayer";

    // List of Bridge configuration params
    public static final String CONFIGURATION_CLIENT_ID = "clientId";

    // List of Bridge/Thing properties
    public static final String PROPERTY_SPOTIFY_USER = "user";
    public static final String PROPERTY_SPOTIFY_DEVICE_ID = "id";
}
