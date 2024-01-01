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
package org.openhab.binding.modbus.sungrow.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ModbusSungrowBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class ModbusSungrowBindingConstants {

    /**
     * ThingType-ID for Inverter.
     */
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(ModbusBindingConstants.BINDING_ID,
            "sungrow-inverter");
}
