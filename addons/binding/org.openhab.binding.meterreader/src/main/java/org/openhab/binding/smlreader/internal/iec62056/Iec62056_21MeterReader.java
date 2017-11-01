/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlreader.internal.iec62056;

import java.util.Arrays;

import org.openhab.binding.smlreader.connectors.IMeterReaderConnector;
import org.openhab.binding.smlreader.internal.MeterDevice;
import org.openhab.binding.smlreader.internal.MeterValue;
import org.openhab.binding.smlreader.internal.helper.ProtocolMode;
import org.openmuc.j62056.DataMessage;
import org.openmuc.j62056.DataSet;
import org.openmuc.jsml.EObis;
import org.openmuc.jsml.structures.OctetString;

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

                EObis obisEnum = Arrays.asList(EObis.values()).stream()
                        .filter((a) -> a.obisCode().equals(new OctetString(address))).findAny()
                        .orElseGet(() -> EObis.UNKNOWN);

                addObisCache(address, new MeterValue(obisEnum.name(), dataSet.getValue(), dataSet.getUnit()));
            }
        }
    }

}
