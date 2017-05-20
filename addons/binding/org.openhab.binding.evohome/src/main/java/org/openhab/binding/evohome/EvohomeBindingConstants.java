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
    public static final ThingTypeUID THING_TYPE_EVOHOME_RADIATOR_VALVE = new ThingTypeUID(BINDING_ID, "radiatorValve");
    public static final ThingTypeUID THING_TYPE_EVOHOME_LOCATION       = new ThingTypeUID(BINDING_ID, "location");

    // List of all Channel ids
    public static final String TEMPERATURE_CHANNEL = "temperature";
    public static final String SET_POINT_CHANNEL   = "setpoint";

    // List of all adressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_EVOHOME_GATEWAY,
            THING_TYPE_EVOHOME_RADIATOR_VALVE);

}
