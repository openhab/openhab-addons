/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.api1;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellyMotionSettings;
import org.openhab.core.thing.CommonTriggerEvents;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Shelly1ApiJsonDTO} is used for the JSon/GSon mapping
 *
 * @author Markus Michels - Initial contribution
 */
public class Shelly1ApiJsonDTO {
    public static final String SHELLY_NULL_URL = "null";
    public static final String SHELLY_URL_DEVINFO = "/shelly";
    public static final String SHELLY_URL_STATUS = "/status";
    public static final String SHELLY_URL_SETTINGS = "/settings";
    public static final String SHELLY_URL_SETTINGS_AP = "/settings/ap";
    public static final String SHELLY_URL_SETTINGS_STA = "/settings/sta";
    public static final String SHELLY_URL_SETTINGS_LOGIN = "/settings/sta";
    public static final String SHELLY_URL_SETTINGS_CLOUD = "/settings/cloud";
    public static final String SHELLY_URL_LIST_IR = "/ir/list";
    public static final String SHELLY_URL_SEND_IR = "/ir/emit";
    public static final String SHELLY_URL_RESTART = "/reboot";

    public static final String SHELLY_URL_SETTINGS_RELAY = "/settings/relay";
    public static final String SHELLY_URL_STATUS_RELEAY = "/status/relay";
    public static final String SHELLY_URL_CONTROL_RELEAY = "/relay";

    public static final String SHELLY_URL_SETTINGS_EMETER = "/settings/emeter";
    public static final String SHELLY_URL_STATUS_EMETER = "/emeter";
    public static final String SHELLY_URL_DATA_EMETER = "/emeter/{0}/em_data.csv";

    public static final String SHELLY_URL_CONTROL_ROLLER = "/roller";
    public static final String SHELLY_URL_SETTINGS_ROLLER = "/settings/roller";

    public static final String SHELLY_URL_SETTINGS_LIGHT = "/settings/light";
    public static final String SHELLY_URL_STATUS_LIGHT = "/light";
    public static final String SHELLY_URL_CONTROL_LIGHT = "/light";

    public static final String SHELLY_URL_SETTINGS_DIMMER = "/settings/light";

    // Wakeup reasons
    public static final String SHELLY_WAKEUPT_SENSOR = "SENSOR"; // new sensordata
    public static final String SHELLY_WAKEUPT_PERIODIC = "PERIODIC"; // periodic wakeup
    public static final String SHELLY_WAKEUPT_BUTTON = "BUTTON"; // button pressed
    public static final String SHELLY_WAKEUPT_POWERON = "POWERON"; // device powered up
    public static final String SHELLY_WAKEUPT_EXT_POWER = "EXT_POWER"; // charger connected
    public static final String SHELLY_WAKEUPT_UNKNOWN = "UNKNOWN"; // other event

    //
    // Action URLs according to the device type
    //
    public static final String SHELLY_EVENTURL_SUFFIX = "_url";

    // Relay
    public static final String SHELLY_EVENT_BTN_ON = "btn_on";
    public static final String SHELLY_EVENT_BTN_OFF = "btn_off";
    public static final String SHELLY_EVENT_OUT_ON = "out_on";
    public static final String SHELLY_EVENT_OUT_OFF = "out_off";
    public static final String SHELLY_EVENT_SHORTPUSH = "shortpush";
    public static final String SHELLY_EVENT_LONGPUSH = "longpush";
    // Button
    public static final String SHELLY_EVENT_DOUBLE_SHORTPUSH = "double_shortpush";
    public static final String SHELLY_EVENT_TRIPLE_SHORTPUSH = "triple_shortpush";
    public static final String SHELLY_EVENT_SHORT_LONGTPUSH = "shortpush_longpush";
    public static final String SHELLY_EVENT_LONG_SHORTPUSH = "longpush_shortpush";

    // Dimmer
    public static final String SHELLY_EVENT_BTN1_ON = "btn1_on";
    public static final String SHELLY_EVENT_BTN1_OFF = "btn1_off";
    public static final String SHELLY_EVENT_BTN2_ON = "btn2_on";
    public static final String SHELLY_EVENT_BTN2_OFF = "btn2_off";
    public static final String SHELLY_EVENT_SHORTPUSH1 = "btn1_shortpush";
    public static final String SHELLY_EVENT_LONGPUSH1 = "btn1_longpush";
    public static final String SHELLY_EVENT_SHORTPUSH2 = "btn2_shortpush";
    public static final String SHELLY_EVENT_LONGPUSH2 = "btn2_longpush";

    // Roller
    public static final String SHELLY_EVENT_ROLLER_OPEN = "roller_open";
    public static final String SHELLY_EVENT_ROLLER_CLOSE = "roller_close";
    public static final String SHELLY_EVENT_ROLLER_STOP = "roller_stop";
    public static final String SHELLY_EVENT_ROLLER_CALIB = "roller_calibrating";

    // Roller states
    public static final String SHELLY_RSTATE_OPEN = "open";
    public static final String SHELLY_RSTATE_STOP = "stop";
    public static final String SHELLY_RSTATE_CLOSE = "close";

    // Sensors
    public static final String SHELLY_EVENT_SENSORREPORT = "report";
    public static final String SHELLY_EVENT_DARK = "dark";
    public static final String SHELLY_EVENT_TWILIGHT = "twilight";
    public static final String SHELLY_EVENT_BRIGHT = "bright";
    public static final String SHELLY_EVENT_FLOOD_DETECTED = "flood_detected";
    public static final String SHELLY_EVENT_FLOOD_GONE = "flood_gone";
    public static final String SHELLY_EVENT_VIBRATION = "vibration"; // DW 1.6.5+
    public static final String SHELLY_EVENT_OPEN = "open"; // DW 1.6.5+
    public static final String SHELLY_EVENT_CLOSE = "close"; // DW 1.6.5+
    public static final String SHELLY_EVENT_TEMP_OVER = "temp_over"; // FW 1.7
    public static final String SHELLY_EVENT_TEMP_UNDER = "temp_under"; // FW 1.7

    // Gas
    public static final String SHELLY_EVENT_ALARM_MILD = "alarm_mild"; // DW 1.7+
    public static final String SHELLY_EVENT_ALARM_HEAVY = "alarm_heavy"; // DW 1.7+
    public static final String SHELLY_EVENT_ALARM_OFF = "alarm_off"; // DW 1.7+

    //
    // API values
    //
    public static final double SHELLY_API_INVTEMP = 999.0;

