/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SamsungTvBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SamsungTvBindingConstants {

    public static final String BINDING_ID = "samsungtv";

    public final static ThingTypeUID SAMSUNG_TV_THING_TYPE = new ThingTypeUID(BINDING_ID, "tv");

    // List of all remote controller thing channel id's
    public final static String KEY_CODE = "keyCode";
    public final static String POWER = "power";

    // List of all media renderer thing channel id's
    public final static String VOLUME = "volume";
    public final static String MUTE = "mute";
    public final static String BRIGHTNESS = "brightness";
    public final static String CONTRAST = "contrast";
    public final static String SHARPNESS = "sharpness";
    public final static String COLOR_TEMPERATURE = "colorTemperature";

    // List of all main TV server thing channel id's
    public final static String SOURCE_NAME = "sourceName";
    public final static String SOURCE_ID = "sourceId";
    public final static String CHANNEL = "channel";
    public final static String PROGRAM_TITLE = "programTitle";
    public final static String CHANNEL_NAME = "channelName";
    public final static String BROWSER_URL = "url";
    public final static String STOP_BROWSER = "stopBrowser";
}
