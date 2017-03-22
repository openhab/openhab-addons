/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SnapcastBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Steffen Folman Sørensen - Initial contribution
 */
public class SnapcastBindingConstants {

    public static final String BINDING_ID = "snapcast";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SNAPSERVER = new ThingTypeUID(BINDING_ID, "snapserver");
    public final static ThingTypeUID THING_TYPE_SNAPCLIENT = new ThingTypeUID(BINDING_ID, "snapclient");

    // List of all Channel ids
    public final static String CHANNEL_STREAM = "stream";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_NAME = "name";
    public final static String CHANNEL_MUTE = "mute";

    public static final String CONFIG_HOST_NAME = "HOST";
    public static final String CONFIG_MAC_ADDRESS = "MAC";
}