    public static final String SHELLY_BTNT_MOMENTARY = "momentary";
    public static final String SHELLY_BTNT_MOM_ON_RELEASE = "momentary_on_release";
    public static final String SHELLY_BTNT_ONE_BUTTON = "one_button";
    public static final String SHELLY_BTNT_TWO_BUTTON = "dual_button";
    public static final String SHELLY_BTNT_TOGGLE = "toggle";
    public static final String SHELLY_BTNT_EDGE = "edge";
    public static final String SHELLY_BTNT_DETACHED = "detached";

    public static final String SHELLY_STATE_LAST = "last";
    public static final String SHELLY_STATE_STOP = "stop";

    public static final String SHELLY_INP_MODE_OPENCLOSE = "openclose";
    public static final String SHELLY_INP_MODE_ONEBUTTON = "onebutton";

    public static final String SHELLY_OBSTMODE_DISABLED = "disabled";
    public static final String SHELLY_SAFETYM_WHILEOPENING = "while_opening";

    public static final String SHELLY_ALWD_TRIGGER_NONE = "none";
    public static final String SHELLY_ALWD_ROLLER_TURN_OPEN = "open";
    public static final String SHELLY_ALWD_ROLLER_TURN_CLOSE = "close";
    public static final String SHELLY_ALWD_ROLLER_TURN_STOP = "stop";

    // API Error Codes
    public static final String SHELLY_APIERR_UNAUTHORIZED = "Unauthorized";
    public static final String SHELLY_APIERR_TIMEOUT = "Timeout";
    public static final String SHELLY_APIERR_NOT_CALIBRATED = "Not calibrated!";

    // API device types / properties
    public static final String SHELLY_CLASS_RELAY = "relay"; // Relay: relay mode
    public static final String SHELLY_CLASS_ROLLER = "roller"; // Relay: roller mode
    public static final String SHELLY_CLASS_LIGHT = "light"; // Bulb: color mode
    public static final String SHELLY_CLASS_EMETER = "emeter"; // EM/EM3: emeter

    public static final String SHELLY_API_ON = "on";
    public static final String SHELLY_API_OFF = "off";
    public static final String SHELLY_API_TRUE = "true";
    public static final String SHELLY_API_FALSE = "false";

    public static final String SHELLY_API_MODE = "mode";
    public static final String SHELLY_MODE_RELAY = "relay"; // Relay: relay mode
    public static final String SHELLY_MODE_ROLLER = "roller"; // Relay: roller mode
    public static final String SHELLY_MODE_COLOR = "color"; // Bulb/RGBW2: color mode
    public static final String SHELLY_MODE_WHITE = "white"; // Bulb/RGBW2: white mode

    public static final String SHELLY_LED_STATUS_DISABLE = "led_status_disable";
    public static final String SHELLY_LED_POWER_DISABLE = "led_power_disable";

    public static final String SHELLY_API_STOPR_NORMAL = "normal";
    public static final String SHELLY_API_STOPR_SAFETYSW = "safety_switch";
    public static final String SHELLY_API_STOPR_OBSTACLE = "obstacle";
    public static final String SHELLY_API_STOPR_OVERPOWER = "overpower";

    public static final String SHELLY_TIMER_AUTOON = "auto_on";
    public static final String SHELLY_TIMER_AUTOOFF = "auto_off";
    public static final String SHELLY_TIMER_ACTIVE = "has_timer";

    public static final String SHELLY_LIGHT_TURN = "turn";
    public static final String SHELLY_LIGHT_DEFSTATE = "def_state";
    public static final String SHELLY_LIGHTTIMER = "timer";

    public static final String SHELLY_COLOR_RED = "red";
    public static final String SHELLY_COLOR_BLUE = "blue";
    public static final String SHELLY_COLOR_GREEN = "green";
    public static final String SHELLY_COLOR_YELLOW = "yellow";
    public static final String SHELLY_COLOR_WHITE = "white";
    public static final String SHELLY_COLOR_GAIN = "gain";
    public static final String SHELLY_COLOR_BRIGHTNESS = "brightness";
    public static final String SHELLY_COLOR_TEMP = "temp";
    public static final String SHELLY_COLOR_EFFECT = "effect";

    public static final int SHELLY_MIN_ROLLER_POS = 0;
    public static final int SHELLY_MAX_ROLLER_POS = 100;
    public static final int SHELLY_MIN_BRIGHTNESS = 0;
    public static final int SHELLY_MAX_BRIGHTNESS = 100;
    public static final int SHELLY_MIN_GAIN = 0;
    public static final int SHELLY_MAX_GAIN = 100;
    public static final int SHELLY_MIN_COLOR = 0;
    public static final int SHELLY_MAX_COLOR = 255;
    public static final int SHELLY_DIM_STEPSIZE = 10;

    // color temperature: 3000 = warm, 4750 = white, 6565 = cold; gain: 0..100
    public static final int MIN_COLOR_TEMP_BULB = 3000;
    public static final int MAX_COLOR_TEMP_BULB = 6500;
    public static final int MIN_COLOR_TEMP_DUO = 2700;
    public static final int MAX_COLOR_TEMP_DUO = 6500;
    public static final int COLOR_TEMP_RANGE_BULB = MAX_COLOR_TEMP_DUO - MIN_COLOR_TEMP_DUO;
    public static final int COLOR_TEMP_RANGE_DUO = MAX_COLOR_TEMP_DUO - MIN_COLOR_TEMP_DUO;
    public static final double MIN_BRIGHTNESS = 0.0;
    public static final double MAX_BRIGHTNESS = 100.0;
    public static final double SATURATION_FACTOR = 2.55;
    public static final double GAIN_FACTOR = SHELLY_MAX_GAIN / 100;
    public static final double BRIGHTNESS_FACTOR = SHELLY_MAX_BRIGHTNESS / 100;

    // Door/Window
    public static final String SHELLY_API_ILLUM_DARK = "dark";
    public static final String SHELLY_API_ILLUM_TWILIGHT = "twilight";
    public static final String SHELLY_API_ILLUM_BRIGHT = "bright";
    public static final String SHELLY_API_DWSTATE_OPEN = "open";
    public static final String SHELLY_API_DWSTATE_CLOSE = "close";

    // Shelly Sense
    public static final String SHELLY_IR_CODET_STORED = "stored";
    public static final String SHELLY_IR_CODET_PRONTO = "pronto";
    public static final String SHELLY_IR_CODET_PRONTO_HEX = "pronto_hex";

    // Bulb/Duo/RGBW2
    public static final int SHELLY_MIN_EFFECT = 0;
    public static final int SHELLY_MAX_EFFECT = 6;

