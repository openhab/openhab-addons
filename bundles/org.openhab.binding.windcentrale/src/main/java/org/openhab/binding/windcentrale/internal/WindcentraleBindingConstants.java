/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.windcentrale.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

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
