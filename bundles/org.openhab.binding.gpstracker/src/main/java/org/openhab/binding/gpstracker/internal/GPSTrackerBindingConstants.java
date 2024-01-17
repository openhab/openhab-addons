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
package org.openhab.binding.gpstracker.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Binding constants
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public abstract class GPSTrackerBindingConstants {
    public static final String BINDING_ID = "gpstracker";
    static final String CONFIG_PID = "binding." + BINDING_ID;
    private static final String THING_TYPE = "tracker";
    public static final ThingTypeUID THING_TYPE_TRACKER = new ThingTypeUID(BINDING_ID, THING_TYPE);

    // channels
    public static final String CHANNEL_REGION_TRIGGER = "regionTrigger";
    public static final String CHANNEL_LAST_REPORT = "lastReport";
    public static final String CHANNEL_LAST_LOCATION = "lastLocation";
    public static final String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    private static final String CHANNEL_REGION_DISTANCE = "regionDistance";
    public static final String CHANNEL_GPS_ACCURACY = "gpsAccuracy";

    // system distance channel
    public static final String CHANNEL_DISTANCE_SYSTEM_ID = "distanceSystem";
    public static final String CHANNEL_DISTANCE_SYSTEM_NAME = "System";
    public static final Integer CHANNEL_DISTANCE_SYSTEM_RADIUS = 100;

    public static final ChannelTypeUID CHANNEL_TYPE_DISTANCE = new ChannelTypeUID(BINDING_ID, CHANNEL_REGION_DISTANCE);
    public static final ChannelTypeUID CHANNEL_TYPE_REGION = new ChannelTypeUID(BINDING_ID, CHANNEL_REGION_TRIGGER);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_TRACKER)
            .collect(Collectors.toSet());
}
