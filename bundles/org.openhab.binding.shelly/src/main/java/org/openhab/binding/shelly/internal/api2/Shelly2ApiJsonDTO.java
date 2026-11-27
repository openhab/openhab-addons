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
package org.openhab.binding.shelly.internal.api2;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage.Shelly2RpcMessageError;
import org.openhab.binding.shelly.internal.api2.dto.Shelly2ConfigJsonDTO.Shelly2ConfigParms;
import org.openhab.binding.shelly.internal.api2.dto.Shelly2StatusJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.dto.ShellyBluJsonDTO.Shelly2NotifyBluEventData;
import org.openhab.binding.shelly.internal.api2.dto.ShellyLoraJsonDTO.Shelly2NotifyEventLoraInfo;
import org.openhab.binding.shelly.internal.util.ShellyUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * {@link Shelly2ApiJsonDTO} wraps the Shelly REST API and provides various low level function to access the device api
 * (not
 * cloud api).
 *
 * @author Markus Michels - Initial contribution
 */
public class Shelly2ApiJsonDTO {
    public static final String SHELLYRPC_ENDPOINT = "/rpc";
    public static final String SHELLY2_JSONRPC_VERSION = "2.0";

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
    public static final String SHELLYRPC_METHOD_SWITCH_RESETCOUNTERS = "Switch.ResetCounters";
    public static final String SHELLYRPC_METHOD_PM1_RESETCOUNTERS = "PM1.ResetCounters";
    public static final String SHELLYRPC_METHOD_COVER_RESETCOUNTERS = "Cover.ResetCounters";
    public static final String SHELLYRPC_METHOD_LIGHT_RESETCOUNTERS = "Light.ResetCounters";
    public static final String SHELLYRPC_METHOD_CB_SET = "CB.Set";
    public static final String SHELLYRPC_METHOD_COVER_SETPOS = "Cover.GoToPosition";
    public static final String SHELLY2_COVER_CMD_OPEN = "Open";
    public static final String SHELLY2_COVER_CMD_CLOSE = "Close";
    public static final String SHELLY2_COVER_CMD_STOP = "Stop";
    public static final String SHELLYRPC_METHOD_LIGHT_STATUS = "Light.GetStatus";
    public static final String SHELLYRPC_METHOD_LIGHT_SET = "Light.Set";
    public static final String SHELLYRPC_METHOD_LIGHT_SETCONFIG = "Light.SetConfig";
    public static final String SHELLYRPC_METHOD_RGB_STATUS = "RGB.GetStatus";
    public static final String SHELLYRPC_METHOD_RGB_SET = "RGB.Set";
    public static final String SHELLYRPC_METHOD_RGB_SETCONFIG = "RGB.SetConfig";
    public static final String SHELLYRPC_METHOD_RGBW_STATUS = "RGBW.GetStatus";
    public static final String SHELLYRPC_METHOD_RGBW_SET = "RGBW.Set";
    public static final String SHELLYRPC_METHOD_RGBW_SETCONFIG = "RGBW.SetConfig";
    public static final String SHELLYRPC_METHOD_CCT_STATUS = "CCT.GetStatus";
    public static final String SHELLYRPC_METHOD_CCT_SET = "CCT.Set";
    public static final String SHELLYRPC_METHOD_CCT_SETCONFIG = "CCT.SetConfig";
    public static final String SHELLYRPC_METHOD_RGBCCT_STATUS = "RGBCCT.GetStatus";
    public static final String SHELLYRPC_METHOD_RGBCCT_SET = "RGBCCT.Set";
    public static final String SHELLYRPC_METHOD_RGBCCT_SETCONFIG = "RGBCCT.SetConfig";
    public static final String SHELLYRPC_METHOD_LED_SETCONFIG = "WD_UI.SetConfig";
    public static final String SHELLYRPC_METHOD_LORA_SENDDATA = "LoRa.SendBytes";
    public static final String SHELLYRPC_METHOD_WIFIGETCONG = "Wifi.GetConfig";
    public static final String SHELLYRPC_METHOD_WIFISETCONG = "Wifi.SetConfig";
    public static final String SHELLYRPC_METHOD_WIFILISTAPCLIENTS = "WiFi.ListAPClients";
    public static final String SHELLYRPC_METHOD_ETHGETCONG = "Eth.GetConfig";
    public static final String SHELLYRPC_METHOD_ETHSETCONG = "Eth.SetConfig";
    public static final String SHELLYRPC_METHOD_BLEGETCONG = "BLE.GetConfig";
    public static final String SHELLYRPC_METHOD_BLESETCONG = "BLE.SetConfig";
    public static final String SHELLYRPC_METHOD_CLOUDSET = "Cloud.SetConfig";
    public static final String SHELLYRPC_METHOD_WSGETCONFIG = "WS.GetConfig";
    public static final String SHELLYRPC_METHOD_WSSETCONFIG = "WS.SetConfig";
    public static final String SHELLYRPC_METHOD_EMDATARESET = "EMData.ResetCounters";
    public static final String SHELLYRPC_METHOD_EM1DATARESET = "EM1Data.ResetCounters";
    public static final String SHELLYRPC_METHOD_SMOKE_SETCONFIG = "Smoke.SetConfig";
    public static final String SHELLYRPC_METHOD_SMOKE_MUTE = "Smoke.Mute";
    public static final String SHELLYRPC_METHOD_FLOOD_SETCONFIG = "Flood.SetConfig";
    public static final String SHELLYRPC_METHOD_SCRIPT_LIST = "Script.List";
    public static final String SHELLYRPC_METHOD_SCRIPT_SETCONFIG = "Script.SetConfig";
    public static final String SHELLYRPC_METHOD_SCRIPT_GETSTATUS = "Script.GetStatus";
    public static final String SHELLYRPC_METHOD_SCRIPT_DELETE = "Script.Delete";
    public static final String SHELLYRPC_METHOD_SCRIPT_CREATE = "Script.Create";
    public static final String SHELLYRPC_METHOD_SCRIPT_GETCODE = "Script.GetCode";
    public static final String SHELLYRPC_METHOD_SCRIPT_PUTCODE = "Script.PutCode";
    public static final String SHELLYRPC_METHOD_SCRIPT_START = "Script.Start";
    public static final String SHELLYRPC_METHOD_SCRIPT_STOP = "Script.Stop";
    public static final String SHELLYRPC_METHOD_MEDIA_PLAYORPAUSE = "Media.MediaPlayer.PlayOrPause";
    public static final String SHELLYRPC_METHOD_MEDIA_NEXT = "Media.MediaPlayer.Next";
    public static final String SHELLYRPC_METHOD_MEDIA_PREVIOUS = "Media.MediaPlayer.Previous";
    public static final String SHELLYRPC_METHOD_MEDIA_PLAY = "Media.MediaPlayer.Play";
    public static final String SHELLYRPC_METHOD_MEDIA_SETVOLUME = "Media.SetVolume";
    public static final String SHELLYRPC_METHOD_MEDIA_RADIO_PLAYFAVOURITE = "Media.Radio.PlayFavourite";
    public static final String SHELLYRPC_METHOD_THERMOSTAT_SETCONFIG = "Thermostat.SetConfig";
    public static final String SHELLYRPC_METHOD_THERMOSTAT_SCHEDULE_SETCONFIG = "Thermostat.Schedule.SetConfig";
    public static final String SHELLYRPC_METHOD_THERMOSTAT_SCHEDULE_LISTPROFILES = "Thermostat.Schedule.ListProfiles";

