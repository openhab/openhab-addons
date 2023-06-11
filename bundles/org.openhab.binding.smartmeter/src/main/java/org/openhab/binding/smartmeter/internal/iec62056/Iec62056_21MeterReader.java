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
package org.openhab.binding.smartmeter.internal.iec62056;

import java.util.function.Supplier;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.connectors.IMeterReaderConnector;
import org.openhab.binding.smartmeter.internal.MeterDevice;
import org.openhab.binding.smartmeter.internal.MeterValue;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openmuc.j62056.DataMessage;
import org.openmuc.j62056.DataSet;

/**
 * Reads meter values from an IEC 62056-21 compatible device with mode A,B,C or D.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class Iec62056_21MeterReader extends MeterDevice<DataMessage> {

    public Iec62056_21MeterReader(Supplier<SerialPortManager> serialPortManagerSupplier, String deviceId,
            String serialPort, byte @Nullable [] initMessage, int baudrate, int baudrateChangeDelay,
            ProtocolMode protocolMode) {
        super(serialPortManagerSupplier, deviceId, serialPort, initMessage, baudrate, baudrateChangeDelay,
                protocolMode);
    }

    @Override
    protected IMeterReaderConnector<DataMessage> createConnector(Supplier<SerialPortManager> serialPortManagerSupplier,
            String serialPort, int baudrate, int baudrateChangeDelay, ProtocolMode protocolMode) {
        return new Iec62056_21SerialConnector(serialPortManagerSupplier, serialPort, baudrate, baudrateChangeDelay,
                protocolMode);
    }

    @Override
    protected <Q extends @NonNull Quantity<Q>> void populateValueCache(DataMessage smlFile) {
        for (DataSet dataSet : smlFile.getDataSets()) {
            String address = dataSet.getAddress();
            if (address != null && !address.isEmpty()) {
                addObisCache(new MeterValue<Q>(address, dataSet.getValue(),
                        Iec62056_21UnitConversion.getUnit(dataSet.getUnit())));
            }
        }
    }
}
