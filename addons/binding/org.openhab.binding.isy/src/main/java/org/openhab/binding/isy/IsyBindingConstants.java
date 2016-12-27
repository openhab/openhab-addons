/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.isy;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link IsyBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Craig Hamilton - Initial contribution
 */
public class IsyBindingConstants {

    public static final String BINDING_ID = "isy";

    public final static ThingTypeUID THING_TYPE_ISYBRIDGE = new ThingTypeUID(BINDING_ID, "isyBridge");

    // List of all Thing Type UIDs
    public final static ThingTypeUID MOTION_THING_TYPE = new ThingTypeUID(BINDING_ID, "motion");
    public final static ThingTypeUID DIMMER_THING_TYPE = new ThingTypeUID(BINDING_ID, "dimmer");
    public final static ThingTypeUID SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, "switch");
    public final static ThingTypeUID LEAKDETECTOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "leakdetector");
    public final static ThingTypeUID RELAY_THING_TYPE = new ThingTypeUID(BINDING_ID, "relay");
    public final static ThingTypeUID GARAGEDOORKIT_THING_TYPE = new ThingTypeUID(BINDING_ID, "garage");
    public final static ThingTypeUID KEYPAD_LINC_6_THING_TYPE = new ThingTypeUID(BINDING_ID, "keypadlinc6");
    public final static ThingTypeUID KEYPAD_LINC_5_THING_TYPE = new ThingTypeUID(BINDING_ID, "keypadlinc5");
    public final static ThingTypeUID REMOTELINC_8_THING_TYPE = new ThingTypeUID(BINDING_ID, "remotelinc8");
    public final static ThingTypeUID INLINELINC_SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, "inlinelincswitch");

    // List of all Channel ids
    // public final static String CHANNEL_ONOFFSENSOR = "OL";
    public final static String CHANNEL_LIGHTLEVEL = "lightlevel";

    public final static String CHANNEL_SWITCH = "state";
    // motion
    public final static String CHANNEL_MOTION_MOTION = "motion";
    public final static String CHANNEL_MOTION_DUSK = "dusk_dawn";
    public final static String CHANNEL_MOTION_BATTERY = "low_battery";
    // garage
    public final static String CHANNEL_GARAGE_CONTACT = "relay";
    public final static String CHANNEL_GARAGE_SENSOR = "contactSensor";
    // leak
    public final static String CHANNEL_LEAK_DRY = "dry";
    public final static String CHANNEL_LEAK_WET = "wet";
    public final static String CHANNEL_LEAK_HEARTBEAT = "heartbeat";

    public final static String CHANNEL_KEYPAD_LINC_A = "button_a";
    public final static String CHANNEL_KEYPAD_LINC_B = "button_b";
    public final static String CHANNEL_KEYPAD_LINC_C = "button_c";
    public final static String CHANNEL_KEYPAD_LINC_D = "button_d";
    public final static String CHANNEL_KEYPAD_LINC_E = "button_e";
    public final static String CHANNEL_KEYPAD_LINC_F = "button_f";
    public final static String CHANNEL_KEYPAD_LINC_G = "button_g";
    public final static String CHANNEL_KEYPAD_LINC_H = "button_h";

}
