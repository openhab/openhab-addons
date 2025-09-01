/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final ThingTypeUID THING_TYPE_SITE = new ThingTypeUID(BINDING_ID, "site");
    public static final ThingTypeUID THING_TYPE_LOADPOINT = new ThingTypeUID(BINDING_ID, "loadpoint");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");
    public static final ThingTypeUID THING_TYPE_PV = new ThingTypeUID(BINDING_ID, "pv");
    public static final ThingTypeUID THING_TYPE_BATTERY = new ThingTypeUID(BINDING_ID, "battery");
    public static final ThingTypeUID THING_TYPE_HEATING = new ThingTypeUID(BINDING_ID, "heating");
    public static final ThingTypeUID THING_TYPE_STATISTICS = new ThingTypeUID(BINDING_ID, "statistics");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SITE, THING_TYPE_VEHICLE,
            THING_TYPE_LOADPOINT, THING_TYPE_BATTERY, THING_TYPE_PV, THING_TYPE_HEATING, THING_TYPE_STATISTICS);

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_INDEX = "index";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_SITE_TITLE = "siteTitle";

    public static final String API_PATH_LOADPOINTS = "/loadpoints";
    public static final String API_PATH_VEHICLES = "/vehicles";

    public static final String PROPERTY_TYPE_BATTERY = "battery";
    public static final String PROPERTY_TYPE_HEATING = "heating";
    public static final String PROPERTY_TYPE_LOADPOINT = "loadpoint";
    public static final String PROPERTY_TYPE_PV = "pv";
    public static final String PROPERTY_TYPE_SITE = "site";
    public static final String PROPERTY_TYPE_STATISTICS = "statistics";
    public static final String PROPERTY_TYPE_VEHICLE = "vehicle";

    public static final String JSON_MEMBER_BATTERY = "battery";
    public static final String JSON_MEMBER_LOADPOINTS = "loadpoints";
    public static final String JSON_MEMBER_PV = "pv";
    public static final String JSON_MEMBER_STATISTICS = "statistics";
    public static final String JSON_MEMBER_VEHICLES = "vehicles";
    public static final String JSON_MEMBER_CHARGER_FEATURE_HEATING = "chargerFeatureHeating";

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
