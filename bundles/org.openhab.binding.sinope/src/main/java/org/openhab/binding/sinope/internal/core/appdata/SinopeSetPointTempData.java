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
package org.openhab.binding.sinope.internal.core.appdata;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The Class SinopeRoomTempData.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeSetPointTempData extends SinopeAppData {

    /**
     * Instantiates a new sinope set point temp data.
     */
    public SinopeSetPointTempData() {
        super(new byte[] { 0x00, 0x00, 0x02, 0x08 }, new byte[] { 0, 0 });
    }

    /**
     * Gets the room temp.
     *
     * @return the room temp
     */
    public int getSetPointTemp() {
        if (getData() != null) {
            ByteBuffer bb = ByteBuffer.wrap(getData());
            bb.order(ByteOrder.LITTLE_ENDIAN);
            return bb.getShort();
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
            sb.append(String.format("\nSet point temperature is %2.2f C", this.getSetPointTemp() / 100.0));
        }
        return sb.toString();
    }

    public void setSetPointTemp(int newTemp) {
        ByteBuffer bb = ByteBuffer.wrap(getData());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) newTemp);
    }
}
