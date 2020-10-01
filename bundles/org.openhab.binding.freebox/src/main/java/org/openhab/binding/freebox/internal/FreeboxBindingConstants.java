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

    public static final String CALLBACK_URL = "callbackUrl";
    // List of all Bridge Type UIDs
    public static final ThingTypeUID FREEBOX_BRIDGE_TYPE_REVOLUTION = new ThingTypeUID(BINDING_ID, "revolution");
    public static final ThingTypeUID FREEBOX_BRIDGE_TYPE_DELTA = new ThingTypeUID(BINDING_ID, "delta");

    // List of all Thing Type UIDs
    public static final ThingTypeUID FREEBOX_THING_TYPE_PHONE = new ThingTypeUID(BINDING_ID, "phone");
    public static final ThingTypeUID FREEBOX_THING_TYPE_HOST = new ThingTypeUID(BINDING_ID, "host");
    public static final ThingTypeUID FREEBOX_THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");
    public static final ThingTypeUID FREEBOX_THING_TYPE_VM = new ThingTypeUID(BINDING_ID, "vm");

    // All supported Thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(FREEBOX_BRIDGE_TYPE_REVOLUTION, FREEBOX_BRIDGE_TYPE_DELTA, FREEBOX_THING_TYPE_PHONE,
                    FREEBOX_THING_TYPE_HOST, FREEBOX_THING_TYPE_PLAYER, FREEBOX_THING_TYPE_VM)
            .collect(Collectors.toSet()));

    // List of things properties
    public static final String PROPERTY_MEDIA = "media";
    public static final String PROPERTY_INTERFACE = "interface";
    public static final String PROPERTY_AUDIO = "audio";
    public static final String PROPERTY_VIDEO = "video";
    public static final String PROPERTY_PHOTO = "photo";
    public static final String PROPERTY_SCREEN = "screen";
    public static final String PROPERTY_FANS = "fans";
    public static final String PROPERTY_SENSORS = "sensors";

    // List of all Group Channel ids
    public static final String CONNECTION_STATUS = "connection-status";
    public static final String SYS_INFO = "sysinfo";
    public static final String ACTIONS = "actions";
    public static final String SAMBA = "samba";
    public static final String PLAYER_ACTIONS = "player-actions";
    public static final String CONNECTIVITY = "connectivity";
    public static final String STATE = "state";
    public static final String DISPLAY = "display";
    public static final String VM_STATUS = "vmstatus";

    // List of all Channel ids
    public static final String UPTIME = "uptime";
    public static final String BOX_EVENT = "box-event";
    public static final String PHONE_EVENT = "phone-event";
    public static final String LCD_BRIGHTNESS = "lcd-brightness";
    public static final String LCD_ORIENTATION = "lcd-orientation";
    public static final String LCD_FORCED = "lcd-forced";
    public static final String WIFI_STATUS = "wifi-status";
    public static final String XDSL_STATUS = "xdsl-status";
    public static final String FTTH_STATUS = "ftth-status";
    public static final String IPV4 = "ipv4";

    public static final String LINE_STATUS = "line-status";
    public static final String RATE_UP = "rate-up";
    public static final String RATE_DOWN = "rate-down";
    public static final String BYTES_UP = "bytes-up";
    public static final String BYTES_DOWN = "bytes-down";
    public static final String BW_UP = "bandwidth-up";
    public static final String BW_DOWN = "bandwidth-down";
    public static final String PCT_BW_UP = "bandwidth-usage-up";
    public static final String PCT_BW_DOWN = "bandwidth-usage-down";

    public static final String ONHOOK = "onhook";
    public static final String RINGING = "ringing";
    public static final String CALL_NUMBER = "call-number";
    public static final String CALL_DURATION = "call-duration";
    public static final String CALL_TIMESTAMP = "call-timestamp";
    public static final String CALL_NAME = "call-name";
    public static final String FTP_STATUS = "ftp-status";
    public static final String SAMBA_FILE_STATUS = "fileshare-status";
    public static final String SAMBA_PRINTER_STATUS = "printershare-status";
    public static final String REACHABLE = "reachable";
    public static final String LAST_SEEN = "last-seen";

    // Freebox player channels
    public static final String AIRMEDIA_STATUS = "airmedia-status";
    public static final String UPNPAV_STATUS = "upnpav-status";
    public static final String KEY_CODE = "key-code";

    // Virtual machine channels
    public static final String STATUS = "status";
}
