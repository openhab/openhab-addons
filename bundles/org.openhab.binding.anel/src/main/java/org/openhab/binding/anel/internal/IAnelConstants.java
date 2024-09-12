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
package org.openhab.binding.anel.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link IAnelConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public interface IAnelConstants {

    String BINDING_ID = "anel";

    /** Message sent to Anel devices to detect new dfevices and to request the current state. */
    String BROADCAST_DISCOVERY_MSG = "wer da?";
    /** Expected prefix for all received Anel status messages. */
    String STATUS_RESPONSE_PREFIX = "NET-PwrCtrl";
    /** Separator of the received Anel status messages. */
    String STATUS_SEPARATOR = ":";

    /** Status message String if the current user / password does not match. */
    String ERROR_CREDENTIALS = ":NoPass:Err";
    /** Status message String if the current user does not have enough rights. */
    String ERROR_INSUFFICIENT_RIGHTS = ":NoAccess:Err";

    /** Property name to uniquely identify (discovered) things. */
    String UNIQUE_PROPERTY_NAME = "mac";

    /** Default port used to send message to Anel devices. */
    int DEFAULT_SEND_PORT = 75;
    /** Default port used to receive message from Anel devices. */
    int DEFAULT_RECEIVE_PORT = 77;

    /** Static refresh interval for heartbeat for Thing status. */
    int REFRESH_INTERVAL_SEC = 60;

    /** Thing is set OFFLINE after so many communication errors. */
    int ATTEMPTS_WITH_COMMUNICATION_ERRORS = 3;

    /** Thing is set OFFLINE if it did not respond to so many refresh requests. */
    int UNANSWERED_REFRESH_REQUESTS_TO_SET_THING_OFFLINE = 5;

    /** Thing Type UID for Anel Net-PwrCtrl HOME. */
    ThingTypeUID THING_TYPE_ANEL_HOME = new ThingTypeUID(BINDING_ID, "home");
    /** Thing Type UID for Anel Net-PwrCtrl PRO / POWER. */
    ThingTypeUID THING_TYPE_ANEL_SIMPLE = new ThingTypeUID(BINDING_ID, "simple-firmware");
    /** Thing Type UID for Anel Net-PwrCtrl ADV / IO / HUT. */
    ThingTypeUID THING_TYPE_ANEL_ADVANCED = new ThingTypeUID(BINDING_ID, "advanced-firmware");
    /** All supported Thing Type UIDs. */
    Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ANEL_HOME, THING_TYPE_ANEL_SIMPLE,
            THING_TYPE_ANEL_ADVANCED);

    /** The device type is part of the status response and is mapped to the thing types. */
    Map<Character, ThingTypeUID> DEVICE_TYPE_TO_THING_TYPE = Map.of( //
            'H', THING_TYPE_ANEL_HOME, // HOME
            'P', THING_TYPE_ANEL_SIMPLE, // PRO / POWER
            'h', THING_TYPE_ANEL_ADVANCED, // HUT (and variants, e.g. h3 for HUT3)
            'a', THING_TYPE_ANEL_ADVANCED, // ADV
            'i', THING_TYPE_ANEL_ADVANCED); // IO

    // All remaining constants are Channel ids

    String CHANNEL_NAME = "prop#name";
    String CHANNEL_TEMPERATURE = "prop#temperature";

    List<String> CHANNEL_RELAY_NAME = List.of("r1#name", "r2#name", "r3#name", "r4#name", "r5#name", "r6#name",
            "r7#name", "r8#name");

    // second character must be the index b/c it is parsed in AnelCommandHandler!
    List<String> CHANNEL_RELAY_STATE = List.of("r1#state", "r2#state", "r3#state", "r4#state", "r5#state", "r6#state",
            "r7#state", "r8#state");

    List<String> CHANNEL_RELAY_LOCKED = List.of("r1#locked", "r2#locked", "r3#locked", "r4#locked", "r5#locked",
            "r6#locked", "r7#locked", "r8#locked");

    List<String> CHANNEL_IO_NAME = List.of("io1#name", "io2#name", "io3#name", "io4#name", "io5#name", "io6#name",
            "io7#name", "io8#name");

    List<String> CHANNEL_IO_MODE = List.of("io1#mode", "io2#mode", "io3#mode", "io4#mode", "io5#mode", "io6#mode",
            "io7#mode", "io8#mode");

    // third character must be the index b/c it is parsed in AnelCommandHandler!
    List<String> CHANNEL_IO_STATE = List.of("io1#state", "io2#state", "io3#state", "io4#state", "io5#state",
            "io6#state", "io7#state", "io8#state");

    String CHANNEL_SENSOR_TEMPERATURE = "sensor#temperature";
    String CHANNEL_SENSOR_HUMIDITY = "sensor#humidity";
    String CHANNEL_SENSOR_BRIGHTNESS = "sensor#brightness";

    /**
     * @param channelId A channel ID.
     * @return The zero-based index of the relay or IO channel (<code>0-7</code>); <code>-1</code> if it's not a relay
     *         or IO channel.
     */
    static int getIndexFromChannel(String channelId) {
        if (channelId.startsWith("r") && channelId.length() > 2) {
            return Character.getNumericValue(channelId.charAt(1)) - 1;
        }
        if (channelId.startsWith("io") && channelId.length() > 2) {
            return Character.getNumericValue(channelId.charAt(2)) - 1;
        }
        return -1; // not a relay or io channel
    }
}
