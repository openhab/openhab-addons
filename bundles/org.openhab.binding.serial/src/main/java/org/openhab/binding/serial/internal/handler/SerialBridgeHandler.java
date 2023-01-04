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
package org.openhab.binding.serial.internal.handler;

import static org.openhab.binding.serial.internal.SerialBindingConstants.BINARY_CHANNEL;
import static org.openhab.binding.serial.internal.SerialBindingConstants.STRING_CHANNEL;
import static org.openhab.binding.serial.internal.SerialBindingConstants.TRIGGER_CHANNEL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
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

    private SerialBridgeConfiguration config = new SerialBridgeConfiguration();

    private final SerialPortManager serialPortManager;
    private @Nullable SerialPort serialPort;

    private @Nullable InputStream inputStream;
    private @Nullable OutputStream outputStream;

    private Charset charset = StandardCharsets.UTF_8;

    private @Nullable String lastValue;

    private final AtomicBoolean readerActive = new AtomicBoolean(false);
    private @Nullable ScheduledFuture<?> reader;

    public SerialBridgeHandler(final Bridge bridge, final SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            final String lastValue = this.lastValue;

            if (lastValue != null) {
                refresh(channelUID.getId(), lastValue);
            }
        } else {
            switch (channelUID.getId()) {
                case STRING_CHANNEL:
                    writeString(command.toFullString(), false);
                    break;
                case BINARY_CHANNEL:
                    writeString(command.toFullString(), true);
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SerialBridgeConfiguration.class);

        try {
            if (config.charset != null) {
                charset = Charset.forName(config.charset);
            }
            logger.debug("Serial port '{}' charset '{}' set", config.serialPort, charset);
        } catch (final IllegalCharsetNameException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Invalid charset");
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
    public void dispose() {
        final SerialPort serialPort = this.serialPort;
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            this.serialPort = null;
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

        readerActive.set(false);
        final ScheduledFuture<?> reader = this.reader;
        if (reader != null) {
            reader.cancel(false);
            this.reader = null;
        }

        lastValue = null;
    }

    @Override
    public void serialEvent(final SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                if (readerActive.compareAndSet(false, true)) {
                    reader = scheduler.schedule(() -> receiveAndProcess(new StringBuilder(), true), 0,
                            TimeUnit.MILLISECONDS);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Sends a string to the serial port.
     *
     * @param string the string to send
     */
    public void writeString(final String string) {
        writeString(string, false);
    }

    /**
     * Refreshes the channel with the last received data
     *
     * @param channelId the channel to refresh
     * @param channelId the data to use
     */
    private void refresh(final String channelId, final String data) {
        if (!isLinked(channelId)) {
            return;
        }

        switch (channelId) {
            case STRING_CHANNEL:
                updateState(channelId, new StringType(data));
                break;
            case BINARY_CHANNEL:
                final StringBuilder sb = new StringBuilder("data:");
                sb.append(RawType.DEFAULT_MIME_TYPE).append(";base64,")
                        .append(Base64.getEncoder().encodeToString(data.getBytes(charset)));
                updateState(channelId, new StringType(sb.toString()));
                break;
            default:
                break;
        }
    }

    /**
     * Read from the serial port and process the data
     * 
     * @param sb the string builder to receive the data
     * @param firstAttempt indicates if this is the first read attempt without waiting
     */
    private void receiveAndProcess(final StringBuilder sb, final boolean firstAttempt) {
        final InputStream inputStream = this.inputStream;

        if (inputStream == null) {
            readerActive.set(false);
            return;
        }

        try {
            if (firstAttempt || inputStream.available() > 0) {
                final byte[] readBuffer = new byte[20];

                // read data from serial device
                while (inputStream.available() > 0) {
                    final int bytes = inputStream.read(readBuffer);
                    sb.append(new String(readBuffer, 0, bytes, charset));
                }

                // Add wait states around reading the stream, so that interrupted transmissions
                // are merged
                if (readerActive.get()) {
                    reader = scheduler.schedule(() -> receiveAndProcess(sb, false), 100, TimeUnit.MILLISECONDS);
                }

            } else {
                final String result = sb.toString();

                triggerChannel(TRIGGER_CHANNEL, CommonTriggerEvents.PRESSED);
                refresh(STRING_CHANNEL, result);
                refresh(BINARY_CHANNEL, result);

                result.lines().forEach(l -> getThing().getThings().forEach(t -> {
                    final SerialDeviceHandler device = (SerialDeviceHandler) t.getHandler();
                    if (device != null) {
                        device.handleData(l);
                    }
                }));

                lastValue = result;

                if (readerActive.compareAndSet(true, false)) {
                    // Check we haven't received more data while processing
                    if (inputStream.available() > 0 && readerActive.compareAndSet(false, true)) {
                        reader = scheduler.schedule(() -> receiveAndProcess(new StringBuilder(), true), 0,
                                TimeUnit.MILLISECONDS);
                    }
                }
            }
        } catch (final IOException e) {
            logger.debug("Error reading from serial port: {}", e.getMessage(), e);
            readerActive.set(false);
        }
    }

    /**
     * Sends a string to the serial port.
     *
     * @param string the string to send
     * @param isRawType the string should be handled as a RawType
     */
    private void writeString(final String string, final boolean isRawType) {
        final OutputStream outputStream = this.outputStream;

        if (outputStream == null) {
            return;
        }

        logger.debug("Writing '{}' to serial port {}", string, config.serialPort);

        try {
            // write string to serial port
            if (isRawType) {
                final RawType rt = RawType.valueOf(string);
                outputStream.write(rt.getBytes());
            } else {
                outputStream.write(string.getBytes(charset));
            }

            outputStream.flush();
        } catch (final IOException | IllegalArgumentException e) {
            logger.warn("Error writing '{}' to serial port {}: {}", string, config.serialPort, e.getMessage());
        }
    }
}
