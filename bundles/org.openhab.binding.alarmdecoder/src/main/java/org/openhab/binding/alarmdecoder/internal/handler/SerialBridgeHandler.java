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
package org.openhab.binding.alarmdecoder.internal.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.alarmdecoder.internal.config.SerialBridgeConfig;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    private SerialBridgeConfig config = new SerialBridgeConfig();
    private final SerialPortManager serialPortManager;
    private @NonNullByDefault({}) SerialPortIdentifier portIdentifier;
    private @Nullable SerialPort serialPort;
    private int serialPortSpeed = 115200;

    public SerialBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing serial bridge handler");
        config = getConfigAs(SerialBridgeConfig.class);
        discovery = config.discovery;

        if (config.serialPort.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no serial port configured");
            return;
        }

        if (config.bitrate > 0) {
            serialPortSpeed = config.bitrate;
        }

        portIdentifier = serialPortManager.getIdentifier(config.serialPort);
        if (portIdentifier == null) {
            logger.debug("Serial Error: Port {} does not exist.", config.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configured serial port does not exist");
            return;
        }

        connect();

        logger.trace("Finished initializing serial bridge handler");
    }

    @Override
    protected synchronized void connect() {
        disconnect(); // make sure we are disconnected
        try {
            SerialPort serialPort = portIdentifier.open("org.openhab.binding.alarmdecoder", 100);
            serialPort.setSerialPortParams(serialPortSpeed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            // Note: The V1 code called disableReceiveFraming() and disableReceiveThreshold() here

            this.serialPort = serialPort;
            reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream(), AD_CHARSET));
            writer = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream(), AD_CHARSET));

            logger.debug("connected to serial port: {}", config.serialPort);
            panelReadyReceived = false;
            startMsgReader();
            updateStatus(ThingStatus.ONLINE);
        } catch (PortInUseException e) {
            logger.debug("Cannot open serial port: {}, it is already in use", config.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Serial port already in use");
        } catch (UnsupportedCommOperationException | IOException | IllegalStateException e) {
            logger.debug("Error connecting to serial port: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected synchronized void disconnect() {
        logger.trace("Disconnecting");
        SerialPort sp = serialPort;
        if (sp != null) {
            logger.trace("Closing serial port");
            sp.close();
            serialPort = null;
        }

        stopMsgReader();

        BufferedReader br = reader;
        if (br != null) {
            logger.trace("Closing reader");
            try {
                br.close();
            } catch (IOException e) {
                logger.info("IO Exception closing reader: {}", e.getMessage());
            } finally {
                reader = null;
            }
        }

        BufferedWriter bw = writer;
        if (bw != null) {
            logger.trace("Closing writer");
            try {
                bw.close();
            } catch (IOException e) {
                logger.info("IO Exception closing writer: {}", e.getMessage());
            } finally {
                writer = null;
            }
        }
    }
}
