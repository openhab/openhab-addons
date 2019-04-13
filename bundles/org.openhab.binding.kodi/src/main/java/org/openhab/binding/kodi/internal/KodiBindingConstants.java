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
package org.openhab.binding.kodi.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KodiBinding} class defines common constants, which are used across
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
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_KODI);

    // List of thing parameters names
    public static final String HOST_PARAMETER = "ipAddress";
    public static final String WS_PORT_PARAMETER = "port";
    public static final String HTTP_PORT_PARAMETER = "httpPort";
    public static final String HTTP_USER_PARAMETER = "httpUser";
    public static final String HTTP_PASSWORD_PARAMETER = "httpPassword";
    public static final String REFRESH_PARAMETER = "refreshInterval";

    // List of all Channel ids
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

    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_INPUTTEXT = "inputtext";
    public static final String CHANNEL_INPUTACTION = "inputaction";

    public static final String CHANNEL_SYSTEMCOMMAND = "systemcommand";

    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_SHOWTITLE = "showtitle";
    public static final String CHANNEL_ALBUM = "album";
    public static final String CHANNEL_MEDIATYPE = "mediatype";
    public static final String CHANNEL_GENRELIST = "genreList";
    public static final String CHANNEL_PVR_CHANNEL = "pvr-channel";
    public static final String CHANNEL_THUMBNAIL = "thumbnail";
    public static final String CHANNEL_FANART = "fanart";
    public static final String CHANNEL_AUDIO_CODEC = "audio-codec";
    public static final String CHANNEL_VIDEO_CODEC = "video-codec";
    public static final String CHANNEL_CURRENTTIME = "currenttime";
    public static final String CHANNEL_CURRENTTIMEPERCENTAGE = "currenttimepercentage";
    public static final String CHANNEL_DURATION = "duration";

    // Module Properties
    public static final String PROPERTY_VERSION = "version";

    // Used for Discovery service
    public static final String MANUFACTURER = "XBMC Foundation";
    public static final String UPNP_DEVICE_TYPE = "MediaRenderer";

    public static final String PVR_TV = "tv";
    public static final String PVR_RADIO = "radio";
}
