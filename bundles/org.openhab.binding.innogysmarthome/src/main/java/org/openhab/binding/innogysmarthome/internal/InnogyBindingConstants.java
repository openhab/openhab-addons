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
package org.openhab.binding.innogysmarthome.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link InnogyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@NonNullByDefault
public class InnogyBindingConstants {

    public static final String BINDING_ID = "innogysmarthome";

    // brands and client ids
    public static final String BRAND_INNOGY_SMARTHOME = "innogy_smarthome";
    public static final String DEFAULT_BRAND = BRAND_INNOGY_SMARTHOME;

    public static final String CLIENT_ID_INNOGY_SMARTHOME = "24635748";
    public static final String CLIENT_SECRET_INNOGY_SMARTHOME = "no secret";
    public static final String REDIRECT_URL_INNOGY_SMARTHOME = "https://www.openhab.org/oauth/innogy/innogy-smarthome.html";

    public static final String CONFIG_AUTH_CODE = "authcode";

    public static final long REINITIALIZE_DELAY_SECONDS = 30;

    // API URLs
    public static final String API_VERSION = "1.1";
    public static final String WEBSOCKET_API_URL_EVENTS = "wss://api.services-smarthome.de/API/" + API_VERSION
            + "/events?token={token}";

    // properties
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_VERSION = "Version";
    public static final String PROPERTY_LOCATION = "Location";
    public static final String PROPERTY_GEOLOCATION = "Geo Location";
    public static final String PROPERTY_SOFTWARE_VERSION = "Software version";
    public static final String PROPERTY_IP_ADDRESS = "IP address";
    public static final String PROPERTY_REGISTRATION_TIME = "Registration Time";
    public static final String PROPERTY_TIME_OF_ACCEPTANCE = "Time of acceptance";
    public static final String PROPERTY_TIME_OF_DISCOVERY = "Time of discovery";
    public static final String PROPERTY_BATTERY_POWERED = "Battery powered";
    public static final String PROPERTY_DEVICE_TYPE = "Device Type";
    public static final String PROPERTY_CONFIGURATION_STATE = "Configuration state";
    public static final String PROPERTY_SHC_TYPE = "Controller Type";
    public static final String PROPERTY_TIME_ZONE = "Time Zone";
    public static final String PROPERTY_CURRENT_UTC_OFFSET = "Current UTC offset (minutes)";
    public static final String PROPERTY_PROTOCOL_ID = "Protocol ID";
    public static final String PROPERTY_BACKEND_CONNECTION_MONITORED = "Backend connection monitored";
    public static final String PROPERTY_RFCOM_FAILURE_NOTIFICATION = "RFComm failure notification";
    public static final String PROPERTY_DISPLAY_CURRENT_TEMPERATURE = "Display current temperature";
    public static final String PROPERTY_METER_ID = "Meter ID";
    public static final String PROPERTY_METER_FIRMWARE_VERSION = "Meter firmware version";

    // List of main device types
    public static final String DEVICE_SHC = "SHC"; // smarthome controller - the bridge
    public static final String DEVICE_SHCA = "SHCA"; // smarthome controller version 2
    public static final String DEVICE_PSS = "PSS"; // pluggable smart switch
    public static final String DEVICE_PSSO = "PSSO"; // pluggable smart switch outdoor
    public static final String DEVICE_BT_PSS = "BT-PSS"; // Bluetooth pluggable smart switch
    public static final String DEVICE_VARIABLE_ACTUATOR = "VariableActuator";
    public static final String DEVICE_RST = "RST"; // radiator mounted smart thermostat
    public static final String DEVICE_RST2 = "RST2"; // radiator mounted smart thermostat (newer version)
    public static final String DEVICE_WRT = "WRT"; // wall mounted room thermostat
    public static final String DEVICE_WDS = "WDS"; // window door sensor
    public static final String DEVICE_ISS2 = "ISS2"; // inwall smart switch
    public static final String DEVICE_WSD = "WSD"; // wall mounted smoke detector
    public static final String DEVICE_WSD2 = "WSD2"; // wall mounted smoke detector
    public static final String DEVICE_WMD = "WMD"; // wall mounted motion detector indoor
    public static final String DEVICE_WMDO = "WMDO"; // wall mounted motion detector outdoor
    public static final String DEVICE_WSC2 = "WSC2"; // wall mounted smart controller (2 buttons)
    public static final String DEVICE_BRC8 = "BRC8"; // basic remote controller (8 buttons)
    public static final String DEVICE_ISC2 = "ISC2"; // in wall smart controller (2 buttons)
    public static final String DEVICE_ISD2 = "ISD2"; // in wall smart dimmer (2 buttons)
    public static final String DEVICE_ISR2 = "ISR2"; // in wall smart rollershutter (2 buttons)
    public static final String DEVICE_PSD = "PSD"; // pluggable smart dimmer
    public static final String DEVICE_ANALOG_METER = "AnalogMeter";
    public static final String DEVICE_GENERATION_METER = "GenerationMeter";
    public static final String DEVICE_SMART_METER = "SmartMeter";
    public static final String DEVICE_TWO_WAY_METER = "TwoWayMeter";

