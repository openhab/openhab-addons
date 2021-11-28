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
    public static final ThingTypeUID THING_TYPE_SONNEN = new ThingTypeUID(BINDING_ID, "battery");

    // List of all Channel ids
    public static final String CHANNELBATTERYCHARGING = "channelBatteryCharging";
    public static final String CHANNELBATTERYDISCHARGING = "channelBatteryDischarging";
    public static final String CHANNELCONSUMPTION = "channelConsumption";
    public static final String CHANNELGRIDFEEDIN = "channelGridFeedIn";
    public static final String CHANNELSOLARPRODUCTION = "channelSolarProduction";
    public static final String CHANNELBATTERYLEVEL = "channelBatteryLevel";
    public static final String CHANNELFLOWCONSUMPTIONBATTERY = "channelFlowConsumptionBattery";
    public static final String CHANNELFLOWCONSUMPTIONGRID = "channelFlowConsumptionGrid";
    public static final String CHANNELFLOWCONSUMPTIONPRODUCTION = "channelFlowConsumptionProduction";
    public static final String CHANNELFLOWGRIDBATTERY = "channelFlowGridBattery";
    public static final String CHANNELFLOWPRODUCTIONBATTERY = "channelFlowProductionBattery";
    public static final String CHANNELFLOWPRODUCTIONGRID = "channelFlowProductionGrid";
}
