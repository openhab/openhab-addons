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
package org.openhab.binding.freebox.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FreeboxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxBindingConstants {

    public static final String BINDING_ID = "freebox";

    // List of all Bridge Type UIDs
    public static final ThingTypeUID FREEBOX_BRIDGE_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");

    // List of all Thing Type UIDs
    public static final ThingTypeUID FREEBOX_THING_TYPE_PHONE = new ThingTypeUID(BINDING_ID, "phone");
    public static final ThingTypeUID FREEBOX_THING_TYPE_NET_DEVICE = new ThingTypeUID(BINDING_ID, "net_device");
    public static final ThingTypeUID FREEBOX_THING_TYPE_NET_INTERFACE = new ThingTypeUID(BINDING_ID, "net_interface");
    public static final ThingTypeUID FREEBOX_THING_TYPE_AIRPLAY = new ThingTypeUID(BINDING_ID, "airplay");

    // All supported Bridge types
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = Collections
            .singleton(FREEBOX_BRIDGE_TYPE_SERVER);

    // All supported Thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(FREEBOX_THING_TYPE_PHONE, FREEBOX_THING_TYPE_NET_DEVICE,
                    FREEBOX_THING_TYPE_NET_INTERFACE, FREEBOX_THING_TYPE_AIRPLAY).collect(Collectors.toSet()));

    // List of properties
    public static final String API_BASE_URL = "apiBaseUrl";
    public static final String API_VERSION = "apiVersion";

    // List of all Group Channel ids
    public static final String STATE = "state";
    public static final String ANY = "any";
    public static final String ACCEPTED = "accepted";
    public static final String MISSED = "missed";
    public static final String OUTGOING = "outgoing";

    // List of all Channel ids
    public static final String FWVERSION = "fwversion";
    public static final String UPTIME = "uptime";
    public static final String RESTARTED = "restarted";
    public static final String TEMPCPUM = "tempcpum";
    public static final String TEMPCPUB = "tempcpub";
    public static final String TEMPSWITCH = "tempswitch";
    public static final String FANSPEED = "fanspeed";
    public static final String LCDBRIGHTNESS = "lcd_brightness";
    public static final String LCDORIENTATION = "lcd_orientation";
    public static final String LCDFORCED = "lcd_forced";
    public static final String WIFISTATUS = "wifi_status";
    public static final String XDSLSTATUS = "xdsl_status";
    public static final String FTTHSTATUS = "ftth_status";
    public static final String LINESTATUS = "line_status";
    public static final String IPV4 = "ipv4";
    public static final String RATEUP = "rate_up";
    public static final String RATEDOWN = "rate_down";
    public static final String BYTESUP = "bytes_up";
    public static final String BYTESDOWN = "bytes_down";
    public static final String ONHOOK = "onhook";
    public static final String RINGING = "ringing";
    public static final String CALLNUMBER = "call_number";
    public static final String CALLDURATION = "call_duration";
    public static final String CALLTIMESTAMP = "call_timestamp";
    public static final String CALLSTATUS = "call_status";
    public static final String CALLNAME = "call_name";
    public static final String REBOOT = "reboot";
    public static final String FTPSTATUS = "ftp_status";
    public static final String AIRMEDIASTATUS = "airmedia_status";
    public static final String UPNPAVSTATUS = "upnpav_status";
    public static final String SAMBAFILESTATUS = "sambafileshare_status";
    public static final String SAMBAPRINTERSTATUS = "sambaprintershare_status";
    public static final String REACHABLE = "reachable";
    public static final String PLAYURL = "playurl";
    public static final String STOP = "stop";
}
