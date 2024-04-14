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
package org.openhab.binding.gridbox.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GridBoxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class GridBoxBindingConstants {

    private static final String BINDING_ID = "gridbox";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GRIDBOX = new ThingTypeUID(BINDING_ID, "gridbox");

    // List of all Channel ids
    public static final String BATTERY_CAPACITY = "batteryCapacity";
    public static final String BATTERY_NOMINAL_CAPACITY = "batteryNominalCapacity";
    public static final String BATTERY_POWER = "batteryPower";
    public static final String BATTERY_REMAINING_CHARGE = "batteryRemainingCharge";
    public static final String BATTERY_STATE_OF_CHARGE = "batteryStateOfCharge";
    public static final String BATTERY_LEVEL = "batteryLevel";
    public static final String CONSUMPTION = "consumption";
    public static final String DIRECT_CONSUMPTION = "directConsumption";
    public static final String DIRECT_CONSUMPTION_EV = "directConsumptionEV";
    public static final String DIRECT_CONSUMPTION_HEAT_PUMP = "directConsumptionHeatPump";
    public static final String DIRECT_CONSUMPTION_HEATER = "directConsumptionHeater";
    public static final String DIRECT_CONSUMPTION_HOUSEHOLD = "directConsumptionHousehold";
    public static final String DIRECT_CONSUMPTION_RATE = "directConsumptionRate";
    public static final String EV_CHARGING_STATION_POWER = "evChargingStationPower";
    public static final String HEAT_PUMP_POWER = "heatPumpPower";
    public static final String PHOTOVOLTAIC_PRODUCTION = "photovoltaicProduction";
    public static final String PRODUCTION = "production";
    public static final String SELF_CONSUMPTION = "selfConsumption";
    public static final String SELF_CONSUMPTION_RATE = "selfConsumptionRate";
    public static final String SELF_SUFFICIENCY_RATE = "selfSufficiencyRate";
    public static final String SELF_SUPPLY = "selfSupply";
    public static final String TOTAL_CONSUMPTION = "totalConsumption";
}
