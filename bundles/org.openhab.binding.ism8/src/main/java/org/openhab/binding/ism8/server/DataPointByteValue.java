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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataPointByteValue} is the data points for byte values
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class DataPointByteValue extends DataPointBase<@Nullable Byte> {
    private final Logger logger = LoggerFactory.getLogger(DataPointByteValue.class);

    public DataPointByteValue(int id, String knxDataType, String description) {
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
            if (data[3] != 1 && data.length <= 4) {
                logger.debug("DataPoint-ProcessData: Data size wrong for this type({}/1).", data[3]);
                return;
            }
            this.setValue(data[4]);
        }
    }

    @Override
    protected byte[] convertWriteValue(Object value) {
        this.setValue(Byte.parseByte(value.toString()));
        return new byte[] { this.getValue() };
    }
}
