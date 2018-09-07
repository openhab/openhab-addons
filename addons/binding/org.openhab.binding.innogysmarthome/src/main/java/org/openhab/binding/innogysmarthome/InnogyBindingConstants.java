/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

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
    public static final String BRAND_SMARTHOME_AUSTRIA = "smarthome_austria";
    public static final String BRAND_START_SMARTHOME = "start_smarthome";
    public static final String DEFAULT_BRAND = BRAND_INNOGY_SMARTHOME;

    public static final String CLIENT_ID_INNOGY_SMARTHOME = "24635748";
    public static final String CLIENT_ID_SMARTHOME_AUSTRIA = "24635749";
    public static final String CLIENT_ID_START_SMARTHOME = "24635750";

    public static final String CLIENT_SECRET_INNOGY_SMARTHOME = "no secret";
    public static final String CLIENT_SECRET_SMARTHOME_AUSTRIA = "no secret";
    public static final String CLIENT_SECRET_START_SMARTHOME = "no secret";

    public static final String REDIRECT_URL_INNOGY_SMARTHOME = "https://www.openhab.org/oauth/innogy/innogy-smarthome.html";
    public static final String REDIRECT_URL_SMARTHOME_AUSTRIA = "https://www.openhab.org/oauth/innogy/smarthome-austria.html";
    public static final String REDIRECT_URL_START_SMARTHOME = "https://www.openhab.org/oauth/innogy/start-smarthome.html";

    // Bridge config parameters
    public static final String CONFIG_BRAND = "brand";

    public static final String CONFIG_AUTH_CODE = "authcode";
    public static final String CONFIG_ACCESS_TOKEN = "accesstoken";
    public static final String CONFIG_REFRESH_TOKEN = "refreshtoken";
    public static final String CONFIG_WEBSOCKET_IDLE_TIMEOUT = "websocketidletimeout";

    public static final long REINITIALIZE_DELAY_SECONDS = 30;
    public static final long REINITIALIZE_DELAY_LONG_SECONDS = 120;

    // API URLs
    public static final String API_VERSION = "1.0";
    public static final String WEBSOCKET_API_URL_EVENTS = "wss://api.services-smarthome.de/API/" + API_VERSION
            + "/events?token={token}";

    // properties
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_VERSION = "Version";
    public static final String PROPERTY_LOCATION = "Location";
    public static final String PROPERTY_SOFTWARE_VERSION = "Software version";
    public static final String PROPERTY_IP_ADDRESS = "IP address";
    public static final String PROPERTY_REGISTRATION_TIME = "Registration Time";
    public static final String PROPERTY_TIME_OF_ACCEPTANCE = "Time of acceptance";
    public static final String PROPERTY_TIME_OF_DISCOVERY = "Time of discovery";
    public static final String PROPERTY_BATTERY_POWERED = "Battery powered";
    public static final String PROPERTY_DEVICE_TYPE = "Device Type";

    // List of main device types
    public static final String DEVICE_SHC = "SHC"; // smarthome controller - the bridge
    public static final String DEVICE_PSS = "PSS"; // pluggable smart switch
    public static final String DEVICE_PSSO = "PSSO"; // pluggable smart switch outdoor
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

    public static final Set<String> SUPPORTED_DEVICES = ImmutableSet.of(DEVICE_SHC, DEVICE_PSS, DEVICE_PSSO,
            DEVICE_VARIABLE_ACTUATOR, DEVICE_RST, DEVICE_RST2, DEVICE_WRT, DEVICE_WDS, DEVICE_ISS2, DEVICE_WSD,
            DEVICE_WSD2, DEVICE_WMD, DEVICE_WMDO, DEVICE_WSC2, DEVICE_BRC8, DEVICE_ISC2, DEVICE_ISD2, DEVICE_ISR2,
            DEVICE_PSD, DEVICE_ANALOG_METER, DEVICE_GENERATION_METER, DEVICE_SMART_METER, DEVICE_TWO_WAY_METER);

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_PSS = new ThingTypeUID(BINDING_ID, DEVICE_PSS);
    public static final ThingTypeUID THING_TYPE_PSSO = new ThingTypeUID(BINDING_ID, DEVICE_PSSO);
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

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES = ImmutableSet.of(THING_TYPE_PSS,
            THING_TYPE_PSSO, THING_TYPE_VARIABLE_ACTUATOR, THING_TYPE_RST, THING_TYPE_RST2, THING_TYPE_WRT,
            THING_TYPE_WDS, THING_TYPE_ISS2, THING_TYPE_WSD, THING_TYPE_WSD2, THING_TYPE_WMD, THING_TYPE_WMDO,
            THING_TYPE_WSC2, THING_TYPE_BRC8, THING_TYPE_ISC2, THING_TYPE_ISD2, THING_TYPE_ISR2, THING_TYPE_PSD,
            THING_TYPE_ANALOG_METER, THING_TYPE_GENERATION_METER, THING_TYPE_SMART_METER, THING_TYPE_TWO_WAY_METER);

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
    public static final String CHANNEL_BUTTON1 = "button1";
    public static final String CHANNEL_BUTTON2 = "button2";
    public static final String CHANNEL_BUTTON3 = "button3";
    public static final String CHANNEL_BUTTON4 = "button4";
    public static final String CHANNEL_BUTTON5 = "button5";
    public static final String CHANNEL_BUTTON6 = "button6";
    public static final String CHANNEL_BUTTON7 = "button7";
    public static final String CHANNEL_BUTTON8 = "button8";
    public static final String CHANNEL_BUTTON1_COUNT = "button1_count";
    public static final String CHANNEL_BUTTON2_COUNT = "button2_count";
    public static final String CHANNEL_BUTTON3_COUNT = "button3_count";
    public static final String CHANNEL_BUTTON4_COUNT = "button4_count";
    public static final String CHANNEL_BUTTON5_COUNT = "button5_count";
    public static final String CHANNEL_BUTTON6_COUNT = "button6_count";
    public static final String CHANNEL_BUTTON7_COUNT = "button7_count";
    public static final String CHANNEL_BUTTON8_COUNT = "button8_count";
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

}
