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
package org.openhab.binding.bluetooth.bluez.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BlueZAdapterConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@NonNullByDefault
public class BlueZAdapterConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BLUEZ = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID, "bluez");

    // Properties
    public static final String PROPERTY_ADDRESS = "address";

    private BlueZAdapterConstants() {
    }
}
