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
package org.openhab.binding.modbus.sbc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SBCBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class SBCBindingConstants {
    public static final ThingTypeUID THING_TYPE_ALD1_UNIDIRECTIONAL = new ThingTypeUID(
            ModbusBindingConstants.BINDING_ID, "ald1Unidirectional");
    public static final ThingTypeUID THING_TYPE_ALD1_BIDIRECTIONAL = new ThingTypeUID(ModbusBindingConstants.BINDING_ID,
            "ald1Bidirectional");
}
