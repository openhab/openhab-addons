/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link YeelightBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Coaster Li - Initial contribution
 */
public class YeelightBindingConstants {

    public static final String BINDING_ID = "yeelight";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CEILING = new ThingTypeUID(BINDING_ID, "ceiling");
    public static final ThingTypeUID THING_TYPE_CEILING1 = new ThingTypeUID(BINDING_ID, "ceiling1");
    public static final ThingTypeUID THING_TYPE_CEILING3 = new ThingTypeUID(BINDING_ID, "ceiling3");
    public static final ThingTypeUID THING_TYPE_DOLPHIN = new ThingTypeUID(BINDING_ID, "dolphin");
    public static final ThingTypeUID THING_TYPE_CTBULB = new ThingTypeUID(BINDING_ID, "ct_bulb");
    public static final ThingTypeUID THING_TYPE_WONDER = new ThingTypeUID(BINDING_ID, "wonder");
    public static final ThingTypeUID THING_TYPE_STRIPE = new ThingTypeUID(BINDING_ID, "stripe");

    // List of thing Parameters names
    public static final String PARAMETER_DEVICE_ID = "deviceId";

    // List of all Channel ids
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "colorTemperature";

    // Constants used
    public static final int COLOR_TEMPERATURE_MINIMUM = 1700;
    public static final int COLOR_TEMPERATURE_STEP = 48;
}
