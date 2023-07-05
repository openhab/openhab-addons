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
package org.openhab.binding.toyota.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ToyotaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ToyotaBindingConstants {
    private static final String BINDING_ID = "toyota";

    // List of all Thing Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "mytapi");
    public static final ThingTypeUID VEHICLE_THING_TYPE = new ThingTypeUID(BINDING_ID, "vehicle");

    // List of channel groups
    public static final String GROUP_DOORS = "doors";
    public static final String GROUP_LAMPS = "lamps";
    public static final String GROUP_WINDOWS = "windows";
    public static final String GROUP_LOCKS = "locks";
    public static final String GROUP_KEY = "key";
    public static final String GROUP_POSITION = "position";

    // List of channel ids
    public static final String DRIVER = "driver";
    public static final String PASSENGER = "passenger";
    public static final String REAR_RIGHT = "rear-right";
    public static final String REAR_LEFT = "rear-left";
    public static final String TAILGATE = "tailgate";
    public static final String HOOD = "hood";

    public static final String HEAD = "head";
    public static final String TAIL = "tail";
    public static final String HAZARD = "hazard";

    public static final String STATUS = "status";
    public static final String SOURCE = "source";

    public static final String IN_CAR = "in-car";
    public static final String WARNING = "warning";

    public static final String LOCATION = "location";
    public static final String TIMESTAMP = "timestamp";

    // List of all adressable things
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(APIBRIDGE_THING_TYPE, VEHICLE_THING_TYPE);
}
