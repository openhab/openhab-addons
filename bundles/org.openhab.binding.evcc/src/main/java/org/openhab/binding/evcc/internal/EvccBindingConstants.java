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
package org.openhab.binding.evcc.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EvccBindingConstants} class contains fields mapping thing configuration parameters.
 *
 * @author Florian Hotze - Initial contribution
 * @author Luca Arnecke - Update to evcc version 0.123.1
 * @author Marcel Goerentz - Reworked the binding
 */
@NonNullByDefault
public class EvccBindingConstants {

    public static final String BINDING_ID = "evcc";

    public static final ThingTypeUID THING_TYPE_BATTERY = new ThingTypeUID(BINDING_ID, "battery");
    public static final ThingTypeUID THING_TYPE_HEATING = new ThingTypeUID(BINDING_ID, "heating");
    public static final ThingTypeUID THING_TYPE_LOADPOINT = new ThingTypeUID(BINDING_ID, "loadpoint");
    public static final ThingTypeUID THING_TYPE_PLAN = new ThingTypeUID(BINDING_ID, "plan");
    public static final ThingTypeUID THING_TYPE_PV = new ThingTypeUID(BINDING_ID, "pv");
    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final ThingTypeUID THING_TYPE_SITE = new ThingTypeUID(BINDING_ID, "site");
    public static final ThingTypeUID THING_TYPE_STATISTICS = new ThingTypeUID(BINDING_ID, "statistics");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BATTERY, THING_TYPE_HEATING,
            THING_TYPE_LOADPOINT, THING_TYPE_PLAN, THING_TYPE_PV, THING_TYPE_SITE, THING_TYPE_STATISTICS,
            THING_TYPE_VEHICLE);

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_INDEX = "index";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_SITE_TITLE = "siteTitle";
    public static final String PROPERTY_VEHICLE_ID = "vehicleId";

    public static final String API_PATH_LOADPOINTS = "/loadpoints";
    public static final String API_PATH_VEHICLES = "/vehicles";

    public static final String PROPERTY_TYPE_BATTERY = "battery";
    public static final String PROPERTY_TYPE_HEATING = "heating";
    public static final String PROPERTY_TYPE_LOADPOINT = "loadpoint";
    public static final String PROPERTY_TYPE_PLAN = "plan";
    public static final String PROPERTY_TYPE_PV = "pv";
    public static final String PROPERTY_TYPE_SITE = "site";
    public static final String PROPERTY_TYPE_STATISTICS = "statistics";
    public static final String PROPERTY_TYPE_VEHICLE = "vehicle";

    public static final String JSON_KEY_BATTERY = "battery";
    public static final String JSON_KEY_CHARGE_CURRENT = "chargeCurrent";
    public static final String JSON_KEY_CHARGE_CURRENTS = "chargeCurrents";
    public static final String JSON_KEY_CHARGE_VOLTAGES = "chargeVoltages";
    public static final String JSON_KEY_CHARGER_FEATURE_HEATING = "chargerFeatureHeating";
    public static final String JSON_KEY_CHARGING = "charging";
    public static final String JSON_KEY_CONNECTED = "connected";
    public static final String JSON_KEY_EFFECTIVE_LIMIT_SOC = "effectiveLimitSoc";
    public static final String JSON_KEY_EFFECTIVE_PLAN_SOC = "effectivePlanSoc";
    public static final String JSON_KEY_ENABLED = "enabled";
    public static final String JSON_KEY_GRID = "grid";
    public static final String JSON_KEY_GRID_CONFIGURED = "gridConfigured";
    public static final String JSON_KEY_LIMIT_SOC = "limitSoc";
    public static final String JSON_KEY_LOADPOINTS = "loadpoints";
    public static final String JSON_KEY_OFFERED_CURRENT = "offeredCurrent";
    public static final String JSON_KEY_PHASES = "phases";
    public static final String JSON_KEY_PHASES_CONFIGURED = "phasesConfigured";
    public static final String JSON_KEY_PLAN = "plan";
    public static final String JSON_KEY_PV = "pv";
    public static final String JSON_KEY_REPEATING_PLANS = "repeatingPlans";
    public static final String JSON_KEY_SMART_COST_TYPE = "smartCostType";
    public static final String JSON_KEY_STATISTICS = "statistics";
    public static final String JSON_KEY_TITLE = "title";
    public static final String JSON_KEY_VEHICLE_LIMIT_SOC = "vehicleLimitSoc";
    public static final String JSON_KEY_VEHICLE_PRESENT = "vehiclePresent";
    public static final String JSON_KEY_VEHICLE_SOC = "vehicleSoc";
    public static final String JSON_KEY_VEHICLES = "vehicles";
    public static final String JSON_KEY_WEEKDAYS = "weekdays";

    public static final String NUMBER_CURRENCY = CoreItemFactory.NUMBER + ":Currency";
    public static final String NUMBER_DIMENSIONLESS = CoreItemFactory.NUMBER + ":Dimensionless";
    public static final String NUMBER_ELECTRIC_CURRENT = CoreItemFactory.NUMBER + ":ElectricCurrent";
    public static final String NUMBER_EMISSION_INTENSITY = CoreItemFactory.NUMBER + ":EmissionIntensity";
    public static final String NUMBER_ENERGY = CoreItemFactory.NUMBER + ":Energy";
    public static final String NUMBER_POWER = CoreItemFactory.NUMBER + ":Power";
    public static final String NUMBER_TIME = CoreItemFactory.NUMBER + ":Time";
    public static final String NUMBER_LENGTH = CoreItemFactory.NUMBER + ":Length";
    public static final String NUMBER_ENERGY_PRICE = CoreItemFactory.NUMBER + ":EnergyPrice";
    public static final String NUMBER_TEMPERATURE = CoreItemFactory.NUMBER + ":Temperature";
}
