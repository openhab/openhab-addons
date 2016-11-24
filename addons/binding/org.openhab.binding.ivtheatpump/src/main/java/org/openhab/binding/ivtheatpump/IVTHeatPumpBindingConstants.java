/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ivtheatpump;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link IVTHeatPumpBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class IVTHeatPumpBindingConstants {

    public static final String BINDING_ID = "ivtheatpump";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_IP_REGO6XX = new ThingTypeUID(BINDING_ID, "ipRego6xx");

    // List of all Channel ids
    public final static String CHANNEL_GROUP_SENSORS = "sensors#";

    public final static String HOST_PARAMETER = "address";
    public final static String TCP_PORT_PARAMETER = "port";
    public final static String REFRESH_INTERVAL = "refreshInterval";
}
