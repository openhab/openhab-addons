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
package org.openhab.binding.anthem.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AnthemBindingConstants} class defines common constants, which are
 * used across the entire binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemBindingConstants {
    public static final String BINDING_ID = "anthem";

    public static final ThingTypeUID THING_TYPE_ANTHEM = new ThingTypeUID(BINDING_ID, "anthem");

    // List of all Thing Type UIDs
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ANTHEM);

    // Channel groups
    public static final String CHANNEL_GROUP_GENERAL = "general";

    // Channel Ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_VOLUME_DB = "volumeDB";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_ACTIVE_INPUT = "activeInput";
    public static final String CHANNEL_ACTIVE_INPUT_SHORT_NAME = "activeInputShortName";
    public static final String CHANNEL_ACTIVE_INPUT_LONG_NAME = "activeInputLongName";
    public static final String CHANNEL_COMMAND = "command";

    // Connection-related configuration parameters
    public static final int DEFAULT_PORT = 14999;
    public static final int DEFAULT_RECONNECT_INTERVAL_MINUTES = 2;
    public static final int DEFAULT_COMMAND_DELAY_MSEC = 100;

    public static final char COMMAND_TERMINATION_CHAR = ';';

    public static final String PROPERTY_REGION = "region";
    public static final String PROPERTY_SOFTWARE_BUILD_DATE = "softwareBuildDate";
    public static final String PROPERTY_NUM_AVAILABLE_INPUTS = "numAvailableInputs";
}
