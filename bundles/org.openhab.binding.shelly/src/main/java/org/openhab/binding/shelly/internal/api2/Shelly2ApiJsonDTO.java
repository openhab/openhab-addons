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
package org.openhab.binding.shelly.internal.api2;

import java.util.ArrayList;

import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage.Shelly2RpcMessageError;

import com.google.gson.annotations.SerializedName;

/**
 * {@link Shelly2ApiJsonDTO} wraps the Shelly REST API and provides various low level function to access the device api
 * (not
 * cloud api).
 *
 * @author Markus Michels - Initial contribution
 */
public class Shelly2ApiJsonDTO {
    public static final String SHELLYRPC_METHOD_CLASS_SHELLY = "Shelly";
    public static final String SHELLYRPC_METHOD_CLASS_SWITCH = "Switch";

    public static final String SHELLYRPC_METHOD_GETDEVCONFIG = "GetDeviceInfo";
    public static final String SHELLYRPC_METHOD_GETSYSCONFIG = "GetSysConfig"; // only sys
    public static final String SHELLYRPC_METHOD_GETCONFIG = "GetConfig"; // sys + components
    public static final String SHELLYRPC_METHOD_GETSYSSTATUS = "GetSysStatus"; // only sys
    public static final String SHELLYRPC_METHOD_GETSTATUS = "GetStatus"; // sys + components
    public static final String SHELLYRPC_METHOD_REBOOT = "Shelly.Reboot";
    public static final String SHELLYRPC_METHOD_RESET = "Shelly.FactoryReset";
    public static final String SHELLYRPC_METHOD_CHECKUPD = "Shelly.CheckForUpdate";
    public static final String SHELLYRPC_METHOD_UPDATE = "Shelly.Update";
    public static final String SHELLYRPC_METHOD_AUTHSET = "Shelly.SetAuth";
    public static final String SHELLYRPC_METHOD_SWITCH_STATUS = "Switch.GetStatus";
    public static final String SHELLYRPC_METHOD_SWITCH_SET = "Switch.Set";
    public static final String SHELLYRPC_METHOD_SWITCH_SETCONFIG = "Switch.SetConfig";
    public static final String SHELLYRPC_METHOD_COVER_SETPOS = "Cover.GoToPosition";
    public static final String SHELLY2_COVER_CMD_OPEN = "Open";
    public static final String SHELLY2_COVER_CMD_CLOSE = "Close";
    public static final String SHELLY2_COVER_CMD_STOP = "Stop";
    public static final String SHELLYRPC_METHOD_WIFIGETCONG = "Wifi.GetConfig";
    public static final String SHELLYRPC_METHOD_WIFISETCONG = "Wifi.SetConfig";
    public static final String SHELLYRPC_METHOD_ETHGETCONG = "Eth.GetConfig";
    public static final String SHELLYRPC_METHOD_ETHSETCONG = "Eth.SetConfig";
    public static final String SHELLYRPC_METHOD_BLEGETCONG = "BLE.GetConfig";
    public static final String SHELLYRPC_METHOD_BLESETCONG = "BLE.SetConfig";
    public static final String SHELLYRPC_METHOD_CLOUDSET = "Cloud.SetConfig";
    public static final String SHELLYRPC_METHOD_WSGETCONFIG = "WS.GetConfig";
    public static final String SHELLYRPC_METHOD_WSSETCONFIG = "WS.SetConfig";

    public static final String SHELLYRPC_METHOD_NOTIFYSTATUS = "NotifyStatus"; // inbound status
    public static final String SHELLYRPC_METHOD_NOTIFYFULLSTATUS = "NotifyFullStatus"; // inbound status from bat device
    public static final String SHELLYRPC_METHOD_NOTIFYEVENT = "NotifyEvent"; // inbound event

    // Component types
    public static final String SHELLY2_PROFILE_RELAY = "switch";
    public static final String SHELLY2_PROFILE_ROLLER = "cover";

    // Button types/modes
    public static final String SHELLY2_BTNT_MOMENTARY = "momentary";
    public static final String SHELLY2_BTNT_FLIP = "flip";
    public static final String SHELLY2_BTNT_FOLLOW = "follow";
    public static final String SHELLY2_BTNT_DETACHED = "detached";

    // Input types
    public static final String SHELLY2_INPUTT_SWITCH = "switch";
    public static final String SHELLY2_INPUTT_BUTTON = "button";
    public static final String SHELLY2_INPUTT_ANALOG = "analog"; // Shelly Addon: analogous input

