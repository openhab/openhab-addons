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
package org.openhab.binding.shelly.internal.api;

import java.util.ArrayList;

/**
 * Json/Gson mapping
 *
 * @author Markus Michels - Initial contribution
 *
 */

public class ShellyApiJson {

    public static final String SHELLY_API_ON = "on";
    public static final String SHELLY_API_OFF = "off";
    public static final String SHELLY_API_TRUE = "true";
    public static final String SHELLY_API_FALSE = "false";

    public static final String SHELLY_CLASS_RELAY = "relay"; // Relay: relay mode
    public static final String SHELLY_CLASS_ROLLER = "roller"; // Relay: roller mode
    public static final String SHELLY_CLASS_LIGHT = "light"; // Bulb: color mode

    public static class ShellySettingsDevice {
        public String type;
        public String mac;
        public String hostname;
        public String fw;
        public Boolean auth;
        public Integer num_outputs;
        public Integer num_meters;
        public Integer num_rollers;
    }

    public static class ShellySettingsWiFiAp {
        public Boolean enabled;
        public String ssid;
        public String key;
    }

    public static class ShellySettingsWiFiNetwork {
        public Boolean enabled;
        public String ssid;
        public Integer rssi;

        public String ipv4_method;
        public final String SHELLY_IPM_STATIC = "static";
        public final String SHELLY_IPM_DHCP = "dhcp";

        public String ip;
        public String gw;
        public String mask;
        public String dns;
    }

    public static class ShellySettingsMqtt {
        public Boolean enabled;
        public String server;
        public String user;
        public Double reconnect_timeout_max;
        public Double reconnect_timeout_min;
        public Boolean clean_session;
        public Integer keep_alive;
        public String will_topic;
        public String will_message;
        public Integer max_qos;
        public Boolean retain;
        public Integer update_period;
    }

    public static class ShellySettingsSntp {
        public String server;
    }

    public static class ShellySettingsLogin {
        public Boolean enabled;
        public Boolean unprotected;
        public String username;
        public String password;
    }

    public static class ShellySettingsBuildInfo {
        public String build_id;
        public String build_timestamp;
        public String build_version;
    }

    public static class ShellyStatusCloud {
        public Boolean enabled;
        public Boolean connected;
    }

    public static class ShellyStatusMqtt {
        public Boolean connected;
    }

    public static class ShellySettingsHwInfo {
        public String hw_revision;
        public Integer batch_id;
    }

    public static class ShellySettingsScheduleRules {
    }

    public static class ShellySettingsRelay {
        public String name;
        public Boolean ison;
        public Boolean overpower;
        public String default_state; // Accepted values: off, on, last, switch
        public String btn_type; // Accepted values: momentary, toggle, edge, detached - // see SHELLY_BTNT_xxx
        public Boolean has_timer; // Whether a timer is currently armed for this channel
        public Double auto_on; // Automatic flip back timer, seconds. Will engage after turning Shelly1 OFF.
        public Double auto_off; // Automatic flip back timer, seconds. Will engage after turning Shelly1 ON.
        public String btn_on_url; // URL to access when SW input is activated
        public String btn_off_url; // URL to access when SW input is deactivated
        public String out_on_url; // URL to access when output is activated
        public String out_off_url; // URL to access when output is deactivated
        public String roller_open_url; // URL to access when roller reaches open position
        public String roller_close_url; // URL to access when roller reaches close position
        public String roller_stop_url; // URL to access when roller stopped
        public Boolean schedule;
        // ArrayList<ShellySettingsScheduleRules> schedule_rules;
    }

    public static class ShellySettingsDimmer {
        public String name; // unique name of the device
        public Boolean ison; // true: output is ON
        public String default_state; // Accepted values: off, on, last, switch
        public Double auto_on; // Automatic flip back timer, seconds. Will engage after turning Shelly1 OFF.
        public Double auto_off; // Automatic flip back timer, seconds. Will engage after turning Shelly1 ON.
        public String btn1_on_url; // URL to access when SW input is activated
        public String btn1_off_url; // URL to access when SW input is deactivated
        public String btn2_on_url; // URL to access when SW input is activated
        public String btn2_off_url; // URL to access when SW input is deactivated
        public String out_on_url; // URL to access when output is activated
        public String out_off_url; // URL to access when output is deactivated
        public String btn_type; // Accepted values: momentary, toggle, edge, detached - // see SHELLY_BTNT_xxx
        public Integer swap_inputs; // 0=no
    }

