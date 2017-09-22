/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.co2signal;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link CO2SignalBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jens Viebig - Initial contribution
 */
public class CO2SignalBindingConstants {

    public static final String BINDING_ID = "co2signal";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CO2 = new ThingTypeUID(BINDING_ID, "co2signal");

    // List of all Channel id's
    public static final String COUNTRYCODE = "countryCode";
    public static final String CARBONINTENSITY = "carbonIntensity";
    public static final String FOSSILFUELPERCENTAGE = "fossilFuelPercentage";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_CO2);
    public static final Set<String> SUPPORTED_CHANNEL_IDS = ImmutableSet.of(COUNTRYCODE, CARBONINTENSITY,
            FOSSILFUELPERCENTAGE);

}
