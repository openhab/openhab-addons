/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    private final static String BINDING_ID = "senechome";
    private final static String THING_BASE_ID = "senechome";
    public final static ThingTypeUID THING_TYPE_SENEC_HOME_BATTERY = new ThingTypeUID(BINDING_ID, THING_BASE_ID);

    public final static String CHANNEL_SENEC_POWER_LIMITATION = "powerLimitation";
    public final static String CHANNEL_SENEC_POWER_LIMITATION_STATE = "powerLimitationState";
    public final static String CHANNEL_SENEC_BATTERY_STATE = "batteryState";
    public final static String CHANNEL_SENEC_POWER_CONSUMPTION = "houseConsumption";
    public final static String CHANNEL_SENEC_ENERGY_PRODUCTION = "energyProduction";
    public final static String CHANNEL_SENEC_BATTERY_POWER = "batteryPower";
    public final static String CHANNEL_SENEC_BATTERY_FUEL_CHARGE = "batteryFuelCharge";
    public final static String CHANNEL_SENEC_GRID_POWER = "gridPower";
    public final static String CHANNEL_SENEC_GRID_POWER_SUPPLY = "gridPowerSupply";
    public final static String CHANNEL_SENEC_GRID_POWER_DRAW = "gridPowerDraw";
}