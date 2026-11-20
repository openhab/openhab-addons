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
package org.openhab.binding.shelly.internal.api2.dto;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api2.dto.Shelly2ConfigJsonDTO.Shelly2DevConfigBle.Shelly2DevConfigBleObserver;
import org.openhab.binding.shelly.internal.api2.dto.Shelly2ConfigJsonDTO.Shelly2DevConfigBle.Shelly2DevConfigBleRpc;
import org.openhab.binding.shelly.internal.api2.dto.ShellyCoverJsonDTO.Shelly2DevConfigCover;
import org.openhab.binding.shelly.internal.api2.dto.ShellyLightJsonDTO.Shelly2GetConfigLight;
import org.openhab.binding.shelly.internal.api2.dto.ShellyLoraJsonDTO.Shelly2DeviceConfigLora;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMeterJsonDTO.Shelly2DevConfigEm;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMeterJsonDTO.Shelly2DevConfigPm1;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMeterJsonDTO.Shelly2DevConfigVoltmeter;
import org.openhab.binding.shelly.internal.api2.dto.ShellyPresenceJsonDTO.Shelly2DevConfigPresence;

import com.google.gson.annotations.SerializedName;

/**
 * {@link Shelly2ConfigJsonDTO} contains the JSON structures of the Shelly.GetConfig result and the SetConfig
 * parameters.
 *
 * @author Markus Michels - Initial contribution
 */
public class Shelly2ConfigJsonDTO {

    public static class Shelly2DevConfigBle {
        public static class Shelly2DevConfigBleRpc {
            public @Nullable Boolean enable;
        }

        public static class Shelly2DevConfigBleObserver {
            public @Nullable Boolean enable;
        }

        public @Nullable Boolean enable;
        public @Nullable Shelly2DevConfigBleRpc rpc;
        public @Nullable Shelly2DevConfigBleObserver observer;
    }

    public static class Shelly2DevConfigEth {
        public @Nullable Boolean enable;
        public @Nullable String ipv4mode;
        public @Nullable String ip;
        public @Nullable String netmask;
        public @Nullable String gw;
        public @Nullable String nameserver;
    }

    public static class Shelly2DeviceSettings {
        public @Nullable String name;
        public @Nullable String id;
        public @Nullable String mac;
        public @Nullable String model;
        public @Nullable String profile;
        public @Nullable Integer gen;
        @SerializedName("fw_id")
        public @Nullable String fw;
        public @Nullable String ver;
        public @Nullable String app;
        @SerializedName("auth_en")
        public @Nullable Boolean auth;
        @SerializedName("auth_domain")
        public @Nullable String authDomain;
    }

    public static class Shelly2DeviceConfigAp {
        public static class Shelly2DeviceConfigApRE {
            public @Nullable Boolean enable;
        }

        public @Nullable Boolean enable;
        public @Nullable String ssid;
        public @Nullable String password;
        @SerializedName("is_open")
        public @Nullable Boolean isOpen;
        @SerializedName("range_extender")
        public @Nullable Shelly2DeviceConfigApRE rangeExtender;
    }

    public static class Shelly2DeviceConfig {
        public class Shelly2DeviceConfigSys {
            public class Shelly2DeviceConfigDevice {
                public @Nullable String name;
                public @Nullable String mac;
                @SerializedName("fw_id")
                public @Nullable String fwId;
                public @Nullable String profile;
                @SerializedName("eco_mode")
                public @Nullable Boolean ecoMode;
                public @Nullable Boolean discoverable;
            }

            public class Shelly2DeviceConfigLocation {
                public @Nullable String tz;
                public @Nullable Double lat;
                public @Nullable Double lon;
            }

            public class Shelly2DeviceConfigSntp {
                public @Nullable String server;
            }

            public class Shelly2DeviceConfigSleep {
                @SerializedName("wakeup_period")
                public @Nullable Integer wakeupPeriod;
            }

            public class Shelly2DeviceConfigDebug {
                public class Shelly2DeviceConfigDebugMqtt {
                    public @Nullable Boolean enable;
                }

                public class Shelly2DeviceConfigDebugWebSocket {
                    public @Nullable Boolean enable;
                }

                public class Shelly2DeviceConfigDebugUdp {
                    public @Nullable String addr;
                }

                public @Nullable Shelly2DeviceConfigDebugMqtt mqtt;
                public @Nullable Shelly2DeviceConfigDebugWebSocket websocket;
                public @Nullable Shelly2DeviceConfigDebugUdp udp;
            }

            public class Shelly2DeviceConfigUiData {
                public @Nullable String cover; // hold comma separated list of roller favorites
            }

            public class Shelly2DeviceConfigRpcUdp {
                @SerializedName("dst_addr")
                public @Nullable String dstAddr;
                @SerializedName("listenPort")
                public @Nullable String listenPort;
            }