    // Switcm modes
    public static final String SHELLY2_API_MODE_DETACHED = "detached";
    public static final String SHELLY2_API_MODE_FOLLOW = "follow";

    // Initial switch states
    public static final String SHELLY2_API_ISTATE_ON = "on";
    public static final String SHELLY2_API_ISTATE_OFF = "off";
    public static final String SHELLY2_API_ISTATE_FOLLOWLAST = "restore_last";
    public static final String SHELLY2_API_ISTATE_MATCHINPUT = "match_input";

    // Cover/Roller modes
    public static final String SHELLY2_RMODE_SINGLE = "single";
    public static final String SHELLY2_RMODE_DUAL = "dual";
    public static final String SHELLY2_RMODE_DETACHED = "detached";

    public static final String SHELLY2_RSTATE_OPENING = "opening";
    public static final String SHELLY2_RSTATE_OPEN = "open";
    public static final String SHELLY2_RSTATE_CLOSING = "closing";
    public static final String SHELLY2_RSTATE_CLOSED = "closed";
    public static final String SHELLY2_RSTATE_STOPPED = "stopped";
    public static final String SHELLY2_RSTATE_CALIB = "calibrating";

    // Event notifications
    public static final String SHELLY2_EVENT_BTNUP = "btn_up";
    public static final String SHELLY2_EVENT_BTNDOWN = "btn_down";
    public static final String SHELLY2_EVENT_1PUSH = "single_push";
    public static final String SHELLY2_EVENT_2PUSH = "double_push";
    public static final String SHELLY2_EVENT_3PUSH = "triple_push";
    public static final String SHELLY2_EVENT_LPUSH = "long_push";
    public static final String SHELLY2_EVENT_SLPUSH = "short_long_push";
    public static final String SHELLY2_EVENT_LSPUSH = "long_short_push";

    public static final String SHELLY2_EVENT_SLEEP = "sleep";
    public static final String SHELLY2_EVENT_CFGCHANGED = "config_changed";
    public static final String SHELLY2_EVENT_OTASTART = "ota_begin";
    public static final String SHELLY2_EVENT_OTAPROGRESS = "ota_progress";
    public static final String SHELLY2_EVENT_OTADONE = "ota_success";
    public static final String SHELLY2_EVENT_WIFICONNFAILED = "sta_connect_fail";
    public static final String SHELLY2_EVENT_WIFIDISCONNECTED = "sta_disconnected";

    // Error Codes
    public static final String SHELLY2_ERROR_OVERPOWER = "overpower";
    public static final String SHELLY2_ERROR_OVERTEMP = "overtemp";
    public static final String SHELLY2_ERROR_OVERVOLTAGE = "overvoltage";

    // Wakeup reasons (e.g. Plus HT)
    public static final String SHELLY2_WAKEUPO_BOOT_POWERON = "poweron";
    public static final String SHELLY2_WAKEUPO_BOOT_RESTART = "software_restart";
    public static final String SHELLY2_WAKEUPO_BOOT_WAKEUP = "deepsleep_wake";
    public static final String SHELLY2_WAKEUPO_BOOT_INTERNAL = "internal";
    public static final String SHELLY2_WAKEUPO_BOOT_UNKNOWN = "unknown";

    public static final String SHELLY2_WAKEUPOCAUSE_BUTTON = "button";
    public static final String SHELLY2_WAKEUPOCAUSE_USB = "usb";
    public static final String SHELLY2_WAKEUPOCAUSE_PERIODIC = "periodic";
    public static final String SHELLY2_WAKEUPOCAUSE_UPDATE = "status_update";
    public static final String SHELLY2_WAKEUPOCAUSE_UNDEFINED = "undefined";

    public class Shelly2DevConfigBle {
        public Boolean enable;
    }

    public class Shelly2DevConfigEth {
        public Boolean enable;
        public String ipv4mode;
        public String ip;
        public String netmask;
        public String gw;
        public String nameserver;
    }

    public static class Shelly2DeviceSettings {
        public String name;
        public String id;
        public String mac;
        public String model;
        public Integer gen;
        @SerializedName("fw_id")
        public String firmware;
        public String ver;
        public String app;
        @SerializedName("auth_en")
        public Boolean authEnable;
        @SerializedName("auth_domain")
        public String authDomain;
    }

