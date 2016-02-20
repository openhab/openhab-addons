/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link ZWaveBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class ZWaveBindingConstants {

    public static final String BINDING_ID = "zwave";

    // Controllers
    public final static ThingTypeUID CONTROLLER_SERIAL = new ThingTypeUID(BINDING_ID, "serial_zstick");

    public final static String CONFIGURATION_PORT = "port";
    public final static String CONFIGURATION_MASTER = "master";
    public final static String CONFIGURATION_SUC = "SUC";

    public final static String UNKNOWN_THING = BINDING_ID + ":unknown";

    public final static String PARAMETER_NODEID = "nodeid";
    public final static String PARAMETER_POLLPERIOD = "binding_pollperiod";

    public final static String CHANNEL_SERIAL_SOF = "serial_sof";
    public final static String CHANNEL_SERIAL_ACK = "serial_ack";
    public final static String CHANNEL_SERIAL_NAK = "serial_nak";
    public final static String CHANNEL_SERIAL_CAN = "serial_can";
    public final static String CHANNEL_SERIAL_OOF = "serial_oof";

    public final static String CHANNEL_CFG_BINDING = "binding";
    public final static String CHANNEL_CFG_COMMANDCLASS = "commandClass";

    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = ImmutableSet.of(CONTROLLER_SERIAL);
}
