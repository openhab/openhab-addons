/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eightdevices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EightDevicesBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nedas - Initial contribution
 */
@NonNullByDefault
public class EightDevicesBindingConstants {

    private static final String BINDING_ID = "eightdevices";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_3700 = new ThingTypeUID(BINDING_ID, "s3700");
    public static final ThingTypeUID THING_TYPE_3800 = new ThingTypeUID(BINDING_ID, "s3800");
    public static final ThingTypeUID THING_TYPE_4400 = new ThingTypeUID(BINDING_ID, "s4400");
    public static final ThingTypeUID THING_TYPE_4500 = new ThingTypeUID(BINDING_ID, "s4500");

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_ACTIVE_POWER = "activePower";
    public static final String CHANNEL_ACTIVE_ENERGY = "activeEnergy";
    public static final String CHANNEL_REACTIVE_POWER = "reactivePower";
    public static final String CHANNEL_REACTIVE_ENERGY = "reactiveEnergy";
    public static final String CHANNEL_RELAY = "relay";
    public static final String CHANNEL_MAGNETIC_FIELD = "magneticField";
    public static final String CHANNEL_MAGNETIC_COUNTER = "magneticCounter";
    public static final String CHANNEL_VOLTAGE = "voltage";
}
