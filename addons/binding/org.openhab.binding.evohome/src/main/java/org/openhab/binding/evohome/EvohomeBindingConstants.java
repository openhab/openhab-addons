/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link EvohomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jasper van Zuijlen - Initial contribution
 */
public class EvohomeBindingConstants {

    private static final String BINDING_ID = "evohome";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_EVOHOME_GATEWAY        = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_EVOHOME_DISPLAY        = new ThingTypeUID(BINDING_ID, "display");
    public static final ThingTypeUID THING_TYPE_EVOHOME_LOCATION       = new ThingTypeUID(BINDING_ID, "location");
    public static final ThingTypeUID THING_TYPE_EVOHOME_HEATING_ZONE   = new ThingTypeUID(BINDING_ID, "heatingzone");

    // List of all Channel ids
    public static final String TEMPERATURE_CHANNEL = "temperature";
    public static final String SET_POINT_CHANNEL   = "setpoint";
    public static final String SET_POINT_STATUS_CHANNEL = "setpointstatus";
    public static final String SYSTEM_MODE_CHANNEL = "SystemMode";

    // List of Discovery properties
    public static final String LOCATION_NAME   = "LOCATION_NAME";
    public static final String LOCATION_ID     = "LOCATION_ID";
    public static final String DEVICE_NAME     = "DEVICE_NAME";
    public static final String DEVICE_ID       = "DEVICE_ID";
    public static final String ZONE_ID         = "ZONE_ID";
    public static final String ZONE_NAME       = "ZONE_NAME";
    public static final String ZONE_TYPE       = "ZONE_TYPE";
    public static final String ZONE_MODEL_TYPE = "ZONE_MODEL_TYPE";

    // List of all addressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(
            THING_TYPE_EVOHOME_GATEWAY,
            THING_TYPE_EVOHOME_DISPLAY,
            THING_TYPE_EVOHOME_HEATING_ZONE);
}
