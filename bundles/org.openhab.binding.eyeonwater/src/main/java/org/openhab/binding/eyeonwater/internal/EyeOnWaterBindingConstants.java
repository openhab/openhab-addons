/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.eyeonwater.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EyeOnWaterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
public class EyeOnWaterBindingConstants {

    public static final String BINDING_ID = "eyeonwater";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, "meter");

    // List of all Channel ids
    public static final String CHANNEL_READING = "reading";
    public static final String CHANNEL_LEAK_FLOW_RATE = "leak-flow-rate";
    public static final String CHANNEL_LEAK_ALERT = "leak-alert";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";
    public static final String CHANNEL_REVERSE_FLOW = "reverse-flow";
    public static final String CHANNEL_LAST_READ_TIME = "last-read-time";

    // List of all Configuration Parameter Names
    public static final String CONFIG_METER_UUID = "meterUuid";
    public static final String CONFIG_METER_ID = "meterId";
}
