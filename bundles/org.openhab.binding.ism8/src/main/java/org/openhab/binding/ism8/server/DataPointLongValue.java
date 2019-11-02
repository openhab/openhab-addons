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
 * The {@link DataPointLongValue} is the data points for long values
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class DataPointLongValue extends DataPointBase<Double> {
    private final Logger logger = LoggerFactory.getLogger(DataPointLongValue.class);
    private float pFactor;
    private String pOutputFormat = new String();

    public DataPointLongValue(int id, String knxDataType, String description) throws Exception {
        super(id, knxDataType, description);
        this.pFactor = 0.0f;

        this.setUnit("");
        this.pFactor = 1.0f;
        this.pOutputFormat = "%.1f";

        if (knxDataType.equals("13.002")) {
            this.setUnit("m³/h");
            this.pFactor = 0.0001f;
            this.pOutputFormat = "%.1f";
        }
    }

    @Override
    public String getValueText() throws Exception {
        return String.format(this.pOutputFormat, this.getValue());
    }

    @Override
    public void processData(byte[] data) throws Exception {
        if (this.checkProcessData(data)) {
            if (data[3] != 4 && data.length < 7) {
                logger.error("DataPoint-ProcessData: Data size wrong for this type({}/4).", data[3]);
                return;
            }

            int rawValue = Byte.toUnsignedInt(data[4]) * 0x1000000 + Byte.toUnsignedInt(data[5]) * 0x10000
                    + Byte.toUnsignedInt(data[6]) * 0x100 + Byte.toUnsignedInt(data[7]);
            this.setValue((double) rawValue * this.pFactor);
        }
    }

    @Override
    protected byte[] convertWriteValue(Object value) throws Exception {
        ByteBuffer data = ByteBuffer.allocate(4);
        int val = (int) (Double.parseDouble(value.toString()) / this.pFactor);
        data.put((byte) (val & 0xFF));
        val = (val & 0xFF) / 256;
        data.put((byte) (val & 0xFF));
        val = (val & 0xFF) / 256;
        data.put((byte) (val & 0xFF));
        val = (val & 0xFF) / 256;
        data.put((byte) (val & 0xFF));
        return data.array();
    }

}
