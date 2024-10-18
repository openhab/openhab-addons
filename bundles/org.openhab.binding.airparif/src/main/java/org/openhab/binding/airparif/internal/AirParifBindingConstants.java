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
package org.openhab.binding.airparif.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AirParifBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirParifBindingConstants {
    public static final String BINDING_ID = "airparif";
    public static final String LOCAL = "local";

    // List of Bridge Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "api");

    // List of Things Type UIDs
    public static final ThingTypeUID LOCATION_THING_TYPE = new ThingTypeUID(BINDING_ID, "location");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(APIBRIDGE_THING_TYPE,
            LOCATION_THING_TYPE);
}