    public static final String SHELLYRPC_METHOD_NOTIFYSTATUS = "NotifyStatus"; // inbound status
    public static final String SHELLYRPC_METHOD_NOTIFYFULLSTATUS = "NotifyFullStatus"; // inbound status from bat device
    public static final String SHELLYRPC_METHOD_NOTIFYEVENT = "NotifyEvent"; // inbound event

    // Component types
    public static final String SHELLY2_PROFILE_RELAY = "switch";
    public static final String SHELLY2_PROFILE_COVER = "cover";
    public static final String SHELLY2_PROFILE_LIGHT = "light";
    public static final String SHELLY2_PROFILE_RGB = "rgb";
    public static final String SHELLY2_PROFILE_RGBW = "rgbw";
    public static final String SHELLY2_PROFILE_MONOPHASE = "monophase";
    public static final String SHELLY2_PROFILE_TRIPHASE = "triphase";
    public static final String SHELLY2_PROFILE_RGBCCT = "rgbcct"; // Pro RGBWW PM: RGB:0 + CCT:0
    public static final String SHELLY2_PROFILE_CCTX2 = "cctx2"; // Pro RGBWW PM: CCT:0 + CCT:1
    public static final String SHELLY2_PROFILE_RGBX2LIGHT = "rgbx2light"; // Pro RGBWW PM: RGB:0 + Light:0/1

