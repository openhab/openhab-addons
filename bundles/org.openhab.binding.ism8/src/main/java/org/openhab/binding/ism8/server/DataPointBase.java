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
package org.openhab.binding.ism8.server;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataPointBase} is the base class for all data points
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public abstract class DataPointBase<T> implements IDataPoint {

    private final Logger logger = LoggerFactory.getLogger(DataPointBase.class);

    private int __Id;
    private String __KnxDataType = new String();
    private String __Description = new String();
    private T __Value;
    private String __Unit = new String();

    protected DataPointBase(int id, String knxDataType, String description) throws Exception {
        this.__Id = id;
        this.__KnxDataType = knxDataType;
        this.__Description = description;
    }

    @Override
    public int getId() {
        return __Id;
    }

    @Override
    public String getKnxDataType() {
        return __KnxDataType;
    }

    @Override
    public String getDescription() {
        return __Description;
    }

    public T getValue() {
        return __Value;
    }

    @Override
    public Object getValueObject() {
        return __Value;
    }

    public void setValue(T value) {
        __Value = value;
    }

    @Override
    public abstract String getValueText() throws Exception;

    @Override
    public String getUnit() {
        return __Unit;
    }

    public void setUnit(String value) {
        __Unit = value;
    }

    @Override
    public abstract void processData(byte[] data) throws Exception;

    @Override
    public byte[] createWriteData(Object value) throws Exception {
        logger.debug("Convert into byte array '{}'", value);
        byte[] val = this.convertWriteValue(value);
        byte length = (byte) (val.length + 20);
        ByteBuffer list = ByteBuffer.allocate(length);
        try {
            list.put(KnxNetFrame.KnxHeader);
            list.put(KnxNetFrame.ConnectionHeader);
            list.put((byte) 0xF0); // Main Service
            list.put(SubServiceType.DatapointValueWrite); // Sub Service
            byte low = (byte) (this.getId() & 0xFF);
            byte high = (byte) ((this.getId() & 0xFF) / 256);
            list.put(high);
            list.put(low); // Start DataPoint
            list.put((byte) 0x00); // Amount DataPoints (high-byte)
            list.put((byte) 0x01); // Amount DataPoints (low-byte)
            list.put(high);
            list.put(low); // Write: ID of DataPoint
            list.put((byte) 0x00); // State
            list.put((byte) val.length); // Length of Data
            list.put(val); // Data Value
            list.put(5, length);
        } catch (Exception err) {
            logger.error("DataPoint-CreateWriteData: Error converting value ({}) of ID {}. {}", value, this.getId(),
                    err.getMessage());
            list.clear();
        }

        return list.array();
    }

    @Override
    public String toString() {
        return String.format("DataPoint {}={}", this.getId(), this.getValue());
    }

    protected abstract byte[] convertWriteValue(Object value) throws Exception;

    protected boolean checkProcessData(byte[] data) throws Exception {
        if (data.length < 4) {
            logger.error("DataPoint-ProcessData: Data size too small ({}).", data.length);
            return false;
        }

        int dataPointId = Byte.toUnsignedInt(data[0]) * 256 + Byte.toUnsignedInt(data[1]);
        if (dataPointId != this.getId()) {
            logger.error("DataPoint-ProcessData: Data contains the wrong ID ({}/{}).", dataPointId, this.getId());
            return false;
        }

        int length = data[3];
        int expectedLength = length + 4;
        if (length <= 0 && expectedLength != data.length) {
            logger.error("DataPoint-ProcessData: Data size wrong ({}/{}).", data.length, expectedLength);
            return false;
        }

        return true;
    }
}