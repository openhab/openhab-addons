/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link TankerkoenigBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dennis Dollinger - Initial contribution
 */
@NonNullByDefault
public class TankerkoenigBindingConstants {

    public static final String BINDING_ID = "tankerkoenig";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_TANKSTELLE = new ThingTypeUID(BINDING_ID, "station");
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "webservice");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_TANKSTELLE);

    // List of all Channel ids
    public static final String CHANNEL_DIESEL = "diesel";
    public static final String CHANNEL_E10 = "e10";
    public static final String CHANNEL_E5 = "e5";
    public static final String CHANNEL_STATION_OPEN = "station_open";
    public static final String CHANNEL_HOLIDAY = "holiday";

    // config
    public static final String CONFIG_LOCATION_ID = "locationid";
    public static final String CONFIG_API_KEY = "apikey";
    public static final String CONFIG_REFRESH = "refresh";
    public static final String CONFIG_MODE_OPENINGTIME = "modeOpeningTime";

    // String used Identify unsucessful web-return
    public static final String NO_VALID_RESPONSE = "No valid response from the web-request!";

}