            @SerializedName("cfg_rev")
            public @Nullable Integer cfgRevision;
            public @Nullable Shelly2DeviceConfigDevice device;
            public @Nullable Shelly2DeviceConfigLocation location;
            public @Nullable Shelly2DeviceConfigSntp sntp;
            public @Nullable Shelly2DeviceConfigSleep sleep;
            public @Nullable Shelly2DeviceConfigDebug debug;
            @SerializedName("ui_data")
            public @Nullable Shelly2DeviceConfigUiData uiData;
            @SerializedName("rpc_udp")
            public @Nullable Shelly2DeviceConfigRpcUdp rpcUdp;
        }

        public class Shelly2DevConfigInput {
            public @Nullable Integer id;
            public @Nullable String name;
            public @Nullable String type;
            public @Nullable Boolean invert;
            @SerializedName("factory_reset")
            public @Nullable Boolean factoryReset;
            @SerializedName("report_thr")
            public @Nullable Double reportThreshold; // only for type analog
        }

        public class Shelly2DevConfigSwitch {
            public @Nullable Integer id;
            public @Nullable String name;

            @SerializedName("in_mode")
            public @Nullable String mode;

            @SerializedName("initial_state")
            public @Nullable String initialState;
            @SerializedName("auto_on")
            public @Nullable Boolean autoOn;
            @SerializedName("auto_on_delay")
            public @Nullable Double autoOnDelay;
            @SerializedName("auto_off")
            public @Nullable Boolean autoOff;
            @SerializedName("auto_off_delay")
            public @Nullable Double autoOffDelay;
            @SerializedName("power_limit")
            public @Nullable Integer powerLimit;
            @SerializedName("voltage_limit")
            public @Nullable Integer voltageLimit;
            @SerializedName("current_limit")
            public @Nullable Double currentLimit;
        }

        public static class ShellyDeviceConfigCB {
            public @Nullable Integer id;
            public @Nullable String name;
            @SerializedName("undervoltage_limit")
            public @Nullable Integer undervoltageLimit;
            @SerializedName("voltage_limit")
            public @Nullable Integer voltageLimit;
            @SerializedName("reaction_delay")
            public @Nullable Double reactionDelay;
        }

        public static class Shelly2ConfigSmoke {
            public @Nullable Integer id;
            public @Nullable Boolean alarm;
            public @Nullable Boolean mute;
        }

        public static class Shelly2ConfigFlood {
            public @Nullable Integer id;
            public @Nullable String name;
            @SerializedName("alarm_mode")
            public @Nullable String alarmMode;
            @SerializedName("report_holdoff")
            public @Nullable Integer reportHoldoff;
        }

        public class Shelly2DeviceConfigLed {
            @SerializedName("sys_led_enable")
            public @Nullable Boolean sysLedEnable;
            @SerializedName("power_led")
            public @Nullable String powerLed;
        }

        public static class Shelly2GetConfigResult {

            public class Shelly2DevConfigCloud {
                public @Nullable Boolean enable;
                public @Nullable String server;
            }

            public class Shelly2DevConfigMqtt {
                public @Nullable Boolean enable;
                public @Nullable String server;
                public @Nullable String user;
                @SerializedName("topic_prefix:0")
                public @Nullable String topicPrefix;
                @SerializedName("rpc_ntf")
                public @Nullable String rpcNtf;
                @SerializedName("status_ntf")
                public @Nullable String statusNtf;
            }

            public @Nullable Shelly2DevConfigBle ble;
            public @Nullable Shelly2DevConfigEth eth;
            public @Nullable Shelly2DevConfigCloud cloud;
            public @Nullable Shelly2DevConfigMqtt mqtt;
            public @Nullable Shelly2DeviceConfigSys sys;
            public @Nullable Shelly2DeviceConfigWiFi wifi;
            @SerializedName("wd_ui")
            public @Nullable Shelly2DeviceConfigLed led;

            @SerializedName("input:0")
            public @Nullable Shelly2DevConfigInput input0;
            @SerializedName("input:1")
            public @Nullable Shelly2DevConfigInput input1;
            @SerializedName("input:2")
            public @Nullable Shelly2DevConfigInput input2;
            @SerializedName("input:3")
            public @Nullable Shelly2DevConfigInput input3;
            @SerializedName("input:4")
            public @Nullable Shelly2DevConfigInput input4;

            @SerializedName("switch:0")
            public @Nullable Shelly2DevConfigSwitch switch0;
            @SerializedName("switch:1")
            public @Nullable Shelly2DevConfigSwitch switch1;
            @SerializedName("switch:2")
            public @Nullable Shelly2DevConfigSwitch switch2;
            @SerializedName("switch:3")
            public @Nullable Shelly2DevConfigSwitch switch3;
            @SerializedName("switch:100")
            public @Nullable Shelly2DevConfigSwitch switch100; // Pro 3EM Add-On

            @SerializedName("cb:0")
            public @Nullable ShellyDeviceConfigCB cb0;
            @SerializedName("voltmeter:0")
            public @Nullable Shelly2DevConfigVoltmeter voltmeter0;
            @SerializedName("voltmeter:1")
            public @Nullable Shelly2DevConfigVoltmeter voltmeter1;
            @SerializedName("voltmeter:2")
            public @Nullable Shelly2DevConfigVoltmeter voltmeter2;

