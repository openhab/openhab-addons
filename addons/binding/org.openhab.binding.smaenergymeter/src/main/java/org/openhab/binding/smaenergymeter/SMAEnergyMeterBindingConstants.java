/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smaenergymeter;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SMAEnergyMeterBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Osman Basha - Initial contribution
 */
public class SMAEnergyMeterBindingConstants {

    public static final String BINDING_ID = "smaenergymeter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGY_METER = new ThingTypeUID(BINDING_ID, "energymeter");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_ENERGY_METER);

    // List of all Channel IDs
    public static final String CHANNEL_POWER_IN = "powerIn";
    public static final String CHANNEL_POWER_OUT = "powerOut";
    public static final String CHANNEL_ENERGY_IN = "energyIn";
    public static final String CHANNEL_ENERGY_OUT = "energyOut";

}
