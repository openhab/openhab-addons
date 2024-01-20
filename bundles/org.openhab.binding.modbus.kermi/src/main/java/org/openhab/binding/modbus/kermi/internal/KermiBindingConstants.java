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
package org.openhab.binding.modbus.kermi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link KermiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Neuhaus - Initial contribution
 */
@NonNullByDefault
public class KermiBindingConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // Supported Thing Types
    public static final ThingTypeUID THING_TYPE_KERMI_XCENTER = new ThingTypeUID(BINDING_ID, "kermi-xcenter");

    // Channels for State Block
    public static final String GLOBAL_STATE_CHANNEL = "global-state";
    public static final String GLOBAL_STATE_ID_CHANNEL = "global-state-id";
}
