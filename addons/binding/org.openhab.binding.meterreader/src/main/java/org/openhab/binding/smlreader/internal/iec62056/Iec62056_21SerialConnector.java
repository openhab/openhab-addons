/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlreader.internal.iec62056;

import java.io.IOException;

import org.openhab.binding.smlreader.connectors.IMeterReaderConnector;
import org.openhab.binding.smlreader.internal.helper.Baudrate;
import org.openhab.binding.smlreader.internal.helper.ProtocolMode;
import org.openmuc.j62056.DataMessage;
import org.openmuc.j62056.Iec21Port;
import org.openmuc.j62056.Iec21Port.Builder;
import org.openmuc.j62056.ModeDListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MatthiasS
 *
 */
public class Iec62056_21SerialConnector implements IMeterReaderConnector<DataMessage> {

    private final static Logger logger = LoggerFactory.getLogger(Iec62056_21SerialConnector.class);
    private String portName;
    private int baudrate;
    private int baudrateChangeDelay;
    private ProtocolMode protocolMode;

    public Iec62056_21SerialConnector(String portName, int baudrate, int baudrateChangeDelay,
            ProtocolMode protocolMode) {
        this.portName = portName;
        this.baudrate = baudrate;
        this.baudrateChangeDelay = baudrateChangeDelay;
        this.protocolMode = protocolMode;
    }

    @Override
    public DataMessage getMeterValues(byte[] initMessage) throws IOException {
        Builder iec21Builder = new Iec21Port.Builder(portName);
        if (Baudrate.fromBaudrate(this.baudrate) != Baudrate.AUTO) {
            iec21Builder.setInitialBaudrate(this.baudrate);
        }
        iec21Builder.setBaudRateChangeDelay(baudrateChangeDelay);
        iec21Builder.enableVerboseMode(true);
        Iec21Port iec21Port = iec21Builder.buildAndOpen();

        try {
            switch (protocolMode) {
                case ABC:
                    DataMessage dataMessage = iec21Port.read();
                    logger.info("Datamessage read: {}", dataMessage);
                    return dataMessage;
                case D:
                    final DataMessage[] message = new DataMessage[1];
                    final Exception[] exception = new Exception[1];
                    Object mutex = new Object();
                    iec21Port.listen(new ModeDListener() {

                        @Override
                        public void newDataMessage(DataMessage dataMessage) {
                            synchronized (mutex) {
                                message[0] = dataMessage;
                                mutex.notifyAll();
                            }
                            iec21Port.close();
                        }

                        @Override
                        public void exceptionWhileListening(Exception e) {
                            exception[0] = e;
                        }
                    });
                    synchronized (mutex) {
                        try {
                            mutex.wait(10000);
                        } catch (InterruptedException e1) {
                            logger.warn("Interruped while waiting for data package", e1);
                        }
                        if (message[0] != null) {
                            return message[0];
                        }
                        if (exception[0] != null) {
                            throw new IOException(exception[0]);
                        }
                    }
                    throw new IOException("No data message received!");
                case SML:
                    throw new IOException("SML mode not supported");
                default:
                    return null;

            }
        } finally {
            iec21Port.close();
        }
    }

}
