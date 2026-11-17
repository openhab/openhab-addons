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
package org.openhab.binding.pleos.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The PleosBindingConstants class defines all global IDs,
 * thing types, and constants for the Pleos Vehicle Data API binding.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosBindingConstants {

    // The unique ID of the binding
    public static final String BINDING_ID = "pleos";

    // ==================== THING TYPES ====================

    // The Account Bridge (Private User Authentication)
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "account");

    // The actual vehicle that receives the data
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    // Immutable set of all supported Thing types for the HandlerFactory
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_SET = Set.of(THING_TYPE_BRIDGE, THING_TYPE_VEHICLE);

    // ==================== ADDITIONAL CONSTANTS ====================

    // Account Bridge Channel IDs
    public static final String CHANNEL_SECRET_EXPIRES = "secret-expires";
    public static final String CHANNEL_SECRET_REMAINING_DAYS = "secret-remaining-days";
    public static final String CHANNEL_SECRET_EXPIRY_WARNING = "secret-expiry-warning";
    public static final String CHANNEL_SECRET_EXPIRY_EVENT = "secret-expiry-event";

    // Channel group IDs
    public static final String GROUP_VEHICLE = "vehicle";
    public static final String GROUP_DOORS = "doors";
    public static final String GROUP_TIRES = "tires";
    public static final String GROUP_CLIMATE = "climate";
    public static final String GROUP_SEATS = "seats";
    public static final String GROUP_WARNINGS = "warnings";
    public static final String GROUP_FREEZER = "freezer";
}
