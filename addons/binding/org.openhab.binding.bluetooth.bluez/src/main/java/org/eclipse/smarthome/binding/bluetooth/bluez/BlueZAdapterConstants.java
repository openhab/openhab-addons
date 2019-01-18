/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bluetooth.bluez;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
    public static final String PROPERTY_DISCOVERY = "discovery";

}
