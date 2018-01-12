/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KodiBinding} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Paul Frank - Initial contribution
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 * @author Andreas Reinhardt & Christoph Weitkamp - Added channels for thumbnail and fanart
 * 
 */
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
    public static final String CHANNEL_PVR_OPEN_TV = "pvr-open-tv";
    public static final String CHANNEL_PVR_OPEN_RADIO = "pvr-open-radio";
    public static final String CHANNEL_SHOWNOTIFICATION = "shownotification";

    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_INPUTTEXT = "inputtext";
    public static final String CHANNEL_SYSTEMCOMMAND = "systemcommand";

    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_SHOWTITLE = "showtitle";
    public static final String CHANNEL_ALBUM = "album";
    public static final String CHANNEL_MEDIATYPE = "mediatype";
    public static final String CHANNEL_PVR_CHANNEL = "pvr-channel";
    public static final String CHANNEL_THUMBNAIL = "thumbnail";
    public static final String CHANNEL_FANART = "fanart";

    // Module Properties
    public static final String PROPERTY_VERSION = "version";

    // Used for Discovery service
    public static final String MANUFACTURER = "XBMC Foundation";
    public static final String UPNP_DEVICE_TYPE = "MediaRenderer";

}
