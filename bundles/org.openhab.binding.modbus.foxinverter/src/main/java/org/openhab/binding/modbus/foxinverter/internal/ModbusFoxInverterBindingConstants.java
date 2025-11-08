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
package org.openhab.binding.modbus.foxinverter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ModbusFoxInverterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class ModbusFoxInverterBindingConstants {

    public static final String MODEL_NAME = "modelName";
    public static final String SERIAL_NO = "serialNumber";
    public static final String MANUFACTURER_ID = "manufacturerId";
    public static final String RATED_POWER = "ratedPower";
    public static final String MAX_ACTIVE_POWER = "maxActivePower";
    public static final String FIRMWARE_BMS = "firmwareBMS";
    public static final String FIRMWARE_WR = "firmwareWR";
    public static final String FIRMWARE_PV = "firmwarePV";

    /**
     * ThingType-ID for Inverter.
     */
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(ModbusBindingConstants.BINDING_ID,
            "solakon-one-inverter");
}
