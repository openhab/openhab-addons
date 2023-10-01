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
package org.openhab.binding.kodi.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link KodiBindingConstants} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 * @author Andreas Reinhardt & Christoph Weitkamp - Added channels for thumbnail and fanart
 * @author Christoph Weitkamp - Improvements for playing audio notifications
 */
@NonNullByDefault
public class KodiBindingConstants {

    public static final String BINDING_ID = "kodi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_KODI = new ThingTypeUID(BINDING_ID, "kodi");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_KODI);

    // List of thing parameters names
    public static final String HOST_PARAMETER = "ipAddress";
    public static final String WS_PORT_PARAMETER = "port";
    public static final String HTTP_PORT_PARAMETER = "httpPort";
    public static final String HTTP_USER_PARAMETER = "httpUser";
    public static final String HTTP_PASSWORD_PARAMETER = "httpPassword";
    public static final String REFRESH_PARAMETER = "refreshInterval";

    // List of all Channel ids
    public static final String CHANNEL_SCREENSAVER = "screensaver";
    public static final String CHANNEL_INPUTREQUESTED = "inputrequested";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_STOP = "stop";
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_PLAYURI = "playuri";
    public static final String CHANNEL_PLAYFAVORITE = "playfavorite";
    public static final String CHANNEL_PVR_OPEN_TV = "pvr-open-tv";
    public static final String CHANNEL_PVR_OPEN_RADIO = "pvr-open-radio";
    public static final String CHANNEL_SHOWNOTIFICATION = "shownotification";
    public static final String CHANNEL_PLAYNOTIFICATION = "playnotification";
    public static final String CHANNEL_PROFILE = "profile";

    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_INPUTTEXT = "inputtext";
    public static final String CHANNEL_INPUTACTION = "inputaction";
    public static final String CHANNEL_INPUTBUTTONEVENT = "inputbuttonevent";

    public static final String CHANNEL_SYSTEMCOMMAND = "systemcommand";

    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_ORIGINALTITLE = "originaltitle";
    public static final String CHANNEL_SHOWTITLE = "showtitle";
    public static final String CHANNEL_ALBUM = "album";
    public static final String CHANNEL_MEDIATYPE = "mediatype";
    public static final String CHANNEL_MEDIAID = "mediaid";
    public static final String CHANNEL_MEDIAFILE = "mediafile";
    public static final String CHANNEL_GENRELIST = "genreList";
    public static final String CHANNEL_PVR_CHANNEL = "pvr-channel";
    public static final String CHANNEL_THUMBNAIL = "thumbnail";
    public static final String CHANNEL_FANART = "fanart";
    public static final String CHANNEL_AUDIO_CODEC = "audio-codec";
    public static final String CHANNEL_AUDIO_CHANNELS = "audio-channels";
    public static final String CHANNEL_AUDIO_INDEX = "audio-index";
    public static final String CHANNEL_AUDIO_LANGUAGE = "audio-language";
    public static final String CHANNEL_AUDIO_NAME = "audio-name";
    public static final String CHANNEL_VIDEO_CODEC = "video-codec";
    public static final String CHANNEL_VIDEO_INDEX = "video-index";
    public static final String CHANNEL_VIDEO_HEIGHT = "video-height";
    public static final String CHANNEL_VIDEO_WIDTH = "video-width";
    public static final String CHANNEL_SUBTITLE_ENABLED = "subtitle-enabled";
    public static final String CHANNEL_SUBTITLE_INDEX = "subtitle-index";
    public static final String CHANNEL_SUBTITLE_LANGUAGE = "subtitle-language";
    public static final String CHANNEL_SUBTITLE_NAME = "subtitle-name";

    public static final String CHANNEL_CURRENTTIME = "currenttime";
    public static final String CHANNEL_CURRENTTIMEPERCENTAGE = "currenttimepercentage";
    public static final String CHANNEL_DURATION = "duration";
    public static final String CHANNEL_UNIQUEID_IMDB = "uniqueid-imdb";
    public static final String CHANNEL_UNIQUEID_IMDBTVSHOW = "uniqueid-imdbtvshow";
    public static final String CHANNEL_UNIQUEID_TMDB = "uniqueid-tmdb";
    public static final String CHANNEL_UNIQUEID_TMDBTVSHOW = "uniqueid-tmdbtvshow";
    public static final String CHANNEL_UNIQUEID_TMDBEPISODE = "uniqueid-tmdbepisode";
    public static final String CHANNEL_UNIQUEID_DOUBAN = "uniqueid-douban";
    public static final String CHANNEL_MPAA = "mpaa";
    public static final String CHANNEL_RATING = "rating";
    public static final String CHANNEL_USERRATING = "userrating";

    public static final String CHANNEL_SEASON = "season";
    public static final String CHANNEL_EPISODE = "episode";

    public static final String CHANNEL_TYPE_SHOWNOTIFICATION = "shownotification";
    public static final String CHANNEL_TYPE_SHOWNOTIFICATION_PARAM_TITLE = "title";
    public static final String CHANNEL_TYPE_SHOWNOTIFICATION_PARAM_DISPLAYTIME = "displayTime";
    public static final String CHANNEL_TYPE_SHOWNOTIFICATION_PARAM_ICON = "icon";

    // Module Properties
    public static final String PROPERTY_VERSION = "version";

    // Used for Discovery service
    public static final String MANUFACTURER = "XBMC Foundation";
    public static final String UPNP_DEVICE_TYPE = "MediaRenderer";

    public static final String PVR_TV = "tv";
    public static final String PVR_RADIO = "radio";
}
