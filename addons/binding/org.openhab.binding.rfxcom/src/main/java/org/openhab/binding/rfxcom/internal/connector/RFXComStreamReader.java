/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFXCOM stream reader to parse RFXCOM output into messages.
 *
 * @author James Hewitt-Thomas - New class
 * @author Pauli Anttila - Original read loop
 */
public class RFXComStreamReader extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(RFXComStreamReader.class);

    private boolean interrupted = false;
    private RFXComBaseConnector connector;
    private InputStream in;

    public RFXComStreamReader(RFXComBaseConnector connector, InputStream in) {
        this.connector = connector;
        this.in = in;
    }

    @Override
    public void interrupt() {
        interrupted = true;
        super.interrupt();
        try {
            in.close();
        } catch (IOException e) {
        } // quietly close
    }

    @Override
    public void run() {
        final int dataBufferMaxLen = Byte.MAX_VALUE;

        byte[] dataBuffer = new byte[dataBufferMaxLen];

        int msgLen = 0;
        int index = 0;
        boolean start_found = false;

        logger.debug("Data listener started");

        try {

            byte[] tmpData = new byte[20];
            int len = -1;

            while (interrupted != true) {

                if ((len = in.read(tmpData)) > 0) {

                    byte[] logData = Arrays.copyOf(tmpData, len);
                    logger.trace("Received data (len={}): {}", len, DatatypeConverter.printHexBinary(logData));

                    for (int i = 0; i < len; i++) {

                        if (index > dataBufferMaxLen) {
                            // too many bytes received, try to find new
                            // start
                            start_found = false;
                        }

                        if (start_found == false && tmpData[i] > 0) {

                            start_found = true;
                            index = 0;
                            dataBuffer[index++] = tmpData[i];
                            msgLen = tmpData[i] + 1;

                        } else if (start_found) {

                            dataBuffer[index++] = tmpData[i];

                            if (index == msgLen) {

                                // whole message received, send an event

                                byte[] msg = new byte[msgLen];

                                for (int j = 0; j < msgLen; j++) {
                                    msg[j] = dataBuffer[j];
                                }

                                connector.sendMsgToListeners(msg);

                                // find new start
                                start_found = false;
                            }
                        }
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted via InterruptedIOException");
        } catch (IOException e) {
            logger.error("Reading from serial port failed", e);
            connector.sendErrorToListeners(e.getMessage());
        }

        logger.debug("Data listener stopped");
    }
}