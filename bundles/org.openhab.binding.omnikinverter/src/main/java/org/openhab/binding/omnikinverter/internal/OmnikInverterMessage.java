/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.omnikinverter.internal;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Hans van den Bogert - Initial contribution
 *
 */
@NonNullByDefault
public class OmnikInverterMessage {

    final private byte[] bytes;

    public OmnikInverterMessage(byte[] b) {
        this.bytes = b;
    }

    public double getPower() {
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.put(bytes, 59, 2);
        buf.rewind();
        return buf.getShort();
    }

    /**
     *
     * @return the total energy outputted this day in kWh
     */
    public double getEnergyToday() {
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.put(bytes, 69, 2);
        buf.rewind();
        return (buf.getShort() / 100.0);
    }

    /**
     *
     * @return the total energy outputted in kWh
     */
    public double getTotalEnergy() {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.put(bytes, 71, 4);
        buf.rewind();
        return buf.getInt() / 10.0;
    }
}
