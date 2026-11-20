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
import org.openhab.binding.shelly.internal.api2.dto.ShellyCoverJsonDTO.Shelly2CoverStatus;
import org.openhab.binding.shelly.internal.api2.dto.ShellyLightJsonDTO.Shelly2DaliStatus;
import org.openhab.binding.shelly.internal.api2.dto.ShellyLightJsonDTO.Shelly2DeviceStatusLight;
import org.openhab.binding.shelly.internal.api2.dto.ShellyLightJsonDTO.Shelly2RGBCCTStatus;
import org.openhab.binding.shelly.internal.api2.dto.ShellyLightJsonDTO.Shelly2RGBWStatus;
import org.openhab.binding.shelly.internal.api2.dto.ShellyLoraJsonDTO.Shelly2DeviceStatusLora;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMediaJsonDTO.Shelly2DeviceStatusMedia;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMeterJsonDTO.Shelly2DeviceStatusEm;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMeterJsonDTO.Shelly2DeviceStatusEmData;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMeterJsonDTO.Shelly2DeviceStatusVoltage;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMeterJsonDTO.Shelly2Energy;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMeterJsonDTO.Shelly2StatusEm1;
import org.openhab.binding.shelly.internal.api2.dto.ShellyThermostatJsonDTO.Shelly2DeviceStatusThermostat;

import com.google.gson.annotations.SerializedName;

/**
 * {@link Shelly2StatusJsonDTO} contains the JSON structures of the Shelly.GetStatus result.
 *
 * @author Markus Michels - Initial contribution
 */
public class Shelly2StatusJsonDTO {

    public static class Shelly2DeviceStatus {
        public static class Shelly2DeviceStatusSysAvlUpdate {
            public static class Shelly2DeviceStatusSysUpdate {
                public @Nullable String version;
            }

            public @Nullable Shelly2DeviceStatusSysUpdate stable;
            public @Nullable Shelly2DeviceStatusSysUpdate beta;
        }

        public class Shelly2InputCounts {
            public Integer total;
            @SerializedName("by_minute")
            public Double[] byMinute;
            public Double xtotal;
            @SerializedName("xby_minute")
            public Double[] xbyMinute;
            @SerializedName("minute_ts")
            public Integer minuteTS;
        }

        public class Shelly2InputStatus {
            public Integer id;
            public Boolean state;
            public Double percent; // analog input only
            public @Nullable ArrayList<String> errors;
            public Double xpercent;
            public Shelly2InputCounts counts;
            public Double freq;
            public Double xfreq;
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

            public class Shelly2DeviceStatusHumidity {
                public @Nullable Integer id;
                public @Nullable Double rh;
                public @Nullable ArrayList<String> errors;
            }

            public class Shelly2DeviceStatusIlluminance {
                public Integer id;
                public Double lux;
                public String illumination;
            }

            public class Shelly2DeviceStatusTempId extends Shelly2DeviceStatusTemp {
                public @Nullable Integer id;
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

            public class Shelly2DeviceStatusSmoke {
                public Integer id;
                public Boolean alarm;
                public Boolean mute;
            }

            public class Shelly2DeviceStatusFlood {
                public @Nullable Integer id;
                public @Nullable Boolean alarm;
                public @Nullable Boolean mute;
                public @Nullable String[] errors;
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
            @SerializedName("input:4")
            public Shelly2InputStatus input4;
            @SerializedName("input:100")
            public Shelly2InputStatus input100; // Digital Input from Add-On

            @SerializedName("rgbw:0")
            public @Nullable Shelly2RGBWStatus rgbw0;

            @SerializedName("switch:0")
            public Shelly2RelayStatus switch0;
            @SerializedName("switch:1")
            public Shelly2RelayStatus switch1;
            @SerializedName("switch:2")
            public Shelly2RelayStatus switch2;
            @SerializedName("switch:3")
            public Shelly2RelayStatus switch3;
            @SerializedName("switch:100")
            public Shelly2RelayStatus switch100; // Pro 3EM Add-On

            @SerializedName("cb:0")
            public Shelly2CBStatus cb0;

            @SerializedName("pm1:0")
            public Shelly2RelayStatus pm10;

            @SerializedName("em:0")
            public Shelly2DeviceStatusEm em0; // 3-phase status (phases A/B/C) or Gen3 per-phase channel 0 (phase A)
            @SerializedName("em:1")
            public @Nullable Shelly2DeviceStatusEm emCh1; // Gen3: per-phase channel 1 (phase B)
            @SerializedName("em:2")
            public @Nullable Shelly2DeviceStatusEm emCh2; // Gen3: per-phase channel 2 (phase C)
            @SerializedName("emdata:0")
            public Shelly2DeviceStatusEmData emdata0;
            @SerializedName("em1:0")
            public Shelly2StatusEm1 em10; // single-phase clamp 0 (EM Mini, Pro EM-50, Pro 3EM monophase)
            @SerializedName("em1:1")
            public Shelly2StatusEm1 em11; // single-phase clamp 1
            @SerializedName("em1:2")
            public @Nullable Shelly2StatusEm1 em12; // Pro 3EM monophase profile: 3rd clamp
            @SerializedName("em1data:0")
            public Shelly2DeviceStatusEmData em1data0; // accumulated energy of clamp 0
            @SerializedName("em1data:1")
            public @Nullable Shelly2DeviceStatusEmData em1data1; // accumulated energy of clamp 1
            @SerializedName("em1data:2")
            public @Nullable Shelly2DeviceStatusEmData em1data2; // Pro 3EM monophase profile: 3rd clamp