    // RGBCCT component runtime submode (RGBCCT.Set/GetStatus/NotifyStatus "mode" field) - distinct from the
    // SHELLY2_PROFILE_* device profile selection above, even though the RGB value happens to coincide
    public static final String SHELLY_RGBCCT_MODE_RGB = "rgb";
    public static final String SHELLY_RGBCCT_MODE_CCT = "cct";

    // Button types/modes
    public static final String SHELLY2_BTNT_MOMENTARY = "momentary";
    public static final String SHELLY2_BTNT_FLIP = "flip";
    public static final String SHELLY2_BTNT_FOLLOW = "follow";
    public static final String SHELLY2_BTNT_DETACHED = "detached";
    public static final String SHELLY2_BTNT_ACTIVATE = "activate";
    public static final String SHELLY2_BTNT_CYCLE = "cycle"; // Switch component in_mode: button cycles the output
    public static final String SHELLY2_BTNT_DIM = "dim"; // Light component in_mode: single button toggles+dims
    public static final String SHELLY2_BTNT_DUAL_DIM = "dual_dim"; // Light component in_mode: two buttons toggle+dim

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
    public static final String SHELLY2_EVENT_HOLDING = "holding";

    public static final String SHELLY2_EVENT_SLEEP = "sleep";
    public static final String SHELLY2_EVENT_CFGCHANGED = "config_changed";
    public static final String SHELLY2_EVENT_OTASTART = "ota_begin";
    public static final String SHELLY2_EVENT_OTAPROGRESS = "ota_progress";
    public static final String SHELLY2_EVENT_OTADONE = "ota_success";
    public static final String SHELLY2_EVENT_RESTART = "scheduled_restart";
    public static final String SHELLY2_EVENT_WIFICONNFAILED = "sta_connect_fail";
    public static final String SHELLY2_EVENT_WIFIDISCONNECTED = "sta_disconnected";
    public static final String SHELLY2_EVENT_FLOOD_ALARM = "flood.alarm";
    public static final String SHELLY2_EVENT_FLOOD_ALARM_OFF = "flood.alarm_off";
    public static final String SHELLY2_EVENT_FLOOD_CABLE_UNPLUGGED = "flood.cable_unplugged";

    public static final String SHELLY2_EVENT_BLE_SCAN_RESULT = "ble.scan_result";

    // LoRa
    public static final String SHELLY2_EVENT_LORADATA = "lora";
    public static final String SHELLY2_EVENT_LORA_USERRX = "user_rx"; // SheLR datagram (LoRa.Send)

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

    // Dimmer US: LED power modes
    public static final String SHELLY2_POWERLED_ON = "on";
    public static final String SHELLY2_POWERLED_OFF = "off";
    public static final String SHELLY2_POWERLED_MATCH = "match_output";
    public static final String SHELLY2_POWERLED_INVERT = "inverted_output";

    public static final String SHELLY2_DEFAULT_USERID = "admin"; // Gen2 devices only accept user admin
    public static final String SHELLY2_DEFAULT_PASSWORD = "admin";

    public static class Shelly2RpcRequest {
        public Integer id = 0;
        public @Nullable String method;

        public static class Shelly2RpcRequestParams {
            public Integer id = 1;

            // Cover
            public @Nullable Integer pos;
            public @Nullable Boolean on;
            public @Nullable Boolean output;

            // Dimmer / Light
            public @Nullable Integer brightness;
            public @Nullable Integer ct; // color temperature in Kelvin (Light.Set / CCT.Set / RGBCCT.Set)
            @SerializedName("toggle_after")
            public @Nullable Integer toggleAfter;
            public @Nullable Integer white;
            public Integer @Nullable [] rgb;
            public @Nullable String mode; // for RGBCCT mode switching: "rgb" or "cct"

            // Presence.SetSensor / generic enable
            public @Nullable Boolean enable;

            // Shelly.SetAuth
            public @Nullable String user;
            public @Nullable String realm;
            public @Nullable String ha1;

            // Shelly.Update
            public @Nullable String stage;
            public @Nullable String url;

            // Cloud.SetConfig
            public @Nullable Shelly2ConfigParms config;

            // Script
            public @Nullable String name;

            // LoRa.SendBytes
            public @Nullable String data;

            // Media
            public @Nullable Integer volume;
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

        public Shelly2RpcRequest withVolume(int volume) {
            params.volume = volume;
            return this;
        }

        public Shelly2RpcRequest withName(String name) {
            params.name = name;
            return this;
        }

        public Shelly2RpcRequest withData(String data) {
            params.data = data;
            return this;
        }
    }

    public static class Shelly2WsConfigResponse {
        public @Nullable Integer id;
        public @Nullable String src;