    // Button
    public static final String SHELLY_BTNEVENT_1SHORTPUSH = "S";
    public static final String SHELLY_BTNEVENT_2SHORTPUSH = "SS";
    public static final String SHELLY_BTNEVENT_3SHORTPUSH = "SSS";
    public static final String SHELLY_BTNEVENT_LONGPUSH = "L";
    public static final String SHELLY_BTNEVENT_SHORTLONGPUSH = "SL";
    public static final String SHELLY_BTNEVENT_LONGSHORTPUSH = "LS";

    public static final String SHELLY_TEMP_CELSIUS = "C";
    public static final String SHELLY_TEMP_FAHRENHEIT = "F";

    // Motion
    public static final int SHELLY_MOTION_SLEEPTIME_OFFSET = 3; // we need to substract and offset

    // TRV
    public static final int SHELLY_TRV_MIN_TEMP = 5; // < 5: means: lowest (valve fully closed)
    public static final int SHELLY_TRV_MAX_TEMP = 30; // > 30: means: highest (valve fully open)

    public static final String SHELLY_TRV_MODE_MANUAL = "manual";
    public static final String SHELLY_TRV_MODE_AUTO = "automatic";

    // CoIoT Multicast setting
    public static final String SHELLY_COIOT_MCAST = "mcast";

    public static class ShellySettingsDevice {
        public String type;
        public String mac;
        public String hostname;
        public String fw;
        public Boolean auth;
        public Integer gen;
        public String coiot;
        public Integer longid;

        @SerializedName("num_outputs")
        public Integer numOutputs;
        @SerializedName("num_meters")
        public Integer numMeters;
        @SerializedName("num_emeters")
        public Integer numEMeters;
        @SerializedName("num_rollers")
        public Integer numRollers;
    }

    public static class ShellySettingsWiFiAp {
        public Boolean enabled;
        public String ssid;
        public String key;
        public Boolean rangeExtender; // Gen2 only
    }

    public static class ShellySettingsWiFiNetwork {
        public Boolean enabled;
        public String ssid;
        public Integer rssi;

        @SerializedName("ipv4_method")
        public String ipv4Method;
        public String ip;
        public String gw;
        public String mask;
        public String dns;
    }

    public static class ShellySettingsMqtt {
        public Boolean enable;
        public String server;
        public String user;
        @SerializedName("reconnect_timeout_max")
        public Double reconnectTimeoutMax;
        @SerializedName("reconnect_timeout_min")
        public Double reconnectTimeoutMin;
        @SerializedName("clean_session")
        public Boolean cleanSession;
        @SerializedName("keep_alive")
        public Integer keepAlive;
        @SerializedName("will_topic")
        public String willTopic;
        @SerializedName("will_message")
        public String willMessage;
        @SerializedName("max_qos")
        public Integer maxQOS;
        public Boolean retain;
        @SerializedName("update_period")
        public Integer updatePeriod;
    }

    public static class ShellySettingsCoiot { // FW 1.6+
        @SerializedName("update_period")
        public Integer updatePeriod;
        public Boolean enabled; // Motion 1.0.7: Coap can be disabled
        public String peer; // if set the device uses singlecast CoAP, mcast=set back to Multicast
    }

    public static class ShellyStatusMqtt {
        public Boolean connected;
    }

    public static class ShellySettingsSntp {
        public String server;
        public Boolean enabled;
    }

    public static class ShellySettingsLogin {
        public Boolean enabled;
        public Boolean unprotected;
        public String username;
        public String password;
    }

    public static class ShellySettingsBuildInfo {
        @SerializedName("build_id")
        public String buildId;
        @SerializedName("build_timestamp")
        public String buildTimestamp;
        @SerializedName("build_version")
        public String buildVersion;
    }

    public static class ShellyStatusCloud {
        public Boolean enabled;
        public Boolean connected;
    }

    public static class ShellySettingsHwInfo {
        @SerializedName("hw_revision")
        public String hwRevision;
        @SerializedName("batch_id")
        public Integer batchId;
    }

    public static class ShellySettingsScheduleRules {
    }

    public static class ShellySettingsRelay {
        public String name;
        @SerializedName("default_state")
        public String defaultState; // Accepted values: off, on, last, switch
        @SerializedName("btn_type")
        public String btnType; // Accepted values: momentary, toggle, edge, detached - // see SHELLY_BTNT_xxx
        @SerializedName("btn1_type") // Shelly 1L
        public String btnType1;
        @SerializedName("btn2_type") // Shelly 1L
        public String btnType2;
        @SerializedName("has_timer")
        public Boolean hasTimer; // Whether a timer is currently armed for this channel
        @SerializedName("auto_on")
        public Double autoOn; // Automatic flip back timer, seconds. Will engage after turning Shelly1 OFF.
        @SerializedName("auto_off")
        public Double autoOff; // Automatic flip back timer, seconds. Will engage after turning Shelly1 ON.
        @SerializedName("btn_on_url")
        public String btnOnUrl; // input is activated
        @SerializedName("btnOffUrl")
        public String btnOffUrl; // input is deactivated
        @SerializedName("out_on_url")
        public String outOnUrl; // output is activated
        @SerializedName("out_off_url")
        public String outOffUrl; // output is deactivated
        @SerializedName("roller_open_url")
        public String rollerOpenUrl; // to access when roller reaches open position
        @SerializedName("roller_close_url")
        public String rollerCloseUrl; // to access when roller reaches close position
        @SerializedName("roller_stop_url")
        public String rollerStopUrl; // to access when roller stopped
        @SerializedName("longpush_url")
        public String pushLongUrl; // to access when roller stopped
        @SerializedName("shortpush_url")
        public String pushShortUrl; // to access when roller stopped

        // Status information
        public Boolean ison;
        public Boolean overpower;
        @SerializedName("is_valid")
        public Boolean isValid;
    }

