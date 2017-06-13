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

    public static final String BINDING_ID = "stiebelheatpump";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LWZ206 = new ThingTypeUID(BINDING_ID, "stiebelHeatPumpLWZ303_2_06");
    public static final ThingTypeUID THING_TYPE_LWZ509 = new ThingTypeUID(BINDING_ID, "stiebelHeatPumpLWZ303_5_09");
    public static final ThingTypeUID THING_TYPE_LWZ539 = new ThingTypeUID(BINDING_ID, "stiebelHeatPumpLWZ303_5_39");

    // List of all Channel ids
    public static final String CHANNELGROUP_VERSION = "channelGroupTypeVersion";
    public static final String CHANNEL_VERSION = "version#version";

    // Custom Properties
    public final static String PROPERTY_PORT = "port";
    public final static String PROPERTY_BAUDRATE = "baudRate";
    public final static String PROPERTY_WAITINGTIME = "waitingTime";
    public final static String PROPERTY_REFRESH = "refresh";

    public static final String CHANNEL_PROPERTY_REQUEST = "requestByte";
    public static final String CHANNEL_PROPERTY_POSITION = "position";
    public static final String CHANNEL_PROPERTY_LENGTH = "length";
    public static final String CHANNEL_PROPERTY_SCALE = "scale";

    public static final String CHANNELGROUPSEPERATOR = "#";
    public static final int MAXRETRY = 5;
}
