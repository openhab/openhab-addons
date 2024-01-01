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
package org.openhab.binding.bluetooth.grundfosalpha.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GrundfosAlphaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
public class GrundfosAlphaBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MI401 = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID, "mi401");

    // List of all Channel ids
    public static final String CHANNEL_TYPE_FLOW_RATE = "flow-rate";
    public static final String CHANNEL_TYPE_PUMP_HEAD = "pump-head";
    public static final String CHANNEL_TYPE_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_TYPE_PUMP_TEMPERATUR = "pump-temperature";
}
