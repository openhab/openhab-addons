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
package org.openhab.binding.shelly.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ShellyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBindingConstants {

    public static final String            VENDOR                           = "Shelly";
    public static final String            BINDING_ID                       = "shelly";

    // List of all Thing Type UIDs
    public static final ThingTypeUID      THING_TYPE_SHELLY1               = new ThingTypeUID(BINDING_ID, "shelly1");
    public static final ThingTypeUID      THING_TYPE_SHELLY1PM             = new ThingTypeUID(BINDING_ID, "shelly1pm");
    public static final ThingTypeUID      THING_TYPE_SHELLYEM              = new ThingTypeUID(BINDING_ID, "shellyem");
    public static final ThingTypeUID      THING_TYPE_SHELLY2_RELAY         = new ThingTypeUID(BINDING_ID,
            "shelly2-relay");
    public static final ThingTypeUID      THING_TYPE_SHELLY2_ROLLER        = new ThingTypeUID(BINDING_ID,
            "shelly2-roller");
    public static final ThingTypeUID      THING_TYPE_SHELLY25_RELAY        = new ThingTypeUID(BINDING_ID,
            "shelly25-relay");
    public static final ThingTypeUID      THING_TYPE_SHELLY25_ROLLER       = new ThingTypeUID(BINDING_ID,
            "shelly25-roller");
    public static final ThingTypeUID      THING_TYPE_SHELLY4PRO            = new ThingTypeUID(BINDING_ID, "shelly4pro");
    public static final ThingTypeUID      THING_TYPE_SHELLYPLUG            = new ThingTypeUID(BINDING_ID, "shellyplug");
    public static final ThingTypeUID      THING_TYPE_SHELLYPLUGS           = new ThingTypeUID(BINDING_ID,
            "shellyplugs");
    public static final ThingTypeUID      THING_TYPE_SHELLYDIMMER          = new ThingTypeUID(BINDING_ID,
            "shellydimmer");
    public static final ThingTypeUID      THING_TYPE_SHELLYBULB            = new ThingTypeUID(BINDING_ID, "shellybulb");
    public static final ThingTypeUID      THING_TYPE_SHELLYHT              = new ThingTypeUID(BINDING_ID, "shellyht");
    public static final ThingTypeUID      THING_TYPE_SHELLYSENSE           = new ThingTypeUID(BINDING_ID,
            "shellysense");
    public static final ThingTypeUID      THING_TYPE_SHELLYSMOKE           = new ThingTypeUID(BINDING_ID,
            "shellysmoke");
    public static final ThingTypeUID      THING_TYPE_SHELLYFLOOD           = new ThingTypeUID(BINDING_ID,
            "shellyflood");
    public static final ThingTypeUID      THING_TYPE_SHELLYEYE             = new ThingTypeUID(BINDING_ID, "shellyseye");
    public static final ThingTypeUID      THING_TYPE_SHELLYRGBW2_COLOR     = new ThingTypeUID(BINDING_ID,
            "shellyrgbw2-color");
    public static final ThingTypeUID      THING_TYPE_SHELLYRGBW2_WHITE     = new ThingTypeUID(BINDING_ID,
            "shellyrgbw2-white");
    public static final ThingTypeUID      THING_TYPE_SHELLYPROTECTED       = new ThingTypeUID(BINDING_ID,
            "shelly-protected");

    public static final String            SHELLYDT_DIMMER                  = "SHDM-1";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS       = Collections
            .unmodifiableSet(Stream
                    .of(THING_TYPE_SHELLY1, THING_TYPE_SHELLY1PM, THING_TYPE_SHELLYEM, THING_TYPE_SHELLY2_RELAY,
                            THING_TYPE_SHELLY2_ROLLER, THING_TYPE_SHELLY25_RELAY, THING_TYPE_SHELLY25_ROLLER,
                            THING_TYPE_SHELLY4PRO, THING_TYPE_SHELLYPLUG, THING_TYPE_SHELLYPLUGS,
                            THING_TYPE_SHELLYDIMMER, THING_TYPE_SHELLYBULB, THING_TYPE_SHELLYRGBW2_COLOR,
                            THING_TYPE_SHELLYRGBW2_WHITE, THING_TYPE_SHELLYHT, THING_TYPE_SHELLYSENSE,
                            THING_TYPE_SHELLYEYE, THING_TYPE_SHELLYSMOKE, THING_TYPE_SHELLYFLOOD)
                    .collect(Collectors.toSet()));

    // check for updates every x sec
    public static final int               UPDATE_STATUS_INTERVAL_SECONDS   = 3;
    // update every x triggers or when a key was pressed
    public static final int               UPDATE_SKIP_COUNT                = 20;
    // update every x triggers or when a key was pressed
    public static final int               UPDATE_MIN_DELAY                 = 15;
    // check for updates every x sec
    public static final int               UPDATE_SETTINGS_INTERVAL_SECONDS = 60;

    // Thing Configuration Properties
    public static final String            CONFIG_DEVICEIP                  = "deviceIp";
    public static final String            CONFIG_HTTP_USERID               = "userId";
    public static final String            CONFIG_HTTP_PASSWORD             = "password";
    public static final String            CONFIG_UPDATE_INTERVAL           = "updateInterval";

    public static final String            PROPERTY_SERVICE_NAME            = "serviceName";
    public static final String            PROPERTY_THINGTYPE               = "thingType";
    public static final String            PROPERTY_HOSTNAME                = "hostname";
    public static final String            PROPERTY_HWBATCH                 = "hardwareBatch";
    public static final String            PROPERTY_DEV_MODE                = "deviceMode";
    public static final String            PROPERTY_HWREV                   = "devHwRev";
    public static final String            PROPERTY_NUM_RELAYS              = "numberRelays";
    public static final String            PROPERTY_NUM_ROLLERS             = "numberRollers";
    public static final String            PROPERTY_NUM_METER               = "numberMeters";
    public static final String            PROPERTY_LAST_ACTIVE             = "lastActive";
    public static final String            PROPERTY_WIFI_NETW               = "wifiNetwork";
    public static final String            PROPERTY_WIFI_IP                 = "networkIP";
    public static final String            PROPERTY_UPDATE_STATUS           = "updateStatus";
    public static final String            PROPERTY_UPDATE_AVAILABLE        = "updateAvailable";
    public static final String            PROPERTY_UPDATE_CURR_VERS        = "updateCurrentVersion";
    public static final String            PROPERTY_UPDATE_NEW_VERS         = "updateNewVersion";
    public static final String            PROPERTY_COAP_DESCR              = "coapDeviceDescr";

    // Relay
    public static final String            CHANNEL_GROUP_RELAY_CONTROL      = "relay";
    public static final String            CHANNEL_OUTPUT                   = "output";
    public static final String            CHANNEL_INPUT                    = "input";
    public static final String            CHANNEL_INPUT1                   = "input1";
    public static final String            CHANNEL_INPUT2                   = "input2";
    public static final String            CHANNEL_OVERPOWER                = "overpower";
    public static final String            CHANNEL_OVERTEMP                 = "overtemp";
    public static final String            CHANNEL_BRIGHTNESS               = "brightness";

    public static final String            CHANNEL_TIMER_AUTOON             = "autoOn";
    public static final String            CHANNEL_TIMER_AUTOOFF            = "autoOff";
    public static final String            CHANNEL_TIMER_ACTIVE             = "timerActive";

    // Roller
    public static final String            CHANNEL_GROUP_ROL_CONTROL        = "roller";
    public static final String            CHANNEL_ROL_CONTROL_CONTROL      = "control";
    public static final String            CHANNEL_ROL_CONTROL_POS          = "rollerpos";
    public static final String            CHANNEL_ROL_CONTROL_TIMER        = "timer";
    public static final String            CHANNEL_ROL_CONTROL_STOPR        = "stopReason";
    public static final String            CHANNEL_ROL_CONTROL_DIR          = "lastDirection";

    // Dimmer
    public static final String            CHANNEL_GROUP_DIMMER_CONTROL     = CHANNEL_GROUP_RELAY_CONTROL;

    public static final String            CHANNEL_GROUP_DIMMER_STATUS      = "status";
    public static final String            CHANNEL_DIMMER_LOAD_ERROR        = "loaderror";
    public static final String            CHANNEL_DIMMER_OVERLOAD          = "overload";

    // Power meter
    public static final String            CHANNEL_GROUP_METER              = "meter";
    public static final String            CHANNEL_METER_CURRENTWATTS       = "currentWatts";
    public static final String            CHANNEL_METER_LASTMIN1           = "lastPower1";
    public static final String            CHANNEL_METER_LASTMIN2           = "lastPower2";
    public static final String            CHANNEL_METER_LASTMIN3           = "lastPower3";
    public static final String            CHANNEL_METER_TOTALKWH           = "totalKWH";
    public static final String            CHANNEL_EMETER_TOTALRET          = "returnedKWH";
    public static final String            CHANNEL_EMETER_REACTWATTS        = "reactiveWatts";
    public static final String            CHANNEL_EMETER_VOLTAGE           = "voltage";
    public static final String            CHANNEL_METER_TIMESTAMP          = "timestamp";

    public static final String            CHANNEL_GROUP_LED_CONTROL        = "led";
    public static final String            CHANNEL_LED_STATUS_DISABLE       = "statusLed";
    public static final String            CHANNEL_LED_POWER_DISABLE        = "powerLed";

    public static final String            CHANNEL_GROUP_SENSOR             = "sensors";
    public static final String            CHANNEL_SENSOR_TEMP              = "temperature";
    public static final String            CHANNEL_SENSOR_HUM               = "humidity";
    public static final String            CHANNEL_SENSOR_LUX               = "lux";
    public static final String            CHANNEL_SENSOR_FLOOD             = "flood";
    public static final String            CHANNEL_SENSOR_MOTION            = "motion";
    public static final String            CHANNEL_SENSOR_CHARGER           = "charger";

    public static final String            CHANNEL_GROUP_SENSE_CONTROL      = "control";
    public static final String            CHANNEL_SENSE_KEY                = "key";

    public static final String            CHANNEL_GROUP_BATTERY            = "battery";
    public static final String            CHANNEL_SENSOR_BAT_LEVEL         = "batteryLevel";
    public static final String            CHANNEL_SENSOR_BAT_LOW           = "lowBattery";
    public static final String            CHANNEL_SENSOR_BAT_VOLT          = "voltage";

    public static final String            CHANNEL_GROUP_LIGHT_CONTROL      = "control";
    public static final String            CHANNEL_LIGHT_COLOR_MODE         = "mode";
    public static final String            CHANNEL_LIGHT_POWER              = "power";
    public static final String            CHANNEL_LIGHT_DEFSTATE           = "defaultState";
    public static final String            CHANNEL_GROUP_LIGHT_CHANNEL      = "channel";

    // Bulb/RGBW2 in color mode
    public static final String            CHANNEL_GROUP_COLOR_CONTROL      = "color";
    public static final String            CHANNEL_COLOR_PICKER             = "hsb";
    public static final String            CHANNEL_COLOR_FULL               = "full";
    public static final String            CHANNEL_COLOR_RED                = "red";
    public static final String            CHANNEL_COLOR_GREEN              = "green";
    public static final String            CHANNEL_COLOR_BLUE               = "blue";
    public static final String            CHANNEL_COLOR_WHITE              = "white";
    public static final String            CHANNEL_COLOR_GAIN               = "gain";
    public static final String            CHANNEL_COLOR_EFFECT             = "effect";

    // Bulb/RGBW2 in White Mode
    public static final String            CHANNEL_GROUP_WHITE_CONTROL      = "white";
    public static final String            CHANNEL_COLOR_TEMP               = "temperature";

    // Device Status
    public static final String            CHANNEL_GROUP_DEV_STATUS         = "device";
    public static final String            CHANNEL_DEVST_UPTIME             = "uptime";
    public static final String            CHANNEL_DEVST_LAST_UPDATE        = "lastUpdate";
    public static final String            CHANNEL_DEVST_RSSI               = "signal";

    // General
    public static final String            SERVICE_TYPE                     = "_http._tcp.local.";
    public static final String            CHANNEL_EVENT_TRIGGER            = "event";

    // Coap
    public static final int               COIOT_PORT                       = 5683;
    public static final String            COAP_MULTICAST_ADDRESS           = "224.0.1.187";

    public static final String            COLOIT_URI_BASE                  = "/cit/";
    public static final String            COLOIT_URI_DEVDESC               = COLOIT_URI_BASE + "d";
    public static final String            COLOIT_URI_DEVSTATUS             = COLOIT_URI_BASE + "s";

    public static final int               COIOT_OPTION_GLOBAL_DEVID        = 3332;
    public static final int               COIOT_OPTION_STATUS_VALIDITY     = 3412;
    public static final int               COIOT_OPTION_STATUS_SERIAL       = 3420;

    public static final byte[]            EMPTY_BYTE                       = new byte[0];

    public static final int               SHELLY_API_TIMEOUT_MS            = 2500;

    public static final String            SHELLY_API_MIN_FWVERSION         = "v1.5.2";

    public static final String            SHELLY_NULL_URL                  = "null";
    public static final String            SHELLY_URL_DEVINFO               = "/shelly";
    public static final String            SHELLY_URL_STATUS                = "/status";
    public static final String            SHELLY_URL_SETTINGS              = "/settings";
    public static final String            SHELLY_URL_SETTINGS_AP           = "/settings/ap";
    public static final String            SHELLY_URL_SETTINGS_STA          = "/settings/sta";
    public static final String            SHELLY_URL_SETTINGS_LOGIN        = "/settings/sta";
    public static final String            SHELLY_URL_SETTINGS_CLOUD        = "/settings/cloud";
    public static final String            SHELLY_URL_LIST_IR               = "/ir/list";
    public static final String            SHELLY_URL_SEND_IR               = "/ir/emit";

    public static final String            SHELLY_URL_SETTINGS_RELAY        = "/settings/relay";
    public static final String            SHELLY_URL_STATUS_RELEAY         = "/status/relay";
    public static final String            SHELLY_URL_CONTROL_RELEAY        = "/relay";

    public static final String            SHELLY_URL_SETTINGS_EMETER       = "/settings/emeter";
    public static final String            SHELLY_URL_STATUS_EMETER         = "/emeter";
    public static final String            SHELLY_URL_DATA_EMETER           = "/emeter/{0}/em_data.csv";

    public static final String            SHELLY_URL_CONTROL_ROLLER        = "/roller";
    public static final String            SHELLY_URL_SETTINGS_ROLLER       = "/settings/roller";

    public static final String            SHELLY_URL_SETTINGS_LIGHT        = "/settings/light";
    public static final String            SHELLY_URL_STATUS_LIGHT          = "/light";
    public static final String            SHELLY_URL_CONTROL_LIGHT         = "/light";

    public static final String            SHELLY_URL_SETTINGS_DIMMER       = "/settings/light";

    public static final String            SHELLY_CALLBACK_URI              = "/shelly/event";
    public static final String            EVENT_TYPE_RELAY                 = "relay";
    public static final String            EVENT_TYPE_ROLLER                = "roller";
    public static final String            EVENT_TYPE_SENSORDATA            = "sensordata";
    public static final String            EVENT_TYPE_LIGHT                 = "light";

    public static final String            SHELLY_IR_CODET_STORED           = "stored";
    public static final String            SHELLY_IR_CODET_PRONTO           = "pronto";
    public static final String            SHELLY_IR_CODET_PRONTO_HEX       = "pronto_hex";

    public static final String            HTTP_DELETE                      = "DELETE";
    public static final String            HTTP_HEADER_AUTH                 = "Authorization";
    public static final String            HTTP_AUTH_TYPE_BASIC             = "Basic";
    public static final String            CONTENT_TYPE_XML                 = "text/xml; charset=UTF-8";

    public static final String            APIERR_HTTP_401_UNAUTHORIZED     = "401 Unauthorized";
    public static final String            APIERR_TIMEOUT                   = "Timeout";
    public static final String            APIERR_NOT_CALIBRATED            = "Not calibrated!";

    // Minimum signal strength for basic connectivity. Packet delivery may be unreliable.
    public static final int               SIGNAL_ALARM_INTERVAL_SEC        = 600;                                       // every
                                                                                                                        // 10min
    public static final int               SIGNAL_ALARM_MIN_RSSI            = -80;

    public static final int               DIM_STEPSIZE                     = 5;

    // Formatting: Number of scaling digits
    public static final int               DIGITS_WATT                      = 3;
    public static final int               DIGITS_KWH                       = 4;
    public static final int               DIGITS_VOLT                      = 2;
    public static final int               DIGITS_TEMP                      = 2;
    public static final int               DIGITS_LUX                       = 2;
    public static final int               DIGITS_PERCENT                   = 2;
}