    public static class Shelly2DeviceConfigAp {
        public static class Shelly2DeviceConfigApRE {
            public Boolean enable;
        }

        public Boolean enable;
        public String ssid;
        public String password;
        @SerializedName("is_open")
        public Boolean isOpen;
        @SerializedName("range_extender")
        Shelly2DeviceConfigApRE rangeExtender;
    }

    public static class Shelly2DeviceConfig {
        public class Shelly2DeviceConfigSys {
            public class Shelly2DeviceConfigDevice {
                public String name;
                public String mac;
                @SerializedName("fw_id")
                public String fwId;
                public String profile;
                @SerializedName("eco_mode")
                public Boolean ecoMode;
                public Boolean discoverable;
            }

            public class Shelly2DeviceConfigLocation {
                public String tz;
                public Double lat;
                public Double lon;
            }

            public class Shelly2DeviceConfigSntp {
                public String server;
            }

            public class Shelly2DeviceConfigSleep {
                @SerializedName("wakeup_period")
                public Integer wakeupPeriod;
            }

            public class Shelly2DeviceConfigDebug {
                public class Shelly2DeviceConfigDebugMqtt {
                    public Boolean enable;
                }

                public class Shelly2DeviceConfigDebugWebSocket {
                    public Boolean enable;
                }

                public class Shelly2DeviceConfigDebugUdp {
                    public String addr;
                }

                public Shelly2DeviceConfigDebugMqtt mqtt;
                public Shelly2DeviceConfigDebugWebSocket websocket;
                public Shelly2DeviceConfigDebugUdp udp;
            }

            public class Shelly2DeviceConfigUiData {
                public String cover; // hold comma seperated list of roller favorites
            }

            public class Shelly2DeviceConfigRpcUdp {
                @SerializedName("dst_addr")
                public String dstAddr;
                @SerializedName("listenPort")
                public String listenPort;
            }

            @SerializedName("cfg_rev")
            public Integer cfgRevision;
            public Shelly2DeviceConfigDevice device;
            public Shelly2DeviceConfigLocation location;
            public Shelly2DeviceConfigSntp sntp;
            public Shelly2DeviceConfigSleep sleep;
            public Shelly2DeviceConfigDebug debug;
            @SerializedName("ui_data")
            public Shelly2DeviceConfigUiData uiData;
            @SerializedName("rpc_udp")
            public Shelly2DeviceConfigRpcUdp rpcUdp;
        }

        public class Shelly2DevConfigInput {
            public String id;
            public String name;
            public String type;
            public Boolean invert;
            @SerializedName("factory_reset")
            public Boolean factoryReset;
            @SerializedName("report_thr")
            public Double reportTreshold; // only for type analog
        }

        public class Shelly2DevConfigSwitch {
            public String id;
            public String name;

            @SerializedName("in_mode")
            public String mode;

            @SerializedName("initial_state")
            public String initialState;
            @SerializedName("auto_on")
            public Boolean autoOn;
            @SerializedName("auto_on_delay")
            public Double autoOnDelay;
            @SerializedName("auto_off")
            public Boolean autoOff;
            @SerializedName("auto_off_delay")
            public Double autoOffDelay;
            @SerializedName("power_limit")
            public Integer powerLimit;
            @SerializedName("voltage_limit")
            public Integer voltageLimit;
            @SerializedName("current_limit")
            public Double currentLimit;
        }

        public class Shelly2DevConfigCover {
            public class Shelly2DeviceConfigCoverMotor {
                @SerializedName("idle_power_thr")
                public Double idle_powerThr;
            }

            public class Shelly2DeviceConfigCoverSafetySwitch {
                public Boolean enable;
                public String direction;
                public String action;
                @SerializedName("allowed_move")
                public String allowedMove;
            }

            public class Shelly2DeviceConfigCoverObstructionDetection {
                public Boolean enable;
                public String direction;
                public String action;
                @SerializedName("power_thr")
                public Integer powerThr;
                public Double holdoff;
            }

            public String id;
            public String name;
            public Shelly2DeviceConfigCoverMotor motor;
            @SerializedName("maxtime_open")
            public Double maxtimeOpen;
            @SerializedName("maxtime_close")
            public Double maxtimeClose;
            @SerializedName("initial_state")
            public String initialState;
            @SerializedName("invert_directions")
            public Boolean invertDirections;
            @SerializedName("in_mode")
            public String inMode;
            @SerializedName("swap_inputs")
            public Boolean swapInputs;
            @SerializedName("safety_switch")
            public Shelly2DeviceConfigCoverSafetySwitch safetySwitch;
            @SerializedName("power_limit")
            public Integer powerLimit;
            @SerializedName("voltage_limit")
            public Integer voltageLimit;
            @SerializedName("current_limit")
            public Double currentLimit;
            @SerializedName("obstruction_detection")
            public Shelly2DeviceConfigCoverObstructionDetection obstructionDetection;
        }

