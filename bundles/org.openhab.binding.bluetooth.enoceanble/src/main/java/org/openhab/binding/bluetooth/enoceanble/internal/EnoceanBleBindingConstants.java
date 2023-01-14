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
package org.openhab.binding.bluetooth.enoceanble.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EnoceanBleBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Fink - Initial contribution
 */
@NonNullByDefault
public class EnoceanBleBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PTM215B = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "ptm215b");

    // Channel IDs
    public static final String CHANNEL_ID_ROCKER1 = "rocker1";
    public static final String CHANNEL_ID_ROCKER2 = "rocker2";
}
