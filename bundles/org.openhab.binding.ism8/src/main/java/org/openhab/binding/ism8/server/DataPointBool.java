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
package org.openhab.binding.ism8.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DataPointBool} is the data points for boolean values
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
public class DataPointBool extends DataPointBase<Boolean> {
    private final Logger logger = LoggerFactory.getLogger(DataPointBool.class);

    public DataPointBool(int id, String knxDataType, String description) {
        super(id, knxDataType, description);
    }

    @Override
    public String getValueText() {
        return this.getValue() ? "True" : "False";
    }

    @Override
    public Object getValueObject() {
        return this.getValue() ? "1" : "0";
    }

    @Override
    public void processData(byte[] data) {
        if (this.checkProcessData(data)) {
            if (data[3] != 1 && data.length <= 4) {
                logger.warn("DataPoint-ProcessData: Data size wrong for this type({}/1).", data[3]);
                return;
            }
            this.setValue((data[4] & 0x1) > 0);
        }
    }

    @Override
    protected byte[] convertWriteValue(Object value) {
        String valueText = value.toString().toLowerCase();
        if (valueText.equalsIgnoreCase("true") || valueText.equalsIgnoreCase("1")) {
            this.setValue(true);
            return new byte[] { 0x01 };
        }
        this.setValue(false);
        return new byte[] { 0x00 };
    }
}
