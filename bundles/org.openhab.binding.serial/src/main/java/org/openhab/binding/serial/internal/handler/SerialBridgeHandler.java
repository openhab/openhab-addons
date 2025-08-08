/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal.handler;

import static org.openhab.binding.serial.internal.SerialBindingConstants.*;

import java.io.IOException;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.serial.internal.util.Parity;
import org.openhab.binding.serial.internal.util.StopBits;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link SerialBridgeHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Mike Major - Initial contribution
 * @author Roland Tapken - Refactored common code into CommonBridgeHandler
 */
@NonNullByDefault
public class SerialBridgeHandler extends CommonBridgeHandler implements SerialPortEventListener {

    private SerialBridgeConfiguration config = new SerialBridgeConfiguration();

    private final SerialPortManager serialPortManager;

    private @Nullable SerialPort serialPort;

    public SerialBridgeHandler(final Bridge bridge, final SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        config = getConfigAs(SerialBridgeConfiguration.class);
        if (!checkAndProcessConfiguration(config)) {
            return;
        }

        final String port = config.serialPort;
        if (port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set");
            return;
        }

        // parse ports and if the port is found, initialize the reader
        final SerialPortIdentifier portId = serialPortManager.getIdentifier(port);
        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port is not known");
            return;
        }

        // initialize serial port
        try {
            final SerialPort serialPort = portId.open(getThing().getUID().toString(), 2000);
            this.serialPort = serialPort;

            serialPort.setSerialPortParams(config.baudRate, config.dataBits,
                    StopBits.fromConfig(config.stopBits).getSerialPortValue(),
                    Parity.fromConfig(config.parity).getSerialPortValue());

            serialPort.addEventListener(this);

            // activate the DATA_AVAILABLE notifier
            serialPort.notifyOnDataAvailable(true);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            updateStatus(ThingStatus.ONLINE);
        } catch (final IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error");
        } catch (final PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Port is in use");
        } catch (final TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Cannot attach listener to port");
        } catch (final UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Unsupported port parameters: " + e.getMessage());
        }
    }

    @Override
    protected void processInput(String result) {
        if (isLinked(TRIGGER_CHANNEL)) {
            triggerChannel(TRIGGER_CHANNEL, CommonTriggerEvents.PRESSED);
        }
        if (isLinked(STRING_CHANNEL)) {
            refresh(STRING_CHANNEL, result);
        }
        if (isLinked(BINARY_CHANNEL)) {
            refresh(BINARY_CHANNEL, result);
        }
    }

    @Override
    public void dispose() {
        final SerialPort serialPort = this.serialPort;
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            this.serialPort = null;
        }

        super.dispose();
    }

    @Override
    public void serialEvent(final SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                receiveAndProcessNow();
                break;
            default:
                break;
        }
    }

    @Override
    protected String getLogPrefix() {
        return String.format("Serial port '%s'", config.serialPort);
    }
}
