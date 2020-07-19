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
    public OnOffType wbCharging = OnOffType.OFF;
    public OnOffType wbJackLocked = OnOffType.OFF;
    public OnOffType wbJackPlugged = OnOffType.OFF;
    public OnOffType wbSchukoOn = OnOffType.OFF;
    public OnOffType wbSchukoPlugged = OnOffType.OFF;
    public OnOffType wbSchukoLocked = OnOffType.OFF;
    public OnOffType wbRealy16 = OnOffType.OFF;
    public OnOffType wbRelay32 = OnOffType.OFF;
    public OnOffType wb3phase = OnOffType.OFF;

    public WallboxBlock(byte[] bArray) {
        // logger.info("Wallbox {} {}", bArray[0], bArray[1]);
        bitSet = BitSet.valueOf(bArray);
        // logger.info("BitSet String {}", bs.toString());
        wbAvailable = bitSet.get(0) ? OnOffType.ON : OnOffType.OFF;
        wbSunmode = bitSet.get(1) ? OnOffType.ON : OnOffType.OFF;
        wbCharging = bitSet.get(2) ? OnOffType.ON : OnOffType.OFF;
        wbJackLocked = bitSet.get(3) ? OnOffType.ON : OnOffType.OFF;
        wbJackPlugged = bitSet.get(4) ? OnOffType.ON : OnOffType.OFF;
        wbSchukoOn = bitSet.get(5) ? OnOffType.ON : OnOffType.OFF;
        wbSchukoPlugged = bitSet.get(6) ? OnOffType.ON : OnOffType.OFF;
        wbSchukoLocked = bitSet.get(7) ? OnOffType.ON : OnOffType.OFF;
        wbRealy16 = bitSet.get(8) ? OnOffType.ON : OnOffType.OFF;
        wbRelay32 = bitSet.get(9) ? OnOffType.ON : OnOffType.OFF;
        wb3phase = bitSet.get(10) ? OnOffType.ON : OnOffType.OFF;
    }

    public BitSet getBitSet() {
        return bitSet;
    }
}
