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
    public final static ThingTypeUID THING_TYPE_KODI = new ThingTypeUID(BINDING_ID, "kodi");
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_KODI);

    // List of thing parameters names
    public final static String HOST_PARAMETER = "ipAddress";
    public final static String PORT_PARAMETER = "port";

    // List of all Channel ids
    public final static String CHANNEL_MUTE = "mute";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_STOP = "stop";
    public final static String CHANNEL_CONTROL = "control";
    public final static String CHANNEL_PLAYURI = "playuri";
    public final static String CHANNEL_SHOWNOTIFICATION = "shownotification";

    public final static String CHANNEL_INPUT = "input";
    public final static String CHANNEL_INPUTTEXT = "inputtext";
    public final static String CHANNEL_SYSTEMCOMMAND = "systemcommand";

    public final static String CHANNEL_ARTIST = "artist";
    public final static String CHANNEL_TITLE = "title";
    public final static String CHANNEL_SHOWTITLE = "showtitle";
    public final static String CHANNEL_ALBUM = "album";
    public final static String CHANNEL_MEDIATYPE = "mediatype";

    // Module Properties
    public final static String PROPERTY_VERSION = "version";

    // Used for Discovery service
    public final static String MANUFACTURER = "XBMC Foundation";
    public final static String UPNP_DEVICE_TYPE = "MediaRenderer";

}
