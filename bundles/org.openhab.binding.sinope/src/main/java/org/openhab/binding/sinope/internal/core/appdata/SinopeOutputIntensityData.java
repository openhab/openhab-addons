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
 * The Class SinopeOutputIntensityData.
 *
 * @author Christos Karras - Initial contribution
 */
@NonNullByDefault
public class SinopeOutputIntensityData extends SinopeAppData {

    /**
     * Instantiates a new sinope set point temp data.
     */
    public SinopeOutputIntensityData() {

        super(new byte[] { 0x00, 0x00, 0x10, 0x00 }, new byte[] { 0, 0 });
    }

    /**
     * Gets the dimmer output intensity.
     *
     * @return the dimmer outut intensity (0-100)
     */
    public int getOutputIntensity() {
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
            sb.append(String.format("\nOutput intensity %d %%", this.getOutputIntensity()));
        }
        return sb.toString();
    }

    public void setOutputIntensity(int newOutputIntensity) {
        ByteBuffer bb = ByteBuffer.wrap(getData());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put((byte) (newOutputIntensity & 0xFF));
    }
}
