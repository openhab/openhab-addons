/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
    public static final String CHANNELBATTERYCHARGING = "BatteryCharging";
    public static final String CHANNELBATTERYDISCHARGING = "BatteryDischarging";
    public static final String CHANNELBATTERYFEEDIN = "BatteryFeedIn";
    public static final String CHANNELBATTERYDISPENSE = "BatteryDispense";
    public static final String CHANNELCONSUMPTION = "Consumption";
    public static final String CHANNELGRIDFEEDIN = "GridFeedIn";
    public static final String CHANNELGRIDRECEIVE = "GridReceive";
    public static final String CHANNELSOLARPRODUCTION = "SolarProduction";
    public static final String CHANNELBATTERYLEVEL = "BatteryLevel";
    public static final String CHANNELFLOWCONSUMPTIONBATTERY = "FlowConsumptionBattery";
    public static final String CHANNELFLOWCONSUMPTIONGRID = "FlowConsumptionGrid";
    public static final String CHANNELFLOWCONSUMPTIONPRODUCTION = "FlowConsumptionProduction";
    public static final String CHANNELFLOWGRIDBATTERY = "FlowGridBattery";
    public static final String CHANNELFLOWPRODUCTIONBATTERY = "FlowProductionBattery";
    public static final String CHANNELFLOWPRODUCTIONGRID = "FlowProductionGrid";
}
