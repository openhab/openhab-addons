/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link FreeboxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class FreeboxBindingConstants {

    public static final String BINDING_ID = "freebox";

    // List of all Bridge Type UIDs
    public final static ThingTypeUID FREEBOX_BRIDGE_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");

    // List of all Thing Type UIDs
    public final static ThingTypeUID FREEBOX_THING_TYPE_PHONE = new ThingTypeUID(BINDING_ID, "phone");
    public final static ThingTypeUID FREEBOX_THING_TYPE_NET_DEVICE = new ThingTypeUID(BINDING_ID, "net_device");
    public final static ThingTypeUID FREEBOX_THING_TYPE_NET_INTERFACE = new ThingTypeUID(BINDING_ID, "net_interface");

    // All supported Bridge types
    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = ImmutableSet.of(FREEBOX_BRIDGE_TYPE_SERVER);

    // All supported Thing types
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(FREEBOX_THING_TYPE_PHONE,
            FREEBOX_THING_TYPE_NET_DEVICE, FREEBOX_THING_TYPE_NET_INTERFACE);

    // List of properties
    public final static String API_BASE_URL = "apiBaseUrl";
    public final static String API_VERSION = "apiVersion";

    // List of all Group Channel ids
    public final static String STATE = "state";
    public final static String ANY = "any";
    public final static String ACCEPTED = "accepted";
    public final static String MISSED = "missed";
    public final static String OUTGOING = "outgoing";

    // List of all Channel ids
    public final static String FWVERSION = "fwversion";
    public final static String UPTIME = "uptime";
    public final static String RESTARTED = "restarted";
    public final static String TEMPCPUM = "tempcpum";
    public final static String TEMPCPUB = "tempcpub";
    public final static String TEMPSWITCH = "tempswitch";
    public final static String FANSPEED = "fanspeed";
    public final static String LCDBRIGHTNESS = "lcd_brightness";
    public final static String LCDORIENTATION = "lcd_orientation";
    public final static String LCDFORCED = "lcd_forced";
    public final static String WIFISTATUS = "wifi_status";
    public final static String XDSLSTATUS = "xdsl_status";
    public final static String LINESTATUS = "line_status";
    public final static String IPV4 = "ipv4";
    public final static String RATEUP = "rate_up";
    public final static String RATEDOWN = "rate_down";
    public final static String BYTESUP = "bytes_up";
    public final static String BYTESDOWN = "bytes_down";
    public final static String ONHOOK = "onhook";
    public final static String RINGING = "ringing";
    public final static String CALLNUMBER = "call_number";
    public final static String CALLDURATION = "call_duration";
    public final static String CALLTIMESTAMP = "call_timestamp";
    public final static String CALLSTATUS = "call_status";
    public final static String CALLNAME = "call_name";
    public final static String REBOOT = "reboot";
    public final static String FTPSTATUS = "ftp_status";
    public final static String AIRMEDIASTATUS = "airmedia_status";
    public final static String UPNPAVSTATUS = "upnpav_status";
    public final static String SAMBAFILESTATUS = "sambafileshare_status";
    public final static String SAMBAPRINTERSTATUS = "sambaprintershare_status";
    public final static String REACHABLE = "reachable";
}