    public static class ShellySettingsDimmer {
        public String name; // unique name of the device
        public Boolean ison; // true: output is ON
        @SerializedName("default_state")
        public String defaultState; // Accepted values: off, on, last, switch
        @SerializedName("auto_on")
        public Double autoOn; // Automatic flip back timer, seconds. Will engage after turning Shelly1 OFF.
        @SerializedName("auto_off")
        public Double autoOff; // Automatic flip back timer, seconds. Will engage after turning Shelly1 ON.
        @SerializedName("btn1_on_url")
        public String btn1OnUrl; // URL to access when SW input is activated
        @SerializedName("btn1_off_url")
        public String btn1OffUrl; // URL to access when SW input is deactivated
        @SerializedName("btn2_on_url")
        public String btn2OnUrl; // URL to access when SW input is activated
        @SerializedName("btn2_off_url")
        public String btn2OoffUrl; // URL to access when SW input is deactivated
        @SerializedName("out_on_url")
        public String outOnUrl; // URL to access when output is activated
        @SerializedName("out_off_url")
        public String outOffUrl; // URL to access when output is deactivated
        @SerializedName("longpush_url")
        public String pushLongUrl; // long push button event
        @SerializedName("shortpush_url")
        public String pushShortUrl; // short push button event
        @SerializedName("btn_type")
        public String btnType; // Accepted values: momentary, toggle, edge, detached - // see SHELLY_BTNT_xxx
        @SerializedName("btn1_type")
        public String btnType1; // Accepted values: momentary, toggle, edge, detached - // see SHELLY_BTNT_xxx
        @SerializedName("btn2_type")
        public String btnType2; // Accepted values: momentary, toggle, edge, detached - // see SHELLY_BTNT_xxx
        @SerializedName("swap_inputs")
        public Integer swapInputs; // 0=no
    }

    public static class ShellySettingsRoller {
        public Double maxtime;
        @SerializedName("maxtime_open")
        public Double maxtimeOpen;
        @SerializedName("maxtime_close")
        public Double maxtimeClose;
        @SerializedName("default_state")
        public String defaultState; // see SHELLY_STATE_xxx
        public Boolean swap;
        @SerializedName("swap_inputs")
        public Boolean swapInputs;
        @SerializedName("input_mode")
        public String inputMode; // see SHELLY_INP_MODE_OPENCLOSE
        @SerializedName("button_type")
        public String buttonType; // // see SHELLY_BTNT_xxx
        @SerializedName("btn_Reverse")
        public Integer btnReverse;
        public String state;
        public Double power;
        @SerializedName("is_valid")
        public Boolean isValid;
        @SerializedName("safety_switch")
        public Boolean safetySwitch;
        @SerializedName("obstacle_mode")
        public String obstaclMode; // SHELLY_OBSTMODE_
        @SerializedName("obstacle_action")
        public String obstacleAction; // see SHELLY_STATE_xxx
        @SerializedName("obstacle_power")
        public Integer obstaclePower;
        @SerializedName("obstacle_delay")
        public Integer obstacleDelay;
        @SerializedName("safety_mode")
        public String safetyMode; // see SHELLY_SAFETYM_xxx
        @SerializedName("safety_action")
        public String safetyAction; // see SHELLY_STATE_xxx
        @SerializedName("safety_allowed_on_trigger")
        public String safetyAllowedOnTrigger; // see SHELLY_ALWD_TRIGGER_xxx
        @SerializedName("off_power")
        public Integer offPower;
        public Boolean positioning;
    }

    public static class ShellySettingsRgbwLight {
        public String name;
        public Boolean ison; // true: output is ON
        public Integer brightness;
        public Integer temp;
        public Integer transition;
        @SerializedName("default_state")
        public String defaultState;
        @SerializedName("auto_on")
        public Double autoOn; // Automatic flip back timer, seconds. Will engage after turning Shelly1 OFF.
        @SerializedName("auto_off")
        public Double autoOff; // Automatic flip back timer, seconds. Will engage after turning Shelly1 ON.
        public Boolean schedule;
        @SerializedName("btn_type")
        public String btnType; // Accepted values: momentary, toggle, edge, detached - // see SHELLY_BTNT_xxx
        @SerializedName("btn_reverse")
        public Integer btnReverse; // Accepted values: momentary, toggle, edge, detached - // see SHELLY_BTNT_xxx
        @SerializedName("out_on_url")
        public String outOnUrl; // output is activated
        @SerializedName("out_off_url")
        public String outOffUrl; // output is deactivated
    }

    public static class ShellyFavPos { // FW 1.9.2+ in roller mode
        public String name;
        public Integer pos;
    }

    public static class ShellyInputState {
        public Integer input;

        // Shelly Button
        public String event;
        @SerializedName("event_cnt")
        public Integer eventCount;
    }

    public static class ShellySettingsMeter {
        @SerializedName("is_valid")
        public Boolean isValid;
        public Double power;
        public Double[] counters = { 0.0, 0.0, 0.0 };
        public Double total;
        public Long timestamp;
    }

    public static class ShellySettingsEMeter { // ShellyEM meter
        @SerializedName("is_valid")
        public Boolean isValid; // Whether the associated meter is functioning properly
        public Double power; // Instantaneous power, Watts
        public Double reactive; // Instantaneous reactive power, Watts
        public Double voltage; // RMS voltage, Volts
        public Double total; // Total consumed energy, Wh
        @SerializedName("total_returned")
        public Double totalReturned; // Total returned energy, Wh

        public Double pf; // 3EM
        public Double current; // 3EM
    }

    public static class ShellySettingsUpdate {
        public String status;
        @SerializedName("has_update")
        public Boolean hasUpdate;
        @SerializedName("new_version")
        public String newVersion;
        @SerializedName("old_version")
        public String oldVersion;
        @SerializedName("beta_version")
        public String betaVersion;
    }

    public static class ShellySettingsGlobal {
        // https://shelly-api-docs.shelly.cloud/#shelly1pm-settings
        public ShellySettingsDevice device = new ShellySettingsDevice();
        @SerializedName("wifi_ap")
        public ShellySettingsWiFiAp wifiAp = new ShellySettingsWiFiAp();
        @SerializedName("wifi_sta")
        public ShellySettingsWiFiNetwork wifiSta = new ShellySettingsWiFiNetwork();
        @SerializedName("wifi_sta1")
        public ShellySettingsWiFiNetwork wifiSta1 = new ShellySettingsWiFiNetwork();
        @SerializedName("wifirecovery_reboot_enabled")
        public Boolean wifiRecoveryReboot; // FW 1.10+
        @SerializedName("ap_roaming")
        public ShellyApRoaming apRoaming; // FW 1.10+

