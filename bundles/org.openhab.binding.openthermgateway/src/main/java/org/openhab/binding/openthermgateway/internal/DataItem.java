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
package org.openhab.binding.openthermgateway.internal;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DataItem} holds the internal OpenTherm message and meta data.
 * 
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class DataItem {
    private int id;
    private Msg msg;
    private ByteType byteType;
    private DataType dataType;
    private int bitpos;
    private String subject;
    private @Nullable Unit<?> unit;

    public int getID() {
        return id;
    }

    public Msg getMsg() {
        return msg;
    }

    public ByteType getByteType() {
        return this.byteType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public int getBitPos() {
        return bitpos;
    }

    public String getSubject() {
        return subject;
    }

    public @Nullable Unit<?> getUnit() {
        return unit;
    }

    public DataItem(int id, Msg msg, ByteType byteType, DataType dataType, int bit, String subject) {
        this.id = id;
        this.msg = msg;
        this.byteType = byteType;
        this.dataType = dataType;
        this.bitpos = bit;
        this.subject = subject;
    }

    public DataItem(int id, Msg msg, ByteType byteType, DataType dataType, int bit, String subject, Unit<?> unit) {
        this(id, msg, byteType, dataType, bit, subject);
        this.unit = unit;
    }
}
