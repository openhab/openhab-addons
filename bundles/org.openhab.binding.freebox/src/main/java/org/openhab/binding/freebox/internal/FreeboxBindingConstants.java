/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FreeboxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxBindingConstants {

    public static final String BINDING_ID = "freebox";

    public static final String CALLBACK_URL = "callbackUrl";
    // List of all Bridge Type UIDs
    public static final ThingTypeUID FREEBOX_BRIDGE_TYPE_API = new ThingTypeUID(BINDING_ID, "api");

    // List of all Thing Type UIDs
    public static final ThingTypeUID FREEBOX_THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");
    public static final ThingTypeUID FREEBOX_THING_TYPE_DELTA = new ThingTypeUID(BINDING_ID, "delta");
    public static final ThingTypeUID FREEBOX_THING_TYPE_PHONE = new ThingTypeUID(BINDING_ID, "phone");
    public static final ThingTypeUID FREEBOX_THING_TYPE_NET_DEVICE = new ThingTypeUID(BINDING_ID, "net_device");
    public static final ThingTypeUID FREEBOX_THING_TYPE_AIRPLAY = new ThingTypeUID(BINDING_ID, "airplay");
    public static final ThingTypeUID FREEBOX_THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public static final ThingTypeUID FREEBOX_THING_TYPE_VM = new ThingTypeUID(BINDING_ID, "vm");

    // All supported Thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(FREEBOX_THING_TYPE_SERVER, FREEBOX_THING_TYPE_DELTA, FREEBOX_THING_TYPE_PHONE,
                    FREEBOX_THING_TYPE_NET_DEVICE, FREEBOX_THING_TYPE_AIRPLAY, FREEBOX_THING_TYPE_PLAYER,
                    FREEBOX_THING_TYPE_VM).collect(Collectors.toSet()));

    // All supported Thing types
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(FREEBOX_THING_TYPE_PHONE, FREEBOX_THING_TYPE_NET_DEVICE,
                    FREEBOX_THING_TYPE_AIRPLAY, FREEBOX_THING_TYPE_VM).collect(Collectors.toSet()));

    // List of properties
    public static final String API_BASE_URL = "apiBaseUrl";
    public static final String API_VERSION = "apiVersion";

    // List of all Group Channel ids
    public static final String CONNECTION_STATUS = "connections-status";
    public static final String SYS_INFO = "sysinfo";
    public static final String ACTIONS = "actions";
    public static final String SAMBA = "samba";
    public static final String PLAYER_ACTIONS = "player-actions";
    public static final String CONNECTIVITY = "connectivity";
    public static final String STATE = "state";
    public static final String DISPLAY = "display";
    public static final String VM_STATUS = "vmstatus";

    public static final String ANY = "any";
    public static final String ACCEPTED = "accepted";
    public static final String MISSED = "missed";
    public static final String OUTGOING = "outgoing";

    // List of all Channel ids
    public static final String UPTIME = "uptime";
    public static final String BOX_EVENT = "boxevent";
    public static final String LCDBRIGHTNESS = "lcd_brightness";
    public static final String LCDORIENTATION = "lcd_orientation";
    public static final String LCDFORCED = "lcd_forced";
    public static final String WIFISTATUS = "wifi_status";
    public static final String XDSLSTATUS = "xdsl_status";
    public static final String FTTHSTATUS = "ftth_status";
    public static final String IPV4 = "ipv4";

    public static final String LINESTATUS = "line_status";
    public static final String RATEUP = "rate_up";
    public static final String RATEDOWN = "rate_down";
    public static final String BYTESUP = "bytes_up";
    public static final String BYTESDOWN = "bytes_down";
    public static final String BWUP = "bandwidthUp";
    public static final String BWDOWN = "bandwidthDown";

    public static final String ONHOOK = "onhook";
    public static final String RINGING = "ringing";
    public static final String CALLNUMBER = "call_number";
    public static final String CALLDURATION = "call_duration";
    public static final String CALLTIMESTAMP = "call_timestamp";
    public static final String CALLSTATUS = "call_status";
    public static final String CALLNAME = "call_name";
    public static final String FTPSTATUS = "ftp_status";
    public static final String SAMBAFILESTATUS = "fileshare_status";
    public static final String SAMBAPRINTERSTATUS = "printershare_status";
    public static final String REACHABLE = "reachable";
    public static final String LAST_SEEN = "lastSeen";

    // Freebox player channels
    public static final String AIRMEDIASTATUS = "airmedia_status";
    public static final String UPNPAVSTATUS = "upnpav_status";

    // Virtual machine channels
    public static final String STATUS = "status";

    // Thing properties
    public static final String PROPERTY_FANS = "fans";
    public static final String PROPERTY_SENSORS = "sensors";

}
