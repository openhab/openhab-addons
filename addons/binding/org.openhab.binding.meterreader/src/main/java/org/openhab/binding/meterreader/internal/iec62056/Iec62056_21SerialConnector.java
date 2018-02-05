/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal.iec62056;

import java.io.IOException;

import org.openhab.binding.meterreader.connectors.ConnectorBase;
import org.openhab.binding.meterreader.internal.helper.Baudrate;
import org.openhab.binding.meterreader.internal.helper.ProtocolMode;
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
public class Iec62056_21SerialConnector extends ConnectorBase<DataMessage> {

    private final static Logger logger = LoggerFactory.getLogger(Iec62056_21SerialConnector.class);
    private String portName;
    private int baudrate;
    private int baudrateChangeDelay;
    private ProtocolMode protocolMode;
    private Iec21Port iec21Port;

    public Iec62056_21SerialConnector(String portName, int baudrate, int baudrateChangeDelay,
            ProtocolMode protocolMode) {
        this.portName = portName;
        this.baudrate = baudrate;
        this.baudrateChangeDelay = baudrateChangeDelay;
        this.protocolMode = protocolMode;
    }

    @Override
    public DataMessage getMeterValuesInternal(byte[] initMessage) throws IOException {

        try {
            switch (protocolMode) {
                case ABC:
                    DataMessage dataMessage = iec21Port.read();
                    logger.info("Datamessage read: {}", dataMessage);
                    return dataMessage;
                case D:
                    iec21Port.listen(new ModeDListener() {

                        @Override
                        public void newDataMessage(DataMessage dataMessage) {
                            notifyListeners(dataMessage);
                        }

                        @Override
                        public void exceptionWhileListening(Exception e) {
                            logger.error("Exception while listening for mode D data message", e);
                        }
                    });
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException e1) {
                            // don't care
                        }
                    }
                    return null;
                case SML:
                    throw new IOException("SML mode not supported");
                default:
                    return null;

            }
        } finally {
            iec21Port.close();
        }
    }

    @Override
    public void openConnection() throws IOException {
        Builder iec21Builder = new Iec21Port.Builder(portName);
        if (Baudrate.fromBaudrate(this.baudrate) != Baudrate.AUTO) {
            iec21Builder.setInitialBaudrate(this.baudrate);
        }
        iec21Builder.setBaudRateChangeDelay(baudrateChangeDelay);
        iec21Builder.enableVerboseMode(true);
        iec21Port = iec21Builder.buildAndOpen();
    }

    @Override
    public void closeConnection() {
        iec21Port.close();
    }

}
