/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.panamaxfurman.internal.transport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.PanamaxFurmanAbstractHandler;
import org.openhab.binding.panamaxfurman.internal.protocol.PowerConditionerChannel;
import org.openhab.binding.panamaxfurman.internal.protocol.ProtocolMapper;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectionActiveEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectionBrokenEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectivityEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectivityListener;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanInformationReceivedEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanInformationReceivedListener;
import org.openhab.binding.panamaxfurman.internal.util.Util;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the communication transport layer to Panamax/Furman Power Conditioner devices which support asynchronous push
 * messaging via input and output streams.
 * Such communication transports do not require polling (telnet, RS232, etc) for state updates.
 *
 * see {@link http://resources.corebrands.com/products/M4315-PRO/pdf_M4315-PRO_manual.pdf}
 * for the telnet and RS232 streaming specs
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public abstract class PanamaxFurmanStreamTransport implements PanamaxFurmanTransport {
    private final Logger logger = LoggerFactory.getLogger(PanamaxFurmanStreamTransport.class);

    private final Set<PanamaxFurmanInformationReceivedListener> informationReceivedListeners = Collections
            .synchronizedSet(new HashSet<>());
    private final Set<PanamaxFurmanConnectivityListener> connectivityListeners = Collections
            .synchronizedSet(new HashSet<>());

    private static final long AVOID_DUPLICATE_REQUESTS_WITHIN_MILLIS = TimeUnit.SECONDS.toMillis(3);
    private static final Map<String, Long> REQUEST_LAST_SENT_AT = new ConcurrentHashMap<>();

    protected final Object ioMutex = new Object();
    private final ProtocolMapper protocolMapper;

    private volatile boolean stopReader = false;
    private @Nullable InputStreamReaderThread inputStreamReaderThread;
    private @Nullable BufferedWriter writer;

    public PanamaxFurmanStreamTransport() {
        this.protocolMapper = new TelnetAndRs232ProtocolMapper();
    }

    /**
     * Attempt to open a connection to the Power Conditioner.
     */
    protected abstract void openConnection() throws IOException;

    /**
     * @return true if the underlying transport appears to be connected to the device
     */
    protected abstract boolean isConnected();

    /**
     * @return the stream to receive updates from the Power Conditioner.
     */
    protected abstract InputStream getInputStream() throws IOException;

    /**
     * @return the stream to send commands to the Power Conditioner.
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    /**
     * request the subclass to close all connection objects and streams. This is typically required to unblock our
     * InputStreamReaderThread
     */
    protected abstract void closeConnectionAndStreams();

    private boolean checkConnected() {
        synchronized (ioMutex) {
            if (!isConnected()) {
                try {
                    openConnection();

                    this.stopReader = false;
                    this.inputStreamReaderThread = new InputStreamReaderThread(getInputStream());
                    this.inputStreamReaderThread.start();

                    this.writer = new BufferedWriter(
                            new OutputStreamWriter(getOutputStream(), StandardCharsets.US_ASCII));
                } catch (IOException ioException) {
                    onIOError(ioException);
                }
            }
            return isConnected();
        }
    }

    @Override
    public boolean requestStatusOf(String channelString) {
        Integer outletNumber = PanamaxFurmanAbstractHandler.getOutletFromChannelUID(channelString);
        PowerConditionerChannel channel = PowerConditionerChannel.from(channelString);
        String textToTransmitToThing = protocolMapper.buildQueryString(channel, outletNumber);
        if (textToTransmitToThing == null) {
            return true; // not supported, but still considered a success
        }
        if (System.currentTimeMillis() - REQUEST_LAST_SENT_AT.getOrDefault(textToTransmitToThing,
                0L) < AVOID_DUPLICATE_REQUESTS_WITHIN_MILLIS) {
            logger.debug("Dropping request of '{}' since it was sent less than {}ms ago.  @{}", textToTransmitToThing,
                    AVOID_DUPLICATE_REQUESTS_WITHIN_MILLIS, getConnectionName());
            return true; // for all intents and purposes, the request was sent
        } else {
            REQUEST_LAST_SENT_AT.put(textToTransmitToThing, System.currentTimeMillis());
            return sendRequestToDevice(textToTransmitToThing);
        }
    }

    @Override
    public boolean sendUpdateCommand(ChannelUID channelUID, Command command) {
        String channelString = channelUID.getId();
        Integer outletNumber = PanamaxFurmanAbstractHandler.getOutletFromChannelUID(channelString);
        PowerConditionerChannel channel = PowerConditionerChannel.from(channelString);
        if (!channel.getStateClass().isInstance(command)) {
            logger.error("Update command {} NOT sent:  it expected State of class {} but was passed {}.  @{}", channel,
                    channel.getStateClass(), command.getClass(), getConnectionName());
            return true; // only return false on connection errors
        }
        State stateToSet = channel.getStateClass().cast(command);
        String testToTransmitToThing = protocolMapper.buildUpdateString(channel, outletNumber, stateToSet);
        return sendRequestToDevice(testToTransmitToThing);
    }

    @SuppressWarnings("null")
    public boolean sendRequestToDevice(@Nullable String textToSend) {
        if (textToSend == null) {
            return true;
        }
        boolean isSent = false;
        synchronized (ioMutex) {
            String command = textToSend;
            if (checkConnected()) {
                try {
                    writer.write(command + "\r");
                    writer.flush();
                    isSent = true;
                    logger.debug("sent: {}    @{}", command, getConnectionName());
                    onActiveConnection();
                } catch (IOException e) {
                    onIOError(e);
                }
            }
        }
        return isSent;
    }

    private void onActiveConnection() {
        PanamaxFurmanConnectivityEvent connectivityEvent = new PanamaxFurmanConnectionActiveEvent();
        connectivityListeners.stream().forEach(cl -> cl.onConnectivityEvent(connectivityEvent));
    }

    private void onIOError(Exception e) {
        logger.warn("Error with connection to {}. Cause: {}", getConnectionName(), e.getMessage());
        if (logger.isDebugEnabled()) {
            logger.debug("Error with connection to {}", getConnectionName(), e);
        }
        synchronized (ioMutex) {
            shutdown();
            PanamaxFurmanConnectionBrokenEvent event = new PanamaxFurmanConnectionBrokenEvent(e.getMessage());
            connectivityListeners.stream().forEach(dl -> dl.onConnectivityEvent(event));
        }
    }

    @Override
    @SuppressWarnings("null")
    public void shutdown() {
        synchronized (ioMutex) {
            stopReader = true;
            // This will close the socket which should unblock the call in InputStreamReaderThread.readLine()
            closeConnectionAndStreams();
            if (inputStreamReaderThread != null) {
                try {
                    inputStreamReaderThread.shutdown();
                } finally {
                    inputStreamReaderThread = null;
                }
            }
        }
    }

    @Override
    public void addConnectivityListener(PanamaxFurmanConnectivityListener listener) {
        connectivityListeners.add(listener);
    }

    @Override
    public void addInformationReceivedListener(PanamaxFurmanInformationReceivedListener listener) {
        informationReceivedListeners.add(listener);
    }

    /**
     * Read incoming data from the Power Conditioner device and notify listeners accordingly.
     *
     * @author Dave Badia - Initial contribution
     *
     */
    private class InputStreamReaderThread extends Thread {
        private BufferedReader bufferedReader;

        /**
         * This latch is used to track when the thread has terminated
         */
        private CountDownLatch stopLatch;

        /**
         * Construct a reader thread to poll the given InputStream for incoming data
         */
        private InputStreamReaderThread(InputStream inputStream) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            this.stopLatch = new CountDownLatch(1);

            this.setDaemon(true);
            this.setName("PanamaxFurmanInputStreamReader-" + getConnectionName());
        }

        @Override
        public void run() {
            try {
                while (!stopReader && !Thread.currentThread().isInterrupted()) {
                    String receivedData = null;
                    if (!checkConnected()) {
                        return;
                    }
                    try {
                        // Blocks forever until data is received so DO NOT synchronize
                        receivedData = bufferedReader.readLine();
                    } catch (SocketTimeoutException e) {
                        logger.debug("SocketTimeoutException while trying to read data, retrying.  detail: {}.  @{}",
                                e.getMessage(), getConnectionName());
                        // Let the while loop try again
                    }

                    processReceivedData(receivedData);
                }
            } catch (IOException e) {
                // This is normal when the socket is closed, so don't log anything if we are stopping
                if (!stopReader) {
                    logger.debug("Caught error during socket read: {}.  @{}", e.getMessage(), getConnectionName());
                    onIOError(e);
                }
            } finally {
                // Notify the stopReader method caller that the reader is stopped.
                this.stopLatch.countDown();
                Util.closeQuietly(bufferedReader);
            }
            logger.debug("InputStreamReaderThread thread exiting.  @{}", getConnectionName());
        }

        private void processReceivedData(@Nullable String receivedData) {
            if (receivedData != null && !receivedData.isBlank()) {
                logger.debug("received: {}    @{}", receivedData, getConnectionName());

                // Data received means we are connected
                onActiveConnection();

                // Parse the event (if we can) and notify listeners accordingly
                @Nullable
                PanamaxFurmanInformationReceivedEvent infoEvent = protocolMapper.parseUpdateIfSupported(receivedData);
                if (infoEvent != null) {
                    synchronized (ioMutex) {
                        informationReceivedListeners.stream().forEach(ul -> ul.onInformationReceived(infoEvent));
                    }
                }
            }
        }

        private void shutdown() {
            try {
                // This needs to return quickly so keep the timeout short
                boolean stopped = this.stopLatch.await(500, TimeUnit.MILLISECONDS);
                if (!stopped) {
                    logger.debug("Timed out waiting for InputStreamReaderThread to stop.  @{}", getConnectionName());
                }
            } catch (InterruptedException e) {
                logger.debug("Timed out waiting for InputStreamReaderThread to stop.  @{}", getConnectionName());
            } finally {
                Util.closeQuietly(bufferedReader);
            }
        }
    }
}
