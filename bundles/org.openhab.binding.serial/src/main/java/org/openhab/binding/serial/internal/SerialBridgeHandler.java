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

    private @Nullable SerialBridgeConfiguration config;

    private @Nullable SerialPortIdentifier portId;
    private @Nullable SerialPort serialPort;

    private @Nullable InputStream inputStream;
    private @Nullable OutputStream outputStream;

    private @Nullable Charset charset;

    private @NonNullByDefault StringType data;

    /**
     * Serial Port Manager.
     */
    private final SerialPortManager serialPortManager;

    public SerialBridgeHandler(final Bridge bridge, final SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
        this.data = new StringType();
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SerialBindingConstants.STRING_CHANNEL:
                    if (data != null) {
                        updateState(channelUID, data);
                    }
                    break;
                default:
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SerialBindingConstants.STRING_CHANNEL:
                    writeString(command);
                    break;
                case SerialBindingConstants.BINARY_CHANNEL:
                    writeString(command);
                    break;
                default:
                    break;
            }
        }

        /*
         * if (CHANNEL_1.equals(channelUID.getId())) { if (command instanceof
         * RefreshType) { // TODO: handle data refresh }
         * 
         * // TODO: handle command
         * 
         * // Note: if communication with thing fails for some reason, // indicate that
         * by setting the status with detail information: //
         * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, //
         * "Could not control device at IP address x.x.x.x"); }
         */

        // sudo socat
        // PTY,raw,echo=0,link=/dev/ttyUSB01,user=openhab,group=openhab,mode=777
        // PTY,raw,echo=0,link=/dev/ttyS11
    }

    /**
     * Sends a string to the serial port of this device
     *
     * @param msg the string to send
     */
    public void writeString(Command msg) {
        if (msg == null) {
            return;
        }

        logger.debug("Writing '{}' to serial port {}", msg.toFullString(), config.serialPort);

        try {
            // write string to serial port
            if (msg instanceof RawType) {
                outputStream.write(((RawType) msg).getBytes());
            } else {
                outputStream.write(msg.toFullString().getBytes(charset));
            }

            outputStream.flush();
        } catch (IOException e) {
            logger.warn("Error writing '{}' to serial port {}: {}", msg, config.serialPort, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SerialBridgeConfiguration.class);

        // TODO validate config

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
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Unsupported parameters!");
        }

        try {
            if (config.charset == null) {
                charset = Charset.defaultCharset();
            } else {
                charset = Charset.forName(config.charset);
            }
            logger.debug("Serial port '{}' charset '{}' set.", config.serialPort, charset);
        } catch (IllegalCharsetNameException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Invalid charset!");
            return;
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
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.debug("Error while closing the output stream: {}", e.getMessage());
            }
        }
        if (serialPort != null) {
            serialPort.close();
        }
        inputStream = null;
        serialPort = null;
    }

    @Override
    public void serialEvent(final SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                StringBuilder sb = new StringBuilder();
                byte[] readBuffer = new byte[20];
                try {
                    do {
                        // read data from serial device
                        while (inputStream.available() > 0) {
                            int bytes = inputStream.read(readBuffer);
                            sb.append(new String(readBuffer, 0, bytes, charset));
                        }
                        try {
                            // add wait states around reading the stream, so that interrupted transmissions
                            // are merged
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // ignore interruption
                        }
                    } while (inputStream.available() > 0);

                    String data = sb.toString();

                    if (!data.isEmpty()) {
                        this.data = StringType.valueOf(data);

                        if (isLinked(SerialBindingConstants.STRING_CHANNEL)) {
                            updateState(SerialBindingConstants.STRING_CHANNEL, this.data);
                        }
                        if (isLinked(SerialBindingConstants.BINARY_CHANNEL)) {
                            updateState(SerialBindingConstants.BINARY_CHANNEL,
                                    new RawType(data.getBytes(charset), RawType.DEFAULT_MIME_TYPE));
                        }

                        triggerChannel(SerialBindingConstants.TRIGGER_CHANNEL);

                        data.lines().forEach(l -> getThing().getThings()
                                .forEach(t -> ((SerialDeviceHandler) t.getHandler()).handleData(l)));
                    }
                } catch (IOException e) {
                    logger.debug("Error reading from serial port: {}", e.getMessage(), e);
                }
                break;
            default:
                break;
        }
    }
}
