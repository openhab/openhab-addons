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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CommonBridgeHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Mike Major - Initial contribution
 * @author Roland Tapken - Added code for charset=HEX and channel refresh
 */
@NonNullByDefault
public abstract class CommonBridgeHandler extends BaseBridgeHandler {

    protected final Logger logger = LoggerFactory.getLogger(CommonBridgeHandler.class);

    protected CommonBridgeConfiguration config = new CommonBridgeConfiguration();

    protected @Nullable InputStream inputStream;
    protected @Nullable OutputStream outputStream;

    private Charset charset = StandardCharsets.UTF_8;

    private boolean binaryHexData = false;

    private @Nullable Pattern eolPattern;

    private @Nullable String lastValue;

    protected final AtomicBoolean readerActive = new AtomicBoolean(false);

    @Nullable
    private ScheduledFuture<?> reader;

    public CommonBridgeHandler(final Bridge bridge) {
        super(bridge);
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
                case STRING_CHANNEL -> writeString(command.toFullString(), false);
                case BINARY_CHANNEL -> writeString(command.toFullString(), true);
                default -> {
                }
            }

        }
    }

    public boolean checkAndProcessConfiguration(CommonBridgeConfiguration config) {
        this.config = config;

        try {
            String strCharset = config.charset;
            if (strCharset != null) {
                if ("hex".equalsIgnoreCase(strCharset)) {
                    binaryHexData = true;
                    logger.debug("{} converting to hex", getLogPrefix());
                } else {
                    binaryHexData = false;
                    Charset charset = Charset.forName(strCharset);
                    logger.debug("{} charset '{}' set", getLogPrefix(), charset);
                    this.charset = charset;
                }
            }
        } catch (final IllegalCharsetNameException | UnsupportedCharsetException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Invalid charset");
            return false;
        }

        String eolPatternStr = config.eolPattern;
        this.eolPattern = null;
        if (eolPatternStr != null && !eolPatternStr.isBlank()) {
            try {
                Pattern eolPattern = Pattern.compile(eolPatternStr, Pattern.CASE_INSENSITIVE);
                this.eolPattern = eolPattern;
                logger.debug("{} eolPattern '{}' set", getLogPrefix(), eolPattern);
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Invalid EOL sequence");
                return false;
            }
        } else if (binaryHexData) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "EOL pattern required for charset = HEX");
            return false;
        }

        return true;
    }

    protected void disposeReader() {
        final InputStream inputStream = this.inputStream;
        this.inputStream = null;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (final IOException e) {
                logger.debug("Error while closing the input stream: {}", e.getMessage());
            }
        }

        final OutputStream outputStream = this.outputStream;
        this.outputStream = null;
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (final IOException e) {
                logger.debug("Error while closing the output stream: {}", e.getMessage());
            }
        }

        readerActive.set(false);
        final ScheduledFuture<?> reader = this.reader;
        this.reader = null;
        if (reader != null) {
            reader.cancel(false);
        }
    }

    @Override
    public void dispose() {
        disposeReader();
        lastValue = null;
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
     * @param channelUID the channel to refresh
     * @param data the data to use
     */
    protected void refresh(final String channelUID, final String data) {
        if (!isLinked(channelUID)) {
            return;
        }

        switch (channelUID) {
            case STRING_CHANNEL -> updateState(channelUID, new StringType(data));
            case BINARY_CHANNEL -> {
                String sb = "data:" + RawType.DEFAULT_MIME_TYPE + ";base64,"
                        + Base64.getEncoder().encodeToString(data.getBytes(charset));
                updateState(channelUID, new StringType(sb));
            }
            default -> {
            }
        }
    }

    protected void receiveAndProcessNow() {
        if (readerActive.compareAndSet(false, true)) {
            reader = scheduler.schedule(() -> receiveAndProcess(new StringBuilder(), true), 0, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Read from the serial port and process the data
     * 
     * @param sb the string builder to receive the data
     * @param firstAttempt indicates if this is the first read attempt without waiting
     */
    protected void receiveAndProcess(final StringBuilder sb, final boolean firstAttempt) {
        final InputStream inputStream = this.inputStream;

        if (inputStream == null) {
            readerActive.set(false);
            return;
        }

        try {
            synchronized (inputStream) {
                if (firstAttempt || inputStream.available() > 0) {
                    final byte[] readBuffer = new byte[20];

                    String line = "";
                    boolean binaryHexData = this.binaryHexData;
                    Pattern eolPattern = this.eolPattern;
                    Charset charset = this.charset;

                    // read data from serial device
                    while (inputStream.available() > 0) {
                        final int bytes = inputStream.read(readBuffer);
                        if (binaryHexData) {
                            for (int i = 0; i < bytes; i++) {
                                if (eolPattern == null) {
                                    // Should not happen, but the code actually allows this to happen,
                                    // so just make the compiler happy and suppress any warnings.
                                    throw new IOException(
                                            "Failed to parse input stream as HEX pattern: Parameter 'eolPattern' is null.");
                                }

                                line += String.format("%02X", readBuffer[i]);
                                if (eolPattern.matcher(line).find()) {
                                    sb.append(line).append(System.lineSeparator());
                                    line = "";
                                } else {
                                    line += " ";
                                }
                            }
                        } else {
                            sb.append(new String(readBuffer, 0, bytes, charset));
                        }
                    }
                    line = line.trim();
                    if (!line.isEmpty()) {
                        sb.append(line);
                    }

                    // Add wait states around reading the stream, so that interrupted transmissions
                    // are merged
                    if (readerActive.get()) {
                        reader = scheduler.schedule(() -> receiveAndProcess(sb, false), 100, TimeUnit.MILLISECONDS);
                    }
                } else {
                    final String result = sb.toString();

                    processInput(result);
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
            }
        } catch (final IOException e) {
            logger.debug("Error reading from serial port: {}", e.getMessage(), e);
            readerActive.set(false);
            handleIOException(e);
        }
    }

    protected void processInput(String result) {
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

        logger.debug("Writing '{}' to {}", string, getLogPrefix());
        try {
            synchronized (outputStream) {
                // write string to serial port
                if (isRawType) {
                    final RawType rt = RawType.valueOf(string);
                    outputStream.write(rt.getBytes());
                } else if (binaryHexData) {
                    outputStream.write(parseHexString(string));
                } else {
                    outputStream.write(string.getBytes(charset));
                }

                outputStream.flush();
            }
        } catch (final IOException e) {
            logger.warn("Error writing '{}' to {}: {}", string, getLogPrefix(), e.getMessage());
            handleIOException(e);
        } catch (IllegalArgumentException e) {
            logger.warn("Error writing '{}' to {}: {}", string, getLogPrefix(), e.getMessage());

        }
    }

    private byte[] parseHexString(String input) {
        input = input.replaceAll("\\s", "");
        return HexFormat.of().parseHex(input);
    }

    protected void handleIOException(IOException e) {
        // Only used in TcpBridgeHandler
    }

    protected abstract String getLogPrefix();
}
