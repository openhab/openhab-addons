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
package org.openhab.binding.ism8.server;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataPointValue} is the data points for double values
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class DataPointValue extends DataPointBase<@Nullable Double> {
    private final Logger logger = LoggerFactory.getLogger(DataPointValue.class);
    private float factor;
    private String outputFormat = "";

    public DataPointValue(int id, String knxDataType, String description) {
        super(id, knxDataType, description);
        this.factor = 0.0f;
        if ("9.001".equals(knxDataType)) {
            this.setUnit("°C");
            this.factor = 0.01f;
            this.outputFormat = "%.1f";
        } else if ("9.002".equals(knxDataType)) {
            this.setUnit("°K");
            this.factor = 0.01f;
            this.outputFormat = "%.1f";
        } else if ("9.006".equals(knxDataType)) {
            this.setUnit("Bar");
            this.factor = 0.0000001f;
            this.outputFormat = "%.2f";
        }
    }

    @Override
    public String getValueText() {
        return String.format(this.outputFormat, this.getValue());
    }

    @Override
    public void processData(byte[] data) {
        if (this.checkProcessData(data)) {
            if (data[3] != 2 && data.length <= 5) {
                logger.debug("DataPoint-ProcessData: Data size wrong for this type({}/2).", data[3]);
                return;
            }

            int rawValue = Byte.toUnsignedInt(data[4]) * 256 + Byte.toUnsignedInt(data[5]);
            boolean inverted = (rawValue & 0x8000) > 0;
            double exp = (rawValue & 0x7800) / 2048;
            rawValue = rawValue & 0x07FF;
            exp = Math.pow(2, exp);
            if (inverted) {
                rawValue = rawValue - 1;
                rawValue = rawValue ^ 0x7FF;
                this.setValue(rawValue * exp * this.factor * (-1.0));
            } else {
                this.setValue(rawValue * exp * this.factor);
            }
        }
    }

    @Override
    protected byte[] convertWriteValue(Object value) {
        ByteBuffer data = ByteBuffer.allocate(2);
        this.setValue(Double.parseDouble(value.toString()));
        Object rawVal = this.getValue();
        double rawValResult = rawVal != null ? (Double) rawVal : 0.0;
        double dblValue = rawValResult / this.factor;
        boolean inverted = dblValue < 0.0;
        int exp = 0;
        dblValue = Math.abs(dblValue);
        while (dblValue > 2047.0) {
            dblValue = dblValue / 2.0;
            exp++;
        }
        int val = (int) dblValue;
        if (inverted) {
            val = val ^ 0x7FF;
            val = val + 1;
            val |= 0x8000;
        }

        val |= exp * 2048;
        byte low = (byte) (val & 0xFF);
        val = (val & 0xFF00) / 256;
        byte high = (byte) (val & 0xFF);
        data.put(high);
        data.put(low);
        return data.array();
    }
}
