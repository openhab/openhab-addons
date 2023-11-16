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
package org.openhab.binding.opengarage.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OpenGarageBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class OpenGarageBindingConstants {

    private static final String BINDING_ID = "opengarage";

    // List of all Thing Type UIDs
    public static final ThingTypeUID OPENGARAGE_THING = new ThingTypeUID(BINDING_ID, "opengarage");

    // List of all Channel ids
    public static final String CHANNEL_OG_DISTANCE = "distance";
    public static final String CHANNEL_OG_STATUS = "status"; // now deprecated
    public static final String CHANNEL_OG_STATUS_SWITCH = "status-switch";
    public static final String CHANNEL_OG_STATUS_CONTACT = "status-contact";
    public static final String CHANNEL_OG_STATUS_ROLLERSHUTTER = "status-rollershutter";
    public static final String CHANNEL_OG_VEHICLE = "vehicle"; // now deprecated
    public static final String CHANNEL_OG_VEHICLE_STATUS = "vehicle-status";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(OPENGARAGE_THING);
}
