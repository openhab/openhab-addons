/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mpower;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link mPowerBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marko Donke - Initial contribution
 */
public class MpowerBindingConstants {

    public static final String BINDING_ID = "mpower";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_MPOWER = new ThingTypeUID(BINDING_ID, "mpower");
    public final static ThingTypeUID THING_TYPE_SOCKET = new ThingTypeUID(BINDING_ID, "socket");

    // List of all Channel ids
    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_ENERGY = "energy";
    public final static String CHANNEL_VOLTAGE = "voltage";
    public final static String CHANNEL_OUTLET = "outlet";
    public final static String CHANNEL_LASTUPDATE = "lastupdate";

    // properties of things
    public final static String HOST_PROP_NAME = "host";
    public final static String DEVICE_MODEL_PROP_NAME = "deviceModel";
    public final static String SOCKET_NUMBER_PROP_NAME = "socketNumber";
    public final static String FIRMWARE_PROP_NAME = "firmware";

    public final static Set<String> SUPPORTED_DEVICE_MODELS = ImmutableSet.of("P1E", "P3E", "P6E", "P1U", "P3U", "P8U");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_MPOWER,
            THING_TYPE_SOCKET);
    public final static Set<ThingTypeUID> SUPPORTED_SOCKET_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_SOCKET);
    public final static String MANUFACTURER = "Ubiquiti Networks";

    // once a socket has been switched we wait 3 seconds until we update the channels
    public final static long waitAfterSwitch = 3000;
}
