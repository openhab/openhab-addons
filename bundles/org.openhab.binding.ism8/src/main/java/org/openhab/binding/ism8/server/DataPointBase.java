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
package org.openhab.binding.ism8.server;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataPointBase} is the base class for all data points
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public abstract class DataPointBase<@Nullable T> implements IDataPoint {
    private final Logger logger = LoggerFactory.getLogger(DataPointBase.class);

    private final int id;
    private final String knxDataType;
    private final String description;
    private T value;
    private String unit = "";

    protected DataPointBase(int id, String knxDataType, String description) {
        this.id = id;
        this.knxDataType = knxDataType;
        this.description = description;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKnxDataType() {
        return this.knxDataType;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the value of the data-point
     *
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the value of the data-point
     *
     */
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    @Nullable
    public Object getValueObject() {
        return this.value;
    }

    @Override
    public abstract String getValueText();

    @Override
    public String getUnit() {
        return this.unit;
    }

    /**
     * Sets the unit of the data-point.
     *
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    @Override
    public abstract void processData(byte[] data);

    @Override
    public byte[] createWriteData(Object value) {
        logger.debug("Convert into byte array '{}'", value);
        byte[] val = this.convertWriteValue(value);
        byte length = (byte) (val.length + 20);
        ByteBuffer list = ByteBuffer.allocate(length);
        list.put(KnxNetFrame.KNX_HEADER);
        list.put(KnxNetFrame.CONNECTION_HEADER);
        list.put((byte) 0xF0); // Main Service
        list.put(SubServiceType.DATAPOINT_VALUE_WRITE); // Sub Service
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
        return list.array();
    }

    @Override
    public String toString() {
        return String.format("DataPoint %d=%s", this.getId(), this.getValueText());
    }

    /**
     * Converts the value to be written into a data array of bytes.
     *
     */
    protected abstract byte[] convertWriteValue(Object value);

    /**
     * Checks the data to be processed.
     *
     */
    protected boolean checkProcessData(byte[] data) {
        if (data.length < 4) {
            logger.debug("DataPoint-ProcessData: Data size too small ({}).", data.length);
            return false;
        }

        int dataPointId = Byte.toUnsignedInt(data[0]) * 256 + Byte.toUnsignedInt(data[1]);
        if (dataPointId != this.getId()) {
            logger.debug("DataPoint-ProcessData: Data contains the wrong ID ({}/{}).", dataPointId, this.getId());
            return false;
        }

        int length = data[3];
        int expectedLength = length + 4;
        if (length <= 0 && expectedLength != data.length) {
            logger.debug("DataPoint-ProcessData: Data size wrong ({}/{}).", data.length, expectedLength);
            return false;
        }
        return true;
    }
}
