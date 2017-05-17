/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link YamahaReceiver2Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
public class YamahaReceiverBindingConstants {

    public static final String BINDING_ID = "yamahareceiver";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_YAMAHAAV = new ThingTypeUID(BINDING_ID, "yamahaAV");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_SURROUND = "surroundProgram";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_VOLUME_DB = "volumeDB";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_NETRADIO_TUNE = "netradiotune";

    public static final String UPNP_TYPE = "MediaRenderer";

    public static final CharSequence UPNP_MANUFACTURER = "YAMAHA";

    public static final String CONFIG_REFRESH = "REFRESH_IN_SEC";
    public static final String CONFIG_HOST_NAME = "HOST";
    public static final String CONFIG_ZONE = "ZONE";
    public static final String CONFIG_RELVOLUMECHANGE = "RELVOLUMECHANGE";

}