        public ShellySettingsMqtt mqtt = new ShellySettingsMqtt();
        public ShellySettingsSntp sntp = new ShellySettingsSntp();
        public ShellySettingsCoiot coiot = new ShellySettingsCoiot();
        public ShellySettingsLogin login = new ShellySettingsLogin();
        @SerializedName("pin_code")
        public String pinCode;
        @SerializedName("coiot_execute_enable")
        public Boolean coiotExecuteEnable;
        public String name;
        public Boolean discoverable; // FW 1.6+
        public String fw;
        @SerializedName("build_info")
        public ShellySettingsBuildInfo buildInfo = new ShellySettingsBuildInfo();
        public ShellyStatusCloud cloud = new ShellyStatusCloud();
        @SerializedName("sleep_mode")
        public ShellySensorSleepMode sleepMode; // FW 1.6
        @SerializedName("external_power")
        public Integer externalPower; // H&T FW 1.6, seems to be the same like charger for the Sense
        @SerializedName("debug_enable") // FW 1.10+
        public Boolean debugEnable;

        public String timezone;
        public Double lat;
        public Double lng;
        public Boolean tzautodetect;
        public String time;

        public ShellySettingsHwInfo hwinfo;
        public String mode;
        @SerializedName("max_power")
        public Double maxPower;
        public Boolean calibrated;

        public Double voltage; // AC voltage for Shelly 2.5
        @SerializedName("supply_voltage")
        public Long supplyVoltage; // Shelly 1PM/1L: 0=110V, 1=220V

        public @Nullable ArrayList<ShellySettingsRelay> relays;
        public @Nullable ArrayList<ShellySettingsInput> inputs; // ix3
        public @Nullable ArrayList<ShellySettingsDimmer> dimmers;
        public @Nullable ArrayList<ShellySettingsRoller> rollers;
        public @Nullable ArrayList<ShellySettingsRgbwLight> lights;
        public @Nullable ArrayList<ShellySettingsEMeter> emeters;
        public @Nullable ArrayList<ShellyThermnostat> thermostats; // TRV

        @SerializedName("ext_switch_enable")
        public Boolean externalSwitchEnable;
        @SerializedName("ext_switch")
        public ShellyStatusSensor.ShellyExtSwitchSettings extSwitch;
        @SerializedName("ext_temperature")
        public ShellyStatusSensor.ShellyExtTemperature extTemperature; // Shelly 1/1PM: sensor values
        @SerializedName("ext_humidity")
        public ShellyStatusSensor.ShellyExtHumidity extHumidity; // Shelly 1/1PM: sensor values
        public ShellyStatusSensor.ShellyExtVoltage extVoltage; // Shelly ´Plus 1/1PM: sensor values
        public ShellyStatusSensor.ShellyExtAnalogInput extAnalogInput; // Shelly ´Plus 1/1PM: sensor values
        public ShellyStatusSensor.ShellyExtDigitalInput extDigitalInput; // Shelly ´Plus 1/1PM: state of digital input

        @SerializedName("temperature_units")
        public String temperatureUnits = "C"; // Either'C'or'F'

        @SerializedName("led_status_disable")
        public Boolean ledStatusDisable; // PlugS only Disable LED indication for network
                                         // status
        @SerializedName("led_power_disable")
        public Boolean ledPowerDisable; // PlugS only Disable LED indication for network
                                        // status
        @SerializedName("light_sensor")
        public String lightSensor; // Sense: sensor type
        @SerializedName("rain_sensor")
        public Boolean rainSensor; // Flood: true=in rain mode

        // FW 1.5.7: Door Window
        @SerializedName("dark_treshold")
        public Integer darkTreshold; // Illumination definition for "dark" in lux
        @SerializedName("twilight_treshold")
        public Integer twiLightTreshold; // Illumination definition for "twilight" in lux
        @SerializedName("dark_url")
        public String darkUrl; // URL to report to when luminance <= dark_threshold
        @SerializedName("twilight_url")
        public String twiLightUrl; // URL reports when luminance > dark_threshold AND luminance <=
        @SerializedName("close_url")
        public String closeUrl; // URL reports when DW contact is closed FW 1.6.5+
        @SerializedName("vibration_url")
        public String vibrationUrl; // URL reports when DW detects vibration FW 1.6.5+

        // Gas FW 1.7
        @SerializedName("set_volume")
        public Integer volume; // Speaker volume for alarm
        @SerializedName("alarm_off_url")
        public String alarmOffUrl; // URL reports when alarm went off
        @SerializedName("alarm_mild_url")
        public String alarmMidUrl; // URL reports middle alarm
        @SerializedName("alarm_heavy_url")
        public String alarmHeavyfUrl; // URL reports heavy alarm

        // Roller with FW 1.9.2+
        @SerializedName("favorites_enabled")
        public Boolean favoritesEnabled = false;
        public ArrayList<ShellyFavPos> favorites;

        // Motion
        public ShellyMotionSettings motion;
        @SerializedName("tamper_sensitivity")
        public Integer tamperSensitivity;
        @SerializedName("dark_threshold")
        public Integer darkThreshold;
        @SerializedName("twilight_threshold")
        public Integer twilightThreshold;

        @SerializedName("sleep_time") // Shelly Motion
        public Integer sleepTime;

        // Gen2
        public Boolean ethernet;
        public Boolean bluetooth;
    }

    public static class ShellySettingsAttributes {
        @SerializedName("device_type")
        public String deviceType; // Device model identifier
        @SerializedName("device_mac")
        public String deviceMac; // MAC address of the device in hexadecimal
        @SerializedName("wifi_ap")
        public String wifiAp; // WiFi access poInteger configuration, see /settings/ap for details
        @SerializedName("wifi_sta")
        public String wifiSta; // WiFi client configuration. See /settings/sta for details
        public String login; // credentials used for HTTP Basic authentication for the REST interface. If
                             // enabled is true clients must include an Authorization: Basic ... HTTP header with valid
                             // credentials when performing TP requests.
        public String name; // unique name of the device.
        public String fw; // current FW version
    }

    public static class ShellyActionsStats {
        public Integer skipped;
    }

    public static class ShellySettingsStatus {
        public String name; // FW 1.8: Symbolic Device name is configurable

        @SerializedName("wifi_sta")
        public ShellySettingsWiFiNetwork wifiSta = new ShellySettingsWiFiNetwork(); // WiFi client configuration. See
                                                                                    // /settings/sta for details
        public ShellyStatusCloud cloud = new ShellyStatusCloud();
        public ShellyStatusMqtt mqtt = new ShellyStatusMqtt();

        public String time;
        public Integer serial = -1;
        @SerializedName("has_update")
        public Boolean hasUpdate;
        public String mac;
        public Boolean discoverable; // FW 1.6+
        @SerializedName("cfg_changed_cnt")
        public Integer cfgChangedCount; // FW 1.8
        @SerializedName("actions_stats")
        public ShellyActionsStats astats;

