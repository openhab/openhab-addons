/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
<<<<<<< 60b2641262654f560ba41b55ecd404bec7547f0b
<<<<<<< 22e7f0057024a151fbe7e0c2e676ca9e9bcf6997
=======
 *
>>>>>>> Project skeleton.
=======
 *
>>>>>>> Added Chamberlain MyQ skeleton
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WinkBinding} class defines common constants, which are used across the whole binding.
 *
 * @author Sebastien Marchand - Initial contribution
 */
public class WinkBindingConstants {

    public static final String BINDING_ID = "wink";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_WINK_HUB_2 = new ThingTypeUID(BINDING_ID, "wink_hub_2");
    public final static ThingTypeUID THING_TYPE_LIGHT_BULB = new ThingTypeUID(BINDING_ID, "light_bulb");
    public final static ThingTypeUID THING_TYPE_REMOTE = new ThingTypeUID(BINDING_ID, "remote");
<<<<<<< 60b2641262654f560ba41b55ecd404bec7547f0b
<<<<<<< 8b97becdef5db102e58dcf125a75329676144f43
<<<<<<< 22e7f0057024a151fbe7e0c2e676ca9e9bcf6997
	public final static ThingTypeUID THING_TYPE_BINARY_SWITCH = new ThingTypeUID(BINDING_ID, "binary_switch");

    // List of all Channel ids for a light bulb
    public final static String CHANNEL_LIGHTLEVEL = "lightlevel";
	public final static String CHANNEL_SWITCHSTATE = "switchstate";
=======

    // List of all Channel ids for a light bulb
    public final static String CHANNEL_LIGHTLEVEL = "lightlevel";
>>>>>>> Project skeleton.
=======
=======
>>>>>>> Added Chamberlain MyQ skeleton
    public final static ThingTypeUID THING_TYPE_BINARY_SWITCH = new ThingTypeUID(BINDING_ID, "binary_switch");

    // List of all Channel ids for a light bulb
    public final static String CHANNEL_LIGHTLEVEL = "lightlevel";
    public final static String CHANNEL_SWITCHSTATE = "switchstate";
<<<<<<< 60b2641262654f560ba41b55ecd404bec7547f0b
>>>>>>> added binary switch type
=======
>>>>>>> Added Chamberlain MyQ skeleton

    // REST URI constants
    public static final String WINK_URI = "https://api.wink.com/";
    public static final String WINK_DEVICES_REQUEST_PATH = "users/me/wink_devices";
    public static final String WINK_ACCESS_TOKEN = "access_token";
    public static final String WINK_REFRESH_TOKEN = "refresh_token";

    // Configuration of a device (JSON).
    public static final String WINK_DEVICE_CONFIG = "device_config_string";

    public static final String WINK_DEVICE_ID = "device_id";
}
