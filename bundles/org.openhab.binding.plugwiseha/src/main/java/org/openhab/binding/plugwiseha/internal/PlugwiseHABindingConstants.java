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
package org.openhab.binding.plugwiseha.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link PlugwiseHABindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 */
@NonNullByDefault
public class PlugwiseHABindingConstants {

    public static final String BINDING_ID = "plugwiseha";

    // List of PlugwiseHA services, related urls, information

    public static final String PLUGWISEHA_API_URL = "http://%s";
    public static final String PLUGWISEHA_API_APPLIANCES_URL = PLUGWISEHA_API_URL + "/core/appliances";
    public static final String PLUGWISEHA_API_APPLIANCE_URL = PLUGWISEHA_API_URL + "/core/appliances;id=%s";
    public static final String PLUGWISEHA_API_LOCATIONS_URL = PLUGWISEHA_API_URL + "/core/locations";
    public static final String PLUGWISEHA_API_LOCATION_URL = PLUGWISEHA_API_URL + "/core/locations;id=%s";

    // Bridge
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_APPLIANCE_VALVE = new ThingTypeUID(BINDING_ID, "appliance_valve");
    public static final ThingTypeUID THING_TYPE_APPLIANCE_PUMP = new ThingTypeUID(BINDING_ID, "appliance_pump");
    public static final ThingTypeUID THING_TYPE_APPLIANCE_THERMOSTAT = new ThingTypeUID(BINDING_ID,
            "appliance_thermostat");
    public static final ThingTypeUID THING_TYPE_APPLIANCE_BOILER = new ThingTypeUID(BINDING_ID, "appliance_boiler");

    // List of channel Type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_BATTERYLEVEL = new ChannelTypeUID("system:battery-level");
    public static final ChannelTypeUID CHANNEL_TYPE_BATTERYLEVELLOW = new ChannelTypeUID("system:low-battery");

    // Empty set
    public static final Set<ThingTypeUID> SUPPORTED_INTERFACE_TYPES_UIDS_EMPTY = Set.of();

    // List of all Gateway configuration properties
    public static final String GATEWAY_CONFIG_HOST = "host";
    public static final String GATEWAY_CONFIG_USERNAME = "username";
    public static final String GATEWAY_CONFIG_SMILEID = "smileId";
    public static final String GATEWAY_CONFIG_REFRESH = "refresh";

    // List of all Zone configuration properties
    public static final String ZONE_CONFIG_ID = "id";
    public static final String ZONE_CONFIG_NAME = "zoneName";

    // List of all Appliance configuration properties
    public static final String APPLIANCE_CONFIG_ID = "id";
    public static final String APPLIANCE_CONFIG_NAME = "applianceName";
    public static final String APPLIANCE_CONFIG_LOWBATTERY = "lowBatteryPercentage";

    // List of all Appliance properties
    public static final String APPLIANCE_PROPERTY_DESCRIPTION = "description";
    public static final String APPLIANCE_PROPERTY_TYPE = "type";
    public static final String APPLIANCE_PROPERTY_FUNCTIONALITIES = "functionalities";
    public static final String APPLIANCE_PROPERTY_ZB_TYPE = "zigbee type";
    public static final String APPLIANCE_PROPERTY_ZB_REACHABLE = "zigbee reachable";
    public static final String APPLIANCE_PROPERTY_ZB_POWERSOURCE = "zigboo power source";

    // List of all Location properties
    public static final String LOCATION_PROPERTY_DESCRIPTION = "description";
    public static final String LOCATION_PROPERTY_TYPE = "type";
    public static final String LOCATION_PROPERTY_FUNCTIONALITIES = "functionalities";

    // List of all Channel IDs
    public static final String ZONE_SETPOINT_CHANNEL = "setpointTemperature";
    public static final String ZONE_TEMPERATURE_CHANNEL = "temperature";
    public static final String ZONE_PRESETSCENE_CHANNEL = "presetScene";
    public static final String ZONE_PREHEAT_CHANNEL = "preHeat";

