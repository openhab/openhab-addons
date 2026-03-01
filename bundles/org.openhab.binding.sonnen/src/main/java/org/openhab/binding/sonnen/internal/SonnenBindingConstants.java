/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
    public static final String CHANNEL_BATTERY_CHARGING_STATE = "batteryChargingState";
    public static final String CHANNEL_BATTERY_DISCHARGING_STATE = "batteryDischargingState";
    public static final String CHANNEL_BATTERY_CHARGING = "batteryCharging";
    public static final String CHANNEL_BATTERY_DISCHARGING = "batteryDischarging";
    public static final String CHANNEL_CONSUMPTION = "consumption";
    public static final String CHANNEL_GRID_FEED_IN = "gridFeedIn";
    public static final String CHANNEL_GRID_CONSUMPTION = "gridConsumption";
    public static final String CHANNEL_SOLAR_PRODUCTION = "solarProduction";
    public static final String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_FLOW_CONSUMPTION_BATTERY_STATE = "flowConsumptionBatteryState";
    public static final String CHANNEL_FLOW_CONSUMPTION_GRID_STATE = "flowConsumptionGridState";
    public static final String CHANNEL_FLOW_CONSUMPTION_PRODUCTION_STATE = "flowConsumptionProductionState";
    public static final String CHANNEL_FLOW_GRID_BATTERY_STATE = "flowGridBatteryState";
    public static final String CHANNEL_FLOW_PRODUCTION_BATTERY_STATE = "flowProductionBatteryState";
    public static final String CHANNEL_FLOW_PRODUCTION_GRID_STATE = "flowProductionGridState";

    // List of new Channel ids for PowerMeter API
    public static final String CHANNEL_ENERGY_IMPORTED_STATE_PRODUCTION = "energyImportedStateProduction";
    public static final String CHANNEL_ENERGY_EXPORTED_STATE_PRODUCTION = "energyExportedStateProduction";
    public static final String CHANNEL_ENERGY_IMPORTED_STATE_CONSUMPTION = "energyImportedStateConsumption";
    public static final String CHANNEL_ENERGY_EXPORTED_STATE_CONSUMPTION = "energyExportedStateConsumption";

    // List of new Channel ids for battery charging from Grid
    public static final String CHANNEL_BATTERY_CHARGING_GRID = "batteryChargingFromGrid";
    public static final String CHANNEL_BATTERY_OPERATION_MODE = "batteryOperationMode";
    public static final String CHANNEL_BATTERY_DISCHARGE_RATE = "batteryChargeRate";

    // List of new Channel ids for battery discharging to Grid
    public static final String CHANNEL_BATTERY_DISCHARGING_GRID = "batteryDischargingToGrid";
    public static final String CHANNEL_BATTERY_CHARGE_RATE = "batteryDischargeRate";
}
