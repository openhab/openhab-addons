/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataPointLongValue} is the data points for long values
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class DataPointLongValue extends DataPointBase<@Nullable Double> {
    private final Logger logger = LoggerFactory.getLogger(DataPointLongValue.class);
    private final float factor;
    private final String outputFormat;

    public DataPointLongValue(int id, String knxDataType, String description) {
        super(id, knxDataType, description);

        if ("13.002".equals(knxDataType)) {
            this.setUnit(Units.CUBICMETRE_PER_HOUR);
            this.factor = 0.0001f;
            this.outputFormat = "%.1f";
        } else if ("13.010".equals(knxDataType)) {
            this.setUnit(Units.WATT_HOUR);
            this.factor = 1.0f;
            this.outputFormat = "%.1f";
        } else if ("13.013".equals(knxDataType)) {
            this.setUnit(Units.WATT_HOUR);
            this.factor = 1000.0f;
            this.outputFormat = "%.1f";
        } else {
            this.setUnit(Units.ONE);
            this.factor = 1.0f;
            this.outputFormat = "%.1f";
        }
    }

    @Override
    public String getValueText() {
        return String.format(this.outputFormat, this.getValue());
    }

    @Override
    public void processData(byte[] data) {
        if (this.checkProcessData(data)) {
            if (data[3] != 4 && data.length <= 7) {
                logger.debug("DataPoint-ProcessData: Data size wrong for this type({}/4).", data[3]);
                return;
            }

            int rawValue = Byte.toUnsignedInt(data[4]) * 0x1000000 + Byte.toUnsignedInt(data[5]) * 0x10000
                    + Byte.toUnsignedInt(data[6]) * 0x100 + Byte.toUnsignedInt(data[7]);
            this.setValue((double) rawValue * this.factor);
        }
    }

    @Override
    protected byte[] convertWriteValue(Object value) {
        ByteBuffer data = ByteBuffer.allocate(4);
        double dblVal;
        try {
            dblVal = Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            dblVal = 0.0;
        }

        int val = (int) (dblVal / this.factor);
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
