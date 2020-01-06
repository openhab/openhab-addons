/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link PlugwiseHABindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Bas van Wetten - Initial contribution
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
        public static final ThingTypeUID THING_TYPE_APPLIANCE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "appliance_thermostat");

        // List of channel Type UIDs
        public static final ChannelTypeUID CHANNEL_TYPE_BATTERYLEVEL = new ChannelTypeUID("system:battery-level");
        public static final ChannelTypeUID CHANNEL_TYPE_BATTERYLEVELLOW = new ChannelTypeUID("system:low-battery");

        // Empty set
        public static final Set<ThingTypeUID> SUPPORTED_INTERFACE_TYPES_UIDS_EMPTY = Collections
                        .unmodifiableSet(Stream.<ThingTypeUID>empty().collect(Collectors.toSet()));

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

        // List of all Channel IDs
        public static final String ZONE_SETPOINT_CHANNEL = "setpointTemperature";
        public static final String ZONE_TEMPERATURE_CHANNEL = "temperature";

        public static final String APPLIANCE_SETPOINT_CHANNEL = "setpointTemperature";
        public static final String APPLIANCE_TEMPERATURE_CHANNEL = "temperature";
        public static final String APPLIANCE_BATTERYLEVEL_CHANNEL = "batteryLevel";
        public static final String APPLIANCE_BATTERYLEVELLOW_CHANNEL = "batteryLevelLow";
        public static final String APPLIANCE_POWER_USAGE_CHANNEL = "powerUsage";
        public static final String APPLIANCE_POWER_CHANNEL = "power";
        public static final String APPLIANCE_LOCK_CHANNEL = "lock";

        public static final String APPLIANCE_TYPE_THERMOSTAT = "thermostat";
        public static final String APPLIANCE_TYPE_GATEWAY = "gateway";
        public static final String APPLIANCE_TYPE_CENTRALHEATINGPUMP = "central_heating_pump";
        public static final String APPLIANCE_TYPE_OPENTHERMGATEWAY = "open_therm_gateway";
        public static final String APPLIANCE_TYPE_ZONETHERMOSTAT = "zone_thermostat";
        public static final String APPLIANCE_TYPE_HEATERCENTRAL = "heater_central";
        public static final String APPLIANCE_TYPE_THERMOSTATICRADIATORVALUE = "thermostatic_radiator_valve";

        // Supported things
        public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
                        Stream.of(THING_TYPE_ZONE, THING_TYPE_APPLIANCE_VALVE, THING_TYPE_APPLIANCE_PUMP)
                                        .collect(Collectors.toSet()));

        // Appliance types known to binding
        public static final Set<String> KNOWN_APPLIANCE_TYPES = Stream
                        .of(APPLIANCE_TYPE_THERMOSTAT, APPLIANCE_TYPE_GATEWAY, APPLIANCE_TYPE_CENTRALHEATINGPUMP,
                                        APPLIANCE_TYPE_OPENTHERMGATEWAY, APPLIANCE_TYPE_ZONETHERMOSTAT,
                                        APPLIANCE_TYPE_HEATERCENTRAL, APPLIANCE_TYPE_THERMOSTATICRADIATORVALUE)
                        .collect(Collectors.toSet());

        public static final Set<String> SUPPORTED_APPLIANCE_TYPES = Stream
                        .of(APPLIANCE_TYPE_CENTRALHEATINGPUMP, APPLIANCE_TYPE_THERMOSTATICRADIATORVALUE, APPLIANCE_TYPE_ZONETHERMOSTAT)
                        .collect(Collectors.toSet());

        // Supported things
        public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = Stream.of(Stream.of(THING_TYPE_GATEWAY))
                        .reduce(Stream::concat).orElseGet(Stream::empty).collect(Collectors.toSet());

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
