/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link CULBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes Goehr - Initial contribution
 */
@NonNullByDefault
public class CULBindingConstants {

    private static final String MAX_BINDING_ID = "maxcul";
    public static final String BRIDGE_CUL_MORIZ = "cul_max_bridge";
    public static final String BRIDGE_CUN_MORIZ = "cun_max_bridge";

    // List of all Thing Type UIDs
    public static final ThingTypeUID CULMORIZBRIDGE_THING_TYPE = new ThingTypeUID(MAX_BINDING_ID, BRIDGE_CUL_MORIZ);
    public static final ThingTypeUID CUNMORIZBRIDGE_THING_TYPE = new ThingTypeUID(MAX_BINDING_ID, BRIDGE_CUN_MORIZ);

    // List of all Channel ids
    public static final String CHANNEL_CREDITS = "credit10ms";
    public static final String CHANNEL_LED = "led";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(CULMORIZBRIDGE_THING_TYPE, CUNMORIZBRIDGE_THING_TYPE).collect(Collectors.toSet()));
}
