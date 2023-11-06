/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.core.library.types.OnOffType;

/**
 * The {@link WallboxBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class WallboxBlock implements Data {
    private BitSet bitSet;
    public OnOffType wbAvailable = OnOffType.OFF;
    public OnOffType wbSunmode = OnOffType.OFF;
    public OnOffType wbChargingAborted = OnOffType.OFF;
    public OnOffType wbCharging = OnOffType.OFF;
    public OnOffType wbJackLocked = OnOffType.OFF;
    public OnOffType wbJackPlugged = OnOffType.OFF;
    public OnOffType wbSchukoOn = OnOffType.OFF;
    public OnOffType wbSchukoPlugged = OnOffType.OFF;
    public OnOffType wbSchukoLocked = OnOffType.OFF;
    public OnOffType wbSchukoRelay16 = OnOffType.OFF;
    public OnOffType wbRelay16 = OnOffType.OFF;
    public OnOffType wbRelay32 = OnOffType.OFF;
    public OnOffType wb1phase = OnOffType.OFF;

    /**
     * For decoding see Modbus Register Mapping Chapter 3.1.2 page 15
     * The Registers for Wallbox Control are declared as uint16 but shall be handled as Bit registers => see chapter
     * 3.1.5 page 19
     *
     * @param bArray - one Modbus Registers according to Wallbox ID
     */
    public WallboxBlock(byte[] bArray) {
        bitSet = BitSet.valueOf(bArray);
        wbAvailable = OnOffType.from(bitSet.get(WB_AVAILABLE_BIT));
        wbSunmode = OnOffType.from(bitSet.get(WB_SUNMODE_BIT));
        wbChargingAborted = OnOffType.from(bitSet.get(WB_CHARGING_ABORTED_BIT));
        wbCharging = OnOffType.from(bitSet.get(WB_CHARGING_BIT));
        wbJackLocked = OnOffType.from(bitSet.get(WB_JACK_LOCKED_BIT));
        wbJackPlugged = OnOffType.from(bitSet.get(WB_JACK_PLUGGED_BIT));
        wbSchukoOn = OnOffType.from(bitSet.get(WB_SCHUKO_ON_BIT));
        wbSchukoPlugged = OnOffType.from(bitSet.get(WB_SCHUKO_PLUGGED_BIT));
        wbSchukoLocked = OnOffType.from(bitSet.get(WB_SCHUKO_LOCKED_BIT));
        wbSchukoRelay16 = OnOffType.from(bitSet.get(WB_SCHUKO_RELAY16A_BIT));
        wbRelay16 = OnOffType.from(bitSet.get(WB_RELAY_16A_BIT));
        wbRelay32 = OnOffType.from(bitSet.get(WB_RELAY_32A_BIT));
        wb1phase = OnOffType.from(bitSet.get(WB_1PHASE_BIT));
    }

    public BitSet getBitSet() {
        return bitSet;
    }
}
