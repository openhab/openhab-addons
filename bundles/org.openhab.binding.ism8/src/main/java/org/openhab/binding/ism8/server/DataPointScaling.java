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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataPointScaling} is the data points for scaling values
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class DataPointScaling extends DataPointBase<@Nullable Double> {
    private final Logger logger = LoggerFactory.getLogger(DataPointScaling.class);
    private String outputFormat = "";

    public DataPointScaling(int id, String knxDataType, String description) {
        super(id, knxDataType, description);
        this.setUnit(Units.PERCENT);
        this.outputFormat = "%.1f";
    }

    @Override
    public String getValueText() {
        return String.format(this.outputFormat, this.getValue());
    }

    @Override
    public void processData(byte[] data) {
        if (this.checkProcessData(data)) {
            if (data[3] != 1 && data.length <= 4) {
                logger.debug("DataPoint-ProcessData: Data size wrong for this type({}/1).", data[3]);
                return;
            }

            this.setValue((Byte.toUnsignedInt(data[4]) * 100.0) / 255.0);
        }
    }

    @Override
    protected byte[] convertWriteValue(Object value) {
        this.setValue(Math.max(Math.min(Double.parseDouble(value.toString()), 100), 0));
        Object rawVal = this.getValue();
        double rawValResult = rawVal != null ? (Double) rawVal : 0.0;
        byte val = (byte) (rawValResult / 100.0 * 255.0);
        return new byte[] { val };
    }
}
