/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link TankerkoenigBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dennis Dollinger - Initial contribution
 */
public class TankerkoenigBindingConstants {

    public static final String BINDING_ID = "tankerkoenig";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_TANKSTELLE = new ThingTypeUID(BINDING_ID, "tankstelle");
    public final static ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "config");

    public final static Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_TANKSTELLE);

    // List of all Channel ids
    public final static String CHANNEL_DIESEL = "diesel";
    public final static String CHANNEL_E10 = "e10";
    public final static String CHANNEL_E5 = "e5";

    // config
    public final static String CONFIG_LOCATION_ID = "locationid";
    public final static String CONFIG_API_KEY = "apikey";
    public final static String CONFIG_REFRESH = "refresh";
    public final static String CONFIG_SETUP_MODE = "setupmode";

}
