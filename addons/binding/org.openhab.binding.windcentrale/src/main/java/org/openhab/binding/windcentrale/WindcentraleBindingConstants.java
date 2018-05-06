/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.windcentrale;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WindcentraleBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public final class WindcentraleBindingConstants {

    public static final String BINDING_ID = "windcentrale";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MILL = new ThingTypeUID(BINDING_ID, "mill");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MILL);

    // List of all Channel IDs
    public static final String CHANNEL_WIND_SPEED = "windSpeed";
    public static final String CHANNEL_WIND_DIRECTION = "windDirection";
    public static final String CHANNEL_POWER_TOTAL = "powerAbsTot";
    public static final String CHANNEL_POWER_PER_WD = "powerAbsWd";
    public static final String CHANNEL_POWER_RELATIVE = "powerRel";
    public static final String CHANNEL_ENERGY = "kwh";
    public static final String CHANNEL_ENERGY_FC = "kwhForecast";
    public static final String CHANNEL_RUNTIME = "runTime";
    public static final String CHANNEL_RUNTIME_PER = "runPercentage";
    public static final String CHANNEL_LAST_UPDATE = "timestamp";

    public static final String PROPERTY_MILL_ID = "millId";
    public static final String PROPERTY_QTY_WINDDELEN = "wd";
    public static final String PROPERTY_REFRESH_INTERVAL = "refreshInterval";

}
