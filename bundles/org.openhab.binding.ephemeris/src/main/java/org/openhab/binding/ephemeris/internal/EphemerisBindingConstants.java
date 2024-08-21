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
package org.openhab.binding.ephemeris.internal;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EphemerisBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class EphemerisBindingConstants {

    public static final String BINDING_ID = "ephemeris";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CUSTOM = new ThingTypeUID(BINDING_ID, "custom");
    public static final ThingTypeUID THING_TYPE_HOLIDAY = new ThingTypeUID(BINDING_ID, "holiday");
    public static final ThingTypeUID THING_TYPE_DAYSET = new ThingTypeUID(BINDING_ID, "dayset");
    public static final ThingTypeUID THING_TYPE_WEEKEND = new ThingTypeUID(BINDING_ID, "weekend");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_EVENT = "title-today";
    public static final String CHANNEL_NEXT_EVENT = "next-title";
    public static final String CHANNEL_NEXT_START = "next-start";
    public static final String CHANNEL_NEXT_REMAINING = "days-remaining";
    public static final String CHANNEL_TODAY = "today";
    public static final String CHANNEL_TOMORROW = "tomorrow";
    public static final String CHANNEL_HOLIDAY_TODAY = "holiday-today";
    public static final String CHANNEL_HOLIDAY_TOMORROW = "holiday-tomorrow";
    public static final String CHANNEL_EVENT_TODAY = "event-today";
    public static final String CHANNEL_EVENT_TOMORROW = "event-tomorrow";

    // Folder for xml storage eg: /etc/openhab/misc/ephemeris
    public static final String BINDING_DATA_PATH = "%s%smisc%s%s".formatted(OpenHAB.getConfigFolder(), File.separator,
            File.separator, BINDING_ID);
}
