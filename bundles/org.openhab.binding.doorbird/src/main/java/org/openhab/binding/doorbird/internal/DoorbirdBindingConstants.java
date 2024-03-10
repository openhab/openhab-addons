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
package org.openhab.binding.doorbird.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DoorbirdBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class DoorbirdBindingConstants {
    public static final String BINDING_ID = "doorbird";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_D101 = new ThingTypeUID(BINDING_ID, "d101");
    public static final ThingTypeUID THING_TYPE_D210X = new ThingTypeUID(BINDING_ID, "d210x");
    public static final ThingTypeUID THING_TYPE_A1081 = new ThingTypeUID(BINDING_ID, "a1081");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_D101, THING_TYPE_D210X, THING_TYPE_A1081).collect(Collectors.toSet());

    // List of all Channel IDs
    public static final String CHANNEL_DOORBELL = "doorbell";
    public static final String CHANNEL_DOORBELL_TIMESTAMP = "doorbellTimestamp";
    public static final String CHANNEL_DOORBELL_IMAGE = "doorbellImage";
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_MOTION_TIMESTAMP = "motionTimestamp";
    public static final String CHANNEL_MOTION_IMAGE = "motionImage";
    public static final String CHANNEL_LIGHT = "light";
    public static final String CHANNEL_OPENDOOR1 = "openDoor1";
    public static final String CHANNEL_OPENDOOR2 = "openDoor2";
    public static final String CHANNEL_OPENDOOR3 = "openDoor3";
    public static final String CHANNEL_IMAGE = "image";
    public static final String CHANNEL_IMAGE_TIMESTAMP = "imageTimestamp";
    public static final String CHANNEL_DOORBELL_HISTORY_INDEX = "doorbellHistoryIndex";
    public static final String CHANNEL_DOORBELL_HISTORY_IMAGE = "doorbellHistoryImage";
    public static final String CHANNEL_DOORBELL_HISTORY_TIMESTAMP = "doorbellHistoryTimestamp";
    public static final String CHANNEL_MOTION_HISTORY_INDEX = "motionHistoryIndex";
    public static final String CHANNEL_MOTION_HISTORY_IMAGE = "motionHistoryImage";
    public static final String CHANNEL_MOTION_HISTORY_TIMESTAMP = "motionHistoryTimestamp";
    public static final String CHANNEL_DOORBELL_IMAGE_MONTAGE = "doorbellMontage";
    public static final String CHANNEL_MOTION_IMAGE_MONTAGE = "motionMontage";
}
