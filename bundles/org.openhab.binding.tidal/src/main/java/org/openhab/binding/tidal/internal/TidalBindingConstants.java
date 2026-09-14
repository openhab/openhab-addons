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
package org.openhab.binding.tidal.internal;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TidalBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class TidalBindingConstants {

    public enum OAuthMode {
        ClientFlow,
        DeviceFlow;
    }

    // List of Tidal services related urls, information
    public static final String TIDAL_LOGIN_URL = "https://login.tidal.com";
    public static final String TIDAL_AUTH_URL = "https://auth.tidal.com";
    public static final String TIDAL_AUTHORIZE_CLIENT_FLOW_URL = TIDAL_LOGIN_URL + "/authorize";
    public static final String TIDAL_AUTHORIZE_DEVICE_FLOW_URL = TIDAL_AUTH_URL + "/v1/oauth2/device_authorization";

    public static final OAuthMode OAUTH_MODE = OAuthMode.DeviceFlow;

    // for device flow

    public static final String TIDAL_API_TOKEN_URL = TIDAL_AUTH_URL + "/v1/oauth2/token";

    /**
     * Tidal scopes needed by this binding to work.
     */
    public static final String TIDAL_SCOPES_NEW_API = Stream
            .of("playlists.read", "entitlements.read", "collection.read", "playlists.write", "collection.write",
                    "user.read", "recommendations.read", "playback", "search.read", "search.write")
            .collect(Collectors.joining(" "));

    public static final String TIDAL_SCOPES_OLD_API = Stream.of("r_usr", "w_usr").collect(Collectors.joining(" "));
    public static final String TIDAL_V1_API_URL = "https://api.tidal.com/v1";
    public static final String TIDAL_API_URL = "https://openapi.tidal.com";

    // Authorization related Servlet and resources aliases.
    public static final String TIDAL_ALIAS = "/connecttidal";
    public static final String TIDAL_IMG_ALIAS = "/img";

    // List of all Thing Type UIDs
    public static final String BINDING_ID = "tidal";
    public static final String BINDING_LABEL = "Tidal";
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");

    // List of all Channel ids
    public static final String CHANNEL_ACCESSTOKEN = "accessToken";

    public static final String CHANNEL_TRACKPLAY = "trackPlay";
    public static final String CHANNEL_TRACKPLAYER = "trackPlayer";
    public static final String CHANNEL_TRACKREPEAT = "trackRepeat";

    public static final String CHANNEL_PLAYLISTS = "playlists";
    public static final String CHANNEL_PLAYLISTNAME = "playlistName";
    public static final String CHANNEL_PLAYLISTS_LIMIT = "limit";
    public static final String CHANNEL_PLAYLISTS_OFFSET = "offset";

    public static final String CHANNEL_PLAYED_TRACKID = "trackId";
    public static final String CHANNEL_PLAYED_TRACKURI = "trackUri";
    public static final String CHANNEL_PLAYED_TRACKHREF = "trackHref";
    public static final String CHANNEL_PLAYED_TRACKNAME = "trackName";
    public static final String CHANNEL_PLAYED_TRACKTYPE = "trackType";
    public static final String CHANNEL_PLAYED_TRACKNUMBER = "trackNumber";
    public static final String CHANNEL_PLAYED_TRACKDISCNUMBER = "trackDiscNumber";
    public static final String CHANNEL_PLAYED_TRACKPOPULARITY = "trackPopularity";
    public static final String CHANNEL_PLAYED_TRACKEXPLICIT = "trackExplicit";
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
    public static final String CHANNEL_PLAYED_ALBUMIMAGEURL = "albumImageUrl";
    public static final String CHANNEL_CONFIG_IMAGE_INDEX = "imageIndex";

    public static final String CHANNEL_PLAYED_ARTISTID = "artistId";
    public static final String CHANNEL_PLAYED_ARTISTURI = "artistUri";
    public static final String CHANNEL_PLAYED_ARTISTHREF = "artistHref";
    public static final String CHANNEL_PLAYED_ARTISTNAME = "artistName";
    public static final String CHANNEL_PLAYED_ARTISTTYPE = "artistType";

    public static final String CHANNEL_DEVICEID = "deviceId";
    public static final String CHANNEL_DEVICES = "devices";
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
    public static final String PROPERTY_TIDAL_USER_NAME = "user";
    public static final String PROPERTY_TIDAL_USER_ID = "userId";
    public static final String PROPERTY_TIDAL_USER_COUNTRY = "userCountry";
    public static final String PROPERTY_TIDAL_DEVICE_NAME = "deviceName";
}
