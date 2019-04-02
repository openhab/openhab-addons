/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.digiplex.handler;

import static org.openhab.binding.digiplex.DigiplexBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.digiplex.communication.CommunicationStatus;
import org.openhab.binding.digiplex.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.communication.DigiplexRequest;
import org.openhab.binding.digiplex.communication.DigiplexResponse;
import org.openhab.binding.digiplex.communication.DigiplexResponseResolver;
import org.openhab.binding.digiplex.communication.events.AbstractEvent;
import org.openhab.binding.digiplex.discovery.DigiplexDiscoveryService;
import org.openhab.binding.digiplex.internal.DigiplexBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link DigiplexBridgeHandler} is responsible for handling communication with PRT3 module
 *
 * @author Robert Michalak - Initial contribution
 */
@NonNullByDefault
public class DigiplexBridgeHandler extends BaseBridgeHandler implements SerialPortEventListener {

    private static final int REINITIALIZE_DELAY = 1; // in minutes
    private static final int STALLED_MESSAGES_THRESHOLD = 5;
    private static final int END_OF_MESSAGE = '\r';
    private static final int END_OF_STREAM = -1;

    private final Logger logger = LoggerFactory.getLogger(DigiplexBridgeHandler.class);

    @Nullable
    private DigiplexBridgeConfiguration config;
    @Nullable
    private RXTXPort serialPort;
    @Nullable
    private DigiplexReceiverThread receiverThread;
    @Nullable
    private DigiplexSenderThread senderThread;
    private BlockingQueue<DigiplexRequest> sendQueue = new LinkedBlockingQueue<>();
    private Set<DigiplexMessageHandler> handlers = ConcurrentHashMap.newKeySet();

    @Nullable
    private ScheduledFuture<?> reinitializeTask;

    private AtomicLong messagesSent = new AtomicLong(0);
    private AtomicLong responsesReceived = new AtomicLong(0);
    private AtomicLong eventsReceived = new AtomicLong(0);

    public DigiplexBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(DigiplexBridgeConfiguration.class);
        if (config.port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        }