    public static final String SHELLY_API_EVENTURL_BTN_ON = "btn_on_url";
    public static final String SHELLY_API_EVENTURL_BTN_OFF = "btn_off_url";
    public static final String SHELLY_API_EVENTURL_BTN1_ON = "btn1_on_url";
    public static final String SHELLY_API_EVENTURL_BTN1_OFF = "btn1_off_url";
    public static final String SHELLY_API_EVENTURL_BTN2_ON = "btn2_on_url";
    public static final String SHELLY_API_EVENTURL_BTN2_OFF = "btn2_off_url";
    public static final String SHELLY_API_EVENTURL_OUT_ON = "out_on_url";
    public static final String SHELLY_API_EVENTURL_OUT_OFF = "out_off_url";
    public static final String SHELLY_API_EVENTURL_ROLLER_OPEN = "roller_open_url";
    public static final String SHELLY_API_EVENTURL_ROLLER_CLOSE = "roller_close_url";
    public static final String SHELLY_API_EVENTURL_ROLLER_STOP = "roller_stop_url";
    public static final String SHELLY_API_EVENTURL_REPORT = "report_url";

    public static final String SHELLY_BTNT_MOMENTARY = "momentary";
    public static final String SHELLY_BTNT_TOGGLE = "toggle";
    public static final String SHELLY_BTNT_EDGE = "edge";
    public static final String SHELLY_BTNT_DETACHED = "detached";
    public static final String SHELLY_STATE_LAST = "last";
    public static final String SHELLY_STATE_STOP = "stop";
    public static final String SHELLY_INP_MODE_OPENCLOSE = "openclose";
    public static final String SHELLY_OBSTMODE_DISABLED = "disabled";
    public static final String SHELLY_SAFETYM_WHILEOPENING = "while_opening";
    public static final String SHELLY_ALWD_TRIGGER_NONE = "none";
    public static final String SHELLY_ALWD_ROLLER_TURN_OPEN = "open";
    public static final String SHELLY_ALWD_ROLLER_TURN_CLOSE = "close";
    public static final String SHELLY_ALWD_ROLLER_TURN_STOP = "stop";

    public static class ShellySettingsRoller {
        public Double maxtime;
        public Double maxtime_open;
        public Double maxtime_close;
        public String default_state; // see SHELLY_STATE_xxx
        public Boolean swap;
        public Boolean swap_inputs;
        public String input_mode; // see SHELLY_INP_MODE_OPENCLOSE
        public String button_type; // // see SHELLY_BTNT_xxx
        public Integer btn_reverse;
        public String state;
        public Double power;
        public Boolean is_valid;
        public Boolean safety_switch;
        public Boolean schedule;
        // ArrayList<ShellySettingsScheduleRules> schedule_rules;
        public String obstacle_mode; // SHELLY_OBSTMODE_
        public String obstacle_action; // see SHELLY_STATE_xxx
        public Integer obstacle_power;
        public Integer obstacle_delay;
        public String safety_mode; // see SHELLY_SAFETYM_xxx
        public String safety_action; // see SHELLY_STATE_xxx
        public String safety_allowed_on_trigger; // see SHELLY_ALWD_TRIGGER_xxx
        public Integer off_power;
        public Boolean positioning;
    }

    public static class ShellyInputState {
        public Integer input;
    }

    public static class ShellySettingsMeter {
        public Boolean is_valid;
        public Double power;
        public Double[] counters = { 0.0, 0.0, 0.0 };
        public Double total;
        public Long timestamp;
    }

