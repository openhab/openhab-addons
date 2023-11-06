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
 * The Class SinopeOutTempData.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeOutTempData extends SinopeAppData {

    /**
     * Instantiates a new sinope out temp data.
     */
    public SinopeOutTempData() {
        super(new byte[] { 0x00, 0x00, 0x02, 0x04 }, new byte[] { 0, 0 });
    }

    /**
     * Gets the out temp.
     *
     * @return the out temp
     */
    public int getOutTemp() {
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
            sb.append(String.format("\n\tOutside temperature is %2.2f C", this.getOutTemp() / 100.0));
        }
        return sb.toString();
    }
}
