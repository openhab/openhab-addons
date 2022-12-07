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
package org.openhab.binding.sinope.internal.core.appdata;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The Class SinopeLightModeData.
 *
 * @author Christos Karras - Initial contribution
 */
@NonNullByDefault
public class SinopeLightModeData extends SinopeAppData {

    /**
     * Instantiates a new sinope set point temp data.
     */
    public SinopeLightModeData() {

        super(new byte[] { 0x00, 0x00, 0x10, 0x09 }, new byte[] { 0, 0 });
    }

    /**
     * Gets the light mode
     *
     * @return the light mode: 1 = Manual (Hold), 2 = Auto (Schedule), 3 = Random (simulation of presence), 130 = Bypass
     *         Auto (Temporary hold until next scheduled period)
     */
    public int getLightMode() {
        if (getData() != null) {
            ByteBuffer bb = ByteBuffer.wrap(getData());
            bb.order(ByteOrder.LITTLE_ENDIAN);
            return (int) (bb.get()) & 0xFF;
        }
        return 0;
    }

    /**
     * @see SinopeAppData#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        if (getData() != null) {
            sb.append(String.format("\nLight mode %d", this.getLightMode()));
        }
        return sb.toString();
    }

    public void setLightMode(int newLightMode) {
        ByteBuffer bb = ByteBuffer.wrap(getData());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put((byte) (newLightMode & 0xFF));
    }
}