            @SerializedName("cover:0")
            public Shelly2CoverStatus cover0;

            @SerializedName("light:0")
            public @Nullable Shelly2DeviceStatusLight light0;
            @SerializedName("light:1")
            public @Nullable Shelly2DeviceStatusLight light1;
            @SerializedName("light:2")
            public @Nullable Shelly2DeviceStatusLight light2;
            @SerializedName("light:3")
            public @Nullable Shelly2DeviceStatusLight light3;
            @SerializedName("light:4")
            public @Nullable Shelly2DeviceStatusLight light4;
            @SerializedName("rgb:0")
            public @Nullable Shelly2RGBWStatus rgb0;
            @SerializedName("cct:0")
            public @Nullable Shelly2DeviceStatusLight cct0;
            @SerializedName("cct:1")
            public @Nullable Shelly2DeviceStatusLight cct1;

            @SerializedName("rgbcct:0")
            public @Nullable Shelly2RGBCCTStatus rgbcct0;

            @SerializedName("temperature:0")
            public @Nullable Shelly2DeviceStatusTempId temperature0;
            @SerializedName("temperature:100")
            public @Nullable Shelly2DeviceStatusTempId temperature100;
            @SerializedName("temperature:101")
            public @Nullable Shelly2DeviceStatusTempId temperature101;
            @SerializedName("temperature:102")
            public @Nullable Shelly2DeviceStatusTempId temperature102;
            @SerializedName("temperature:103")
            public @Nullable Shelly2DeviceStatusTempId temperature103;
            @SerializedName("temperature:104")
            public @Nullable Shelly2DeviceStatusTempId temperature104;

            @SerializedName("humidity:0")
            public @Nullable Shelly2DeviceStatusHumidity humidity0;
            @SerializedName("humidity:100")
            public @Nullable Shelly2DeviceStatusHumidity humidity100;

            @SerializedName("illuminance:0")
            public Shelly2DeviceStatusIlluminance illuminance0;

            @SerializedName("smoke:0")
            public Shelly2DeviceStatusSmoke smoke0;

            @SerializedName("flood:0")
            public @Nullable Shelly2DeviceStatusFlood flood0;

            @SerializedName("voltmeter:0")
            public @Nullable Shelly2DeviceStatusVoltage voltmeter0;
            @SerializedName("voltmeter:1")
            public @Nullable Shelly2DeviceStatusVoltage voltmeter1;
            @SerializedName("voltmeter:2")
            public @Nullable Shelly2DeviceStatusVoltage voltmeter2;
            @SerializedName("voltmeter:100")
            public @Nullable Shelly2DeviceStatusVoltage voltmeter100;

            @SerializedName("devicepower:0")
            public Shelly2DeviceStatusPower devicepower0;
            @SerializedName("devicepower:1")
            public @Nullable Shelly2DeviceStatusPower devicepower1;

            @SerializedName("lora:100")
            public Shelly2DeviceStatusLora lora100;

            @SerializedName("dali")
            public @Nullable Shelly2DaliStatus dali;

            // Media is a singleton (Media.GetStatus takes no id), some firmware reports it indexed
            @SerializedName(value = "media", alternate = { "media:0" })
            public @Nullable Shelly2DeviceStatusMedia media;

            @SerializedName("thermostat:0")
            public @Nullable Shelly2DeviceStatusThermostat thermostat0;
        }

        public class Shelly2DeviceStatusSys {
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
            public Integer cfgRev;
            @SerializedName("available_updates")
            public @Nullable Shelly2DeviceStatusSysAvlUpdate availableUpdates;
            @SerializedName("webhook_rev")
            public Integer webHookRev;
            @SerializedName("wakeup_reason")
            public Shelly2DeviceStatusWakeup wakeUpReason;
            @SerializedName("wakeup_period")
            public Integer wakeupPeriod;

            // Wall Display: reported inside sys, not as a top level component
            @SerializedName("relay_in_thermostat")
            public @Nullable Boolean relayInThermostat;
            @SerializedName("sensor_in_thermostat")
            public @Nullable Boolean sensorInThermostat;
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
        public Double timerDuration;
        public @Nullable Double apower;
        public @Nullable Double voltage;
        public @Nullable Double current;
        @SerializedName("freq")
        public @Nullable Double frequency;
        public @Nullable Double pf;
        public @Nullable Shelly2Energy aenergy;
        public @Nullable Shelly2DeviceStatusTemp temperature;
        public String @Nullable [] errors;
    }

    public static class Shelly2CBStatus {
        public Integer id;
        public Boolean output;
        public String source;
        @SerializedName("total_cycles")
        public Integer totalCycles;
        public Boolean safety;
        public Shelly2DeviceStatusTemp temperature;
    }

    public static class Shelly2DeviceStatusTemp {
        public @Nullable Double tC;
        public @Nullable Double tF;
        public @Nullable ArrayList<String> errors;
    }
}
