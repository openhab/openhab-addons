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
package org.openhab.binding.oceanic.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.oceanic.internal.OceanicBindingConstants;
import org.openhab.binding.oceanic.internal.SerialOceanicBindingConfiguration;
import org.openhab.binding.oceanic.internal.Throttler;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SerialOceanicThingHandler} implements {@link OceanicThingHandler} for an Oceanic water softener that is
 * directly connected to a serial port of the openHAB host
 *
 * @author Karel Goderis - Initial contribution
 * @author Karel Goderis - Harden serial reader against purejavacomm FD_SET failures
 */
@NonNullByDefault
public class SerialOceanicThingHandler extends OceanicThingHandler implements SerialPortEventListener {

    private static final long REQUEST_TIMEOUT = 10000;
    private static final int BAUD = 19200;
    private static final int RECONNECT_INTERVAL_SECONDS = 15;

    private final Logger logger = LoggerFactory.getLogger(SerialOceanicThingHandler.class);

    private final SerialPortManager serialPortManager;
    private final AtomicInteger lifecycleGeneration = new AtomicInteger();
    private volatile boolean disposed;

    private @Nullable SerialPort serialPort;
    private @Nullable InputStream inputStream;
    private @Nullable OutputStream outputStream;
    private @Nullable SerialPortReader readerThread;
    private @Nullable ScheduledFuture<?> reconnectJob;

    public SerialOceanicThingHandler(Thing thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        disposed = false;
        final int generation = lifecycleGeneration.incrementAndGet();
        super.initialize();
        openSerialConnection(generation);
    }

    @Override
    public void dispose() {
        disposed = true;
        lifecycleGeneration.incrementAndGet();
        cancelReconnect();
        closeSerialResources(true);
        super.dispose();
    }

    /**
     * Open the configured serial port for the given lifecycle generation. Used by framework
     * {@link #initialize()} and by soft reconnect; never clears {@link #disposed}.
     */
    private void openSerialConnection(int generation) {
        if (!isLifecycleCurrent(generation)) {
            return;
        }

        SerialOceanicBindingConfiguration config = getConfigAs(SerialOceanicBindingConfiguration.class);

        if (serialPort != null || config.port == null) {
            return;
        }

        SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(config.port);

        if (portIdentifier == null) {
            String availablePorts = serialPortManager.getIdentifiers().map(id -> id.getName())
                    .collect(Collectors.joining(System.lineSeparator()));
            String description = String.format("Serial port '%s' could not be found. Available ports are:%n%s",
                    config.port, availablePorts);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, description);
            // Port may appear later (USB unplug/replug) — keep retrying while the handler is active
            scheduleReconnect(generation);
            return;
        }

