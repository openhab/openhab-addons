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
    public static final String CHANNEL_PLAYER = "fppPlayer";
    public static final String CHANNEL_STATUS = "fppStatus";
    public static final String CHANNEL_VOLUME = "fppVolume";
    public static final String CHANNEL_MODE = "fppMode";
    public static final String CHANNEL_CURRENT_SEQUENCE = "fppCurrentSequence";
    public static final String CHANNEL_CURRENT_SONG = "fppCurrentSong";
    public static final String CHANNEL_CURRENT_PLAYLIST = "fppCurrentPlaylist";
    public static final String CHANNEL_SEC_PLAYED = "fppSecPlayed";
    public static final String CHANNEL_SEC_REMAINING = "fppSecRemaining";
    public static final String CHANNEL_UPTIME = "fppUptime";
    public static final String CHANNEL_UUID = "fppUUID";
    public static final String CHANNEL_VERSION = "fppVersion";
    public static final String CHANNEL_BRIDGING = "fppBridging";
    public static final String CHANNEL_MULTISYNC = "fppMultisync";
    public static final String CHANNEL_TESTING = "fppTesting";
    public static final String CHANNEL_LAST_PLAYLIST = "fppLastPlaylist";

    public static final String CHANNEL_SCHEDULERSTATUS = "fppSchedulerStatus";
    public static final String CHANNEL_SCHEDULERCURRENTPLAYLIST = "fppSchedulerCurrentPlaylist";
    public static final String CHANNEL_SCHEDULERCURRENTPLAYLISTSTART = "fppSchedulerCurrentPlaylistStart";
    public static final String CHANNEL_SCHEDULERCURRENTPLAYLISTEND = "fppSchedulerCurrentPlaylistEnd";
    public static final String CHANNEL_SCHEDULERCURRENTPLAYLISTSTOPTYPE = "fppSchedulerCurrentPlaylistStopType";
    public static final String CHANNEL_SCHEDULERNEXTPLAYLIST = "fppSchedulerNextPlaylist";
    public static final String CHANNEL_SCHEDULERNEXTPLAYLISTSTART = "fppSchedulerNextPlaylistStart";

    // Status
    public static final String CONNECTED = "connected";
    public static final String CHANNEL_STATUS_NAME = "status_name";

    public static final String TESTING = "testing";
}
