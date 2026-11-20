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
            public Boolean enable;
        }

        public static class Shelly2DevConfigBleObserver {
            public Boolean enable;
        }

        public Boolean enable;
        public Shelly2DevConfigBleRpc rpc;
        public Shelly2DevConfigBleObserver observer;
    }

    public static class Shelly2DevConfigEth {
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
        public String profile;
        public Integer gen;
        @SerializedName("fw_id")
        public String fw;
        public String ver;
        public String app;
        @SerializedName("auth_en")
        public Boolean auth;
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
        public Shelly2DeviceConfigApRE rangeExtender;
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
                public String cover; // hold comma separated list of roller favorites
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
            public Integer id;
            public String name;
            public String type;
            public Boolean invert;
            @SerializedName("factory_reset")
            public Boolean factoryReset;
            @SerializedName("report_thr")
            public Double reportThreshold; // only for type analog
        }

        public class Shelly2DevConfigSwitch {
            public Integer id;
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

        public static class ShellyDeviceConfigCB {
            public Integer id;
            public String name;
            @SerializedName("undervoltage_limit")
            public Integer undervoltageLimit;
            @SerializedName("voltage_limit")
            public Integer voltageLimit;
            @SerializedName("reaction_delay")
            public Double reactionDelay;
        }

        public static class Shelly2ConfigSmoke {
            public Integer id;
            public Boolean alarm;
            public Boolean mute;
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
            public Boolean sysLedEnable;
            @SerializedName("power_led")
            public String powerLed;
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
            @SerializedName("wd_ui")
            public Shelly2DeviceConfigLed led;

            @SerializedName("input:0")
            public Shelly2DevConfigInput input0;
            @SerializedName("input:1")
            public Shelly2DevConfigInput input1;
            @SerializedName("input:2")
            public Shelly2DevConfigInput input2;
            @SerializedName("input:3")
            public Shelly2DevConfigInput input3;
            @SerializedName("input:4")
            public Shelly2DevConfigInput input4;

            @SerializedName("switch:0")
            public Shelly2DevConfigSwitch switch0;
            @SerializedName("switch:1")
            public Shelly2DevConfigSwitch switch1;
            @SerializedName("switch:2")
            public Shelly2DevConfigSwitch switch2;
            @SerializedName("switch:3")
            public Shelly2DevConfigSwitch switch3;
            @SerializedName("switch:100")
            public Shelly2DevConfigSwitch switch100; // Pro 3EM Add-On

            @SerializedName("cb:0")
            public ShellyDeviceConfigCB cb0;
            @SerializedName("voltmeter:0")
            public Shelly2DevConfigVoltmeter voltmeter0;
            @SerializedName("voltmeter:1")
            public Shelly2DevConfigVoltmeter voltmeter1;
            @SerializedName("voltmeter:2")
            public Shelly2DevConfigVoltmeter voltmeter2;

            @SerializedName("em:0")
            public Shelly2DevConfigEm em0; // 3-phase config or Gen3 per-phase channel 0
            @SerializedName("em:1")
            public @Nullable Shelly2DevConfigEm emCh1; // Gen3: per-phase channel 1
            @SerializedName("em:2")
            public @Nullable Shelly2DevConfigEm emCh2; // Gen3: per-phase channel 2
            @SerializedName("em1:0")
            public Shelly2DevConfigEm em10; // single-phase clamp 0 config
            @SerializedName("em1:1")
            public Shelly2DevConfigEm em11; // single-phase clamp 1 config
            @SerializedName("em1:2")
            public @Nullable Shelly2DevConfigEm em12; // Pro 3EM monophase profile: 3rd clamp
            @SerializedName("pm1:0")
            public Shelly2DevConfigPm1 pm10;

            @SerializedName("cover:0")
            public Shelly2DevConfigCover cover0;

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
            public Shelly2ConfigSmoke smoke0;

            @SerializedName("flood:0")
            public @Nullable Shelly2ConfigFlood flood0;

            @SerializedName("lora:100")
            public Shelly2DeviceConfigLora lora100;

            public @Nullable Shelly2DevConfigPresence presence;
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

    public static class Shelly2APClientList {
        public static class Shelly2APClient {
            public String mac;
            public String ip;
            @SerializedName("ip_static")
            public Boolean staticIP;
            public Integer mport;
            public Long since;
        }

        public Long ts;
        @SerializedName("ap_clients")
        public ArrayList<Shelly2APClient> apClients;
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

        // WD_UI.SetConfig
        @SerializedName("sys_led_enable")
        public Boolean sysLedEnable;
        @SerializedName("power_led")
        public String powerLed;

        // BLE
        public Shelly2DevConfigBleRpc rpc;
        public Shelly2DevConfigBleObserver observer;

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