        public ArrayList<ShellySettingsRelay> relays;
        public Double voltage; // Shelly 2.5
        public Integer input; // RGBW2 has no JSON array
        public ArrayList<ShellyInputState> inputs;
        public ArrayList<ShellyShortLightStatus> dimmers;
        public ArrayList<ShellyRollerStatus> rollers;
        public ArrayList<ShellySettingsLight> lights;
        public ArrayList<ShellySettingsMeter> meters;
        public ArrayList<ShellySettingsEMeter> emeters;
        @SerializedName("ext_temperature")
        public ShellyStatusSensor.ShellyExtTemperature extTemperature; // Shelly 1/1PM: sensor values
        @SerializedName("ext_humidity")
        public ShellyStatusSensor.ShellyExtHumidity extHumidity; // Shelly 1/1PM: sensor values
        public ShellyStatusSensor.ShellyExtVoltage extVoltage; // Shelly ´Plus 1/1PM: sensor values
        public ShellyStatusSensor.ShellyExtAnalogInput extAnalogInput; // Shelly ´Plus 1/1PM: sensor values
        public ShellyStatusSensor.ShellyExtDigitalInput extDigitalInput; // Shelly ´Plus 1/1PM: sensor values
        @SerializedName("ext_switch")
        public ShellyStatusSensor.ShellyExtSwitchStatus extSwitch;

        // Internal device temp
        public ShellySensorTmp tmp = new ShellySensorTmp(); // Shelly 1PM
        public Double temperature; // Shelly 2.5
        public Boolean overtemperature;

        // Shelly Dimmer only
        public Boolean loaderror;
        public Boolean overload;

        // Shelly TRV
        public Boolean calibrated;
        public ArrayList<ShellyThermnostat> thermostats;

        public ShellySettingsUpdate update = new ShellySettingsUpdate();
        @SerializedName("ram_total")
        public Long ramTotal;
        @SerializedName("ram_free")
        public Long ramFree;
        @SerializedName("fs_size")
        public Long fsSize;
        @SerializedName("fs_free")
        public Long fsFree;
        public Long uptime;

        @SerializedName("sleep_time") // Shelly Motion
        public Integer sleepTime;

        public String json;
    }

    public static class ShellySettingsInput {
        @SerializedName("btn_type")
        public String btnType;
    }

    public static class ShellyControlRelay {
        // https://shelly-api-docs.shelly.cloud/#shelly1-1pm-settings-relay-0
        @SerializedName("is_valid")
        public Boolean isValid;
        @SerializedName("has_timer")
        public Boolean hasTimer; // Whether a timer is currently armed for this channel
        @SerializedName("timer_remaining")
        public Integer timerRemaining; // FW 1.6+
        public Boolean overpower; // Shelly1PM only if maximum allowed power was exceeded

        public String turn; // Accepted values are on and off. This will turn ON/OFF the respective output
                            // channel when request is sent .
        public Integer timer; // A one-shot flip-back timer in seconds.
    }

    public static class ShellyShortStatusRelay {
        public String name; // FW 1.8+: Channel could now have a logical name
        @SerializedName("is_valid")
        public Boolean isValid;
        public Boolean ison; // Whether output channel is on or off
        @SerializedName("has_timer")
        public Boolean hasTimer; // Whether a timer is currently armed for this channel
        @SerializedName("timer_remaining")
        public Integer timerRemaining;
        public Boolean overpower; // Shelly1PM only if maximum allowed power was exceeded
        public Double temperature; // Internal device temperature
        public Boolean overtemperature; // Device over heated
    }

    public static class ShellyShortLightStatus {
        public Boolean ison; // Whether output channel is on or off
        public String mode; // color or white - valid only for Bulb and RGBW2 even Dimmer returns it also
        public Integer brightness; // brightness: 0.100%
        @SerializedName("has_timer")
        public Boolean hasTimer;
    }

    public static class ShellyStatusRelay {
        public String name; // FW 1.8: Symbolic channel name is configurable

        @SerializedName("wifi_sta")
        public ShellySettingsWiFiNetwork wifiSta; // WiFi status
        public ShellySettingsCoiot coiot; // Firmware 1.6+
        public Integer serial;
        public String mac; // MAC
        public ArrayList<ShellyShortStatusRelay> relays; // relay status
        public ArrayList<ShellySettingsMeter> meters; // current meter value
        public ArrayList<ShellyInputState> inputs; // Firmware 1.5.6+

        @SerializedName("ext_temperature")
        public ShellyStatusSensor.ShellyExtTemperature extTemperature; // Shelly 1/1PM: sensor values
        @SerializedName("ext_humidity")
        public ShellyStatusSensor.ShellyExtHumidity extHumidity; // Shelly 1/1PM: sensor values

        public Double temperature; // device temp acc. on the selected temp unit
        public ShellySensorTmp tmp;
    }

    public static class ShellyStatusDimmer {
        @SerializedName("wifi_sta")
        public ShellySettingsWiFiNetwork wifiSta; // WiFi status
        public ArrayList<ShellyShortLightStatus> lights; // relay status
        public ArrayList<ShellySettingsMeter> meters; // current meter value

        public ShellySensorTmp tmp;
        public Boolean overtemperature;

        public Boolean loaderror;
        public Boolean overload;
    }

    public static class ShellyRollerStatus {
        public String name; // FW 1.8: Symbolic name is configurable

        @SerializedName("roller_pos")
        public Integer rollerPos; // number Desired position in percent
        public Integer duration; // If specified, the motor will move for this period in seconds. If missing, the
                                 // value of maxtime in /settings/roller/N will be used.
        public String state; // One of stop, open, close
        public Double power; // Current power consumption in Watts
        @SerializedName("is_valid")
        public Boolean isValid; // If the power meter functions properly
        @SerializedName("safety_switch")
        public Boolean safetySwitch; // Whether the safety input is currently triggered
        public Boolean overtemperature;
        @SerializedName("stop_reason")
        public String stopReason; // Last cause for stopping: normal, safety_switch, obstacle
        @SerializedName("last_direction")
        public String lastDirection; // Last direction of motion, open or close
        public Boolean calibrating;
        public Boolean positioning; // true when calibration was performed
        @SerializedName("current_pos")
        public Integer currentPos; // current position 0..100, 100=open
    }

    public static class ShellyOtaCheckResult {
        public String status;
    }

    public static class ShellyApRoaming {
        public Boolean enabled;
        public Integer threshold;
    }

    public static class ShellySensorSleepMode {
        public Integer period;
        public String unit;
    }

