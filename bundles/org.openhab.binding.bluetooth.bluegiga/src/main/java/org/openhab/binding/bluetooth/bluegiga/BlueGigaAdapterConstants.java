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
package org.openhab.binding.bluetooth.bluegiga;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;

/**
 * The {@link BlueGigaAdapterConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class BlueGigaAdapterConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BLUEGIGA = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "bluegiga");

    public static final String CONFIGURATION_PORT = "port";
    public static final String PROPERTY_LINKLAYER = "linklayer";
    public static final String PROPERTY_PROTOCOL = "protocol";
    public static final String PROPERTY_DISCOVERY = "discovery";
}
