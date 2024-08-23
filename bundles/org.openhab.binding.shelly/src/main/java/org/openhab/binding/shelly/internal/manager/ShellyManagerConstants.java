/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.manager;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CONFIG_DEVICEIP;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link ShellyManagerConstants} defines the constants for Shelly Manager
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyManagerConstants {
    public static final String LOG_PREFIX = "ShellyManager";
    public static final String UTF_8 = StandardCharsets.UTF_8.toString();

    public static final String SHELLY_MANAGER_URI = "/shelly/manager";
    public static final String SHELLY_MGR_OVERVIEW_URI = SHELLY_MANAGER_URI + "/ovierview";
    public static final String SHELLY_MGR_FWUPDATE_URI = SHELLY_MANAGER_URI + "/fwupdate";
    public static final String SHELLY_MGR_IMAGES_URI = SHELLY_MANAGER_URI + "/images";
    public static final String SHELLY_MGR_ACTION_URI = SHELLY_MANAGER_URI + "/action";
    public static final String SHELLY_MGR_OTA_URI = SHELLY_MANAGER_URI + "/ota";

    public static final String ACTION_REFRESH = "refresh";
    public static final String ACTION_RESTART = "restart";
    public static final String ACTION_PROTECT = "protect";
    public static final String ACTION_SETCOIOT_PEER = "setcoiotpeer";
    public static final String ACTION_SETCOIOT_MCAST = "setcoiotmcast";
    public static final String ACTION_SETTZ = "settz";
    public static final String ACTION_ENCLOUD = "encloud";
    public static final String ACTION_DISCLOUD = "discloud";
    public static final String ACTION_RES_STATS = "reset_stat";
    public static final String ACTION_RESET = "reset";
    public static final String ACTION_RESSTA = "resetsta";
    public static final String ACTION_ENWIFIREC = "enwifirec";
    public static final String ACTION_DISWIFIREC = "diswifirec";
    public static final String ACTION_ENAPROAMING = "enaproaming";
    public static final String ACTION_DISAPROAMING = "disaproaming";
    public static final String ACTION_ENRANGEEXT = "enrangeext";
    public static final String ACTION_ENETHERNET = "enethernet";
    public static final String ACTION_DISETHERNET = "disethernet";
    public static final String ACTION_ENBLUETOOTH = "enbluetooth";
    public static final String ACTION_DISBLUETOOTH = "disbluetooth";
    public static final String ACTION_DISRANGEEXT = "disrangeext";
    public static final String ACTION_OTACHECK = "otacheck";
    public static final String ACTION_ENDEBUG = "endebug";
    public static final String ACTION_DISDEBUG = "disdebug";
    public static final String ACTION_GETDEB = "getdebug";
    public static final String ACTION_GETDEB1 = "getdebug1";
    public static final String ACTION_NONE = "-";

    public static final String HEADER_HTML = "header.html";
    public static final String OVERVIEW_HTML = "overview.html";
    public static final String OVERVIEW_HEADER = "ov_header.html";
    public static final String OVERVIEW_DEVICE = "ov_device.html";
    public static final String OVERVIEW_FOOTER = "ov_footer.html";
    public static final String FWUPDATE1_HTML = "fw_update1.html";
    public static final String FWUPDATE2_HTML = "fw_update2.html";
    public static final String ACTION_HTML = "action.html";
    public static final String FOOTER_HTML = "footer.html";
    public static final String IMAGE_PATH = "images/";
    public static final String FORWARD_SCRIPT = "forward.script";

    public static final String ATTRIBUTE_METATAG = "metaTag";
    public static final String ATTRIBUTE_CSS_HEADER = "cssHeader";
    public static final String ATTRIBUTE_CSS_FOOTER = "cssFooter";
    public static final String ATTRIBUTE_URI = "uri";
    public static final String ATTRIBUTE_UID = "uid";
    public static final String ATTRIBUTE_REFRESH = "refreshTimer";
    public static final String ATTRIBUTE_MESSAGE = "message";
    public static final String ATTRIBUTE_TOTAL_DEV = "totalDevices";
    public static final String ATTRIBUTE_STATUS_ICON = "iconStatus";
    public static final String ATTRIBUTE_DEVICEIP = CONFIG_DEVICEIP;
    public static final String ATTRIBUTE_DISPLAY_NAME = "displayName";
    public static final String ATTRIBUTE_DEV_STATUS = "deviceStatus";
    public static final String ATTRIBUTE_DEBUG_MODE = "debugMode";
    public static final String ATTRIBUTE_FIRMWARE_SEL = "firmwareSelection";
    public static final String ATTRIBUTE_ACTION_LIST = "actionList";
    public static final String ATTRIBUTE_VERSION = "version";
    public static final String ATTRIBUTE_FW_URL = "firmwareUrl";
    public static final String ATTRIBUTE_UPDATE_URL = "updateUrl";
    public static final String ATTRIBUTE_LAST_ALARM = "lastAlarmTs";
    public static final String ATTRIBUTE_ACTION = "action";
    public static final String ATTRIBUTE_ACTION_BUTTON = "actionButtonLabel";
    public static final String ATTRIBUTE_ACTION_URL = "actionUrl";
    public static final String ATTRIBUTE_SNTP_SERVER = "sntpServer";
    public static final String ATTRIBUTE_COIOT_STATUS = "coiotStatus";
    public static final String ATTRIBUTE_COIOT_PEER = "coiotDestination";
    public static final String ATTRIBUTE_CLOUD_STATUS = "cloudStatus";
    public static final String ATTRIBUTE_MQTT_STATUS = "mqttStatus";
    public static final String ATTRIBUTE_ACTIONS_SKIPPED = "actionsSkipped";
    public static final String ATTRIBUTE_DISCOVERABLE = "discoverable";
    public static final String ATTRIBUTE_WIFI_RECOVERY = "wifiAutoRecovery";
    public static final String ATTRIBUTE_APR_MODE = "apRoamingMode";
    public static final String ATTRIBUTE_APR_TRESHOLD = "apRoamingThreshold";
    public static final String ATTRIBUTE_MAX_ITEMP = "maxInternalTemp";
    public static final String ATTRIBUTE_TIMEZONE = "deviceTimezone";
    public static final String ATTRIBUTE_PWD_PROTECT = "passwordProtected";

    public static final String URLPARM_UID = "uid";
    public static final String URLPARM_DEVTYPE = "deviceType";
    public static final String URLPARM_DEVMODE = "deviceMode";
    public static final String URLPARM_ACTION = "action";
    public static final String URLPARM_FILTER = "filter";
    public static final String URLPARM_TYPE = "type";
    public static final String URLPARM_VERSION = "version";
    public static final String URLPARM_UPDATE = "update";
    public static final String URLPARM_CONNECTION = "connection";
    public static final String URLPARM_URL = "url";

    public static final String FILTER_ONLINE = "online";
    public static final String FILTER_INACTIVE = "inactive";
    public static final String FILTER_ATTENTION = "attention";
    public static final String FILTER_UPDATE = "update";
    public static final String FILTER_UNPROTECTED = "unprotected";

    // Message classes for visual style
    public static final String MCMESSAGE = "message";
    public static final String MCINFO = "info";
    public static final String MCWARNING = "warning";

    public static final String ICON_ONLINE = "online";
    public static final String ICON_OFFLINE = "offline";
    public static final String ICON_UNINITIALIZED = "uninitialized";
    public static final String ICON_CONFIG = "config";
    public static final String ICON_ATTENTION = "attention";

    public static final String CONNECTION_TYPE_LOCAL = "local";
    public static final String CONNECTION_TYPE_INTERNET = "internet";
    public static final String CONNECTION_TYPE_CUSTOM = "custom";

    public static final String FWPROD = "prod";
    public static final String FWBETA = "beta";

    public static final String FWREPO_PROD_URL = "https://api.shelly.cloud/files/firmware/";
    public static final String FWREPO_TEST_URL = "https://repo.shelly.cloud/files/firmware/";
    public static final String FWREPO_ARCH_URL = "http://archive.shelly-tools.de/archive.php";
    public static final String FWREPO_ARCFILE_URL = "http://archive.shelly-tools.de/version/";

    public static final int CACHE_TIMEOUT_DEF_MIN = 60; // Default timeout for cache entries
    public static final int CACHE_TIMEOUT_FW_MIN = 15; // Cache entries for the firmware list 15min
}
