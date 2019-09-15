/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.senechome.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SenecHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Steven Schwarznau - Initial contribution
 */
@NonNullByDefault
public class SenecHomeBindingConstants {
    private static final String BINDING_ID = "senechome";
    private static final String THING_BASE_ID = "senechome";
    public static final ThingTypeUID THING_TYPE_SENEC_HOME_BATTERY = new ThingTypeUID(BINDING_ID, THING_BASE_ID);

    public static final String CHANNEL_SENEC_POWER_LIMITATION = "powerLimitation";
    public static final String CHANNEL_SENEC_POWER_LIMITATION_STATE = "powerLimitationState";
    public static final String CHANNEL_SENEC_BATTERY_STATE = "batteryState";
    public static final String CHANNEL_SENEC_POWER_CONSUMPTION = "houseConsumption";
    public static final String CHANNEL_SENEC_ENERGY_PRODUCTION = "energyProduction";
    public static final String CHANNEL_SENEC_BATTERY_POWER = "batteryPower";
    public static final String CHANNEL_SENEC_BATTERY_FUEL_CHARGE = "batteryFuelCharge";
    public static final String CHANNEL_SENEC_GRID_POWER = "gridPower";
    public static final String CHANNEL_SENEC_GRID_POWER_SUPPLY = "gridPowerSupply";
    public static final String CHANNEL_SENEC_GRID_POWER_DRAW = "gridPowerDraw";
}