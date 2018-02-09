/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smaenergymeter.handler;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The {@link FieldDTO} class holds the data for a single field (i.e. the power purchased).
 *
 * @author Osman Basha - Initial contribution
 */
public class FieldDTO {

    private final int address;
    private final int length;
    private final int divisor;
    private float value;

    public FieldDTO(int address, int length, int divisor) {
        this.address = address;
        if ((length != 4) && (length != 8)) {
            throw new IllegalArgumentException("length should be 4 or 8 bytes");
        }
        this.length = length;
        this.divisor = divisor;
    }

    public float getValue() {
        return value;
    }

    public void updateValue(byte[] bytes) {
        if (length == 4) {
            value = (float) bytesToUInt16(Arrays.copyOfRange(bytes, address, address + 4)) / divisor;
        } else {
            value = (float) bytesToUInt32(Arrays.copyOfRange(bytes, address, address + 8)) / divisor;
        }
    }

    private int bytesToUInt16(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }

    private long bytesToUInt32(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getLong();
    }

}
