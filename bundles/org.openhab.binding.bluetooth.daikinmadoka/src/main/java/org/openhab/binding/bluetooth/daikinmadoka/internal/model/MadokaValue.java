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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model;

import java.nio.ByteBuffer;

/**
 *
 * @author blafois
 *
 */
public class MadokaValue {

    private int id;
    private int size;
    private byte[] rawValue;

    public MadokaValue(int id, int size, byte[] rawValue) {
        this.id = id;
        this.size = size;
        this.rawValue = rawValue;
    }

    public MadokaValue() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getRawValue() {
        return rawValue;
    }

    public void setRawValue(byte[] rawValue) {
        this.rawValue = rawValue;
    }

    public long getComputedValue() {
        switch (size) {
            case 1:
                return rawValue[0];
            case 2:
                return ByteBuffer.wrap(rawValue, 0, 2).getShort();
            case 4:
                return ByteBuffer.wrap(rawValue, 0, 4).getInt();
            default:
                // unsupported
                break;
        }
        return 0;
    }
}