        try {
            serialPort = initializeSerialPort(config.port);

            registerMessageHandler(new BridgeMessageHandler());

            messagesSent.set(0);
            responsesReceived.set(0);
            eventsReceived.set(0);

            receiverThread = new DigiplexReceiverThread(serialPort.getInputStream());
            senderThread = new DigiplexSenderThread(serialPort.getOutputStream());

            receiverThread.start();
            senderThread.start();

            updateStatus(ThingStatus.ONLINE);
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Port is in use!");
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Communication error!");
        }

    }

    @SuppressWarnings("null")
    private @Nullable RXTXPort initializeSerialPort(String port) throws PortInUseException, NoSuchPortException,
            TooManyListenersException, UnsupportedCommOperationException {
        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);

        RXTXPort serialPort = portId.open(getThing().getUID().toString(), 2000);
        serialPort.setSerialPortParams(config.baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.disableReceiveThreshold();
        serialPort.enableReceiveTimeout(1000);

        // RXTX serial port library causes high CPU load
        // Start event listener, which will just sleep and slow down event loop
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);

        return serialPort;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH && isLinked(channelUID.getId())) {
            switch (channelUID.getId()) {
                case BRIDGE_MESSAGES_SENT:
                    updateState(BRIDGE_MESSAGES_SENT, new DecimalType(messagesSent.get()));
                    break;
                case BRIDGE_RESPONSES_RECEIVED:
                    updateState(BRIDGE_RESPONSES_RECEIVED, new DecimalType(responsesReceived.get()));
                    break;
                case BRIDGE_EVENTS_RECEIVED:
                    updateState(BRIDGE_EVENTS_RECEIVED, new DecimalType(eventsReceived.get()));
                    break;
            }
        }
    }

    public void sendRequest(DigiplexRequest request) {
        sendQueue.add(request);
    }

    public void handleResponse(String message) {
        DigiplexResponse response = DigiplexResponseResolver.resolveResponse(message);
        handlers.forEach(visitor -> response.accept(visitor));
        if (response instanceof AbstractEvent) {
            updateState(BRIDGE_EVENTS_RECEIVED, new DecimalType(eventsReceived.incrementAndGet()));
        } else {
            updateState(BRIDGE_RESPONSES_RECEIVED, new DecimalType(responsesReceived.incrementAndGet()));
        }
    }

    public void registerMessageHandler(DigiplexMessageHandler handler) {
        handlers.add(handler);
    }

    public void unregisterMessageHandler(DigiplexMessageHandler handler) {
        handlers.remove(handler);
    }

    /**
     * Closes the connection to the PRT3 module.
     */
    @SuppressWarnings("null")
    @Override
    public void dispose() {
        stopThread(senderThread);
        stopThread(receiverThread);
        senderThread = null;
        receiverThread = null;
        if (serialPort != null) {
            IOUtils.closeQuietly(serialPort.getInputStream());
            IOUtils.closeQuietly(serialPort.getOutputStream());
            serialPort.close();
            serialPort = null;
        }
        logger.info("Stopped Digiplex serial handler");

        super.dispose();
    }

    private void stopThread(@Nullable Thread thread) {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public void handleCommunicationError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        if (reinitializeTask == null) {
            reinitializeTask = scheduler.schedule(() -> {
                logger.info("Reconnecting to PRT3 device...");
                thingUpdated(getThing());
                reinitializeTask = null;
            }, REINITIALIZE_DELAY, TimeUnit.MINUTES);
        }
    }

    @Override
    public void serialEvent(@Nullable SerialPortEvent arg0) {
        try {
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(DigiplexDiscoveryService.class);
    }

    private class BridgeMessageHandler implements DigiplexMessageHandler {

        @Override
        public void handleCommunicationStatus(CommunicationStatus response) {
            if (response.success) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    }

    private class DigiplexReceiverThread extends Thread {

        private final Logger logger = LoggerFactory.getLogger(DigiplexReceiverThread.class);

        private final InputStream stream;

        DigiplexReceiverThread(InputStream stream) {
            super("DigiplexReceiveThread");
            this.stream = stream;
        }

        @Override
        public void run() {
            logger.debug("Receiver thread started");
            while (!interrupted()) {
                try {
                    Optional<String> message = readLineBlocking();
                    message.ifPresent(m -> {
                        logger.debug("message received: '{}'", m);
                        handleResponse(m);
                    });
                    if (messagesSent.get() - responsesReceived.get() > STALLED_MESSAGES_THRESHOLD) {
                        throw new IOException("PRT3 module is not responding!");
                    }

                } catch (IOException e) {
                    handleCommunicationError();
                    break;
                }
            }
            logger.debug("Receiver thread finished");
        }

        private Optional<String> readLineBlocking() throws IOException {
            StringBuilder s = new StringBuilder();
            while (true) {
                int c = stream.read();
                if (c == END_OF_STREAM) {
                    return Optional.empty();
                }
                if (c == END_OF_MESSAGE) {
                    break;
                }
                s.append((char) c);
            }
            return Optional.of(s.toString());
        }
    }

    private class DigiplexSenderThread extends Thread {

        private static final int SLEEP_TIME = 150;

        private final Logger logger = LoggerFactory.getLogger(DigiplexSenderThread.class);

        private OutputStream stream;

        public DigiplexSenderThread(OutputStream stream) {
            super("DigiplexSenderThread");
            this.stream = stream;
        }

        @Override
        public void run() {
            logger.debug("Sender thread started");
            while (!interrupted()) {
                try {
                    DigiplexRequest request = sendQueue.take();
                    stream.write(request.getSerialMessage().getBytes());
                    stream.flush();
                    updateState(BRIDGE_MESSAGES_SENT, new DecimalType(messagesSent.incrementAndGet()));
                    logger.debug("message sent: '{}'", request.getSerialMessage().replaceAll("\r", ""));
                    Thread.sleep(SLEEP_TIME); // do not flood PRT3 with messages as it creates unpredictable responses
                } catch (IOException e) {
                    handleCommunicationError();
                    break;
                } catch (InterruptedException e) {
                    break;
                }
            }
            logger.debug("Sender thread finished");
        }
    }
}
