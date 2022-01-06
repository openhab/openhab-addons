/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yeelight.internal;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link YeelightBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Coaster Li - Initial contribution
 * @author Joe Ho - Added Duration / Added command channel
 */
public class YeelightBindingConstants {

    public static final String BINDING_ID = "yeelight";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CEILING = new ThingTypeUID(BINDING_ID, "ceiling");
    public static final ThingTypeUID THING_TYPE_CEILING1 = new ThingTypeUID(BINDING_ID, "ceiling1");
    public static final ThingTypeUID THING_TYPE_CEILING3 = new ThingTypeUID(BINDING_ID, "ceiling3");
    public static final ThingTypeUID THING_TYPE_CEILING4 = new ThingTypeUID(BINDING_ID, "ceiling4");
    public static final ThingTypeUID THING_TYPE_DOLPHIN = new ThingTypeUID(BINDING_ID, "dolphin");
    public static final ThingTypeUID THING_TYPE_CTBULB = new ThingTypeUID(BINDING_ID, "ct_bulb");
    public static final ThingTypeUID THING_TYPE_WONDER = new ThingTypeUID(BINDING_ID, "wonder");
    public static final ThingTypeUID THING_TYPE_STRIPE = new ThingTypeUID(BINDING_ID, "stripe");
    public static final ThingTypeUID THING_TYPE_DESKLAMP = new ThingTypeUID(BINDING_ID, "desklamp");

    // List of thing Parameters names
    public static final String PARAMETER_DEVICE_ID = "deviceId";
    public static final String PARAMETER_DURATION = "duration";

    // List of all Channel ids
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "colorTemperature";
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_BACKGROUND_COLOR = "backgroundColor";
    public static final String CHANNEL_NIGHTLIGHT = "nightlight";

    // Constants used
    public static final int COLOR_TEMPERATURE_MINIMUM = 1700;
    public static final int COLOR_TEMPERATURE_STEP = 48;
}
