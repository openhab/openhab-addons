/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.teslapowerwall.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TeslaPowerwallBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class TeslaPowerwallBindingConstants {

    private static final String BINDING_ID = "teslapowerwall";

    // List of all Thing Type UIDs
    public static final ThingTypeUID TESLAPOWERWALL_THING = new ThingTypeUID(BINDING_ID, "tesla-powerwall");

    // List of all Channel ids
    public static final String CHANNEL_TESLAPOWERWALL_GRIDSTATUS = "grid-status";
    public static final String CHANNEL_TESLAPOWERWALL_GRIDSERVICES = "gridservices";
    public static final String CHANNEL_TESLAPOWERWALL_BATTERYSOE = "battery-soe";
    public static final String CHANNEL_TESLAPOWERWALL_MODE = "mode";
    public static final String CHANNEL_TESLAPOWERWALL_RESERVE = "reserve";
    public static final String CHANNEL_TESLAPOWERWALL_GRID_INSTPOWER = "grid-inst-power";
    public static final String CHANNEL_TESLAPOWERWALL_GRID_ENERGYEXPORTED = "grid-energy-exported";
    public static final String CHANNEL_TESLAPOWERWALL_GRID_ENERGYIMPORTED = "grid-energy-imported";
    public static final String CHANNEL_TESLAPOWERWALL_BATTERY_INSTPOWER = "battery-inst-power";
    public static final String CHANNEL_TESLAPOWERWALL_BATTERY_ENERGYIMPORTED = "battery-energy-imported";
    public static final String CHANNEL_TESLAPOWERWALL_BATTERY_ENERGYEXPORTED = "battery-energy-exported";
    public static final String CHANNEL_TESLAPOWERWALL_HOME_INSTPOWER = "home-inst-power";
    public static final String CHANNEL_TESLAPOWERWALL_HOME_ENERGYEXPORTED = "home-energy-exported";
    public static final String CHANNEL_TESLAPOWERWALL_HOME_ENERGYIMPORTED = "home-energy-imported";
    public static final String CHANNEL_TESLAPOWERWALL_SOLAR_INSTPOWER = "solar-inst-power";
    public static final String CHANNEL_TESLAPOWERWALL_SOLAR_ENERGYEXPORTED = "solar-energy-exported";
    public static final String CHANNEL_TESLAPOWERWALL_SOLAR_ENERGYIMPORTED = "solar-energy-imported";
    public static final String CHANNEL_TESLAPOWERWALL_FULL_PACK_ENERGY = "full-pack-energy";
    public static final String CHANNEL_TESLAPOWERWALL_DEGRADATION = "degradation";

    public static final int TESLA_POWERWALL_CAPACITY = 13500;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(TESLAPOWERWALL_THING);
}
