/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.dscalarm.internal.config.IT100BridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * The bridge handler for the DSC IT100 RS232 Serial interface.
 *
 * @author Russell Stephens - Initial Contribution
 */

public class IT100BridgeHandler extends DSCAlarmBaseBridgeHandler implements SerialPortEventListener {

    private Logger logger = LoggerFactory.getLogger(IT100BridgeHandler.class);

    /**
     * Constructor.
     *
     * @param bridge
     */
    public IT100BridgeHandler(Bridge bridge) {
        super(bridge, DSCAlarmBridgeType.IT100, DSCAlarmProtocol.IT100_API);
    }

    private String serialPortName = "";
    private int baudRate;
    private SerialPort serialPort = null;
    private OutputStreamWriter serialOutput = null;
    private BufferedReader serialInput = null;

    @Override
    public void initialize() {
        logger.debug("Initializing the DSC IT100 Bridge handler.");

        IT100BridgeConfiguration configuration = getConfigAs(IT100BridgeConfiguration.class);

        serialPortName = configuration.serialPort;

        if (serialPortName != null) {
            baudRate = configuration.baud.intValue();
            pollPeriod = configuration.pollPeriod.intValue();

            if (this.pollPeriod > 15) {
                this.pollPeriod = 15;
            } else if (this.pollPeriod < 1) {
                this.pollPeriod = 1;
            }

            logger.debug("IT100 Bridge Handler Initialized.");
            logger.debug("   Serial Port: {},", serialPortName);
            logger.debug("   Baud:        {},", baudRate);
            logger.debug("   Password:    {},", getPassword());
            logger.debug("   PollPeriod:  {},", pollPeriod);

            updateStatus(ThingStatus.OFFLINE);
            startPolling();
        }
    }

    @Override
    public void dispose() {
        stopPolling();
        closeConnection();
        super.dispose();
    }

    @Override
    public void openConnection() {

        try {
            logger.debug("openConnection(): Connecting to IT-100 ");

            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.enableReceiveThreshold(1);
            serialPort.disableReceiveTimeout();

            serialOutput = new OutputStreamWriter(serialPort.getOutputStream(), "US-ASCII");
            serialInput = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

            setSerialEventHandler(this);

            setConnected(true);

        } catch (NoSuchPortException noSuchPortException) {
            logger.error("openConnection(): No Such Port Exception: {}", noSuchPortException.getMessage());
            setConnected(false);
        } catch (PortInUseException portInUseException) {
            logger.error("openConnection(): Port in Use Exception: {}", portInUseException.getMessage());
            setConnected(false);
        } catch (UnsupportedCommOperationException unsupportedCommOperationException) {
            logger.error("openConnection(): Unsupported Comm Operation Exception: {}",
                    unsupportedCommOperationException.getMessage());
            setConnected(false);
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            logger.error("openConnection(): Unsupported Encoding Exception: {}",
                    unsupportedEncodingException.getMessage());
            setConnected(false);
        } catch (IOException ioException) {
            logger.error("openConnection(): IO Exception: {}", ioException.getMessage());
            setConnected(false);
        }
    }

    @Override
    public void write(String writeString) {
        try {
            serialOutput.write(writeString);
            serialOutput.flush();
            logger.debug("write(): Message Sent: {}", writeString);
        } catch (IOException ioException) {
            logger.error("write(): {}", ioException.getMessage());
            setConnected(false);
        } catch (Exception exception) {
            logger.error("write(): Unable to write to serial port: {} ", exception.getMessage(), exception);
            setConnected(false);
        }
    }

    @Override
    public String read() {
        String message = "";

        try {
            message = readLine();
            logger.debug("read(): Message Received: {}", message);
        } catch (IOException ioException) {
            logger.error("read(): IO Exception: {} ", ioException.getMessage());
            setConnected(false);
        } catch (Exception exception) {
            logger.error("read(): Exception: {} ", exception.getMessage(), exception);
            setConnected(false);
        }

        return message;

    }

    /**
     * Read a line from the Input Stream.
     *
     * @return
     * @throws IOException
     */
    private String readLine() throws IOException {
        return serialInput.readLine();
    }

    @Override
    public void closeConnection() {
        logger.debug("closeConnection(): Closing Serial Connection!");

        if (serialPort == null) {
            setConnected(false);
            return;
        }

        serialPort.removeEventListener();

        if (serialInput != null) {
            IOUtils.closeQuietly(serialInput);
            serialInput = null;
        }

        if (serialOutput != null) {
            IOUtils.closeQuietly(serialOutput);
            serialOutput = null;
        }

        serialPort.close();
        serialPort = null;

        setConnected(false);
        logger.debug("close(): Serial Connection Closed!");
    }

    /**
     * Gets the Serial Port Name of the IT-100
     *
     * @return serialPortName
     */
    public String getSerialPortName() {
        return serialPortName;
    }

    /**
     * Receives Serial Port Events and reads Serial Port Data.
     *
     * @param serialPortEvent
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String messageLine = serialInput.readLine();
                handleIncomingMessage(messageLine);
            } catch (IOException ioException) {
                logger.error("serialEvent(): IO Exception: {}", ioException.getMessage());
            }
        }
    }

    /**
     * Set the serial event handler.
     *
     * @param serialPortEventListenser
     */
    private void setSerialEventHandler(SerialPortEventListener serialPortEventListenser) {
        try {
            // Add the serial port event listener
            serialPort.addEventListener(serialPortEventListenser);
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException tooManyListenersException) {
            logger.error("setSerialEventHandler(): Too Many Listeners Exception: {}",
                    tooManyListenersException.getMessage());
        }
    }
}
