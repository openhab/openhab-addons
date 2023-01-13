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
package org.openhab.binding.freeboxos.internal.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * This class holds various definitions and settings provided by the FreeboxOs API documentation
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiConstants {
    public static final String DEFAULT_FREEBOX_NAME = "mafreebox.freebox.fr";
    public static final String AUTH_HEADER = "X-Fbx-App-Auth";
    public static final String SYSTEM_SUB_PATH = "system";
    public static final String REBOOT_SUB_PATH = "reboot";
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String CONTENT_TYPE = "application/json; charset=" + DEFAULT_CHARSET.name();

    // FreeboxOS API paths
    public static final String API_VERSION_PATH = "api_version";
    public static final String LOGIN_PATH = "login";
    public static final String AUTHORIZE_PATH = "authorize";
    public static final String LOGOUT_PATH = "logout";
    public static final String SESSION_PATH = "session";
    public static final String AIR_MEDIA_PATH = "airmedia";
    public static final String FTP_PATH = "ftp";
    public static final String LCD_SUB_PATH = "lcd";
    public static final String PHONE_SUB_PATH = "phone";
    public static final String UPNPAV_URL = "upnpav";
    public static final String WIFI_SUB_PATH = "wifi";
    public static final String CALL_PATH = "call";
    public static final String CONNECTION_PATH = "connection";
    public static final String HOME_PATH = "home";
    public static final String NETSHARE_PATH = "netshare";
    public static final String FREEPLUG_PATH = "freeplug";
    public static final String STATIONS_SUB_PATH = "stations";
    public static final String HOST_SUB_PATH = "host";
    public static final String VM_SUB_PATH = "vm";
    public static final String STATUS_SUB_PATH = "status";
    public static final String PLAYER_SUB_PATH = "player";

    // FreeboxOS API subpaths
    public static final String CONFIG_SUB_PATH = "config";
    public static final String RECEIVERS_SUB_PATH = "receivers";
    public static final String LAN_SUB_PATH = "lan";
    public static final String CALL_LOG_SUB_PATH = "log/";
    public static final String NODES_SUB_PATH = "nodes";
    public static final String ENDPOINTS_SUB_PATH = "endpoints";
    public static final String WOL_SUB_PATH = "wol";
    public static final String INTERFACES_SUB_PATH = "interfaces";
    public static final String BROWSER_SUB_PATH = "browser";
    public static final String SAMBA_SUB_PATH = "samba";
    public static final String AFP_SUB_PATH = "afp";
    public static final String DELETE_ALL = "delete_all";
    public static final String AP_SUB_PATH = "ap";
    public static final String REPEATER_SUB_PATH = "repeater";

    public static final ZonedDateTime EPOCH_ZERO = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC);

    public static enum DiskStatus {
        NOT_DETECTED,
        DISABLED,
        INITIALIZING,
        ERROR,
        ACTIVE,
        UNKNOWN;
    }

    public static enum PhoneNetworkStatus {
        WORKING,
        UNKNOWN;
    }

    public static enum SigAlpStatus {
        DISABLED,
        DIRECT_MEDIA,
        ANY_MEDIA,
        UNKNOWN;
    }

    public static enum ModelInfo {
        FBXGW_R1_FULL, // Freebox Server (v6) revision 1
        FBXGW_R2_FULL, // Freebox Server (v6) revision 2
        FBXGW_R1_MINI, // Freebox Mini revision 1
        FBXGW_R2_MINI, // Freebox Mini revision 2
        FBXGW_R1_ONE, // Freebox One revision 1
        FBXGW_R2_ONE, // Freebox One revision 2
        FBXGW7_R1_FULL, // Freebox v7 revision 1
        FBX7HD_DELTA, // Freebox Player Devialet
        FBXWMR, // Répéteur Wifi
        TBX8AM, // Player Pop
        FBX6HD,
        FBX6LC,
        FBX6LCV2,
        FBX7HD,
        FBX7HD_ONE,
        FBX8AM,
        UNKNOWN;
    }

    public static enum RepeaterStatus {
        STARTING,
        RUNNING,
        REBOOTING,
        UPDATING,
        REBOOT_FAILURE,
        UPDATE_FAILURE,
        UNKNOWN;
    }

    public static enum RepeaterConnection {
        CONNECTED,
        DISCONNECTED,
        UNKNOWN;
    }

    public static enum StbType {
        STB_ANDROID,
        STB_V6,
        STB_V7,
        STB_V8,
        UNKNOWN;
    }

    public static enum ExpansionType {
        UNKNOWN, // unknown module
        DSL_LTE, // xDSL + LTE
        DSL_LTE_EXTERNAL_ANTENNAS, // xDSL + LTE with external antennas switch
        FTTH_P2P, // FTTH P2P
        FTTH_PON, // FTTH PON
        SECURITY; // Security module
    }

    public static enum PlugRole {
        STA, // Freeplug station
        PCO, // Freeplug proxy coordinator
        CCO, // Central Coordinator
        UNKNOWN;
    }

    public static enum HostType {
        WORKSTATION,
        LAPTOP,
        SMARTPHONE,
        TABLET,
        PRINTER,
        VG_CONSOLE,
        TELEVISION,
        NAS,
        IP_CAMERA,
        IP_PHONE,
        FREEBOX_PLAYER,
        FREEBOX_HD,
        FREEBOX_CRYSTAL,
        FREEBOX_MINI,
        FREEBOX_DELTA,
        FREEBOX_ONE,
        FREEBOX_WIFI,
        FREEBOX_POP,
        NETWORKING_DEVICE,
        MULTIMEDIA_DEVICE,
        CAR,
        OTHER,
        UNKNOWN;
    }

    public static enum HostNameSource {
        DHCP,
        NETBIOS,
        MDNS,
        MDNS_SRV,
        UPNP,
        WSD,
        UNKNOWN;
    }

    public static enum StationState {
        ASSOCIATED,
        AUTHENTICATED,
        UNKNOWN;
    }

    public static enum NetworkControlMode {
        ALLOWED,
        DENIED,
        WEBONLY,
        UNKNOWN;
    }

    public static enum WifiApState {
        SCANNING, // Ap is probing wifi channels
        NO_PARAM, // Ap is not configured
        BAD_PARAM, // Ap has an invalid configuration
        DISABLED, // Ap is permanently disabled
        DISABLED_PLANNING, // Ap is currently disabled according to planning
        NO_ACTIVE_BSS, // Ap has no active BSS
        STARTING, // Ap is starting
        ACS, // Ap is selecting the best available channel
        HT_SCAN, // Ap is scanning for other access point
        DFS, // Ap is performing dynamic frequency selection
        ACTIVE, // Ap is active
        FAILED, // Ap has failed to start
        UNKNOWN;
    }

    public static enum CDayRange {
        @SerializedName(":fr_bank_holidays")
        BANK_HOLIDAYS,
        @SerializedName(":fr_school_holidays_a")
        SCHOOL_HOLIDAYS_A,
        @SerializedName(":fr_school_holidays_b")
        SCHOOL_HOLIDAYS_B,
        @SerializedName(":fr_school_holidays_c")
        SCHOOL_HOLIDAYS_C,
        @SerializedName(":fr_school_holidays_corse")
        SCHOOL_HOLIDAYS_CORSE,
        UNKNOWN;
    }

    public static enum L2Type {
        MAC_ADDRESS,
        UNKNOWN;
    }

    public static enum ErrorCode {
        AUTH_REQUIRED,
        BAD_LOGIN,
        TOO_SHORT,
        IN_DICTIONNARY,
        BAD_XKCD,
        NOT_ENOUGH_DIFFERENT_CHARS,
        INVALID_TOKEN,
        PENDING_TOKEN,
        INSUFFICIENT_RIGHTS,
        DENIED_FROM_EXTERNAL_IP,
        INVALID_REQUEST,
        RATELIMITED,
        NEW_APPS_DENIED,
        APPS_AUTHORIZATION_DENIED,
        APPS_AUTHORIZATION_TIMEOUT,
        PASSWORD_RESET_DENIED,
        APPS_DENIED,
        INTERNAL_ERROR,
        SERVICE_DOWN,
        DISK_FULL,
        OP_FAILED,
        DISK_BUSY,
        ARRAY_START_FAILED,
        ARRAY_STOP_FAILED,
        ARRAY_NOT_FOUND,
        INVAL,
        NODEV,
        NOENT,
        NETDOWN,
        BUSY,
        INVALID_PORT,
        INSECURE_PASSWORD,
        INVALID_PROVIDER,
        INVALID_NEXT_HOP,
        INVALID_API_VERSION,
        INVAL_WPS_MACFILTER,
        INVAL_WPS_NEEDS_CCMP,
        INVALID_ID,
        PATH_NOT_FOUND,
        ACCESS_DENIED,
        DESTINATION_CONFLICT,
        CANCELLED,
        TASK_NOT_FOUND,
        HTTP,
        INVALID_URL,
        INVALID_OPERATION,
        INVALID_FILE,
        CTX_FILE_ERROR,
        HIBERNATING,
        TOO_MANY_TASKS,
        EXISTS,
        EXIST,
        CONNECTION_REFUSED,
        NO_FREEBOX,
        ALREADY_AUTHORIZED,
        ECRC,
        ERR_001,
        ERR_002,
        ERR_003,
        ERR_004,
        ERR_005,
        ERR_009,
        ERR_010,
        ERR_030,
        ERR_031,
        NONE,
        UNKNOWN;
    }

    public static enum TokenStatus {
        PENDING, // the user has not confirmed the autorization request yet
        TIMEOUT, // the user did not confirmed the authorization within the given time
        GRANTED, // the app_token is valid and can be used to open a session
        DENIED, // the user denied the authorization request
        UNKNOWN; // the app_token is invalid or has been revoked
    }

    public static enum Permission {
        PARENTAL,
        CONTACTS,
        EXPLORER,
        TV,
        WDO,
        DOWNLOADER,
        PROFILE,
        CAMERA,
        SETTINGS,
        CALLS,
        HOME,
        PVR,
        VM,
        PLAYER,
        NONE,
        UNKNOWN;
    }

    public static enum ServerType {
        POWERBOOK,
        POWERMAC,
        MACMINI,
        IMAC,
        MACBOOK,
        MACBOOKPRO,
        MACBOOKAIR,
        MACPRO,
        APPLETV,
        AIRPORT,
        XSERVE,
        UNKNOWN;
    }

    public static enum MediaAction {
        START,
        STOP,
        UNKNOWN;
    }

    public static enum MediaType {
        VIDEO,
        PHOTO,
        AUDIO,
        SCREEN,
        UNKNOWN;
    }

    public static enum CallType {
        ACCEPTED,
        MISSED,
        OUTGOING,
        INCOMING,
        UNKNOWN;
    }

    public static enum ConnectionState {
        GOING_UP,
        UP,
        GOING_DOWN,
        DOWN,
        UNKNOWN;
    }

    public static enum ConnectionType {
        ETHERNET,
        RFC2684,
        PPPOATM,
        UNKNOWN;
    }

    public static enum ConnectionMedia {
        FTTH,
        ETHERNET,
        XDSL,
        BACKUP_4G,
        UNKNOWN;
    }

    public static enum L3Af {
        IPV4,
        IPV6,
        UNKNOWN;
    }

    public static enum NetworkMode {
        ROUTER,
        BRIDGE,
        UNKNOWN;
    }

    public enum PhoneType {
        FXS,
        DECT,
        UNKNOWN;
    }

    public static enum PowerState {
        STANDBY,
        RUNNING,
        UNKNOWN;
    }

    public static enum VmStatus {
        STOPPED,
        RUNNING,
        UNKNOWN;
    }

    public enum SensorKind {
        FAN("Vitesse"),
        TEMP("Température"),
        UNKNOWN("Uknown");

        private String label;

        SensorKind(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static enum HomeNodeStatus {
        UNREACHABLE,
        DISABLED,
        ACTIVE,
        UNPAIRED,
        UNKNOWN;
    }

    public static enum EpType {
        SIGNAL,
        SLOT,
        UNKNOWN;
    }

    public static enum Visibility {
        INTERNAL,
        NORMAL,
        DASHBOARD,
        UNKNOWN;
    }

    public static enum ValueType {
        BOOL,
        INT,
        FLOAT,
        VOID,
        STRING,
        UNKNOWN;
    }
}