    public static final Set<String> SUPPORTED_DEVICES = Collections.unmodifiableSet(Stream
            .of(DEVICE_SHC, DEVICE_SHCA, DEVICE_PSS, DEVICE_PSSO, DEVICE_BT_PSS, DEVICE_VARIABLE_ACTUATOR, DEVICE_RST,
                    DEVICE_RST2, DEVICE_WRT, DEVICE_WDS, DEVICE_ISS2, DEVICE_WSD, DEVICE_WSD2, DEVICE_WMD, DEVICE_WMDO,
                    DEVICE_WSC2, DEVICE_BRC8, DEVICE_ISC2, DEVICE_ISD2, DEVICE_ISR2, DEVICE_PSD, DEVICE_ANALOG_METER,
                    DEVICE_GENERATION_METER, DEVICE_SMART_METER, DEVICE_TWO_WAY_METER)
            .collect(Collectors.toSet()));

    public static final Set<String> BATTERY_POWERED_DEVICES = Collections
            .unmodifiableSet(Stream.of(DEVICE_RST, DEVICE_RST2, DEVICE_WRT, DEVICE_WDS, DEVICE_WSD, DEVICE_WSD2,
                    DEVICE_WMD, DEVICE_WMDO, DEVICE_WSC2, DEVICE_BRC8).collect(Collectors.toSet()));

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_PSS = new ThingTypeUID(BINDING_ID, DEVICE_PSS);
    public static final ThingTypeUID THING_TYPE_PSSO = new ThingTypeUID(BINDING_ID, DEVICE_PSSO);
    public static final ThingTypeUID THING_TYPE_BT_PSS = new ThingTypeUID(BINDING_ID, DEVICE_BT_PSS);
    public static final ThingTypeUID THING_TYPE_VARIABLE_ACTUATOR = new ThingTypeUID(BINDING_ID,
            DEVICE_VARIABLE_ACTUATOR);
    public static final ThingTypeUID THING_TYPE_RST = new ThingTypeUID(BINDING_ID, DEVICE_RST);
    public static final ThingTypeUID THING_TYPE_RST2 = new ThingTypeUID(BINDING_ID, DEVICE_RST2);
    public static final ThingTypeUID THING_TYPE_WRT = new ThingTypeUID(BINDING_ID, DEVICE_WRT);
    public static final ThingTypeUID THING_TYPE_WDS = new ThingTypeUID(BINDING_ID, DEVICE_WDS);
    public static final ThingTypeUID THING_TYPE_ISS2 = new ThingTypeUID(BINDING_ID, DEVICE_ISS2);
    public static final ThingTypeUID THING_TYPE_WSD = new ThingTypeUID(BINDING_ID, DEVICE_WSD);
    public static final ThingTypeUID THING_TYPE_WSD2 = new ThingTypeUID(BINDING_ID, DEVICE_WSD2);
    public static final ThingTypeUID THING_TYPE_WMD = new ThingTypeUID(BINDING_ID, DEVICE_WMD);
    public static final ThingTypeUID THING_TYPE_WMDO = new ThingTypeUID(BINDING_ID, DEVICE_WMDO);
    public static final ThingTypeUID THING_TYPE_WSC2 = new ThingTypeUID(BINDING_ID, DEVICE_WSC2);
    public static final ThingTypeUID THING_TYPE_BRC8 = new ThingTypeUID(BINDING_ID, DEVICE_BRC8);
    public static final ThingTypeUID THING_TYPE_ISC2 = new ThingTypeUID(BINDING_ID, DEVICE_ISC2);
    public static final ThingTypeUID THING_TYPE_ISD2 = new ThingTypeUID(BINDING_ID, DEVICE_ISD2);
    public static final ThingTypeUID THING_TYPE_ISR2 = new ThingTypeUID(BINDING_ID, DEVICE_ISR2);
    public static final ThingTypeUID THING_TYPE_PSD = new ThingTypeUID(BINDING_ID, DEVICE_PSD);
    public static final ThingTypeUID THING_TYPE_ANALOG_METER = new ThingTypeUID(BINDING_ID, DEVICE_ANALOG_METER);
    public static final ThingTypeUID THING_TYPE_GENERATION_METER = new ThingTypeUID(BINDING_ID,
            DEVICE_GENERATION_METER);
    public static final ThingTypeUID THING_TYPE_SMART_METER = new ThingTypeUID(BINDING_ID, DEVICE_SMART_METER);
    public static final ThingTypeUID THING_TYPE_TWO_WAY_METER = new ThingTypeUID(BINDING_ID, DEVICE_TWO_WAY_METER);

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_PSS, THING_TYPE_PSSO, THING_TYPE_BT_PSS, THING_TYPE_VARIABLE_ACTUATOR, THING_TYPE_RST,
                    THING_TYPE_RST2, THING_TYPE_WRT, THING_TYPE_WDS, THING_TYPE_ISS2, THING_TYPE_WSD, THING_TYPE_WSD2,
                    THING_TYPE_WMD, THING_TYPE_WMDO, THING_TYPE_WSC2, THING_TYPE_BRC8, THING_TYPE_ISC2, THING_TYPE_ISD2,
                    THING_TYPE_ISR2, THING_TYPE_PSD, THING_TYPE_ANALOG_METER, THING_TYPE_GENERATION_METER,
                    THING_TYPE_SMART_METER, THING_TYPE_TWO_WAY_METER).collect(Collectors.toSet()));

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_SET_TEMPERATURE = "set_temperature";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_SMOKE = "smoke";
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_MOTION_COUNT = "motion_count";
    public static final String CHANNEL_LUMINANCE = "luminance";
    public static final String CHANNEL_OPERATION_MODE = "operation_mode";
    public static final String CHANNEL_FROST_WARNING = "frost_warning";
    public static final String CHANNEL_MOLD_WARNING = "mold_warning";
    public static final String CHANNEL_WINDOW_REDUCTION_ACTIVE = "window_reduction_active";
    public static final String CHANNEL_BUTTON = "button";
    public static final String CHANNEL_BUTTON_COUNT = "button%d_count";
    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public static final String CHANNEL_BATTERY_LOW = "battery_low";
    public static final String CHANNEL_ENERGY_CONSUMPTION_MONTH_KWH = "energy_consumption_month_kwh";
    public static final String CHANNEL_ABOLUTE_ENERGY_CONSUMPTION = "absolute_energy_consumption";
    public static final String CHANNEL_ENERGY_CONSUMPTION_MONTH_EURO = "energy_consumption_month_euro";
    public static final String CHANNEL_ENERGY_CONSUMPTION_DAY_EURO = "energy_consumption_day_euro";
    public static final String CHANNEL_ENERGY_CONSUMPTION_DAY_KWH = "energy_consumption_day_kwh";
    public static final String CHANNEL_POWER_CONSUMPTION_WATT = "power_consumption_watt";
    public static final String CHANNEL_ENERGY_GENERATION_MONTH_KWH = "energy_generation_month_kwh";
    public static final String CHANNEL_TOTAL_ENERGY_GENERATION = "total_energy_generation";
    public static final String CHANNEL_ENERGY_GENERATION_MONTH_EURO = "energy_generation_month_euro";
    public static final String CHANNEL_ENERGY_GENERATION_DAY_EURO = "energy_generation_day_euro";
    public static final String CHANNEL_ENERGY_GENERATION_DAY_KWH = "energy_generation_day_kwh";
    public static final String CHANNEL_POWER_GENERATION_WATT = "power_generation_watt";
    public static final String CHANNEL_ENERGY_MONTH_KWH = "energy_month_kwh";
    public static final String CHANNEL_TOTAL_ENERGY = "total_energy";
    public static final String CHANNEL_ENERGY_MONTH_EURO = "energy_month_euro";
    public static final String CHANNEL_ENERGY_DAY_EURO = "energy_day_euro";
    public static final String CHANNEL_ENERGY_DAY_KWH = "energy_day_kwh";
    public static final String CHANNEL_ENERGY_FEED_MONTH_KWH = "energy_feed_month_kwh";
    public static final String CHANNEL_TOTAL_ENERGY_FED = "total_energy_fed";
    public static final String CHANNEL_ENERGY_FEED_MONTH_EURO = "energy_feed_month_euro";
    public static final String CHANNEL_ENERGY_FEED_DAY_EURO = "energy_feed_day_euro";
    public static final String CHANNEL_ENERGY_FEED_DAY_KWH = "energy_feed_day_kwh";
    public static final String CHANNEL_POWER_WATT = "power_watt";
    public static final String CHANNEL_CPU = "cpu";
    public static final String CHANNEL_DISK = "disk";
    public static final String CHANNEL_MEMORY = "memory";
}
