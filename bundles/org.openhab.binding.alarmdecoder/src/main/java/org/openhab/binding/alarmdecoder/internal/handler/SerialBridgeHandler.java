/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.alarmdecoder.internal.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.alarmdecoder.internal.config.SerialBridgeConfig;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating via a serial port with the Nu Tech Alarm Decoder device.
 * Based on code from the original OH1 alarmdecoder binding. Some OHC serial transport code taken from the Zigbee
 * binding.
 *
 * @author Bernd Pfrommer - Initial contribution (OH1 version)
 * @author Bob Adair - Re-factored into OH2 binding and rewrote to use OHC serial transport.
 */
@NonNullByDefault
public class SerialBridgeHandler extends ADBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SerialBridgeHandler.class);

    private @NonNullByDefault({}) SerialBridgeConfig config;
    private final SerialPortManager serialPortManager;

    /** name of serial device */
    private String serialDeviceName = "";
    private @NonNullByDefault({}) SerialPort serialPort = null;
    private int serialPortSpeed = 115200;

    @Activate
    public SerialBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing serial bridge handler");
        config = getConfigAs(SerialBridgeConfig.class);
        discovery = config.discovery;

        if (config.serialPort == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge configuration missing");
            return;
        } else {
            serialDeviceName = config.serialPort;
        }

        if (config.bitrate > 0) {
            serialPortSpeed = config.bitrate;
        }

        connect();

        logger.trace("Finished initializing serial bridge handler");
    }

    @Override
    protected synchronized void connect() {
        disconnect(); // make sure we are disconnected
        try {
            if (!serialDeviceName.isEmpty()) {
                // Exit if no identifiers exist to work around possible bug
                Stream<SerialPortIdentifier> serialPortIdentifiers = serialPortManager.getIdentifiers();
                if (!serialPortIdentifiers.findAny().isPresent()) {
                    logger.debug("No serial communication ports found. Cannot connect to [{}]", serialDeviceName);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No serial ports found");
                    return;
                }

                SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialDeviceName);
                if (portIdentifier == null) {
                    logger.debug("Serial Error: Port {} does not exist.", serialDeviceName);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Configured serial port does not exist");
                    return;
                }

                serialPort = portIdentifier.open("org.openhab.binding.alarmdecoder", 100);

                serialPort.setSerialPortParams(serialPortSpeed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
                // serialPort.disableReceiveFraming();
                // serialPort.disableReceiveThreshold();

                reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));
                logger.debug("connected to serial port: {}", serialDeviceName);
                panelReadyReceived = false;
                startMsgReader();
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Serial device name not configured");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "port name not configured");
            }
        } catch (PortInUseException e) {
            logger.debug("Cannot open serial port: {}, it is already in use", serialDeviceName);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Serial port already in use");
        } catch (UnsupportedCommOperationException | IOException | IllegalStateException e) {
            logger.debug("Error connecting to serial port: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected synchronized void disconnect() {
        logger.trace("Disconnecting");
        if (serialPort != null) {
            logger.trace("Closing serial port");
            serialPort.close();
            serialPort = null;
        }

        stopMsgReader();

        if (reader != null) {
            logger.trace("Closing reader");
            try {
                reader.close();
            } catch (IOException e) {
                logger.info("IO Exception closing reader: {}", e.getMessage());
            } finally {
                reader = null;
            }
        }
        if (writer != null) {
            logger.trace("Closing writer");
            try {
                writer.close();
            } catch (IOException e) {
                logger.info("IO Exception closing writer: {}", e.getMessage());
            } finally {
                writer = null;
            }
        }
    }
}