            @SerializedName("em:0")
            public @Nullable Shelly2DevConfigEm em0; // 3-phase config or Gen3 per-phase channel 0
            @SerializedName("em:1")
            public @Nullable Shelly2DevConfigEm emCh1; // Gen3: per-phase channel 1
            @SerializedName("em:2")
            public @Nullable Shelly2DevConfigEm emCh2; // Gen3: per-phase channel 2
            @SerializedName("em1:0")
            public @Nullable Shelly2DevConfigEm em10; // single-phase clamp 0 config
            @SerializedName("em1:1")
            public @Nullable Shelly2DevConfigEm em11; // single-phase clamp 1 config
            @SerializedName("em1:2")
            public @Nullable Shelly2DevConfigEm em12; // Pro 3EM monophase profile: 3rd clamp
            @SerializedName("pm1:0")
            public @Nullable Shelly2DevConfigPm1 pm10;

            @SerializedName("cover:0")
            public @Nullable Shelly2DevConfigCover cover0;

            @SerializedName("light:0")
            public @Nullable Shelly2GetConfigLight light0;
            @SerializedName("light:1")
            public @Nullable Shelly2GetConfigLight light1;
            @SerializedName("light:2")
            public @Nullable Shelly2GetConfigLight light2;
            @SerializedName("light:3")
            public @Nullable Shelly2GetConfigLight light3;
            @SerializedName("light:4")
            public @Nullable Shelly2GetConfigLight light4;
            @SerializedName("rgb:0")
            public @Nullable Shelly2GetConfigLight rgb0;
            @SerializedName("rgbw:0")
            public @Nullable Shelly2GetConfigLight rgbw0;
            @SerializedName("cct:0")
            public @Nullable Shelly2GetConfigLight cct0;
            @SerializedName("cct:1")
            public @Nullable Shelly2GetConfigLight cct1;
            @SerializedName("rgbcct:0")
            public @Nullable Shelly2GetConfigLight rgbcct0;

            @SerializedName("smoke:0")
            public @Nullable Shelly2ConfigSmoke smoke0;

            @SerializedName("flood:0")
            public @Nullable Shelly2ConfigFlood flood0;

            @SerializedName("lora:100")
            public @Nullable Shelly2DeviceConfigLora lora100;

            public @Nullable Shelly2DevConfigPresence presence;
        }

        public class Shelly2DeviceConfigSta {
            public @Nullable String ssid;
            public @Nullable String password;
            @SerializedName("is_open")
            public @Nullable Boolean isOpen;
            public @Nullable Boolean enable;
            public @Nullable String ipv4mode;
            public @Nullable String ip;
            public @Nullable String netmask;
            public @Nullable String gw;
            public @Nullable String nameserver;
        }

        public class Shelly2DeviceConfigRoam {
            @SerializedName("rssi_thr")
            public @Nullable Integer rssiThr;
            public @Nullable Integer interval;
        }

        public class Shelly2DeviceConfigWiFi {
            public @Nullable Shelly2DeviceConfigAp ap;
            public @Nullable Shelly2DeviceConfigSta sta;
            public @Nullable Shelly2DeviceConfigSta sta1;
            public @Nullable Shelly2DeviceConfigRoam roam;
        }

        public @Nullable String id;
        public @Nullable String src;
        public @Nullable Shelly2GetConfigResult result;
    }

    public static class Shelly2APClientList {
        public static class Shelly2APClient {
            public @Nullable String mac;
            public @Nullable String ip;
            @SerializedName("ip_static")
            public @Nullable Boolean staticIP;
            public @Nullable Integer mport;
            public @Nullable Long since;
        }

        public @Nullable Long ts;
        @SerializedName("ap_clients")
        public @Nullable ArrayList<Shelly2APClient> apClients;
    }

    public static class Shelly2ConfigParms {
        public @Nullable String name;
        public @Nullable Boolean enable;
        public @Nullable String server;
        @SerializedName("ssl_ca")
        public @Nullable String sslCA;

        // WiFi.SetConfig
        public @Nullable Shelly2DeviceConfigAp ap;

        // Switch.SetConfig
        @SerializedName("auto_on")
        public @Nullable Boolean autoOn;
        @SerializedName("auto_on_delay")
        public @Nullable Double autoOnDelay;
        @SerializedName("auto_off")
        public @Nullable Boolean autoOff;
        @SerializedName("auto_off_delay")
        public @Nullable Double autoOffDelay;

        // WD_UI.SetConfig
        @SerializedName("sys_led_enable")
        public @Nullable Boolean sysLedEnable;
        @SerializedName("power_led")
        public @Nullable String powerLed;

        // BLE
        public @Nullable Shelly2DevConfigBleRpc rpc;
        public @Nullable Shelly2DevConfigBleObserver observer;

        // Flood.SetConfig
        @SerializedName("alarm_mode")
        public @Nullable String alarmMode;
        @SerializedName("report_holdoff")
        public @Nullable Integer reportHoldoff;

        // Thermostat.SetConfig, Thermostat.Schedule.SetConfig
        @SerializedName("target_C")
        public @Nullable Double targetC;
        @SerializedName("profile_id")
        public @Nullable Integer profileId;
    }
}
