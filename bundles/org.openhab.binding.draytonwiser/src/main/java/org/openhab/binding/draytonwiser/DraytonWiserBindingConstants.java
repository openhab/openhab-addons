/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser;

import com.google.common.collect.Sets;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DraytonWiserBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class DraytonWiserBindingConstants {

    private static final String BINDING_ID = "draytonwiser";

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "heathub");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");
    public static final ThingTypeUID THING_TYPE_ROOMSTAT = new ThingTypeUID(BINDING_ID, "roomstat");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_TEMPERATURE = "currentTemperature-channel";
    public static final String CHANNEL_CURRENT_HUMIDITY = "currentHumidity-channel";
    public static final String CHANNEL_CURRENT_SETPOINT = "currentSetPoint-channel";
    public static final String CHANNEL_CURRENT_BATTERY_LEVEL = "currentBatteryLevel-channel";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_ROOM,
            THING_TYPE_ROOMSTAT);

}
