/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roomba;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link roombaBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stephen Liang - Initial contribution
 */
public class RoombaBindingConstants {

    public static final String BINDING_ID = "roomba";

    // Roomba SKUs
    public final static String ROOMBA_980_SKU = "R98----";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_IROBOT_ROOMBA = new ThingTypeUID(BINDING_ID, ROOMBA_980_SKU);

    // List of all Channel ids
    public final static String CHANNEL_START = "start";
    public final static String CHANNEL_STOP = "stop";
    public final static String CHANNEL_DOCK = "dock";
    public final static String CHANNEL_PAUSE = "pause";
    public final static String CHANNEL_RESUME = "resume";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_IROBOT_ROOMBA);

    // Thing Properties
    public final static String THING_PROPERTY_IP_ADDRESS = "ipAddress";
    public final static String THING_PROPERTY_PASSWORD = "password";

    // Roomba Discovery
    public final static String BROADCAST_ADDRESS = "255.255.255.255";
    public final static int BROADCAST_PORT = 5678;
    public final static String BROADCAST_MESSAGE = "irobotmcs";

    // Roomba API
    public final static String ROOMBA_USER_AGENT = "aspen%20production/2618 CFNetwork/758.3.15 Darwin/15.4.0";
    public final static long ROOMBA_API_TIMEOUT = 10000L;
}