    public static class ShellySettingsEMeter { // ShellyEM meter
        public Boolean is_valid; // Whether the associated meter is functioning properly
        public Double power; // Instantaneous power, Watts
        public Double reactive; // Instantaneous reactive power, Watts
        public Double voltage; // RMS voltage, Volts
        public Double total; // Total consumed energy, Wh
        public Double total_returned; // Total returned energy, Wh
        public Long timestamp;
    }

    public static class ShellySettingsUpdate {
        public String status;
        public Boolean has_update;
        public String new_version;
        public String old_version;
    }

    public static class ShellySettingsGlobal {
        // https://shelly-api-docs.shelly.cloud/#shelly1pm-settings
        public ShellySettingsDevice device;
        public ShellySettingsWiFiAp wifi_ap;
        public ShellySettingsWiFiNetwork wifi_sta;
        public ShellySettingsWiFiNetwork wifi_sta1;
        public ShellySettingsMqtt mqtt;
        public ShellySettingsSntp sntp;
        public ShellySettingsLogin login;
        public String pin_code;
        public Boolean coiot_execute_enable;
        public String name;
        public String fw;
        ShellySettingsBuildInfo build_info;
        ShellyStatusCloud cloud;
        public String timezone;
        public Double lat;
        public Double lng;
        public Boolean tzautodetect;
        public String time;
        public ShellySettingsHwInfo hwinfo;
        public String mode;
        public Double max_power;
        public ArrayList<ShellySettingsRelay> relays;
        public ArrayList<ShellySettingsDimmer> dimmers;
        public ArrayList<ShellySettingsEMeter> emeters;
        public Boolean led_status_disable; // PlugS only Disable LED indication for network status
        public Boolean led_power_disable; // PlugS only Disable LED indication for network status
        public String light_sensor; // Sense: sensor type
        public Boolean rain_sensor; // Flood: true=in rain mode
    }

    public static final String SHELLY_API_MODE = "mode";
    public static final String SHELLY_MODE_RELAY = "relay"; // Relay: relay mode
    public static final String SHELLY_MODE_ROLLER = "roller"; // Relay: roller mode
    public static final String SHELLY_MODE_COLOR = "color"; // Bulb/RGBW2: color mode
    public static final String SHELLY_MODE_WHITE = "white"; // Bulb/RGBW2: white mode

    public static final String SHELLY_LED_STATUS_DISABLE = "led_status_disable";
    public static final String SHELLY_LED_POWER_DISABLE = "led_power_disable";

    public static class ShellySettingsAttributes {
        public String device_type; // Device model identifier
        public String device_mac; // MAC address of the device in hexadecimal
        public String wifi_ap; // WiFi access poInteger configuration, see /settings/ap for details
        public String wifi_sta; // WiFi client configuration. See /settings/sta for details
        public String login; // credentials used for HTTP Basic authentication for the REST interface. If
                             // enabled is
                             // true clients must include an Authorization: Basic ... HTTP header with valid
                             // credentials
                             // when performing TP requests.
        public String name; // unique name of the device.
        public String fw; // current FW version
    }

    public static class ShellySettingsStatus {
        public ShellySettingsWiFiNetwork wifi_sta;

        public String time;
        public Integer serial;
        public Boolean has_update;
        public String mac;
        public ArrayList<ShellySettingsRelay> relays;
        public ArrayList<ShellySettingsRoller> rollers;
        public ArrayList<ShellyInputState> inputs;
        public ArrayList<ShellySettingsLight> lights;
        public ArrayList<ShellySettingsMeter> meters;
        public ArrayList<ShellySettingsEMeter> emeters;

        public ShellyStatusSensor._tmp tmp;
        public Boolean overtemperature;

        // Shelly Dimmer only
        public Boolean loaderror;
        public Boolean overload;

        public ShellySettingsUpdate update;
        public Long ram_total;
        public Long ram_free;
        public Long fs_size;
        public Long fs_free;
        public Long uptime;

        public String json;
    }

    public static class ShellyControlRelay {
        // https://shelly-api-docs.shelly.cloud/#shelly1-1pm-settings-relay-0
        public Boolean is_valid;
        public Boolean has_timer; // Whether a timer is currently armed for this channel
        public Boolean overpower; // Shelly1PM only if maximum allowed power was exceeded

