/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.connector;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jd2xx.JD2XX;
import jd2xx.JD2XXInputStream;
import jd2xx.JD2XXOutputStream;

/**
 * RFXCOM connector for direct access via D2XX driver.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComJD2XXConnector extends RFXComBaseConnector {
    private final Logger logger = LoggerFactory.getLogger(RFXComJD2XXConnector.class);

    private JD2XX serialPort;
    private JD2XXOutputStream out;

    private Thread readerThread;

    @Override
    public void connect(RFXComBridgeConfiguration device) throws IOException {
        logger.info("Connecting to RFXCOM device '{}' using JD2XX.", device.bridgeId);

        if (serialPort == null) {
            serialPort = new JD2XX();
        }
        serialPort.openBySerialNumber(device.bridgeId);
        serialPort.setBaudRate(38400);
        serialPort.setDataCharacteristics(8, JD2XX.STOP_BITS_1, JD2XX.PARITY_NONE);
        serialPort.setFlowControl(JD2XX.FLOW_NONE, 0, 0);
        serialPort.setTimeouts(100, 100);

        in = new JD2XXInputStream(serialPort);
        out = new JD2XXOutputStream(serialPort);

        out.flush();
        if (in.markSupported()) {
            in.reset();
        }

        readerThread = new RFXComStreamReader(this);
        readerThread.start();
    }

    @Override
    public void disconnect() {
        logger.debug("Disconnecting");

        if (readerThread != null) {
            logger.debug("Interrupt serial listener");
            readerThread.interrupt();
            try {
                readerThread.join();
            } catch (InterruptedException e) {
            }
        }

        if (out != null) {
            logger.debug("Close serial out stream");
            IOUtils.closeQuietly(out);
        }
        if (in != null) {
            logger.debug("Close serial in stream");
            IOUtils.closeQuietly(in);
        }

        if (serialPort != null) {
            logger.debug("Close serial port");
            try {
                serialPort.close();
            } catch (IOException e) {
                logger.warn("Serial port closing error", e);
            }
        }

        readerThread = null;
        serialPort = null;
        out = null;
        in = null;

        logger.debug("Closed");

    }

    @Override
    public void sendMessage(byte[] data) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Send data (len={}): {}", data.length, HexUtils.bytesToHex(data));
        }
        out.write(data);
    }
}
