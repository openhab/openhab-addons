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
package org.openhab.binding.unifi.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link UniFiBindingConstants} class defines common constants, which are
 * used across the UniFi binding.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Patrik Wimnell - Blocking / Unblocking client support
 * @author Hilbrand Bouwkamp - Added poePort
 * @author Mark Herwege - Added guest vouchers
 */
@NonNullByDefault
public final class UniFiBindingConstants {

    public static final String BINDING_ID = "unifi";

    // List of all Thing Types
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_SITE = new ThingTypeUID(BINDING_ID, "site");
    public static final ThingTypeUID THING_TYPE_WLAN = new ThingTypeUID(BINDING_ID, "wlan");
    public static final ThingTypeUID THING_TYPE_WIRED_CLIENT = new ThingTypeUID(BINDING_ID, "wiredClient");
    public static final ThingTypeUID THING_TYPE_WIRELESS_CLIENT = new ThingTypeUID(BINDING_ID, "wirelessClient");
    public static final ThingTypeUID THING_TYPE_POE_PORT = new ThingTypeUID(BINDING_ID, "poePort");
    public static final Set<ThingTypeUID> ALL_THING_TYPE_SUPPORTED = Set.of(THING_TYPE_CONTROLLER, THING_TYPE_SITE,
            THING_TYPE_WLAN, THING_TYPE_WIRED_CLIENT, THING_TYPE_WIRELESS_CLIENT, THING_TYPE_POE_PORT);
    public static final Set<ThingTypeUID> THING_TYPE_SUPPORTED = Set.of(THING_TYPE_SITE, THING_TYPE_WLAN,
            THING_TYPE_WIRED_CLIENT, THING_TYPE_WIRELESS_CLIENT, THING_TYPE_POE_PORT);

    // List of site channels
    public static final String CHANNEL_TOTAL_CLIENTS = "totalClients";
    public static final String CHANNEL_WIRELESS_CLIENTS = "wirelessClients";
    public static final String CHANNEL_WIRED_CLIENTS = "wiredClients";
    public static final String CHANNEL_GUEST_CLIENTS = "guestClients";
    public static final String CHANNEL_GUEST_VOUCHER = "guestVoucher";
    public static final String CHANNEL_GUEST_VOUCHERS_GENERATE = "guestVouchersGenerate";

    // List of wlan channels
    public static final String CHANNEL_SECURITY = "security";
    public static final String CHANNEL_WLANBAND = "wlanBand";
    public static final String CHANNEL_WPAENC = "wpaEnc";
    public static final String CHANNEL_WPAMODE = "wpaMode";
    public static final String CHANNEL_PASSPHRASE = "passphrase";
    public static final String CHANNEL_QRCODE_ENCODING = "qrcodeEncoding";

    // List of common wired + wireless client channels
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_HOSTNAME = "hostname";
    public static final String CHANNEL_SITE = "site";
    public static final String CHANNEL_MAC_ADDRESS = "macAddress";
    public static final String CHANNEL_IP_ADDRESS = "ipAddress";
    public static final String CHANNEL_UPTIME = "uptime";
    public static final String CHANNEL_LAST_SEEN = "lastSeen";
    public static final String CHANNEL_GUEST = "guest";
    public static final String CHANNEL_BLOCKED = "blocked";
    public static final String CHANNEL_RECONNECT = "reconnect";
    public static final String CHANNEL_CMD = "cmd";
    public static final String CHANNEL_CMD_RECONNECT = "reconnect";
    public static final String CHANNEL_EXPERIENCE = "experience";

    // List of additional wireless client channels
    public static final String CHANNEL_AP = "ap";
    public static final String CHANNEL_ESSID = "essid";
    public static final String CHANNEL_RSSI = "rssi";

    // List of switch port channels
    public static final String CHANNEL_ENABLE = "enable";
    public static final String CHANNEL_ENABLE_PARAMETER_MODE = "mode";
    public static final String CHANNEL_ENABLE_PARAMETER_MODE_OFF = "off";
    public static final String CHANNEL_ENABLE_PARAMETER_MODE_AUTO = "auto";
    public static final String CHANNEL_PORT_POE_MODE = "mode";
    public static final String CHANNEL_PORT_POE_CMD = "cmd";
    public static final String CHANNEL_PORT_POE_CMD_POWER_CYCLE = "powercycle";
    public static final String CHANNEL_PORT_POE_ENABLE = "enable";
    public static final String CHANNEL_PORT_POE_POWER = "power";
    public static final String CHANNEL_PORT_POE_VOLTAGE = "voltage";
    public static final String CHANNEL_PORT_POE_CURRENT = "current";

    // List of all Parameters
    public static final String PARAMETER_HOST = "host";
    public static final String PARAMETER_PORT = "port";
    public static final String PARAMETER_USERNAME = "username";
    public static final String PARAMETER_PASSWORD = "password";
    public static final String PARAMETER_UNIFIOS = "unifios";
    public static final String PARAMETER_SITE = "site";
    public static final String PARAMETER_CID = "cid";
    public static final String PARAMETER_SID = "sid";
    public static final String PARAMETER_WID = "wid";
    public static final String PARAMETER_VOUCHER_COUNT = "voucherCount";
    public static final String PARAMETER_VOUCHER_EXPIRATION = "voucherExpiration";
    public static final String PARAMETER_VOUCHER_USERS = "voucherUsers";
    public static final String PARAMETER_VOUCHER_UP_LIMIT = "voucherUpLimit";
    public static final String PARAMETER_VOUCHER_DOWN_LIMIT = "voucherDownLimit";
    public static final String PARAMETER_VOUCHER_DATA_QUOTA = "voucherDataQuota";
    public static final String PARAMETER_PORT_NUMBER = "portNumber";
    public static final String PARAMETER_MAC_ADDRESS = "macAddress";
    public static final String PARAMETER_WIFI_NAME = "wifi";

    private UniFiBindingConstants() {
        // Constants class
    }
}
