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
package org.openhab.binding.meteoblue.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openhab.core.thing.ThingTypeUID;

/**
 * Defines common constants.
 *
 * @author Chris Carman - Initial contribution
 */
public class MeteoBlueBindingConstants {

    private static final String BINDING_ID = "meteoblue";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WEATHER = new ThingTypeUID(BINDING_ID, "weather");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(Arrays.asList(THING_TYPE_WEATHER));

    // Bridge configuration settings
    public static final String APIKEY = "apiKey";
}
