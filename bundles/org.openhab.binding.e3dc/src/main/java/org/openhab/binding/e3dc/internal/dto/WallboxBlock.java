/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.dto;

import static org.openhab.binding.e3dc.internal.modbus.E3DCModbusConstans.*;

import java.util.BitSet;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.e3dc.internal.modbus.Data;

/**
 * The {@link WallboxBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
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
    public OnOffType wbSchukoRealy16 = OnOffType.OFF;
    public OnOffType wbRealy16 = OnOffType.OFF;
    public OnOffType wbRelay32 = OnOffType.OFF;
    public OnOffType wb3phase = OnOffType.OFF;

    /**
     * For decoding see Modbus Register Mapping Chapter 3.1.2 page 15
     * The Registers for Wallbox Control are declared as uint16 but shall be handled as Bit registers => see chapter
     * 3.1.5 page 19
     *
     * @param bArray - one Modbus Registers according to Wallbox ID
     */
    public WallboxBlock(byte[] bArray) {
        bitSet = BitSet.valueOf(bArray);
        // logger.info("BitSet String {}", bs.toString());
        wbAvailable = bitSet.get(WB_AVAILABLE_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbSunmode = bitSet.get(WB_SUNMODE_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbChargingAborted = bitSet.get(WB_CHARGING_ABORTED_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbCharging = bitSet.get(WB_CHARGING_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbJackLocked = bitSet.get(WB_JACK_LOCKED_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbJackPlugged = bitSet.get(WB_JACK_PLUGGED_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbSchukoOn = bitSet.get(WB_SCHUKO_ON_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbSchukoPlugged = bitSet.get(WB_SCHUKO_PLUGGED_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbSchukoLocked = bitSet.get(WB_SCHUKO_LOCKED_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbSchukoRealy16 = bitSet.get(WB_SCHUKO_RELAY16A_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbRealy16 = bitSet.get(WB_RELAY_16A_BIT) ? OnOffType.ON : OnOffType.OFF;
        wbRelay32 = bitSet.get(WB_RELAY_32A_BIT) ? OnOffType.ON : OnOffType.OFF;
        wb3phase = bitSet.get(WB_3PHASE_BIT) ? OnOffType.ON : OnOffType.OFF;
    }

    public BitSet getBitSet() {
        return bitSet;
    }
}