    // Shelly TRV
    public class ShellyThermnostat {
        public class ShellyThermTargetTemp {
            public Boolean enabled;
            public Double value;
            public String unit;
        }

        public class ShellyThermTemp {
            public Double value;
            public String units;
            @SerializedName("is_valid")
            public Boolean isValid;
        }

        public Double pos;
        @SerializedName("target_t")
        public ShellyThermTargetTemp targetTemp;
        public Boolean schedule;
        @SerializedName("schedule_profile")
        public Integer profile;
        @SerializedName("schedule_profile_names")
        public String[] profileNames;
        public ShellyThermTemp tmp;
        @SerializedName("boost_minutes")
        public Integer boostMinutes;
        @SerializedName("window_open")
        public Boolean windowOpen;
    }

    public static class ShellySensorTmp {
        public Double value; // Temperature in configured unites
        public String units; // 'C' or 'F'
        public Double tC; // temperature in deg C
        public Double tF; // temperature in deg F
        @SerializedName("is_valid")
        public Boolean isValid; // whether the internal sensor is operating properly
    }

    public static class ShellyStatusSensor {
        // https://shelly-api-docs.shelly.cloud/#h-amp-t-settings

        public static class ShellySensorHum {
            public Double value; // relative humidity in %
        }

        public static class ShellySensorBat {
            public Double value; // estimated remaining battery capacity in %
            public Double voltage; // battery voltage
        };

        // Door/Window sensor
        public static class ShellySensorState {
            @SerializedName("is_valid")
            public Boolean isValid; // whether the internal sensor is operating properly
            public String state; // Shelly Door/Window

            // Shelly Motion
            public Boolean motion;
            public Boolean vibration;
            @SerializedName("timestamp")
            public Long motionTimestamp;
            @SerializedName("active")
            public Boolean motionActive;
        }

        public static class ShellySensorLux {
            @SerializedName("is_valid")
            public Boolean isValid; // whether the internal sensor is operating properly
            public Double value;

            public String illumination;
        }

        public static class ShellySensorAccel {
            public Integer tilt; // Tilt in °
            public Integer vibration; // Whether vibration is detected
        }

        public static class ShellyMotionSettings {
            public Integer sensitivity;
            @SerializedName("blind_time_minutes")
            public Integer blindTimeMinutes;
            @SerializedName("pulse_count")
            public Integer pulseCount;
            @SerializedName("operating_mode")
            public Integer operatingMode;
            public Boolean enabled;
        }

        public static class ShellyExtTemperature {
            public static class ShellyShortTemp {
                public String hwID; // e.g. "2882379497020381",
                public Double tC; // temperature in deg C
                public Double tF; // temperature in deg F
            }

            // Shelly 1/1PM have up to 3 sensors
            // for whatever reasons it's not an array, but 3 independent elements
            @SerializedName("0")
            public ShellyShortTemp sensor1;
            @SerializedName("1")
            public ShellyShortTemp sensor2;
            @SerializedName("2")
            public ShellyShortTemp sensor3;
            @SerializedName("3")
            public ShellyShortTemp sensor4;
            @SerializedName("4")
            public ShellyShortTemp sensor5;
        }

        public static class ShellyExtHumidity {
            public static class ShellyShortHum {
                public Double hum; // Humidity reading of sensor 0, percent
            }

            public ShellyExtHumidity() {
            }

            public ShellyExtHumidity(double hum) {
                sensor1 = new ShellyShortHum();
                sensor1.hum = hum;
            }

            @SerializedName("0")
            public ShellyShortHum sensor1;
        }

        public static class ShellyExtVoltage {
            public static class ShellyShortVoltage {
                public Double voltage;
            }

            public ShellyExtVoltage() {
            }

            public ShellyExtVoltage(double voltage) {
                sensor1 = new ShellyShortVoltage();
                sensor1.voltage = voltage;
            }

            @SerializedName("0")
            public ShellyShortVoltage sensor1;
        }

        public static class ShellyExtDigitalInput {
            public static class ShellyShortDigitalInput {
                public Boolean state;
            }

            public ShellyExtDigitalInput() {
            }

            public ShellyExtDigitalInput(boolean state) {
                sensor1 = new ShellyShortDigitalInput();
                sensor1.state = state;
            }

            @SerializedName("0")
            public ShellyShortDigitalInput sensor1;
        }

        public static class ShellyExtAnalogInput {
            public static class ShellyShortAnalogInput {
                public Double percent;
            }

            public ShellyExtAnalogInput() {
            }

            public ShellyExtAnalogInput(double percent) {
                sensor1 = new ShellyShortAnalogInput();
                sensor1.percent = percent;
            }

            @SerializedName("0")
            public ShellyShortAnalogInput sensor1;
        }

        public static class ShellyADC {
            public Double voltage;
        }

        public static class ShellyExtSwitchSettings {
            public static class ShellyExtSwitchSettingsInput {
                @SerializedName("relay_num")
                public Integer relayNum;
            }

            @SerializedName("0")
            public ShellyExtSwitchSettingsInput input0;
        }

        public static class ShellyExtSwitchStatus {
            public static class ShellyExtSwitchStatusInput {
                public Integer input;
            }

            @SerializedName("0")
            public ShellyExtSwitchStatusInput input0;
        }

        public ShellySensorTmp tmp;
        public ShellySensorHum hum;
        public ShellySensorLux lux;
        public ShellySensorAccel accel;
        public ShellySensorBat bat;
        @SerializedName("sensor")
        public ShellySensorState sensor;
        public Boolean smoke; // SHelly Smoke
        public Boolean flood; // Shelly Flood: true = flood condition detected
        public Boolean mute; // mute enabled/disabled
        @SerializedName("rain_sensor")
        public Boolean rainSensor; // Shelly Flood: true=in rain mode

        public Boolean motion; // Shelly Sense: true=motion detected
        public Boolean charger; // Shelly Sense, TRV: true=charger connected

        @SerializedName("act_reasons")
        public List<Object> actReasons; // HT/Smoke/Flood: list of reasons which woke up the device

        @SerializedName("sensor_error")
        public String sensorError; // 1.5.7: Only displayed in case of error

        // FW 1.7: Shelly Gas
        @SerializedName("gas_sensor")
        public ShellyStatusGasSensor gasSensor;
        @SerializedName("concentration")
        public ShellyStatusGasConcentration concentration;
        public ArrayList<ShellyStatusValve> valves;

        // FW 1.7 Button
        @SerializedName("connect_retries")
        public Integer connectRetries;
        public ArrayList<ShellyInputState> inputs; // Firmware 1.5.6+

