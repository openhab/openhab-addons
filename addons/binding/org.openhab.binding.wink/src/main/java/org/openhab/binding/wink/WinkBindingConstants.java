/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WinkBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sebastien Marchand - Initial contribution
 */
public class WinkBindingConstants {

    public static final String BINDING_ID = "wink";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_WINK_HUB_2 = new ThingTypeUID(BINDING_ID, "wink_hub_2");
    public final static ThingTypeUID THING_TYPE_LIGHT_BULB = new ThingTypeUID(BINDING_ID, "light_bulb");

    // List of all Channel ids for the common stuffs
    public final static String CHANNEL_NAME = "name";
    public final static String CHANNEL_MODEL_NAME = "model_name";
    public final static String CHANNEL_PUBNUB_SUBSCRIBE_KEY = "subscribe_key";
    public final static String CHANNEL_PUBNUB_CHANNEL = "channel";

    // List of all Channel ids for a light bulb
    public final static String CHANNEL_LIGHT_DESIRED_STATE_POWERED = "powered";
    public final static String CHANNEL_LIGHT_DESIRED_STATE_BRIGHTNESS = "brightness";

    // REST URI constants
    public static final String WINK_URI = "https://api.wink.com/";
    public static final String WINK_DEVICES_REQUEST_PATH = "users/me/wink_devices";
    public static final String WINK_ACCESS_TOKEN = "access_token";
    public static final String WINK_REFRESH_TOKEN = "refresh_token";
}
