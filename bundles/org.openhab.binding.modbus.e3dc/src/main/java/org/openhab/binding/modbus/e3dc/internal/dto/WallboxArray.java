/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.e3dc.internal.modbus.Data;

/**
 * The {@link WallboxArray} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class WallboxArray implements Data {
    private byte[] wbArray;

    /**
     * For decoding see Modbus Register Mapping Chapter 3.1.2 page 15
     * The Registers for Wallbox Control are declared as uint16 but shall be handled as Bit registers => see chapter
     * 3.1.5 page 19
     *
     * @param bArray - Modbus Registers as bytes from 40088 to 40095
     */
    public WallboxArray(byte[] bArray) {
        wbArray = bArray;
    }

    /**
     * Return the 2 bytes according to the Wallbox ID.
     *
     * @param id Wallbox ID valid from 0 - 7
     * @return WallboxBlock initialized with the Modbus registers from the given ID
     */
    public Optional<WallboxBlock> getWallboxBlock(int id) {
        if (id >= 0 && id < 8) {
            int byteIndex = id * 2;
            return Optional.of(new WallboxBlock(new byte[] { wbArray[byteIndex + 1], wbArray[byteIndex * 2] }));
        } else {
            return Optional.empty();
        }
    }
}
