/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.hw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This is the main handler for HomeWorks RS232 Processors.
 *
 * @author Andrew Shilliday - Initial contribution
 *
 */
@NonNullByDefault
public class HwSerialBridgeHandler extends HwBridgeHandler implements SerialPortEventListener {
    private final Logger logger = LoggerFactory.getLogger(HwSerialBridgeHandler.class);
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String serialPortName = "";
    private int baudRate = HwSerialBridgeConfig.DEFAULT_BAUD;
    private boolean updateTime;
    private @Nullable ScheduledFuture<?> updateTimeJob;

    private final SerialPortManager serialPortManager;
    private @Nullable SerialPort serialPort;
    private @Nullable OutputStreamWriter serialOutput;
    private @Nullable BufferedReader serialInput;

    public HwSerialBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the Lutron HomeWorks RS232 bridge handler");
        HwSerialBridgeConfig configuration = getConfigAs(HwSerialBridgeConfig.class);
        String configuredPort = configuration.getSerialPort();
        updateTime = Boolean.TRUE.equals(configuration.getUpdateTime());
        Integer configuredBaud = configuration.getBaudRate();
        baudRate = configuredBaud == null ? HwSerialBridgeConfig.DEFAULT_BAUD : configuredBaud.intValue();

        if (configuredPort == null) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Serial port not specified");
            return;
        }
        serialPortName = configuredPort;

        logger.debug("Lutron HomeWorks RS232 Bridge Handler Initializing.");
        logger.debug("   Serial Port: {},", serialPortName);
        logger.debug("   Baud:        {},", baudRate);

        scheduler.execute(() -> openConnection());
    }

    private void openConnection() {
        SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
        if (portIdentifier == null) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "Invalid port: " + serialPortName);
            return;
        }

        try {
            logger.info("Connecting to Lutron HomeWorks Processor using {}.", serialPortName);
            SerialPort port = portIdentifier.open(this.getClass().getName(), 2000);
            serialPort = port;

            logger.debug("Connection established using {}.  Configuring IO parameters. ", serialPortName);

            int db = SerialPort.DATABITS_8, sb = SerialPort.STOPBITS_1, p = SerialPort.PARITY_NONE;
            port.setSerialPortParams(baudRate, db, sb, p);
            port.enableReceiveThreshold(1);
            port.disableReceiveTimeout();
            serialOutput = new OutputStreamWriter(port.getOutputStream(), "US-ASCII");
            serialInput = new BufferedReader(new InputStreamReader(port.getInputStream(), "US-ASCII"));

            port.addEventListener(this);
            port.notifyOnDataAvailable(true);

            sendStartupCommands();

            updateStatus(ThingStatus.ONLINE);

            if (updateTime) {
                startUpdateProcessorTimeJob();
            }
        } catch (PortInUseException portInUseException) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "Port in use: " + serialPortName);
        } catch (UnsupportedCommOperationException | IOException e) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "Communication error");
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "Too many listeners to serial port.");
        }
    }

    private void startUpdateProcessorTimeJob() {
        ScheduledFuture<?> oldJob = updateTimeJob;
        if (oldJob != null) {
            logger.debug("Canceling old scheduled job");
            oldJob.cancel(false);
            updateTimeJob = null;
        }

        updateTimeJob = scheduler.scheduleWithFixedDelay(() -> updateProcessorTime(), 0, 1, TimeUnit.DAYS);
    }

    private void updateProcessorTime() {
        LocalDate date = LocalDate.now();
        String dateString = date.format(dateFormat);
        String timeString = date.format(timeFormat);
        logger.debug("Updating HomeWorks processor date and time to {} {}", dateString, timeString);

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("HomeWorks Bridge is offline and cannot update time on HomeWorks processor.");
            ScheduledFuture<?> job = updateTimeJob;
            if (job != null) {
                job.cancel(false);
                updateTimeJob = null;
            }
            return;
        }

        sendCommand("SD, " + dateString);
        sendCommand("ST, " + timeString);
    }

    /**
     * Receives Serial Port Events and reads Serial Port Data.
     *
     * @param serialPortEvent
     */
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            BufferedReader input = serialInput;
            if (input == null) {
                return;
            }
            try {
                while (true) {
                    String messageLine = input.readLine();
                    if (messageLine == null) {
                        break;
                    }
                    handleIncomingMessage(messageLine);
                }
            } catch (IOException e) {
                logger.debug("Error reading from serial port: {}", e.getMessage(), e);
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "Error reading from port");
            }
        }
    }

    @Override
    public void sendCommand(String command) {
        OutputStreamWriter output = serialOutput;
        if (output == null) {
            logger.debug("Serial port not open, dropping command: {}", command);
            return;
        }
        try {
            logger.debug("HomeWorks bridge sending command: {}", command);
            output.write(command + "\r");
            output.flush();
        } catch (IOException e) {
            logger.debug("Error writing to serial port: {}", e.getMessage(), e);
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "Error writing to port.");
        }
    }

    @Override
    public void dispose() {
        logger.info("HomeWorks bridge being disposed.");
        SerialPort port = serialPort;
        if (port != null) {
            port.close();
        }

        serialPort = null;
        serialInput = null;
        serialOutput = null;

        ScheduledFuture<?> job = updateTimeJob;
        if (job != null) {
            job.cancel(false);
        }

        logger.debug("Finished disposing bridge.");
    }
}