        public static class Shelly2WsConfigResult {
            @SerializedName("restart_required")
            public @Nullable Boolean restartRequired;
        }

        public @Nullable Shelly2WsConfigResult result;
    }

    public static class ShellyScriptListResponse {
        public static class ShellyScriptListEntry {
            public @Nullable Integer id;
            public @Nullable String name;
            public @Nullable Boolean enable;
            public @Nullable Boolean running;
        }

        public @Nullable ArrayList<ShellyScriptListEntry> scripts;
    }

    public static class ShellyScriptResponse {
        public @Nullable Integer id;
        public @Nullable Boolean running;
        public @Nullable Integer len;
        public @Nullable String data;
    }

    public static class ShellyScriptPutCodeParams {
        public @Nullable Integer id;
        public @Nullable String code;
        public @Nullable Boolean append;
    }

    public static class Shelly2RpcBaseMessage {
        // Basic message format, e.g.
        // {"id":1,"src":"localweb528","method":"Shelly.GetConfig"}
        public class Shelly2RpcMessageError {
            public @Nullable Integer code;
            public @Nullable String message;
        }

        public @Nullable String jsonrpc;
        public @Nullable Integer id;
        public @Nullable String src;
        public @Nullable String dst;
        public @Nullable String component;
        public @Nullable String method;
        public @Nullable Object params;
        public @Nullable String event;
        public @Nullable Object result;
        public @Nullable Shelly2AuthRsp auth;
        public @Nullable Shelly2RpcMessageError error;
    }

    public static class Shelly2RpcNotifyStatus {
        public static class Shelly2NotifyStatus extends Shelly2DeviceStatusResult {
            public @Nullable Double ts;
        }

        public @Nullable Integer id;
        public @Nullable String src;
        public @Nullable String dst;
        public @Nullable String method;
        public @Nullable Shelly2NotifyStatus params;
        public @Nullable Shelly2NotifyStatus result;
        public @Nullable Shelly2RpcMessageError error;
    }

    public static final String SHELLY2_AUTHDEF_USER = "admin";
    public static final String SHELLY2_AUTHTTYPE_DIGEST = "digest";
    public static final String SHELLY2_AUTHTTYPE_STRING = "string";
    public static final String SHELLY2_AUTHALG_SHA256 = "SHA-256";
    // = ':auth:'+HexHash("dummy_method:dummy_uri");
    public static final String SHELLY2_AUTH_NOISE = "6370ec69915103833b5222b368555393393f098bfbfbb59f47e0590af135f062";

    public static class Shelly2AuthChallenge { // on 401 message contains the auth info
        @SerializedName("auth_type")
        public @Nullable String authType;
        public @Nullable String nonce;
        public @Nullable String realm;
        public @Nullable String algorithm;
    }

    public static class Shelly2AuthRsp {
        public @Nullable String username;
        public @Nullable String nonce;
        public @Nullable String cnonce;
        public @Nullable String nc;
        public @Nullable String realm;
        public @Nullable String algorithm;
        public @Nullable String response;
        @SerializedName("auth_type")
        public @Nullable String authType;
    }

    public class Shelly2NotifyEvent {
        public @Nullable Integer id;
        public @Nullable Double ts;
        public @Nullable String component;
        public @Nullable String event;
        // a third-party script may send an array here
        @SerializedName("data")
        public @Nullable JsonElement data;
        public @Nullable String msg;
        public @Nullable Integer reason;
        @SerializedName("cfg_rev")
        public @Nullable Integer cfgRev;
        public @Nullable Boolean value;
        @SerializedName("num_objects")
        public @Nullable Integer numObjects;

        /** The BLU payload, or null when {@code data} is absent or not an object. */
        public @Nullable Shelly2NotifyBluEventData getBluData(Gson gson) throws ShellyApiException {
            JsonElement data = this.data;
            if (data == null || !data.isJsonObject()) {
                return null;
            }
            return ShellyUtils.fromJson(gson, data.toString(), Shelly2NotifyBluEventData.class);
        }

        // LoRa: the "lora" event nests its payload under "info" rather than the generic "data" member
        public @Nullable Shelly2NotifyEventLoraInfo info;
    }

    public class Shelly2NotifyEventData {
        public @Nullable Double ts;
        public @Nullable ArrayList<Shelly2NotifyEvent> events;
    }

    public static class Shelly2RpcNotifyEvent {
        public @Nullable String src;
        public @Nullable Double ts;
        public @Nullable Shelly2NotifyEventData params;
    }
}
