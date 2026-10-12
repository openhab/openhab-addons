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
package org.openhab.binding.bluetooth.miflora.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MiFloraBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Lukas Brinias - Initial contribution
 */
@NonNullByDefault
public class MiFloraBindingConstants {

    public static final ThingTypeUID THING_TYPE_MIFLORA = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "miflora");

    // The name the sensor advertises in its scan response
    public static final String ADVERTISED_NAME = "Flower care";

    public static final String CHANNEL_ID_TEMPERATURE = "temperature";
    public static final String CHANNEL_ID_ILLUMINANCE = "illuminance";
    public static final String CHANNEL_ID_MOISTURE = "moisture";
    public static final String CHANNEL_ID_CONDUCTIVITY = "conductivity";
}
