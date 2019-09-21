/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SurePetcareConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareConstants {

    public static final String BINDING_ID = "surepetcare";

    // List all Thing Type UIDs, related to the binding
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_HOUSEHOLD = new ThingTypeUID(BINDING_ID, "household");
    public static final ThingTypeUID THING_TYPE_PET = new ThingTypeUID(BINDING_ID, "pet");
    public static final ThingTypeUID THING_TYPE_HUB_DEVICE = new ThingTypeUID(BINDING_ID, "hubDevice");
    public static final ThingTypeUID THING_TYPE_FLAP_DEVICE = new ThingTypeUID(BINDING_ID, "flapDevice");

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_HOUSEHOLD, THING_TYPE_PET, THING_TYPE_HUB_DEVICE, THING_TYPE_FLAP_DEVICE));

    // Bridge configuration property names
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String REFRESH_INTERVAL_TOPOLOGY = "refresh_interval_topology";
    public static final String REFRESH_INTERVAL_LOCATION = "refresh_interval_location";

}
