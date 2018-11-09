/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Björn Brings - Initial contribution
 */

public class BindingConstants {

    private static final String BINDING_ID = "deconz";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "deconz");
    public static final ThingTypeUID THING_TYPE_PRESENCE_SENSOR = new ThingTypeUID(BINDING_ID, "presencesensor");
    public static final ThingTypeUID THING_TYPE_POWER_SENSOR = new ThingTypeUID(BINDING_ID, "powersensor");
    public static final ThingTypeUID THING_TYPE_DAYLIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "daylightsensor");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");

    // List of all Channel ids
    public static final String CHANNEL_PRESENCE = "presence";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_VALUE = "value";
    public static final String CHANNEL_LIGHT = "light";
    public static final String CHANNEL_BUTTONEVENT = "buttonevent";

    // Thing configuration
    public static final String CONFIG_IP = "ip";
    public static final String CONFIG_APIKEY = "apikey";
}
