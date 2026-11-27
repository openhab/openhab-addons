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
import java.util.concurrent.CountDownLatch;
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
    /**
     * Serializes {@link #requestResponse} so only one write/wait cycle runs at a time. Must not be the handler
     * monitor: the wait loop sleeps without releasing a lock, and the reader needs that monitor to publish
     * {@code lastResponse}.
     */
    private final Object requestLock = new Object();
    private volatile boolean disposed;
    /**
     * Park gate for the currently published connection's {@link #serialEvent} callbacks. Published atomically with the
     * port; abandoned opens never touch this field. Teardown counts down only this latch before
     * removeEventListener/close.
     */
    private volatile @Nullable CountDownLatch serialEventPark;

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
     * <p>
     * The native open may block and runs outside the handler monitor. Resources are published to shared fields only
     * after a generation check under the monitor; a superseded attempt closes only its local port, never a replacement
     * connection.
     */
    private void openSerialConnection(int generation) {
        if (!isLifecycleCurrent(generation)) {
            return;
        }

        SerialOceanicBindingConfiguration config = getConfigAs(SerialOceanicBindingConfiguration.class);
        if (config.port == null) {
            return;
        }

        synchronized (this) {
            if (!isLifecycleCurrent(generation) || serialPort != null) {
                return;
            }
        }

        SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(config.port);

        if (portIdentifier == null) {
            String availablePorts = serialPortManager.getIdentifiers().map(id -> id.getName())
                    .collect(Collectors.joining(System.lineSeparator()));
            String description = String.format("Serial port '%s' could not be found. Available ports are:%n%s",
                    config.port, availablePorts);
            // Guard status under the publish monitor so a stale attempt cannot take a replacement OFFLINE
            if (publishOfflineIfCurrent(generation, ThingStatusDetail.CONFIGURATION_ERROR, description)) {
                // Port may appear later (USB unplug/replug) — keep retrying while the handler is active
                scheduleReconnect(generation);
            }
            return;
        }

        final SerialPort port;
        try {
            logger.info("Connecting to the Oceanic water softener using {}.", config.port);
            port = portIdentifier.open(this.getThing().getUID().getBindingId(), 2000);
        } catch (PortInUseException portInUseException) {
            if (publishOfflineIfCurrent(generation, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Port in use: " + config.port)) {
                scheduleReconnect(generation);
            }
            return;
        }

        try {
            port.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            port.enableReceiveThreshold(1);
            port.disableReceiveTimeout();

            InputStream in = Objects.requireNonNull(port.getInputStream(), "serial input stream");
            OutputStream out = Objects.requireNonNull(port.getOutputStream(), "serial output stream");
            // Latch is local until publish — abandoned opens must not arm/release the replacement's gate
            CountDownLatch attemptPark = new CountDownLatch(1);
            SerialPortReader reader = new SerialPortReader(in, generation);
            boolean listenerRejected = false;

            synchronized (this) {
                if (!isLifecycleCurrent(generation) || serialPort != null) {
                    // Superseded before publish — close only this attempt's resources (no latch/listener published)
                    closeLocalPort(port);
                    return;
                }
                serialPort = port;
                inputStream = in;
                outputStream = out;
                readerThread = reader;
                serialEventPark = attemptPark;
                try {
                    // Listener only after latch publish so callbacks park on this connection's gate
                    port.addEventListener(this);
                    port.notifyOnDataAvailable(true);
                } catch (TooManyListenersException e) {
                    readerThread = null;
                    inputStream = null;
                    outputStream = null;
                    serialPort = null;
                    serialEventPark = null;
                    attemptPark.countDown();
                    closeLocalPort(port);
                    listenerRejected = true;
                }
                if (!listenerRejected) {
                    // ONLINE before reader.start() so an immediate EOF/FD_SET OFFLINE is not overwritten
                    updateStatus(ThingStatus.ONLINE);
                    reader.start();
                }
            }
            if (listenerRejected) {
                if (publishOfflineIfCurrent(generation, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Too many listeners to serial port.")) {
                    scheduleReconnect(generation);
                }
            }
        } catch (UnsupportedCommOperationException | IOException e) {
            closeLocalPort(port);
            if (publishOfflineIfCurrent(generation, ThingStatusDetail.COMMUNICATION_ERROR, "Communication error")) {
                scheduleReconnect(generation);
            }
        }
    }

    /**
     * Publish an OFFLINE status only when {@code generation} is still current, under the same monitor used to publish
     * a successful connection. Prevents a superseded open from marking a healthy replacement OFFLINE without
     * scheduling a reconnect for that replacement.
     *
     * @return {@code true} if the status was published (caller may schedule reconnect)
     */
    private boolean publishOfflineIfCurrent(int generation, ThingStatusDetail detail, @Nullable String description) {
        synchronized (this) {
            if (!isLifecycleCurrent(generation)) {
                return false;
            }
            updateStatus(detail, description);
            return true;
        }
    }

    private void closeLocalPort(SerialPort port) {
        try {
            port.removeEventListener();
        } catch (Exception e) {
            logger.debug("Error while removing serial event listener from abandoned open: {}", e.getMessage());
        }
        try {
            port.close();
        } catch (Exception e) {
            logger.debug("Error while closing abandoned serial port: {}", e.getMessage());
        }
    }

    /**
     * Unblock {@link #serialEvent} for the given park gate so the serial provider thread can exit before the listener
     * is removed or the port is closed. Only the owning connection's latch may be passed here.
     */
    private void releaseSerialEventWait(@Nullable CountDownLatch owned) {
        if (owned != null) {
            owned.countDown();
        }
    }

    private boolean isLifecycleCurrent(int generation) {
        return !disposed && lifecycleGeneration.get() == generation;
    }

    private void scheduleReconnect(int generation) {
        if (!isLifecycleCurrent(generation)) {
            return;
        }
        synchronized (this) {
            if (!isLifecycleCurrent(generation)) {
                return;
            }
            cancelReconnect();
            logger.info("Scheduling Oceanic serial reconnect in {} seconds", RECONNECT_INTERVAL_SECONDS);
            reconnectJob = scheduler.schedule(() -> reconnect(generation), RECONNECT_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    private void cancelReconnect() {
        ScheduledFuture<?> reconnectJob = this.reconnectJob;
        if (reconnectJob != null) {
            reconnectJob.cancel(false);
            this.reconnectJob = null;
        }
    }

    private void reconnect(int generation) {
        synchronized (this) {
            if (!isLifecycleCurrent(generation)) {
                logger.debug("Skipping Oceanic serial reconnect; handler disposed or superseded");
                return;
            }
            logger.debug("Reconnecting Oceanic serial thing handler");
            // Soft reset only — do not call initialize()/dispose(); those would clear disposed or tear down polling
            closeSerialResources(true);
        }
        openSerialConnection(generation);
    }

    /**
     * Close the reader thread and serial resources currently published on this handler.
     *
     * @param joinReader whether to join the reader thread (must be false when called from the reader itself)
     */
    private synchronized void closeSerialResources(boolean joinReader) {
        SerialPortReader reader = readerThread;
        if (reader != null) {
            try {
                reader.requestStop();
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
        // Wake only this connection's park before removeEventListener/close — never a replacement's latch
        CountDownLatch park = serialEventPark;
        serialEventPark = null;
        releaseSerialEventWait(park);
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
     * Claims failure only for {@code connectionGeneration} via CAS so a stale reader/write cannot advance the
     * lifecycle or schedule cleanup of a replacement connection.
     */
    private void handleSerialFailure(int connectionGeneration, String reason) {
        if (disposed) {
            return;
        }
        // Atomically supersede only the connection generation that observed the failure
        if (!lifecycleGeneration.compareAndSet(connectionGeneration, connectionGeneration + 1)) {
            return;
        }
        if (disposed) {
            return;
        }
        final int failureGeneration = connectionGeneration + 1;
        logger.warn("Oceanic serial failure: {}", reason);
        // Guard status so a delayed failure cannot mark a healthy replacement OFFLINE without reconnect
        if (!publishOfflineIfCurrent(failureGeneration, ThingStatusDetail.COMMUNICATION_ERROR, reason)) {
            return;
        }
        // Release resources from the scheduler so we never join the reader from itself
        scheduler.execute(() -> {
            synchronized (SerialOceanicThingHandler.this) {
                if (!isLifecycleCurrent(failureGeneration)) {
                    return;
                }
                closeSerialResources(true);
                scheduleReconnect(failureGeneration);
            }
        });
    }

    @Override
    protected @Nullable String requestResponse(String commandAsString) {
        // Serialize requests on requestLock; never hold the handler monitor across the wait (see acceptResponse)
        synchronized (requestLock) {
            SerialOceanicBindingConfiguration config = getConfigAs(SerialOceanicBindingConfiguration.class);

            final OutputStream out;
            final SerialPortReader reader;
            final int connectionGeneration;
            synchronized (this) {
                out = outputStream;
                reader = readerThread;
                if (out == null || reader == null || getThing().getStatus() != ThingStatus.ONLINE) {
                    return "";
                }
                connectionGeneration = reader.getConnectionGeneration();
            }

            Throttler.lock(config.port);

            // Clear after throttle acquire so a delayed reply during the wait cannot satisfy this request
            reader.clearLastResponse();
            String request = commandAsString + "\r";

            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Requesting : {} ('{}')", request, request.getBytes());
                }
                out.write(request.getBytes());
                out.flush();
            } catch (IOException | IllegalStateException e) {
                // IllegalStateException: purejavacomm after concurrent close during Throttler.lock wait
                handleSerialFailure(connectionGeneration,
                        "Error writing '" + request + "' to serial port " + config.port + " : " + e.getMessage());
                Throttler.unlock(config.port);
                return "";
            }

            long timeStamp = System.currentTimeMillis();
            while ("".equals(reader.getLastResponse())) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("Interrupted while waiting for serial response: {}", e.getMessage());
                    break;
                }
                if (System.currentTimeMillis() - timeStamp > REQUEST_TIMEOUT) {
                    logger.warn("A timeout occurred while requesting data from the water softener");
                    reader.reset();
                    break;
                }
            }
            String response = reader.getLastResponse();

            Throttler.unlock(config.port);

            return response;
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        /*
         * Park the serial event thread until this connection's latch is released. Some serial providers (historically
         * nrjavaserial; also relevant when a provider spins on select/FD_SET) drive a high-CPU event loop unless this
         * callback blocks. Closing the port does not interrupt a sleeping Java thread, so await a per-connection latch
         * counted down from {@link #releaseSerialEventWait()} before removeEventListener/close.
         */
        logger.trace("Received a serial port event : {}", serialPortEvent.getEventType());
        CountDownLatch park = serialEventPark;
        if (park == null) {
            return;
        }
        try {
            park.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public class SerialPortReader extends Thread {

        private volatile boolean interrupted = false;
        /** Set before intentional shutdown so late EOF/errors do not report as connection failures. */
        private volatile boolean stopping = false;
        private final InputStream inputStream;
        private final int connectionGeneration;
        /** Connection-scoped response; superseded readers must not write the handler/shared field alone. */
        private volatile String lastResponse = "";
        int index = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");

        public SerialPortReader(InputStream in, int connectionGeneration) {
            super(String.format("OH-binding-%s-%s", OceanicBindingConstants.BINDING_ID, "SerialPortReader"));
            this.inputStream = in;
            this.connectionGeneration = connectionGeneration;
            this.setName("SerialPortReader-" + getThing().getUID());
        }

        int getConnectionGeneration() {
            return connectionGeneration;
        }

        void clearLastResponse() {
            lastResponse = "";
        }

        String getLastResponse() {
            return lastResponse;
        }

        /**
         * Publish a completed line only while this reader is still the active connection. Prevents a late read after a
         * bounded join timeout from satisfying a request on a replacement connection.
         */
        private void acceptResponse(String line) {
            if (stopping) {
                return;
            }
            synchronized (SerialOceanicThingHandler.this) {
                if (stopping || !this.equals(readerThread) || !isLifecycleCurrent(connectionGeneration)) {
                    return;
                }
                lastResponse = line;
                lastLineReceived = line;
            }
        }

        public void reset() {
            logger.trace("Resetting the SerialPortReader");
            index = 0;
        }

        /**
         * Request an intentional stop. Must be published before closing streams so a racing EOF does not claim failure.
         */
        void requestStop() {
            stopping = true;
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void interrupt() {
            requestStop();
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
                        if (!stopping) {
                            handleSerialFailure(connectionGeneration, "Serial input stream closed");
                        }
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

                                    acceptResponse(line);
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
                if (!interrupted && !stopping) {
                    interrupted = true;
                    logger.error("An exception occurred while reading serial port : {}", e.getMessage(), e);
                    handleSerialFailure(connectionGeneration, "purejavacomm FD_SET/select failure: " + e.getMessage());
                }
            } catch (Exception e) {
                if (!interrupted && !stopping) {
                    interrupted = true;
                    logger.error("An exception occurred while reading serial port : {}", e.getMessage(), e);
                    String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                    if (reason.contains("out of bounds") || reason.contains("FD_SET")) {
                        reason = "purejavacomm FD_SET/select failure: " + reason;
                    }
                    handleSerialFailure(connectionGeneration, reason);
                }
            }
            logger.debug("Serial port reader for '{}' has stopped", getThing().getUID());
        }
    }
}