        try {
            logger.info("Connecting to the Oceanic water softener using {}.", config.port);
            SerialPort port = portIdentifier.open(this.getThing().getUID().getBindingId(), 2000);
            if (!isLifecycleCurrent(generation)) {
                port.close();
                return;
            }
            serialPort = port;

            port.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            port.enableReceiveThreshold(1);
            port.disableReceiveTimeout();

            InputStream in = Objects.requireNonNull(port.getInputStream(), "serial input stream");
            OutputStream out = Objects.requireNonNull(port.getOutputStream(), "serial output stream");
            inputStream = in;
            outputStream = out;

            port.addEventListener(this);
            port.notifyOnDataAvailable(true);

            SerialPortReader reader = new SerialPortReader(in);
            readerThread = reader;
            reader.start();

            if (!isLifecycleCurrent(generation)) {
                closeSerialResources(true);
                return;
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (PortInUseException portInUseException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Port in use: " + config.port);
            scheduleReconnect(generation);
        } catch (UnsupportedCommOperationException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication error");
            scheduleReconnect(generation);
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Too many listeners to serial port.");
            scheduleReconnect(generation);
        }
    }

    private boolean isLifecycleCurrent(int generation) {
        return !disposed && lifecycleGeneration.get() == generation;
    }

    private void scheduleReconnect(int generation) {
        if (!isLifecycleCurrent(generation)) {
            return;
        }
        cancelReconnect();
        logger.info("Scheduling Oceanic serial reconnect in {} seconds", RECONNECT_INTERVAL_SECONDS);
        reconnectJob = scheduler.schedule(() -> reconnect(generation), RECONNECT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void cancelReconnect() {
        ScheduledFuture<?> reconnectJob = this.reconnectJob;
        if (reconnectJob != null) {
            reconnectJob.cancel(false);
            this.reconnectJob = null;
        }
    }

    private void reconnect(int generation) {
        if (!isLifecycleCurrent(generation)) {
            logger.debug("Skipping Oceanic serial reconnect; handler disposed or superseded");
            return;
        }
        logger.debug("Reconnecting Oceanic serial thing handler");
        // Soft reset only — do not call initialize()/dispose(); those would clear disposed or tear down polling
        closeSerialResources(true);
        openSerialConnection(generation);
    }

    /**
     * Close the reader thread and serial resources.
     *
     * @param joinReader whether to join the reader thread (must be false when called from the reader itself)
     */
    private synchronized void closeSerialResources(boolean joinReader) {
        SerialPortReader reader = readerThread;
        if (reader != null) {
            try {
                reader.interrupt();
                if (joinReader && !reader.equals(Thread.currentThread())) {
                    reader.join(2000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Interrupted while joining the serial port reader thread: {}", e.getMessage());
            }
        }

        InputStream in = inputStream;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.debug("Error while closing the input stream: {}", e.getMessage());
            }
        }
        OutputStream out = outputStream;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                logger.debug("Error while closing the output stream: {}", e.getMessage());
            }
        }
        SerialPort port = serialPort;
        if (port != null) {
            try {
                port.removeEventListener();
            } catch (Exception e) {
                logger.debug("Error while removing serial event listener: {}", e.getMessage());
            }
            port.close();
        }

        readerThread = null;
        inputStream = null;
        outputStream = null;
        serialPort = null;
    }

    /**
     * Stop the reader, mark the thing OFFLINE, release the port, and schedule a backoff reconnect.
     * Never continue reading after FD_SET / stream-closed style failures (avoids busy-loop CPU use).
     * <p>
     * Bumps {@link #lifecycleGeneration} immediately so already-queued failure/reconnect work for the previous
     * generation cannot close a newer connection or revive the handler after {@link #dispose()}.
     */
    private void handleSerialFailure(String reason) {
        if (disposed) {
            return;
        }
        final int generation = lifecycleGeneration.incrementAndGet();
        if (disposed) {
            return;
        }
        logger.warn("Oceanic serial failure: {}", reason);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        // Release resources from the scheduler so we never join the reader from itself
        scheduler.execute(() -> {
            if (!isLifecycleCurrent(generation)) {
                return;
            }
            closeSerialResources(true);
            scheduleReconnect(generation);
        });
    }

    @Override
    protected @Nullable String requestResponse(String commandAsString) {
        synchronized (this) {
            SerialOceanicBindingConfiguration config = getConfigAs(SerialOceanicBindingConfiguration.class);

            OutputStream out = outputStream;
            if (out == null || getThing().getStatus() != ThingStatus.ONLINE) {
                return "";
            }

            Throttler.lock(config.port);

            lastLineReceived = "";
            String request = commandAsString + "\r";

            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Requesting : {} ('{}')", request, request.getBytes());
                }
                out.write(request.getBytes());
                out.flush();
            } catch (IOException e) {
                handleSerialFailure(
                        "Error writing '" + request + "' to serial port " + config.port + " : " + e.getMessage());
                Throttler.unlock(config.port);
                return "";
            }

            long timeStamp = System.currentTimeMillis();
            while ("".equals(lastLineReceived)) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("Interrupted while waiting for serial response: {}", e.getMessage());
                    break;
                }
                if (System.currentTimeMillis() - timeStamp > REQUEST_TIMEOUT) {
                    logger.warn("A timeout occurred while requesting data from the water softener");
                    SerialPortReader reader = readerThread;
                    if (reader != null) {
                        reader.reset();
                    }
                    break;
                }
            }
            String response = lastLineReceived;

            Throttler.unlock(config.port);

            return response;
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        /*
         * Put the serial event thread to sleep. Some serial providers (historically nrjavaserial; also relevant when a
         * provider spins on select/FD_SET) drive a high-CPU event loop unless this callback blocks.
         */
        try {
            logger.trace("Received a serial port event : {}", serialPortEvent.getEventType());
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public class SerialPortReader extends Thread {

        private boolean interrupted = false;
        private final InputStream inputStream;
        int index = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");

        public SerialPortReader(InputStream in) {
            super(String.format("OH-binding-%s-%s", OceanicBindingConstants.BINDING_ID, "SerialPortReader"));
            this.inputStream = in;
            this.setName("SerialPortReader-" + getThing().getUID());
        }

        public void reset() {
            logger.trace("Resetting the SerialPortReader");
            index = 0;
        }

        @Override
        public void interrupt() {
            logger.trace("Interrupting the SerialPortReader");
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            logger.trace("Starting the serial port reader");

            byte[] dataBuffer = new byte[bufferSize];
            byte[] tmpData = new byte[bufferSize];
            String line;

            final byte lineFeed = (byte) '\n';
            final byte carriageReturn = (byte) '\r';
            final byte nullChar = (byte) '\0';

            long sleep = 10;
            int len = -1;

            try {
                while (!interrupted) {
                    logger.trace("Reading the inputStream");

                    len = inputStream.read(tmpData);
                    if (len < 0) {
                        // EOF / stream closed — do not busy-loop on repeated read() calls
                        interrupted = true;
                        handleSerialFailure("Serial input stream closed");
                        return;
                    }

                    if (len > 0) {
                        if (logger.isTraceEnabled()) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < len; i++) {
                                sb.append(String.format("%02X ", tmpData[i]));
                            }
                            logger.trace("Read {} bytes : {}", len, sb.toString());
                        }
                        for (int i = 0; i < len; i++) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Byte {} equals '{}' (hex '{}')", i, new String(new byte[] { tmpData[i] }),
                                        String.format("%02X", tmpData[i]));
                            }

                            if (tmpData[i] != lineFeed && tmpData[i] != carriageReturn && tmpData[i] != nullChar) {
                                dataBuffer[index++] = tmpData[i];
                                if (logger.isTraceEnabled()) {
                                    logger.trace("dataBuffer[{}] set to '{}'(hex '{}')", index - 1,
                                            new String(new byte[] { dataBuffer[index - 1] }),
                                            String.format("%02X", dataBuffer[index - 1]));
                                }
                            }

                            if (i > 0 && (tmpData[i] == lineFeed || tmpData[i] == carriageReturn
                                    || tmpData[i] == nullChar)) {
                                if (index > 0) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("The resulting line is '{}'",
                                                new String(Arrays.copyOf(dataBuffer, index)));
                                    }
                                    line = Objects.requireNonNull(
                                            StringUtils.chomp(new String(Arrays.copyOf(dataBuffer, index))));
                                    line = line.replace(",", ".");
                                    line = line.trim();
                                    index = 0;

                                    lastLineReceived = line;
                                    break;
                                }
                            }

                            if (index == bufferSize) {
                                index = 0;
                            }
                        }
                    }

                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        interrupted = true;
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // Typical on macOS purejavacomm when FD >= 1024 (select fd_set is 1024 bits)
                if (!interrupted) {
                    interrupted = true;
                    logger.error("An exception occurred while reading serial port : {}", e.getMessage(), e);
                    handleSerialFailure("purejavacomm FD_SET/select failure: " + e.getMessage());
                }
            } catch (Exception e) {
                if (!interrupted) {
                    interrupted = true;
                    logger.error("An exception occurred while reading serial port : {}", e.getMessage(), e);
                    String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                    if (reason.contains("out of bounds") || reason.contains("FD_SET")) {
                        reason = "purejavacomm FD_SET/select failure: " + reason;
                    }
                    handleSerialFailure(reason);
                }
            }
            logger.debug("Serial port reader for '{}' has stopped", getThing().getUID());
        }
    }
}
