/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link KodiBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Frank - Initial contribution
 */
public class KodiBindingConstants {

    public static final String BINDING_ID = "kodi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_KODI = new ThingTypeUID(BINDING_ID, "kodi");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_KODI);

    // List of thing parameters names
    public static final String HOST_PARAMETER = "ipAddress";
    public static final String PORT_PARAMETER = "port";

    // List of all Channel ids
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_STOP = "stop";
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_PLAYURI = "playuri";
    public static final String CHANNEL_SHOWNOTIFICATION = "shownotification";

    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_INPUTTEXT = "inputtext";
    public static final String CHANNEL_SYSTEMCOMMAND = "systemcommand";

    public static final String CHANNEL_ARTIST = "artist";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_SHOWTITLE = "showtitle";
    public static final String CHANNEL_ALBUM = "album";
    public static final String CHANNEL_MEDIATYPE = "mediatype";

    // Module Properties
    public static final String PROPERTY_VERSION = "version";

    // Used for Discovery service
    public static final String MANUFACTURER = "XBMC Foundation";
    public static final String UPNP_DEVICE_TYPE = "MediaRenderer";

}
