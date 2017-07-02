/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ChromecastBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class ChromecastBindingConstants {

    public static final String BINDING_ID = "chromecast";

    public static final ThingTypeUID THING_TYPE_CHROMECAST = new ThingTypeUID(BINDING_ID, "chromecast");
    public static final ThingTypeUID THING_TYPE_AUDIO = new ThingTypeUID(BINDING_ID, "audio");
    public static final ThingTypeUID THING_TYPE_AUDIOGROUP = new ThingTypeUID(BINDING_ID, "audiogroup");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_AUDIO);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_AUDIOGROUP);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_CHROMECAST);
    }

    // channel IDs
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_PLAY_URI = "playuri";

    // config parameters
    public static final String HOST = "ipAddress";
    public static final String PORT = "port";

    // properties
    public static final String SERIAL_NUMBER = "serialNumber";

}
