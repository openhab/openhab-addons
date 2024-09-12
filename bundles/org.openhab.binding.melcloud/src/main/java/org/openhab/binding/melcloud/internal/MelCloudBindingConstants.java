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
package org.openhab.binding.melcloud.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MelCloudBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Luca Calcaterra - Initial contribution
 * @author Wietse van Buitenen - Added heatpump device
 */
@NonNullByDefault
public class MelCloudBindingConstants {

    private static final String BINDING_ID = "melcloud";

    // List of Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_MELCLOUD_ACCOUNT = new ThingTypeUID(BINDING_ID, "melcloudaccount");
    public static final ThingTypeUID THING_TYPE_HEATPUMPDEVICE = new ThingTypeUID(BINDING_ID, "heatpumpdevice");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACDEVICE = new ThingTypeUID(BINDING_ID, "acdevice");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_OPERATION_MODE = "operationMode";
    public static final String CHANNEL_SET_TEMPERATURE = "setTemperature";
    public static final String CHANNEL_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_VANE_HORIZONTAL = "vaneHorizontal";
    public static final String CHANNEL_VANE_VERTICAL = "vaneVertical";
    public static final String CHANNEL_SET_TEMPERATURE_ZONE1 = "setTemperatureZone1";
    public static final String CHANNEL_ROOM_TEMPERATURE_ZONE1 = "roomTemperatureZone1";
    public static final String CHANNEL_FORCED_HOTWATERMODE = "forcedHotWaterMode";
    public static final String CHANNEL_TANKWATERTEMPERATURE = "tankWaterTemperature";

    // Read Only Channels
    public static final String CHANNEL_ROOM_TEMPERATURE = "roomTemperature";
    public static final String CHANNEL_LAST_COMMUNICATION = "lastCommunication";
    public static final String CHANNEL_NEXT_COMMUNICATION = "nextCommunication";
    public static final String CHANNEL_HAS_PENDING_COMMAND = "hasPendingCommand";
    public static final String CHANNEL_OFFLINE = "offline";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_MELCLOUD_ACCOUNT, THING_TYPE_ACDEVICE, THING_TYPE_HEATPUMPDEVICE)
                    .collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_ACDEVICE, THING_TYPE_HEATPUMPDEVICE).collect(Collectors.toSet()));
}
