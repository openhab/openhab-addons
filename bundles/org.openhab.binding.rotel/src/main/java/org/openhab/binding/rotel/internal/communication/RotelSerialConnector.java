/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with the Rotel device through a serial connection
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelSerialConnector extends RotelConnector {

    private final Logger logger = LoggerFactory.getLogger(RotelSerialConnector.class);

    private String serialPortName;
    private SerialPortManager serialPortManager;

    private @Nullable SerialPort serialPort;

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param serialPortName the serial port name to be used
     * @param model the projector model in use
     * @param protocol the protocol to be used
     */
    public RotelSerialConnector(SerialPortManager serialPortManager, String serialPortName, RotelModel model,
            RotelProtocol protocol, Map<RotelSource, String> sourcesLabels) {
        super(model, protocol, sourcesLabels, false);

        this.serialPortManager = serialPortManager;
        this.serialPortName = serialPortName;
    }

    @Override
    public synchronized void open() throws RotelException {
        logger.debug("Opening serial connection on port {}", serialPortName);
        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
            if (portIdentifier == null) {
                setConnected(false);
                logger.debug("Opening serial connection failed: No Such Port: {}", serialPortName);
                throw new RotelException("Opening serial connection failed: No Such Port");
            }

            SerialPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            commPort.setSerialPortParams(getModel().getBaudRate(), SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
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
                }
            }

            Thread thread = new RotelReaderThread(this);
            setReaderThread(thread);
            thread.start();

            this.serialPort = commPort;
            this.dataIn = dataIn;
            this.dataOut = dataOut;

            setConnected(true);

            logger.debug("Serial connection opened");
        } catch (PortInUseException e) {
            setConnected(false);
            logger.debug("Opening serial connection failed: Port in Use Exception: {}", e.getMessage(), e);
            throw new RotelException("Opening serial connection failed: Port in Use Exception");
        } catch (UnsupportedCommOperationException e) {
            setConnected(false);
            logger.debug("Opening serial connection failed: Unsupported Comm Operation Exception: {}", e.getMessage(),
                    e);
            throw new RotelException("Opening serial connection failed: Unsupported Comm Operation Exception");
        } catch (UnsupportedEncodingException e) {
            setConnected(false);
            logger.debug("Opening serial connection failed: Unsupported Encoding Exception: {}", e.getMessage(), e);
            throw new RotelException("Opening serial connection failed: Unsupported Encoding Exception");
        } catch (IOException e) {
            setConnected(false);
            logger.debug("Opening serial connection failed: IO Exception: {}", e.getMessage(), e);
            throw new RotelException("Opening serial connection failed: IO Exception");
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
