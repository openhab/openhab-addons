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
package org.openhab.binding.kaleidescape.internal.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.kaleidescape.internal.KaleidescapeException;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with the Kaleidescape component through a serial connection
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the Kaleidescape binding
 */
@NonNullByDefault
public class KaleidescapeSerialConnector extends KaleidescapeConnector {
    private final Logger logger = LoggerFactory.getLogger(KaleidescapeSerialConnector.class);

    private final String serialPortName;
    private final SerialPortManager serialPortManager;
    private final String uid;

    private @Nullable SerialPort serialPort;

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param serialPortName the serial port name to be used
     * @param uid the thing uid string
     */
    public KaleidescapeSerialConnector(SerialPortManager serialPortManager, String serialPortName, String uid) {
        this.serialPortManager = serialPortManager;
        this.serialPortName = serialPortName;
        this.uid = uid;
    }

    @Override
    public synchronized void open() throws KaleidescapeException {
        logger.debug("Opening serial connection on port {}", serialPortName);
        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
            if (portIdentifier == null) {
                setConnected(false);
                throw new KaleidescapeException("Opening serial connection failed: No Such Port");
            }

            SerialPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            commPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            commPort.enableReceiveThreshold(1);
            commPort.enableReceiveTimeout(100);
            commPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            InputStream dataIn = commPort.getInputStream();
            OutputStream dataOut = commPort.getOutputStream();

            if (dataOut != null) {
                dataOut.flush();
            }
            if (dataIn != null && dataIn.markSupported()) {
                try {
                    dataIn.reset();
                } catch (IOException e) {
                    logger.debug("Caught IOException at dataIn.reset(): {}", e.getMessage());
                }
            }

            Thread thread = new KaleidescapeReaderThread(this, this.uid, this.serialPortName);
            setReaderThread(thread);
            thread.start();

            this.serialPort = commPort;
            this.dataIn = dataIn;
            this.dataOut = dataOut;

            setConnected(true);

            logger.debug("Serial connection opened");
        } catch (PortInUseException e) {
            setConnected(false);
            throw new KaleidescapeException("Opening serial connection failed: Port in Use Exception", e);
        } catch (UnsupportedCommOperationException e) {
            setConnected(false);
            throw new KaleidescapeException("Opening serial connection failed: Unsupported Comm Operation Exception",
                    e);
        } catch (UnsupportedEncodingException e) {
            setConnected(false);
            throw new KaleidescapeException("Opening serial connection failed: Unsupported Encoding Exception", e);
        } catch (IOException e) {
            setConnected(false);
            throw new KaleidescapeException("Opening serial connection failed: IO Exception", e);
        }
    }

    @Override
    public synchronized void close() {
        logger.debug("Closing serial connection");
        SerialPort serialPort = this.serialPort;
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        super.cleanup();
        if (serialPort != null) {
            serialPort.close();
            this.serialPort = null;
        }
        setConnected(false);
        logger.debug("Serial connection closed");
    }
}
