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
package org.openhab.binding.smaenergymeter.internal.handler;

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
