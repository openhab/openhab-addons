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
package org.openhab.binding.modbus.e3dc.internal.dto;

import static org.openhab.binding.modbus.e3dc.internal.modbus.E3DCModbusConstans.*;

import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * The {@link EmergencyBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class EmergencyBlock implements Data {
    public StringType epStatus = EP_UNKOWN;
    public OnOffType batteryChargingLocked = OnOffType.OFF;
    public OnOffType batteryDischargingLocked = OnOffType.OFF;
    public OnOffType epPossible = OnOffType.OFF;
    public OnOffType weatherPredictedCharging = OnOffType.OFF;
    public OnOffType regulationStatus = OnOffType.OFF;
    public OnOffType chargeLockTime = OnOffType.OFF;
    public OnOffType dischargeLockTime = OnOffType.OFF;

    // Possible Status definitions according to chapter 3.1.2, Register 40084, page 14 & 15
    public static final StringType EP_NOT_SUPPORTED = StringType.valueOf("EP not supported");
    public static final StringType EP_ACTIVE = StringType.valueOf("EP active");
    public static final StringType EP_NOT_ACTIVE = StringType.valueOf("EP not active");
    public static final StringType EP_POSSIBLE = StringType.valueOf("EP possible");
    public static final StringType EP_SWITCH = StringType.valueOf("EP Switch in wrong position");
    public static final StringType EP_UNKOWN = StringType.valueOf("EP Status unknown");
    public static final StringType[] EP_STATUS_ARRAY = new StringType[] { EP_NOT_SUPPORTED, EP_ACTIVE, EP_NOT_ACTIVE,
            EP_POSSIBLE, EP_SWITCH };

    /**
     * For decoding see Modbus Register Mapping Chapter 3.1.2 page 14 &amp; 15
     *
     * @param bArray - Modbus Registers as bytes from 40084 to 40085
     */
    public EmergencyBlock(byte[] bArray) {
        // uint16 status register 40084 - possible Status Strings are defined in Constants above
        int status = ModbusBitUtilities.extractUInt16(bArray, 0);
        if (status >= 0 && status < 5) {
            epStatus = EP_STATUS_ARRAY[status];
        } else {
            epStatus = EP_UNKOWN;
        }

        // uint16 status register 40085 shall be handled as Bits - check cahpter 3.1.3 page 17
        byte[] emsStatusBytes = new byte[] { bArray[3], bArray[2] };
        BitSet bs = BitSet.valueOf(emsStatusBytes);
        batteryChargingLocked = OnOffType.from(bs.get(EMS_CHARGING_LOCK_BIT));
        batteryDischargingLocked = OnOffType.from(bs.get(EMS_DISCHARGING_LOCK_BIT));
        epPossible = OnOffType.from(bs.get(EMS_AVAILABLE_BIT));
        weatherPredictedCharging = OnOffType.from(bs.get(EMS_WEATHER_CHARGING_BIT));
        regulationStatus = OnOffType.from(bs.get(EMS_REGULATION_BIT));
        chargeLockTime = OnOffType.from(bs.get(EMS_CHARGE_LOCKTIME_BIT));
        dischargeLockTime = OnOffType.from(bs.get(EMS_DISCHARGE_LOCKTIME_BIT));
    }
}
