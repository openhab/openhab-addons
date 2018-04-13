/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.stiebelheatpump;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link stiebelheatpumpBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Peter Kreutzer - Initial contribution
 */
public class stiebelheatpumpBindingConstants {

    public static final String CHANNELGROUPSEPERATOR = "#";
    public static final String BINDING_ID = "stiebelheatpump";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LWZ206 = new ThingTypeUID(BINDING_ID, "stiebelHeatPumpLWZ303_2_06");
    public static final ThingTypeUID THING_TYPE_LWZ236 = new ThingTypeUID(BINDING_ID, "stiebelHeatPumpLWZ303_2_36");
    public static final ThingTypeUID THING_TYPE_LWZ419 = new ThingTypeUID(BINDING_ID, "stiebelHeatPumpLWZ303_4_19");
    public static final ThingTypeUID THING_TYPE_LWZ509 = new ThingTypeUID(BINDING_ID, "stiebelHeatPumpLWZ303_5_09");
    public static final ThingTypeUID THING_TYPE_LWZ539 = new ThingTypeUID(BINDING_ID, "stiebelHeatPumpLWZ303_5_39");
    public static final ThingTypeUID THING_TYPE_LWZ739 = new ThingTypeUID(BINDING_ID, "stiebelHeatPumpLWZ303_7_39");

    // List of important Channel ids
    public static final String CHANNELGROUP_VERSION = "channelGroupTypeVersion";
    public static final String CHANNEL_VERSION = "version" + CHANNELGROUPSEPERATOR + "version";
    public static final String CHANNEL_SETTIME = "time" + CHANNELGROUPSEPERATOR + "setTime";
    public static final String CHANNEL_DUMPRESPONSE = "version" + CHANNELGROUPSEPERATOR + "dumpResponse";

    public static final String CHANNELTYPE_TIMESETTING = BINDING_ID + ":" + "timeSetting";
    public static final String CHANNELTYPE_SWITCHSETTING = BINDING_ID + ":" + "switchSetting";

    // Custom Properties
    public final static String PROPERTY_PORT = "port";
    public final static String PROPERTY_BAUDRATE = "baudRate";
    public final static String PROPERTY_WAITINGTIME = "waitingTime";
    public final static String PROPERTY_REFRESH = "refresh";

    public static final String CHANNEL_PROPERTY_REQUEST = "requestByte";
    public static final String CHANNEL_PROPERTY_POSITION = "position";
    public static final String CHANNEL_PROPERTY_LENGTH = "length";
    public static final String CHANNEL_PROPERTY_SCALE = "scale";
    public static final int MAXRETRY = 5;

    public static final byte[] DEBUGBYTES = { (byte) 0x01, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06,
            (byte) 0x07, (byte) 0x09, (byte) 0x10, (byte) 0x16, (byte) 0x17, (byte) 0xD1, (byte) 0xD2, (byte) 0xE8,
            (byte) 0xE9, (byte) 0xEE, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xFB,
            (byte) 0xFC, (byte) 0xFD, (byte) 0xFE, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E,
            (byte) 0x0F };
}
