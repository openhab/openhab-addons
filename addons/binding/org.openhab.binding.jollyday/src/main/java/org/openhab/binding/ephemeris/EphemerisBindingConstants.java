/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ephemeris;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Defines common constants, which are used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class EphemerisBindingConstants {

    public static final String BINDING_ID = "ephemeris";
    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_HOLIDAY = new ThingTypeUID(BINDING_ID, "holiday");
    public static final ThingTypeUID THING_SOTD = new ThingTypeUID(BINDING_ID, "sotd");
    public static final ThingTypeUID THING_USERFILE = new ThingTypeUID(BINDING_ID, "userfile");

    // Holiday channels
    public static final String CHANNEL_EVENT_NAME = "eventName";
    public static final String CHANNEL_EVENT_DATE = "eventDate";
    public static final String CHANNEL_EVENT_OFFICIAL = "isOfficial";

    // List of supported things
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Stream
            .of(THING_HOLIDAY, THING_SOTD, THING_USERFILE).collect(Collectors.toSet());
}