        // Shelly UNI FW 1.9+
        public ArrayList<ShellyADC> adcs;

        // Shelly TRV
        public Boolean calibrated;
        public ArrayList<ShellyThermnostat> thermostats;
    }

    public static class ShellySettingsSmoke {
        @SerializedName("temperature_units")
        public String temperatureUnits; // Either 'C' or 'F'
        @SerializedName("temperature_threshold")
        public Integer temperatureThreshold; // Temperature delta (in configured degree units) which triggers an update
        @SerializedName("sleep_mode_period")
        public Integer sleepModePeriod; // Periodic update period in hours, between 1 and 24
    }

    // Shelly Gas
    // "gas_sensor":{"sensor_state":"normal","self_test_state":"not_completed","alarm_state":"none"},
    // "concentration":{"ppm":0,"is_valid":true},
    public static class ShellyStatusGasSensor {
        @SerializedName("sensor_state")
        public String sensorState;
        @SerializedName("self_test_state")
        public String selfTestState;
        @SerializedName("alarm_state")
        public String alarmState;
    }

    public static class ShellyStatusGasConcentration {
        public Integer ppm;
        @SerializedName("is_valid")
        public Boolean isValid;
    }

    public static class ShellyStatusValve {
        public String state; // closed/opened/not_connected/failure/closing/opening/checking
    }

    public static class ShellySettingsLight {
        public Integer red; // red brightness, 0..255, applies in mode="color"
        public Integer green; // green brightness, 0..255, applies in mode="color"
        public Integer blue; // blue brightness, 0..255, applies in mode="color"
        public Integer white; // white brightness, 0..255, applies in mode="color"
        public Integer gain; // gain for all channels, 0..100, applies in mode="color"
        public Integer temp; // color temperature in K, 3000..6500, applies in mode="white"
        public Integer brightness; // brightness, 0..100, applies in mode="white"
        public Integer effect; // Currently applied effect, description: 0: Off, 1: Meteor Shower, 2: Gradual
                               // Change, 3: Breath,
                               // 4: Flash, 5: On/Off Gradual, 6: Red/Green Change
        @SerializedName("default_state")
        public String defaultState; // one of on, off or last
        @SerializedName("auto_on")
        public Double autoOn; // see above
        @SerializedName("auto_off")
        public Double autoOff; // see above

        public Integer dcpower; // RGW2:Set to true for 24 V power supply, false for 12 V

        // Shelly Dimmer
        public String mode;
        public Boolean ison;
    }

    public static class ShellySettingsNightMode { // FW1.5.7+
        public Integer enabled;
        @SerializedName("start_time")
        public String startTime;
        @SerializedName("end_time")
        public String endTime;
        public Integer brightness;
    }

    public static class ShellyStatusLightChannel {
        public Boolean ison;
        public Double power;
        public Boolean overpower;
        @SerializedName("has_timer")
        public Boolean hasTimer;
        @SerializedName("timer_started")
        public Integer timerStarted;
        @SerializedName("timer_duration")
        public Integer timerDuration;
        @SerializedName("timer_remaining")
        public Integer timerRemaining;

        public Integer red; // red brightness, 0..255, applies in mode="color"
        public Integer green; // green brightness, 0..255, applies in mode="color"
        public Integer blue; // blue brightness, 0..255, applies in mode="color"
        public Integer white; // white brightness, 0..255, applies in mode="color"
        public Integer gain; // gain for all channels, 0..100, applies in mode="color"
        public Integer temp; // color temperature in K, 3000..6500, applies in mode="white"
        public Integer brightness; // brightness, 0..100, applies in mode="white"
        public Integer effect; // Currently applied effect, description: 0: Off, 1: Meteor Shower, 2: Gradual
                               // Change, 3: Breath,
    }

    public static class ShellyStatusLight {
        public Boolean ison; // Whether output channel is on or off
        public Integer input;

        public ArrayList<ShellyStatusLightChannel> lights;
        public ArrayList<ShellySettingsMeter> meters;
    }

    public static class ShellySenseKeyCode {
        String id; // ID of the stored IR code into Shelly Sense.
        String name; // Short description or name of the stored IR code.
    }

    public static class ShellySendKeyList {
        @SerializedName("key_codes")
        public ArrayList<ShellySenseKeyCode> keyCodes;
    }

    /**
     * Shelly Dimmer returns light[]. However, the structure doesn't match the lights[] of a Bulb/RGBW2.
     * The tag lights[] will be replaced with dimmers[] so this could be mapped to a different Gson structure.
     * The function requires that it's only called when the device is a dimmer - on get settings and get status
     *
     * @param json Input Json as received by the API
     * @return Modified Json
     */
    public static String fixDimmerJson(String json) {
        return !json.contains("\"lights\":[") ? json
                : json.replaceFirst(java.util.regex.Pattern.quote("\"lights\":["), "\"dimmers\":[");
    }

    /**
     * Convert Shelly Button events into OH button states
     *
     * @param eventType S/SS/SSS or L
     * @return OH button states
     */
    public static String mapButtonEvent(String eventType) {
        // decode different codings
        // 0..2: CoAP
        // S/SS/SSS/L: CoAP for Button and xi3
        // shortpush/double_shortpush/triple_shortpush/longpush: REST
        switch (eventType) {
            case "0":
                return CommonTriggerEvents.RELEASED;
            case "1":
            case SHELLY_BTNEVENT_1SHORTPUSH:
            case SHELLY_EVENT_SHORTPUSH:
                return CommonTriggerEvents.SHORT_PRESSED;
            case SHELLY_BTNEVENT_2SHORTPUSH:
            case SHELLY_EVENT_DOUBLE_SHORTPUSH:
                return CommonTriggerEvents.DOUBLE_PRESSED;
            case SHELLY_BTNEVENT_3SHORTPUSH:
            case SHELLY_EVENT_TRIPLE_SHORTPUSH:
                return "TRIPLE_PRESSED";
            case "2":
            case SHELLY_BTNEVENT_LONGPUSH:
            case SHELLY_EVENT_LONGPUSH:
                return CommonTriggerEvents.LONG_PRESSED;
            case SHELLY_BTNEVENT_SHORTLONGPUSH:
            case SHELLY_EVENT_SHORT_LONGTPUSH:
                return "SHORT_LONG_PRESSED";
            case SHELLY_BTNEVENT_LONGSHORTPUSH:
            case SHELLY_EVENT_LONG_SHORTPUSH:
                return "LONG_SHORT_PRESSED";
            default:
                return "";
        }
    }
}
