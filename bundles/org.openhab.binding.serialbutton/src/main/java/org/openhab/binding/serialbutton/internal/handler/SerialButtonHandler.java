/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.serialbutton.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.serialbutton.internal.SerialButtonBindingConstants;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SerialButtonHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class SerialButtonHandler extends BaseThingHandler implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(SerialButtonHandler.class);
    private final SerialPortManager serialPortManager;

    private SerialPortIdentifier portId;
    private SerialPort serialPort;

    private InputStream inputStream;

    public SerialButtonHandler(Thing thing, final SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        String port = (String) getConfig().get(SerialButtonBindingConstants.PARAMETER_CONFIG);
        if (port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        }

        // parse ports and if the port is found, initialize the reader
        portId = serialPortManager.getIdentifier(port);
        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port is not known!");
            return;
        }

        // initialize serial port
        try {
            serialPort = portId.open(getThing().getUID().toString(), 2000);
            serialPort.addEventListener(this);

            // activate the DATA_AVAILABLE notifier
            serialPort.notifyOnDataAvailable(true);
            inputStream = serialPort.getInputStream();

            updateStatus(ThingStatus.ONLINE);
        } catch (final IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error!");
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Port is in use!");
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Cannot attach listener to port!");
        }
    }

    @Override
    public void dispose() {
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.debug("Error while closing the input stream: {}", e.getMessage());
            }
        }
        if (serialPort != null) {
            serialPort.close();
        }
        inputStream = null;
        serialPort = null;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                // we get here if data has been received
                try {
                    do {
                        // read data from serial device
                        byte[] readBuffer = new byte[20];
                        while (inputStream.available() > 0) {
                            inputStream.read(readBuffer);
                        }
                        try {
                            // add wait states around reading the stream, so that interrupted transmissions are merged
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // ignore interruption
                        }
                    } while (inputStream.available() > 0);

                    triggerChannel(SerialButtonBindingConstants.TRIGGER_CHANNEL, CommonTriggerEvents.PRESSED);
                } catch (IOException e1) {
                    logger.debug("Error reading from serial port: {}", e1.getMessage(), e1);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // we do not have any state channels, so nothing to do here
    }
}