        public static class Shelly2GetConfigResult {

            public class Shelly2DevConfigCloud {
                public Boolean enable;
                public String server;
            }

            public class Shelly2DevConfigMqtt {
                public Boolean enable;
                public String server;
                public String user;
                @SerializedName("topic_prefix:0")
                public String topicPrefix;
                @SerializedName("rpc_ntf")
                public String rpcNtf;
                @SerializedName("status_ntf")
                public String statusNtf;
            }

            public Shelly2DevConfigBle ble;
            public Shelly2DevConfigEth eth;
            public Shelly2DevConfigCloud cloud;
            public Shelly2DevConfigMqtt mqtt;
            public Shelly2DeviceConfigSys sys;
            public Shelly2DeviceConfigWiFi wifi;

            @SerializedName("input:0")
            public Shelly2DevConfigInput input0;
            @SerializedName("input:1")
            public Shelly2DevConfigInput input1;
            @SerializedName("input:2")
            public Shelly2DevConfigInput input2;
            @SerializedName("input:3")
            public Shelly2DevConfigInput input3;

            @SerializedName("switch:0")
            public Shelly2DevConfigSwitch switch0;
            @SerializedName("switch:1")
            public Shelly2DevConfigSwitch switch1;
            @SerializedName("switch:2")
            public Shelly2DevConfigSwitch switch2;
            @SerializedName("switch:3")
            public Shelly2DevConfigSwitch switch3;

            @SerializedName("cover:0")
            public Shelly2DevConfigCover cover0;
        }

        public class Shelly2DeviceConfigSta {
            public String ssid;
            public String password;
            @SerializedName("is_open")
            public Boolean isOpen;
            public Boolean enable;
            public String ipv4mode;
            public String ip;
            public String netmask;
            public String gw;
            public String nameserver;
        }

        public class Shelly2DeviceConfigRoam {
            @SerializedName("rssi_thr")
            public Integer rssiThr;
            public Integer interval;
        }

        public class Shelly2DeviceConfigWiFi {
            public Shelly2DeviceConfigAp ap;
            public Shelly2DeviceConfigSta sta;
            public Shelly2DeviceConfigSta sta1;
            public Shelly2DeviceConfigRoam roam;
        }

        public String id;
        public String src;
        public Shelly2GetConfigResult result;
    }

    public static class Shelly2DeviceStatus {
        public class Shelly2InputStatus {
            public Integer id;
            public Boolean state;
            public Double percent; // analog input only
            public ArrayList<String> errors;// shown only if at least one error is present.
        }

        public static class Shelly2DeviceStatusResult {
            public class Shelly2DeviceStatusBle {

            }

            public class Shelly2DeviceStatusCloud {
                public Boolean connected;
            }

            public class Shelly2DeviceStatusMqqt {
                public Boolean connected;
            }

            public class Shelly2CoverStatus {
                public Integer id;
                public String source;
                public String state;
                public Double apower;
                public Double voltage;
                public Double current;
                public Double pf;
                public Shelly2Energy aenergy;
                @SerializedName("current_pos")
                public Integer currentPos;
                @SerializedName("target_pos")
                public Integer targetPos;
                @SerializedName("move_timeout")
                public Double moveTimeout;
                @SerializedName("move_started_at")
                public Double moveStartedAt;
                @SerializedName("pos_control")
                public Boolean posControl;
                public Shelly2DeviceStatusTemp temperature;
                public ArrayList<String> errors;
            }

            public class Shelly2DeviceStatusHumidity {
                public Integer id;
                public Double rh;
            }

            public class Shelly2DeviceStatusVoltage {
                public Integer id;
                public Double voltage;
            }

            public class Shelly2DeviceStatusTempId extends Shelly2DeviceStatusTemp {
                public Integer id;
            }

            public static class Shelly2DeviceStatusPower {
                public static class Shelly2DeviceStatusBattery {
                    @SerializedName("V")
                    public Double volt;
                    public Double percent;
                }

