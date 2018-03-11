/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal.iec62056;

import org.openhab.binding.meterreader.connectors.IMeterReaderConnector;
import org.openhab.binding.meterreader.internal.MeterDevice;
import org.openhab.binding.meterreader.internal.MeterValue;
import org.openhab.binding.meterreader.internal.helper.ProtocolMode;
import org.openmuc.j62056.DataMessage;
import org.openmuc.j62056.DataSet;

/**
 * Reads meter values from an IEC 62056-21 compatible device with mode A,B,C or D.
 *
 * @author MatthiasS
 *
 */
public class Iec62056_21MeterReader extends MeterDevice<DataMessage> {

    public Iec62056_21MeterReader(String deviceId, String serialPort, byte[] initMessage, int baudrate,
            int baudrateChangeDelay, ProtocolMode protocolMode) {
        super(deviceId, serialPort, initMessage, baudrate, baudrateChangeDelay, protocolMode);
    }

    @Override
    protected IMeterReaderConnector<DataMessage> createConnector(String serialPort, int baudrate,
            int baudrateChangeDelay, ProtocolMode protocolMode) {
        return new Iec62056_21SerialConnector(serialPort, baudrate, baudrateChangeDelay, protocolMode);
    }

    @Override
    protected void populateValueCache(DataMessage smlFile) {
        for (DataSet dataSet : smlFile.getDataSets()) {
            String address = dataSet.getAddress();
            if (address != null && !address.isEmpty()) {

                addObisCache(new MeterValue(address, dataSet.getValue(), dataSet.getUnit()));
            }
        }
    }

}
