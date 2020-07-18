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

import org.openhab.binding.e3dc.internal.modbus.Data;

/**
 * The {@link WallboxArray} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
public class WallboxArray implements Data {
    private byte[] wbArray;

    public WallboxArray(byte[] bArray) {
        wbArray = bArray;
    }

    public WallboxBlock getWallboxBlock(int id) {
        if (id >= 0 && id < 8) {
            return new WallboxBlock(new byte[] { wbArray[id + 1], wbArray[id] });
        } else {
            return null;
        }
    }
}
