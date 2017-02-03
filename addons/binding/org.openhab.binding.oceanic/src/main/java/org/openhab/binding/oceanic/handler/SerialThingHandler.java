/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.oceanic.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link SerialThingHandler} is responsible for handling commands, which
 * are sent to one of the channels. Thing Handler classes that use serial
 * communications can extend/implement this class, but must make sure they
 * supplement the configuration parameters into the {@link SerialConfiguration}
 * Configuration of the underlying Thing, if not already specified in the
 * thing.xml definition
 *
 * @author Karel Goderis - Initial contribution
 */
public abstract class SerialThingHandler extends BaseThingHandler implements SerialPortEventListener {

    // List of all Configuration parameters
    public static final String PORT = "port";
    public static final String BAUD_RATE = "baud";
    public static final String BUFFER_SIZE = "buffer";

    private Logger logger = LoggerFactory.getLogger(SerialThingHandler.class);

    private SerialPort serialPort;
    private CommPortIdentifier portId;
    private InputStream inputStream;
    private OutputStream outputStream;
    protected int baud;
    protected String port;
    protected int bufferSize;

    public SerialThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Called when data is received on the serial port
     *
     * @param line
     *            - the received data as a String
     *
     **/
    abstract public void onDataReceived(String line);

    /**
     * Write data to the serial port
     *
     * @param msg
     *            - the received data as a String
     *
     **/
    public void writeString(String msg) {
        String port = (String) this.getConfig().get(PORT);

        try {
            // write string to serial port
            outputStream.write(msg.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error writing '" + msg + "' to serial port " + port + " : " + e.getMessage());
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {

        switch (event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream), bufferSize);
                    while (br.ready()) {
                        String line = br.readLine();
                        logger.debug("Receiving '{}' on '{}'", line, getConfig().get(PORT));
                        onDataReceived(line);
                    }
                } catch (IOException e) {
                    String port = (String) getConfig().get(PORT);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Error receiving data on serial port " + port + " : " + e.getMessage());
                }
                break;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing serial thing handler.");
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
        if (serialPort != null) {
            serialPort.close();
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing serial thing handler.");

        if (serialPort == null && port != null && baud != 0) {

            // parse ports and if the default port is found, initialized the
            // reader
            @SuppressWarnings("rawtypes")
            Enumeration portList = CommPortIdentifier.getPortIdentifiers();
            while (portList.hasMoreElements()) {
                CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
                if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    if (id.getName().equals(port)) {
                        logger.debug("Serial port '{}' has been found.", port);
                        portId = id;
                    }
                }
            }

            if (portId != null) {
                // initialize serial port
                try {
                    serialPort = portId.open(this.getThing().getUID().getBindingId(), 2000);
                } catch (PortInUseException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
                    inputStream = serialPort.getInputStream();
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
                    serialPort.addEventListener(this);
                } catch (TooManyListenersException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not open serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                // activate the DATA_AVAILABLE notifier
                serialPort.notifyOnDataAvailable(true);

                try {
                    // set port parameters
                    serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                } catch (UnsupportedCommOperationException e) {

                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not configure serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

                try {
                    // get the output stream
                    outputStream = serialPort.getOutputStream();
                    updateStatus(ThingStatus.ONLINE);
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Could not communicate with the serial port " + serialPort + ": " + e.getMessage());
                    return;
                }

            } else {
                StringBuilder sb = new StringBuilder();
                portList = CommPortIdentifier.getPortIdentifiers();
                while (portList.hasMoreElements()) {
                    CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
                    if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                        sb.append(id.getName() + "\n");
                    }
                }
                logger.error("Serial port '" + port + "' could not be found. Available ports are:\n" + sb.toString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // by default, we write anything we received as a string to the serial
        // port
        writeString(command.toString());
    }
}