                public static class Shelly2DeviceStatusCharger {
                    public Boolean present;
                }

                public Integer id;
                public Shelly2DeviceStatusBattery battery;
                public Shelly2DeviceStatusCharger external;
            }

            public Shelly2DeviceStatusBle ble;
            public Shelly2DeviceStatusCloud cloud;
            public Shelly2DeviceStatusMqqt mqtt;
            public Shelly2DeviceStatusSys sys;
            public Shelly2DeviceStatusSysWiFi wifi;

            @SerializedName("input:0")
            public Shelly2InputStatus input0;
            @SerializedName("input:1")
            public Shelly2InputStatus input1;
            @SerializedName("input:2")
            public Shelly2InputStatus input2;
            @SerializedName("input:3")
            public Shelly2InputStatus input3;
            @SerializedName("input:100")
            public Shelly2InputStatus input100; // Digital Input from Add-On

            @SerializedName("switch:0")
            public Shelly2RelayStatus switch0;
            @SerializedName("switch:1")
            public Shelly2RelayStatus switch1;
            @SerializedName("switch:2")
            public Shelly2RelayStatus switch2;
            @SerializedName("switch:3")
            public Shelly2RelayStatus switch3;

            @SerializedName("cover:0")
            public Shelly2CoverStatus cover0;

            @SerializedName("temperature:0")
            public Shelly2DeviceStatusTempId temperature0;
            @SerializedName("temperature:100")
            public Shelly2DeviceStatusTempId temperature100;
            @SerializedName("temperature:101")
            public Shelly2DeviceStatusTempId temperature101;
            @SerializedName("temperature:102")
            public Shelly2DeviceStatusTempId temperature102;
            @SerializedName("temperature:103")
            public Shelly2DeviceStatusTempId temperature103;
            @SerializedName("temperature:104")
            public Shelly2DeviceStatusTempId temperature104;

            @SerializedName("humidity:0")
            public Shelly2DeviceStatusHumidity humidity0;
            @SerializedName("humidity:100")
            public Shelly2DeviceStatusHumidity humidity100;

            @SerializedName("voltmeter:100")
            public Shelly2DeviceStatusVoltage voltmeter100;

            @SerializedName("devicepower:0")
            public Shelly2DeviceStatusPower devicepower0;
        }

        public class Shelly2DeviceStatusSys {
            public class Shelly2DeviceStatusSysAvlUpdate {
                public class Shelly2DeviceStatusSysUpdate {
                    public String version;
                }

                public Shelly2DeviceStatusSysUpdate stable;
                public Shelly2DeviceStatusSysUpdate beta;
            }

            public class Shelly2DeviceStatusWakeup {
                public String boot;
                public String cause;
            }

            public String mac;
            @SerializedName("restart_required")
            public Boolean restartRequired;
            public String time;
            public Long unixtime;
            public Long uptime;
            @SerializedName("ram_size")
            public Long ramSize;
            @SerializedName("ram_free")
            public Long ramFree;
            @SerializedName("fs_size")
            public Long fsSize;
            @SerializedName("fs_free")
            public Long fsFree;
            @SerializedName("cfg_rev")
            public Integer cfg_rev;
            @SerializedName("available_updates")
            public Shelly2DeviceStatusSysAvlUpdate availableUpdates;
            @SerializedName("webhook_rev")
            public Integer webHookRev;
            @SerializedName("wakeup_reason")
            public Shelly2DeviceStatusWakeup wakeUpReason;
            @SerializedName("wakeup_period")
            public Integer wakeupPeriod;
        }

        public class Shelly2DeviceStatusSysWiFi {
            @SerializedName("sta_ip")
            public String staIP;
            public String status;
            public String ssid;
            public Integer rssi;
            @SerializedName("ap_client_count")
            public Integer apClientCount;
        }

        public String id;
        public String src;
        public Shelly2DeviceStatusResult result;
    }

    public static class Shelly2RelayStatus {
        public Integer id;
        public String source;
        public Boolean output;
        @SerializedName("timer_started_at")
        public Double timerStartetAt;
        @SerializedName("timer_duration")
        public Integer timerDuration;
        public Double apower;
        public Double voltage;
        public Double current;
        public Double pf;
        public Shelly2Energy aenergy;
        public Shelly2DeviceStatusTemp temperature;
        public String[] errors;
    }

    public static class Shelly2DeviceStatusTemp {
        public Double tC;
        public Double tF;
    }

