/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HDPowerViewBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andy Lintner - Initial contribution
 */
public class HDPowerViewBindingConstants {

    public static final String BINDING_ID = "hdpowerview";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_HUB = new ThingTypeUID(BINDING_ID, "hub");
    public final static ThingTypeUID THING_TYPE_SHADE = new ThingTypeUID(BINDING_ID, "shade");

    // List of all Channel ids
    public final static String CHANNEL_SHADE_POSITION = "position";
    public final static String CHANNEL_SHADE_VANE = "vane";
    public final static String CHANNEL_SHADE_LOW_BATTERY = "lowBattery";

    public final static String CHANNELTYPE_SCENE_ACTIVATE = "scene-activate";

    public final static String NETBIOS_NAME = "PDBU-Hub3.0";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_HUB);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_SHADE);
    }

}
