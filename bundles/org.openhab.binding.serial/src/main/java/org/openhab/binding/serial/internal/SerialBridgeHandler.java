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
package org.openhab.binding.serial.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SerialBridgeHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class SerialBridgeHandler extends BaseBridgeHandler implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(SerialBridgeHandler.class);

    private @NonNullByDefault({}) SerialBridgeConfiguration config;

    private final SerialPortManager serialPortManager;
    private @NonNullByDefault({}) SerialPortIdentifier portId;
    private @NonNullByDefault({}) SerialPort serialPort;

    private @Nullable InputStream inputStream;
    private @Nullable OutputStream outputStream;

    private @NonNullByDefault({}) Charset charset;

    private @Nullable String data;

    public SerialBridgeHandler(final Bridge bridge, final SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            refresh(channelUID.getId());
        } else {
            writeCommand(command);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SerialBridgeConfiguration.class);

        if (config.serialPort == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        }

        // parse ports and if the port is found, initialize the reader
        portId = serialPortManager.getIdentifier(config.serialPort);
        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port is not known!");
            return;
        }

        // initialize serial port
        try {
            serialPort = portId.open(getThing().getUID().toString(), 2000);

            serialPort.setSerialPortParams(config.baudrate, config.databits, config.getStopBitsAsInt(),
                    config.getParityAsInt());

            serialPort.addEventListener(this);

            // activate the DATA_AVAILABLE notifier
            serialPort.notifyOnDataAvailable(true);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            updateStatus(ThingStatus.ONLINE);
        } catch (final IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error!");
        } catch (final PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Port is in use!");
        } catch (final TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Cannot attach listener to port!");
        } catch (final UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Unsupported port parameters: " + e.getMessage());
        }

        try {
            if (config.charset == null) {
                charset = Charset.defaultCharset();
            } else {
                charset = Charset.forName(config.charset);
            }
            logger.debug("Serial port '{}' charset '{}' set.", config.serialPort, charset);
        } catch (final IllegalCharsetNameException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Invalid charset!");
            return;
        }
    }

    @Override
    public void dispose() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            serialPort = null;
        }

        final InputStream inputStream = this.inputStream;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (final IOException e) {
                logger.debug("Error while closing the input stream: {}", e.getMessage());
            }
            this.inputStream = null;
        }

        final OutputStream outputStream = this.outputStream;
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (final IOException e) {
                logger.debug("Error while closing the output stream: {}", e.getMessage());
            }
            this.outputStream = null;
        }
    }

    @Override
    public void serialEvent(final SerialPortEvent event) {
        final InputStream inputStream = this.inputStream;

        if (inputStream == null) {
            return;
        }

        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                final StringBuilder sb = new StringBuilder();
                final byte[] readBuffer = new byte[20];
                try {
                    do {
                        // read data from serial device
                        while (inputStream.available() > 0) {
                            final int bytes = inputStream.read(readBuffer);
                            sb.append(new String(readBuffer, 0, bytes, charset));
                        }
                        try {
                            // add wait states around reading the stream, so that interrupted transmissions
                            // are merged
                            Thread.sleep(100);
                        } catch (final InterruptedException e) {
                            // ignore interruption
                        }
                    } while (inputStream.available() > 0);

                    final String result = sb.toString();
                    data = result;

                    triggerChannel(SerialBindingConstants.TRIGGER_CHANNEL);
                    refresh(SerialBindingConstants.STRING_CHANNEL);
                    refresh(SerialBindingConstants.BINARY_CHANNEL);

                    result.lines().forEach(l -> getThing().getThings().forEach(t -> {
                        final SerialDeviceHandler device = (SerialDeviceHandler) t.getHandler();
                        if (device != null) {
                            device.handleData(l);
                        }
                    }));

                } catch (final IOException e) {
                    logger.debug("Error reading from serial port: {}", e.getMessage(), e);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Refreshes the channel with the last received data
     *
     * @param channelId the channel to refresh
     */
    private void refresh(final String channelId) {
        final String data = this.data;

        if (data == null || !isLinked(channelId)) {
            return;
        }

        switch (channelId) {
            case SerialBindingConstants.STRING_CHANNEL:
                updateState(channelId, new StringType(data));
                break;
            case SerialBindingConstants.BINARY_CHANNEL:
                updateState(channelId, new RawType(data.getBytes(charset), RawType.DEFAULT_MIME_TYPE));
                break;
            default:
                break;
        }
    }

    /**
     * Sends a command as a string to the serial port
     *
     * @param command the string to send
     */
    private void writeCommand(final Command command) {
        final OutputStream outputStream = this.outputStream;

        if (outputStream == null) {
            return;
        }

        logger.debug("Writing '{}' to serial port {}", command.toFullString(), config.serialPort);

        try {
            // write string to serial port
            if (command instanceof RawType) {
                outputStream.write(((RawType) command).getBytes());
            } else {
                outputStream.write(command.toFullString().getBytes(charset));
            }

            outputStream.flush();
        } catch (final IOException e) {
            logger.warn("Error writing '{}' to serial port {}: {}", command.toFullString(), config.serialPort,
                    e.getMessage());
        }
    }
}