        public String turn; // Accepted values are on and off. This will turn ON/OFF the respective output
                            // channel when request is sent .
        public Integer timer; // A one-shot flip-back timer in seconds.
    }

    public static class ShellyShortStatusRelay {
        public Boolean is_valid;
        public Boolean ison; // Whether output channel is on or off
        public Boolean has_timer; // Whether a timer is currently armed for this channel
        public Boolean overpower; // Shelly1PM only if maximum allowed power was exceeded
        public Double temperature; // Internal device temperature
        public Boolean overtemperature; // Device over heated
    }

    public static class ShellyShortStatusDimmer {
        public Boolean ison; // Whether output channel is on or off
        public Integer brightness;
        public String mode;
    }

    public static class ShellyStatusRelay {
        public ShellySettingsWiFiNetwork wifi_sta; // WiFi status
        public ShellyStatusCloud cloud; // Cloud status
        public ShellyStatusMqtt mqtt; // mqtt status
        public String time; // current time
        public Integer serial;
        public String mac; // MAC
        public ArrayList<ShellyShortStatusRelay> relays; // relay status
        public ArrayList<ShellySettingsMeter> meters; // current meter value

        public Boolean has_update; // If a newer firmware version is available
        public ShellySettingsUpdate update; // /status/firmware value

        public Integer ram_total; // Total and available amount of system memory in bytes
        public Integer ram_free;
        public Integer fs_size;
        public Integer fs_free; // Total and available amount of file system space in bytes
        public Integer uptime; // econds elapsed since boot
    }

    public static class ShellyStatusDimmer {
        public ShellySettingsWiFiNetwork wifi_sta; // WiFi status
        public ShellyStatusCloud cloud; // Cloud status
        public ShellyStatusMqtt mqtt; // mqtt status
        public String time; // current time
        public Integer serial;
        public String mac; // MAC
        public ArrayList<ShellyShortStatusDimmer> lights; // relay status
        public ArrayList<ShellySettingsMeter> meters; // current meter value

        public ShellyStatusSensor._tmp tmp;
        public Boolean overtemperature;

        public Boolean loaderror;
        public Boolean overload;

        public Boolean has_update; // If a newer firmware version is available
        public ShellySettingsUpdate update; // /status/firmware value

        public Integer ram_total; // Total and available amount of system memory in bytes
        public Integer ram_free;
        public Integer fs_size;
        public Integer fs_free; // Total and available amount of file system space in bytes
        public Integer uptime; // econds elapsed since boot
    }

    public static class ShellyControlRoller {
        public Integer roller_pos; // number Desired position in percent
        public Integer duration; // If specified, the motor will move for this period in seconds. If missing, the
                                 // value of
                                 // maxtime in /settings/roller/N will be used.
        public String state; // One of stop, open, close
        public Double power; // Current power consumption in Watts
        public Boolean is_valid;// If the power meter functions properly
        public Boolean safety_switch; // Whether the safety input is currently triggered
        public Boolean overtemperature;
        public String stop_reason; // Last cause for stopping: normal, safety_switch, obstacle
        public String last_direction; // Last direction of motion, open or close
        public Boolean calibrating;
        public Boolean positioning; // true when calibration was performed
        public Integer current_pos; // current position 0..100, 100=open
    }

    public static final String SHELLY_STOPR_NORMAL = "normal";
    public static final String SHELLY_STOPR_SAFETYSW = "safety_switch";
    public static final String SHELLY_STOPR_OBSTACLE = "obstacle";

    public static class ShellySettingsSensor {
        public String temperature_units; // Either'C'or'F'
        public Integer temperature_threshold; // Temperature delta (in configured degree units) which triggers an update
        public Integer humidity_threshold; // RH delta in % which triggers an update
        public Integer sleep_mode_period; // Periodic update period in hours, between 1 and 24
        public String report_url; // URL gets posted on updates with sensor data
    }

    public static class ShellyStatusSensor {
        // https://shelly-api-docs.shelly.cloud/#h-amp-t-settings
        public static class _tmp {
            public Double value; // Temperature in configured unites
            public String units; // 'C' or 'F'
            public Double tC; // temperature in deg C
            public Double tF; // temperature in deg F
            public Boolean is_valid; // whether the internal sensor is operating properly
        }

