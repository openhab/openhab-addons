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
package org.openhab.binding.mqtt.fpp.internal;

import static org.openhab.binding.mqtt.MqttBindingConstants.BINDING_ID;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FPPBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class FPPBindingConstants {
    // falcon/player/FPP/fppd_status
    public static final String STATUS_TOPIC = "fppd_status";
    public static final String VERSION_TOPIC = "version";
    public static final String PLAYLIST_TOPIC = "playlist";
    public static final String MQTT_PREFIX = "falcon/player/";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, "player");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_PLAYER);

    // Channels
    public static final String CHANNEL_PLAYER = "player";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_CURRENT_SEQUENCE = "current-sequence";
    public static final String CHANNEL_CURRENT_SONG = "current-song";
    public static final String CHANNEL_CURRENT_PLAYLIST = "current-playlist";
    public static final String CHANNEL_SEC_PLAYED = "seconds-played";
    public static final String CHANNEL_SEC_REMAINING = "seconds-remaining";
    public static final String CHANNEL_UPTIME = "uptime";
    public static final String CHANNEL_BRIDGING = "bridging-enabled";
    public static final String CHANNEL_MULTISYNC = "multisync-enabled";
    public static final String CHANNEL_TESTING = "testing-enabled";
    public static final String CHANNEL_LAST_PLAYLIST = "last-playlist";

    public static final String CHANNEL_SCHEDULER_STATUS = "scheduler-status";
    public static final String CHANNEL_SCHEDULER_CURRENT_PLAYLIST = "scheduler-current-playlist";
    public static final String CHANNEL_SCHEDULER_CURRENT_PLAYLIST_START = "scheduler-current-playlist-start";
    public static final String CHANNEL_SCHEDULER_CURRENT_PLAYLIST_END = "scheduler-current-playlist-end";
    public static final String CHANNEL_SCHEDULER_CURRENT_PLAYLIST_STOP_TYPE = "scheduler-current-playlist-stop-type";
    public static final String CHANNEL_SCHEDULER_NEXT_PLAYLIST = "scheduler-next-playlist";
    public static final String CHANNEL_SCHEDULER_NEXT_PLAYLIST_START = "scheduler-next-playlist-start";

    public static final String PROPERTY_UUID = "uuid";
    public static final String PROPERTY_SOFTWARE_VERSION = "Software Version";

    // Status
    public static final String CONNECTED = "connected";
    public static final String CHANNEL_STATUS_NAME = "status-name";

    public static final String TESTING = "testing";
}
