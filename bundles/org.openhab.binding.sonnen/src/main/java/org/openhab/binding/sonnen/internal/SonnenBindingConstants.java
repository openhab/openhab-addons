/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
    public static final String CHANNELBATTERYCHARGINGSTATE = "batteryChargingState";
    public static final String CHANNELBATTERYDISCHARGINGSTATE = "batteryDischargingState";
    public static final String CHANNELBATTERYCHARGING = "batteryCharging";
    public static final String CHANNELBATTERYDISCHARGING = "batteryDischarging";
    public static final String CHANNELCONSUMPTION = "consumption";
    public static final String CHANNELGRIDFEEDIN = "gridFeedIn";
    public static final String CHANNELGRIDCONSUMPTION = "gridConsumption";
    public static final String CHANNELSOLARPRODUCTION = "solarProduction";
    public static final String CHANNELBATTERYLEVEL = "batteryLevel";
    public static final String CHANNELFLOWCONSUMPTIONBATTERYSTATE = "flowConsumptionBatteryState";
    public static final String CHANNELFLOWCONSUMPTIONGRIDSTATE = "flowConsumptionGridState";
    public static final String CHANNELFLOWCONSUMPTIONPRODUCTIONSTATE = "flowConsumptionProductionState";
    public static final String CHANNELFLOWGRIDBATTERYSTATE = "flowGridBatteryState";
    public static final String CHANNELFLOWPRODUCTIONBATTERYSTATE = "flowProductionBatteryState";
    public static final String CHANNELFLOWPRODUCTIONGRIDSTATE = "flowProductionGridState";

    // List of new Channel ids
    public static final String KWHIMPORTEDSTATE = "kwhImportedState";
    public static final String KWHEXPORTEDSTATE = "kwhExportedState";

    // List of dynamic Channel ids
    // public static final String AL1STATE = "al1";
    // public static final String AL2STATE = "al2";
    // public static final String AL3STATE = "al3";
    // public static final String CHANNELSTATE = "channel";
    // public static final String DEVICEIDSTATE = "deviceid";
    // public static final String DIRECTIONSTATE = "direction";
    // public static final String ERRORSTATE = "error";
    // public static final String VL1L2STATE = "vl1l2";
    // public static final String VL1NSTATE = "vl1n";
    // public static final String VL2L3STATE = "vl2l3";
    // public static final String VL2NSTATE = "vl2n";
    // public static final String VL3L1STATE = "vl2l1";
    // public static final String VL3NSTATE = "vl3n";
    // public static final String VATOTALSTATE = "vatotal";
    // public static final String VARTOTALSTATE = "vartotal";
    // public static final String W11STATE = "w11";
    // public static final String W12STATE = "w12";
    // public static final String W13STATE = "w13";
    // public static final String WTOTALSTATE = "wtotal";

}
