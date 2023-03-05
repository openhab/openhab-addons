/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EvohomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Neil Renaud - Heating Zones
 */
@NonNullByDefault
public class EvohomeBindingConstants {

    private static final String BINDING_ID = "evohome";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_EVOHOME_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_EVOHOME_DISPLAY = new ThingTypeUID(BINDING_ID, "display");
    public static final ThingTypeUID THING_TYPE_EVOHOME_HEATING_ZONE = new ThingTypeUID(BINDING_ID, "heatingzone");

    // List of all Channel IDs
    public static final String DISPLAY_SYSTEM_MODE_CHANNEL = "SystemMode";
    public static final String ZONE_TEMPERATURE_CHANNEL = "Temperature";
    public static final String ZONE_SET_POINT_STATUS_CHANNEL = "SetPointStatus";
    public static final String ZONE_SET_POINT_CHANNEL = "SetPoint";

    // List of Discovery properties
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";

    // List of all addressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_EVOHOME_ACCOUNT, THING_TYPE_EVOHOME_DISPLAY, THING_TYPE_EVOHOME_HEATING_ZONE)
                    .collect(Collectors.toSet()));
}
