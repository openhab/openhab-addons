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
package org.openhab.binding.freeathome.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FreeAtHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andras Uhrin - Initial contribution
 */
@NonNullByDefault
public class FreeAtHomeBindingConstants {

    public static final String CONFIG_DESCRIPTION_URI_THING_PREFIX = "thing-type";

    public static final String BINDING_ID = "freeathome";

    // List of all Thing Type UIDs
    public static final String BRIDGE_TYPE_ID = "gateway";
    public static final String DEVICE_TYPE_ID = "device";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_UID = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_ID);
    public static final ThingTypeUID DEVICE_TYPE_UID = new ThingTypeUID(BINDING_ID, DEVICE_TYPE_ID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_TYPE_UID, DEVICE_TYPE_UID);
}
