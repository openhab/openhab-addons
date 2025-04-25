/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.radoneye.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RadoneyeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Peter Obel - Initial contribution
 */
@NonNullByDefault
public class RadoneyeBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RADONEYE = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "radoneye_rd200");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_RADONEYE);

    // Channel IDs
    public static final String CHANNEL_ID_RADON = "radon";
    public static final String CHANNEL_ID_DECAY = "decay";
}
