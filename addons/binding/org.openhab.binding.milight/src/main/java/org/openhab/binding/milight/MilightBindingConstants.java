/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link MilightBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
public class MilightBindingConstants {

    public static final String BINDING_ID = "milight";

    // List of all Thing Type UIDs
    public final static ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID RGB_THING_TYPE = new ThingTypeUID(BINDING_ID, "rgbLed");
    public final static ThingTypeUID WHITE_THING_TYPE = new ThingTypeUID(BINDING_ID, "whiteLed");

    public final static Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(RGB_THING_TYPE,
            WHITE_THING_TYPE);

    // List of all Channel ids
    public final static String CHANNEL_COLOR = "ledcolor";
    public final static String CHANNEL_NIGHTMODE = "lednightmode";
    public final static String CHANNEL_BRIGHTNESS = "ledbrightness";
    public final static String CHANNEL_TEMP = "ledtemperature";
    public final static String CHANNEL_SPEED = "discospeed";
    public final static String CHANNEL_MODE = "discomode";

    public static final int PORT_SEND_DISCOVER = 48899;
    // public static final int PORT_SEND = 8899;

    public static final String CONFIG_HOST_NAME = "ADDR";
    public static final String CONFIG_PORT = "PORT";
    public static final String CONFIG_ID = "ID";
    public static final String CONFIG_REFRESH = "REFRESH";

}
