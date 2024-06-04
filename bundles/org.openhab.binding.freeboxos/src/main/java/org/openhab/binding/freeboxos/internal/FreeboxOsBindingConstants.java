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
package org.openhab.binding.freeboxos.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Category;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;

/**
 * The {@link FreeboxOsBindingConstants} class defines common constants, which are used across the binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsBindingConstants {

    public static final String BINDING_ID = "freeboxos";

    // List of all Bridge Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_API = new ThingTypeUID(BINDING_ID, "api");

    // Thing Types ID strings
    private static final String THING_DECT = "dect";
    private static final String THING_FXS = "fxs";
    private static final String THING_REVOLUTION = "revolution";
    private static final String THING_DELTA = "delta";
    private static final String THING_WIFI_HOST = "wifihost";
    private static final String THING_ACTIVE_PLAYER = "active-player";

    public static final String THING_FREEPLUG = "freeplug";
    public static final String THING_VM = "vm";
    public static final String THING_CALL = "call";
    public static final String THING_HOST = "host";
    public static final String THING_PLAYER = "player";
    public static final String THING_REPEATER = "repeater";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_REVOLUTION = new ThingTypeUID(BINDING_ID, THING_REVOLUTION);
    public static final ThingTypeUID THING_TYPE_DELTA = new ThingTypeUID(BINDING_ID, THING_DELTA);
    public static final ThingTypeUID THING_TYPE_FXS = new ThingTypeUID(BINDING_ID, THING_FXS);
    public static final ThingTypeUID THING_TYPE_DECT = new ThingTypeUID(BINDING_ID, THING_DECT);
    public static final ThingTypeUID THING_TYPE_CALL = new ThingTypeUID(BINDING_ID, THING_CALL);
    public static final ThingTypeUID THING_TYPE_FREEPLUG = new ThingTypeUID(BINDING_ID, THING_FREEPLUG);
    public static final ThingTypeUID THING_TYPE_HOST = new ThingTypeUID(BINDING_ID, THING_HOST);
    public static final ThingTypeUID THING_TYPE_WIFI_HOST = new ThingTypeUID(BINDING_ID, THING_WIFI_HOST);
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, THING_PLAYER);
    public static final ThingTypeUID THING_TYPE_ACTIVE_PLAYER = new ThingTypeUID(BINDING_ID, THING_ACTIVE_PLAYER);
    public static final ThingTypeUID THING_TYPE_VM = new ThingTypeUID(BINDING_ID, THING_VM);
    public static final ThingTypeUID THING_TYPE_REPEATER = new ThingTypeUID(BINDING_ID, THING_REPEATER);

    // All supported Thing types
    public static final Set<ThingTypeUID> BRIDGE_TYPE_UIDS = Set.of(BRIDGE_TYPE_API);
    public static final Set<ThingTypeUID> THINGS_TYPES_UIDS = Set.of(THING_TYPE_FXS, THING_TYPE_DECT, THING_TYPE_CALL,
            THING_TYPE_HOST, THING_TYPE_VM, THING_TYPE_PLAYER, THING_TYPE_ACTIVE_PLAYER, THING_TYPE_DELTA,
            THING_TYPE_REVOLUTION, THING_TYPE_REPEATER, THING_TYPE_WIFI_HOST, THING_TYPE_FREEPLUG);
    public static final Set<ThingTypeUID> HOME_TYPES_UIDS = Set.of(Category.BASIC_SHUTTER.getThingTypeUID(),
            Category.SHUTTER.getThingTypeUID(), Category.KFB.getThingTypeUID(), Category.CAMERA.getThingTypeUID(),
            Category.ALARM.getThingTypeUID());

    protected static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(BRIDGE_TYPE_UIDS, THINGS_TYPES_UIDS, HOME_TYPES_UIDS).flatMap(Set::stream).collect(Collectors.toSet());

    // Thing properties
    // public static final String LAST_CALL_TIMESTAMP = "lastCallTimestamp";
    public static final String ROLE = "role";
    public static final String NET_ID = "netId";
    public static final String ETHERNET_SPEED = "ethernetSpeed";
    public static final String LOCAL = "local";
    public static final String FULL_DUPLEX = "fullDuplex";

    // List of all Group Channel ids
    public static final String GROUP_SENSORS = "sensors";
    public static final String GROUP_FANS = "fans";
    public static final String CONNECTION_STATUS = "connection-status";
    public static final String SYS_INFO = "sysinfo";
    public static final String ACTIONS = "actions";
    public static final String FILE_SHARING = "file-sharing";
    public static final String CONNECTIVITY = "connectivity";
    public static final String DISPLAY = "display";
    public static final String VM_STATUS = "vmstatus";
    public static final String GROUP_WIFI = "wifi";
    public static final String REPEATER_MISC = "repeater-misc";

    // List of all Channel ids
    public static final String RSSI = "rssi";
    public static final String SSID = "ssid";
    public static final String WIFI_QUALITY = "wifi-quality";
    public static final String WIFI_HOST = "wifi-host";
    public static final String UPTIME = "uptime";
    public static final String BOX_EVENT = "box-event";
    public static final String LCD_BRIGHTNESS = "lcd-brightness";
    public static final String LCD_ORIENTATION = "lcd-orientation";
    public static final String LCD_FORCED = "lcd-forced";
    public static final String WIFI_STATUS = "wifi-status";
    public static final String IP_ADDRESS = "ip-address";
    public static final String IPV6_ADDRESS = "ipv6-address";
    public static final String LINE_STATUS = "line-status";
    public static final String LINE_TYPE = "line-type";
    public static final String LINE_MEDIA = "line-media";
    public static final String PLAYER_STATUS = "player-status";
    public static final String PACKAGE = "package";
    public static final String RATE = "rate";
    public static final String BYTES_UP = "bytes-up";
    public static final String BYTES_DOWN = "bytes-down";
    public static final String BW = "bandwidth";
    public static final String PCT_BW = "bandwidth-usage";
    public static final String ONHOOK = "onhook";
    public static final String RINGING = "ringing";
    public static final String HARDWARE_STATUS = "hardware-status";
    public static final String TELEPHONY_SERVICE = "telephony-service";
    public static final String GAIN_RX = "gain-rx";
    public static final String GAIN_TX = "gain-tx";
    public static final String FTP_STATUS = "ftp-status";
    public static final String SAMBA_FILE_STATUS = "samba-file-status";
    public static final String SAMBA_PRINTER_STATUS = "samba-printer-status";
    public static final String AFP_FILE_STATUS = "afp-file-status";
    public static final String REACHABLE = "reachable";
    public static final String LAST_SEEN = "last-seen";
    public static final String ALTERNATE_RING = "lcd-forced";
    public static final String DECT_ACTIVE = "dect-active";
    public static final String UPNPAV_STATUS = "upnpav-status";

    // Call channels for groups Accepted, Missed and Outgoing
    public static final String NUMBER = "number";
    public static final String DURATION = "duration";
    public static final String TIMESTAMP = "timestamp";
    public static final String NAME = "name";

    // Freebox player channels
    public static final String AIRMEDIA_STATUS = "airmedia-status";
    public static final String KEY_CODE = "key-code";

    // Virtual machine channels
    public static final String STATUS = "status";

    // Repeater channels
    public static final String LED = "led";
    public static final String HOST_COUNT = "host-count";

    // Home channels
    public static final String KEYFOB_ENABLE = "enable";
    public static final String NODE_BATTERY = "battery";
    public static final String SHUTTER_POSITION = "position-set";
    public static final String SHUTTER_STOP = "stop";
    public static final String BASIC_SHUTTER_STATE = "state";
    public static final String BASIC_SHUTTER_UP = "up";
    public static final String BASIC_SHUTTER_DOWN = "down";
    // public static final String BASIC_SHUTTER_CMD = "basic-shutter";
    public static final String ALARM_PIN = "pin";
    public static final String ALARM_SOUND = "sound";
    public static final String ALARM_VOLUME = "volume";
    public static final String ALARM_TIMEOUT1 = "timeout1";
    public static final String ALARM_TIMEOUT2 = "timeout2";
    public static final String ALARM_TIMEOUT3 = "timeout3";

    public static final Set<Command> TRUE_COMMANDS = Set.of(OnOffType.ON, UpDownType.UP, OpenClosedType.OPEN);
    public static final Set<Class<?>> ON_OFF_CLASSES = Set.of(OnOffType.class, UpDownType.class, OpenClosedType.class);
}
