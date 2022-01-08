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
package org.openhab.binding.sonnen.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SonnenBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class SonnenBindingConstants {

    private static final String BINDING_ID = "sonnen";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BATTERY = new ThingTypeUID(BINDING_ID, "sonnenbattery");

    // List of all Channel ids
    public static final String CHANNELBATTERYCHARGING = "batteryCharging";
    public static final String CHANNELBATTERYDISCHARGING = "batteryDischarging";
    public static final String CHANNELBATTERYFEEDIN = "batteryFeedIn";
    public static final String CHANNELBATTERYDISPENSE = "batteryDispense";
    public static final String CHANNELCONSUMPTION = "consumption";
    public static final String CHANNELGRIDFEEDIN = "gridFeedIn";
    public static final String CHANNELGRIDRECEIVE = "gridReceive";
    public static final String CHANNELSOLARPRODUCTION = "solarProduction";
    public static final String CHANNELBATTERYLEVEL = "batteryLevel";
    public static final String CHANNELFLOWCONSUMPTIONBATTERY = "flowConsumptionBattery";
    public static final String CHANNELFLOWCONSUMPTIONGRID = "flowConsumptionGrid";
    public static final String CHANNELFLOWCONSUMPTIONPRODUCTION = "flowConsumptionProduction";
    public static final String CHANNELFLOWGRIDBATTERY = "flowGridBattery";
    public static final String CHANNELFLOWPRODUCTIONBATTERY = "flowProductionBattery";
    public static final String CHANNELFLOWPRODUCTIONGRID = "flowProductionGrid";
}
