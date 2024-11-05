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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataPointIntegerValue} is the data points for integer values
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class DataPointIntegerValue extends DataPointBase<@Nullable Integer> {
    private final Logger logger = LoggerFactory.getLogger(DataPointIntegerValue.class);

    public DataPointIntegerValue(int id, String knxDataType, String description) {
        super(id, knxDataType, description);
    }

    @Override
    public String getValueText() {
        Object val = this.getValue();
        return val != null ? val.toString() : "0";
    }

    @Override
    public void processData(byte[] data) {
        if (this.checkProcessData(data)) {
            if (data[3] != 2 && data.length <= 5) {
                logger.debug("DataPoint-ProcessData: Data size wrong for this type({}/1).", data[3]);
                return;
            }
            int rawValue = Byte.toUnsignedInt(data[4]) * 0x100 + Byte.toUnsignedInt(data[5]);
            this.setValue(rawValue);
        }
    }

    @Override
    protected byte[] convertWriteValue(Object value) {
        ByteBuffer data = ByteBuffer.allocate(2);
        int intVal;
        try {
            intVal = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            intVal = 0;
        }

        int val = intVal;
        data.put((byte) (val & 0xFF));
        val = (val & 0xFF) / 256;
        data.put((byte) (val & 0xFF));
        return data.array();
    }
}
