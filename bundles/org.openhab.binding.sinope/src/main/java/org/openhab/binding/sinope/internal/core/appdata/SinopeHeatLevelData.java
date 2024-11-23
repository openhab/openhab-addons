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
package org.openhab.binding.sinope.internal.core.appdata;

/**
 * The Class SinopeRoomTempData.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeHeatLevelData extends SinopeAppData {

    /**
     * Instantiates a new sinope set point temp data.
     */
    public SinopeHeatLevelData() {
        super(new byte[] { 0x00, 0x00, 0x02, 0x20 }, new byte[] { 0 });
    }

    /**
     * Gets the room temp.
     *
     * @return the room temp
     */
    public int getHeatLevel() {
        if (getData() != null) {
            return getData()[0] & 0xFF;
        }
        return -273;
    }

    /**
     * @see org.openhab.binding.sinope.internal.core.appdata.SinopeAppData#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        if (getData() != null) {
            sb.append(String.format("\nHeat Level is %d %%", this.getHeatLevel()));
        }
        return sb.toString();
    }
}
