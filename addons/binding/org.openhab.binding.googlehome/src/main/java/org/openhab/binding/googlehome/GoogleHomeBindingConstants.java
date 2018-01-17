/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.googlehome;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link GoogleHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kuba Wolanin - Initial contribution
 */
@NonNullByDefault
public class GoogleHomeBindingConstants {

    private static final String BINDING_ID = "googlehome";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GOOGLE_HOME = new ThingTypeUID(BINDING_ID, "home");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_GOOGLE_HOME);

    // List of thing parameters names
    public static final String HOST_PARAMETER = "ipAddress";
    public static final String PORT_PARAMETER = "port";
    public static final String REFRESH_PARAMETER = "refreshInterval";

    // List of all Channel ids
    public static final String CHANNEL_UPTIME = "uptime";
    public static final String CHANNEL_HAS_UPDATE = "has_update";
    public static final String CHANNEL_NOISE_LEVEL = "noise_level";
    public static final String CHANNEL_SIGNAL_LEVEL = "signal_level";

    // https://rithvikvibhu.github.io/GHLocalApi/#device-settings-set-night-mode-settings-post
    public static final String CHANNEL_NIGHT_MODE = "night_mode";

    public static final String CHANNEL_DO_NOT_DISTURB = "do_not_disturb"; // Switch
    public static final String CHANNEL_ALARMS_VOLUME = "alarms_volume";  // Dimmer
    public static final String CHANNEL_EARLIEST_ALARM = "earliest_alarm"; // DateTime
    public static final String CHANNEL_EARLIEST_TIMER = "earliest_timer"; // DateTime

    // Trigger channels
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_TIMER = "timer";
}