    public static class Shelly2Energy {
        // "switch:1":{"id":1,"aenergy":{"total":0.003,"by_minute":[0.000,0.000,0.000],"minute_ts":1619910239}}}}
        public Double total;
        @SerializedName("by_minute")
        public Double[] byMinute;
        @SerializedName("minute_ts")
        public Long minuteTs;
    }

    public static class Shelly2ConfigParms {
        public String name;
        public Boolean enable;
        public String server;
        @SerializedName("ssl_ca")
        public String sslCA;

        // WiFi.SetConfig
        public Shelly2DeviceConfigAp ap;

        // Switch.SetConfig
        @SerializedName("auto_on")
        public Boolean autoOn;
        @SerializedName("auto_on_delay")
        public Double autoOnDelay;
        @SerializedName("auto_off")
        public Boolean autoOff;
        @SerializedName("auto_off_delay")
        public Double autoOffDelay;
    }

    public static class Shelly2RpcRequest {
        public Integer id = 0;
        public String method;

        public static class Shelly2RpcRequestParams {
            public Integer id = 1;

            // Cover
            public Integer pos;
            public Boolean on;

            // Shelly.SetAuth
            public String user;
            public String realm;
            public String ha1;

            // Shelly.Update
            public String stage;
            public String url;

            // Cloud.SetConfig
            public Shelly2ConfigParms config;

            public Shelly2RpcRequestParams withConfig() {
                config = new Shelly2ConfigParms();
                return this;
            }
        }

        public Shelly2RpcRequestParams params = new Shelly2RpcRequestParams();

        public Shelly2RpcRequest() {
        }

        public Shelly2RpcRequest withMethod(String method) {
            this.method = method;
            return this;
        }

        public Shelly2RpcRequest withId(int id) {
            params.id = id;
            return this;
        }

        public Shelly2RpcRequest withPos(int pos) {
            params.pos = pos;
            return this;
        }
    }

    public static class Shelly2WsConfigResponse {
        public Integer id;
        public String src;

        public static class Shelly2WsConfigResult {
            @SerializedName("restart_required")
            public Boolean restartRequired;
        }

        public Shelly2WsConfigResult result;
    }

    public static class Shelly2RpcBaseMessage {
        // Basic message format, e.g.
        // {"id":1,"src":"localweb528","method":"Shelly.GetConfig"}
        public class Shelly2RpcMessageError {
            public Integer code;
            public String message;
        }

        public Integer id;
        public String src;
        public String dst;
        public String method;
        public Object params;
        public Object result;
        public Shelly2AuthRequest auth;
        public Shelly2RpcMessageError error;
    }

    public static class Shelly2RpcNotifyStatus {
        public static class Shelly2NotifyStatus extends Shelly2DeviceStatusResult {
            public Double ts;
        }

        public Integer id;
        public String src;
        public String dst;
        public String method;
        public Shelly2NotifyStatus params;
        public Shelly2NotifyStatus result;
        public Shelly2RpcMessageError error;
    }

    public static String SHELLY2_AUTHTTYPE_DIGEST = "digest";
    public static String SHELLY2_AUTHTTYPE_STRING = "string";
    public static String SHELLY2_AUTHALG_SHA256 = "SHA-256";
    // = ':auth:'+HexHash("dummy_method:dummy_uri");
    public static String SHELLY2_AUTH_NOISE = "6370ec69915103833b5222b368555393393f098bfbfbb59f47e0590af135f062";

    public static class Shelly2AuthRequest {
        public String username;
        public Long nonce;
        public Long cnonce;
        public Integer nc;
        public String realm;
        public String algorithm;
        public String response;
        @SerializedName("auth_type")
        public String authType;
    }

    public static class Shelly2AuthResponse { // on 401 message contains the auth info
        @SerializedName("auth_type")
        public String authType;
        public Long nonce;
        public Integer nc;
        public String realm;
        public String algorithm;
    }

    public class Shelly2NotifyEvent {
        public Integer id;
        public Double ts;
        public String component;
        public String event;
        public String msg;
        public Integer reason;
        @SerializedName("cfg_rev")
        public Integer cfgRev;
    }

    public class Shelly2NotifyEventData {
        public Double ts;
        public ArrayList<Shelly2NotifyEvent> events;
    }

    public static class Shelly2RpcNotifyEvent {
        public Double ts;
        Shelly2NotifyEventData params;
    }
}
