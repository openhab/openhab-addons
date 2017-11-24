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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link CO2SignalBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jens Viebig - Initial contribution
 */
public class CO2SignalBindingConstants {

    public static final String LOCAL = "local";

    public static final String BINDING_ID = "co2signal";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CO2 = new ThingTypeUID(BINDING_ID, "co2signal");

    // List of all Channel id's
    public static final String COUNTRYCODE = "countryCode";
    public static final String CARBONINTENSITY = "carbonIntensity";
    public static final String FOSSILFUELPERCENTAGE = "fossilFuelPercentage";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_CO2)
            .collect(Collectors.toSet());
    public static final Set<String> SUPPORTED_CHANNEL_IDS = Stream
            .of(COUNTRYCODE, CARBONINTENSITY, FOSSILFUELPERCENTAGE).collect(Collectors.toSet());

}