    public static final String APPLIANCE_SETPOINT_CHANNEL = "setpointTemperature";
    public static final String APPLIANCE_TEMPERATURE_CHANNEL = "temperature";
    public static final String APPLIANCE_BATTERYLEVEL_CHANNEL = "batteryLevel";
    public static final String APPLIANCE_BATTERYLEVELLOW_CHANNEL = "batteryLevelLow";
    public static final String APPLIANCE_POWER_USAGE_CHANNEL = "powerUsage";
    public static final String APPLIANCE_POWER_CHANNEL = "power";
    public static final String APPLIANCE_LOCK_CHANNEL = "lock";
    public static final String APPLIANCE_WATERPRESSURE_CHANNEL = "waterPressure";
    public static final String APPLIANCE_DHWSTATE_CHANNEL = "dhwState";
    public static final String APPLIANCE_CHSTATE_CHANNEL = "chState";
    public static final String APPLIANCE_OFFSET_CHANNEL = "offsetTemperature";
    public static final String APPLIANCE_VALVEPOSITION_CHANNEL = "valvePosition";
    public static final String APPLIANCE_COOLINGSTATE_CHANNEL = "coolingState";
    public static final String APPLIANCE_INTENDEDBOILERTEMP_CHANNEL = "intendedBoilerTemp";
    public static final String APPLIANCE_FLAMESTATE_CHANNEL = "flameState";
    public static final String APPLIANCE_INTENDEDHEATINGSTATE_CHANNEL = "intendedHeatingState";
    public static final String APPLIANCE_MODULATIONLEVEL_CHANNEL = "modulationLevel";
    public static final String APPLIANCE_OTAPPLICATIONFAULTCODE_CHANNEL = "otAppFaultCode";
    public static final String APPLIANCE_DHWTEMPERATURE_CHANNEL = "dhwTemperature";
    public static final String APPLIANCE_OTOEMFAULTCODE_CHANNEL = "otOEMFaultCode";
    public static final String APPLIANCE_BOILERTEMPERATURE_CHANNEL = "boilerTemperature";
    public static final String APPLIANCE_DHWSETPOINT_CHANNEL = "dhwSetpoint";
    public static final String APPLIANCE_MAXBOILERTEMPERATURE_CHANNEL = "maxBoilerTemperature";
    public static final String APPLIANCE_DHWCOMFORTMODE_CHANNEL = "dhwComfortMode";

    // List of all Appliance Types
    public static final String APPLIANCE_TYPE_THERMOSTAT = "thermostat";
    public static final String APPLIANCE_TYPE_GATEWAY = "gateway";
    public static final String APPLIANCE_TYPE_CENTRALHEATINGPUMP = "central_heating_pump";
    public static final String APPLIANCE_TYPE_OPENTHERMGATEWAY = "open_therm_gateway";
    public static final String APPLIANCE_TYPE_ZONETHERMOSTAT = "zone_thermostat";
    public static final String APPLIANCE_TYPE_HEATERCENTRAL = "heater_central";
    public static final String APPLIANCE_TYPE_THERMOSTATICRADIATORVALUE = "thermostatic_radiator_valve";

    // List of Plugwise Maesure Units
    public static final String UNIT_CELSIUS = "C";

    // Supported things
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ZONE,
            THING_TYPE_APPLIANCE_VALVE, THING_TYPE_APPLIANCE_PUMP, THING_TYPE_APPLIANCE_BOILER);

    // Appliance types known to binding
    public static final Set<String> KNOWN_APPLIANCE_TYPES = Set.of(APPLIANCE_TYPE_THERMOSTAT, APPLIANCE_TYPE_GATEWAY,
            APPLIANCE_TYPE_CENTRALHEATINGPUMP, APPLIANCE_TYPE_OPENTHERMGATEWAY, APPLIANCE_TYPE_ZONETHERMOSTAT,
            APPLIANCE_TYPE_HEATERCENTRAL, APPLIANCE_TYPE_THERMOSTATICRADIATORVALUE);

    public static final Set<String> SUPPORTED_APPLIANCE_TYPES = Set.of(APPLIANCE_TYPE_CENTRALHEATINGPUMP,
            APPLIANCE_TYPE_THERMOSTATICRADIATORVALUE, APPLIANCE_TYPE_ZONETHERMOSTAT, APPLIANCE_TYPE_HEATERCENTRAL);

    // Supported bridges
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = Set.of(THING_TYPE_GATEWAY);

    // Getters & Setters
    public static String getApiUrl(String host) {
        return String.format(PLUGWISEHA_API_URL, host);
    }

    public static String getAppliancesUrl(String host) {
        return String.format(PLUGWISEHA_API_APPLIANCES_URL, host);
    }

    public static String getApplianceUrl(String host, String applianceId) {
        return String.format(PLUGWISEHA_API_APPLIANCE_URL, host, applianceId);
    }

    public static String getLocationsUrl(String host) {
        return String.format(PLUGWISEHA_API_LOCATIONS_URL, host);
    }

    public static String getLocationUrl(String host, String locationId) {
        return String.format(PLUGWISEHA_API_LOCATION_URL, host, locationId);
    }
}