        public static class _hum {
            public Double value; // relative humidity in %
        }

        public static class _bat {
            public Double value; // estimated remaining battery capacity in %
            public Double voltage; // battery voltage
        };

        public static class _lux {
            public Boolean is_valid;
            public Double value;
        }

        public _tmp tmp;
        public _hum hum;
        public _lux lux;
        public _bat bat;

        public Boolean flood; // Shelly Flood: true = flood condition detected
        public Boolean rain_sensor; // Shelly Flood: true=in rain mode

        public Boolean motion; // Shelly Sense: true=motion detected
        public Boolean charger; // Shelly Sense: true=charger connected

        public String[] act_reasons; // HT/Smoke/Flood: list of reasons which woke up the device
    }

    public static class ShellySettingsSmoke {
        public String temperature_units; // Either 'C' or 'F'
        public Integer temperature_threshold; // Temperature delta (in configured degree units) which triggers an update
        public Integer sleep_mode_period; // Periodic update period in hours, between 1 and 24
    }

    public static final String SHELLY_TEMP_CELSIUS = "C";
    public static final String SHELLY_TEMP_FAHRENHEIT = "F";

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
        public String default_state; // one of on, off or last
        public Double auto_on; // see above
        public Double auto_off; // see above

        public Integer dcpower; // RGW2:Set to true for 24 V power supply, false for 12 V

        // Shelly Dimmer
        public String mode;
        public Boolean ison;
    }

    public static final int SHELLY_MIN_EFFECT = 0;
    public static final int SHELLY_MAX_EFFECT = 6;

    public static class ShellyStatusLightChannel {
        public Boolean ison;
        public Double power;
        public Boolean overpower;
        public Double auto_on; // see above
        public Double auto_off; // see above

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
        public ArrayList<ShellyStatusLightChannel> lights;
        public ArrayList<ShellySettingsMeter> meters;

        // public String mode; // COLOR or WHITE
        // public Integer input;
        // public Boolean has_update;
        // public ShellySettingsUpdate update;
        // public ShellySettingsWiFiNetwork wifi_sta; // WiFi client configuration. See
        // /settings/sta for details
        // public ShellyStatusCloud cloud;
        // public ShellyStatusMqtt mqtt;
    }

    public static class ShellySenseKeyCode {
        String id; // ID of the stored IR code into Shelly Sense.
        String name; // Short description or name of the stored IR code.
    }

    public static class SellySendKeyList {
        public ArrayList<ShellySenseKeyCode> key_codes;
    }

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

    public static final Integer SHELLY_MIN_ROLLER_POS = 0;
    public static final Integer SHELLY_MAX_ROLLER_POS = 100;
    public static final Integer SHELLY_MIN_BRIGHTNESS = 0;
    public static final Integer SHELLY_MAX_BRIGHTNESS = 100;
    public static final int SHELLY_MIN_GAIN = 0;
    public static final Integer SHELLY_MAX_GAIN = 100;
    public static final Integer SHELLY_MIN_COLOR = 0;
    public static final Integer SHELLY_MAX_COLOR = 255;
    public static final int SHELLY_DIM_STEPSIZE = 10;

    // color temperature: 3000 = warm, 4750 = white, 6565 = cold; gain: 0..100
    public static final int MIN_COLOR_TEMPERATURE = 3000;
    public static final int MAX_COLOR_TEMPERATURE = 6500;
    public static final int COLOR_TEMPERATURE_RANGE = MAX_COLOR_TEMPERATURE - MIN_COLOR_TEMPERATURE;
    public static final double MIN_BRIGHTNESS = 0.0;
    public static final double MAX_BRIGHTNESS = 100.0;
    public static final double SATURATION_FACTOR = 2.55;
    public static final double GAIN_FACTOR = SHELLY_MAX_GAIN / 100;
    public static final double BRIGHTNESS_FACTOR = SHELLY_MAX_BRIGHTNESS / 100;
}
