/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link OnkyoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Frank - Initial contribution
 */
public class OnkyoBindingConstants {

    public static final String BINDING_ID = "onkyo";

    // Extend this set with all successfully tested models
    public final static Set<String> SUPPORTED_DEVICE_MODELS = ImmutableSet.of("TX-NR535");

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_ONKYOAV = new ThingTypeUID(BINDING_ID, "onkyoAVR");
    public final static ThingTypeUID THING_TYPE_ONKYO_UNSUPPORTED = new ThingTypeUID(BINDING_ID, "onkyoUnsupported");
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ONKYOAV,
            THING_TYPE_ONKYO_UNSUPPORTED);

    // List of thing parameters names
    public final static String HOST_PARAMETER = "ipAddress";
    public final static String TCP_PORT_PARAMETER = "port";

    // List of all Channel ids
    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_INPUT = "input";
    public final static String CHANNEL_MUTE = "mute";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_CONTROL = "control";
    public final static String CHANNEL_CURRENTPLAYINGTIME = "currentPlayingTime";
    public final static String CHANNEL_ARTIST = "artist";
    public final static String CHANNEL_TITLE = "title";
    public final static String CHANNEL_ALBUM = "album";
    public final static String CHANNEL_LISTENMODE = "listenmode";

    // Used for Discovery service
    public final static String MANUFACTURER = "ONKYO";
    public final static String UPNP_DEVICE_TYPE = "MediaRenderer";

}
