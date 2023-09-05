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
package org.openhab.binding.digiplex.internal.handler;

import static org.openhab.binding.digiplex.internal.DigiplexBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.digiplex.internal.DigiplexBridgeConfiguration;
import org.openhab.binding.digiplex.internal.communication.CommunicationStatus;
import org.openhab.binding.digiplex.internal.communication.DigiplexMessageHandler;
import org.openhab.binding.digiplex.internal.communication.DigiplexRequest;
import org.openhab.binding.digiplex.internal.communication.DigiplexResponse;
import org.openhab.binding.digiplex.internal.communication.DigiplexResponseResolver;
import org.openhab.binding.digiplex.internal.communication.events.AbstractEvent;
import org.openhab.binding.digiplex.internal.communication.events.TroubleEvent;
import org.openhab.binding.digiplex.internal.communication.events.TroubleStatus;
import org.openhab.binding.digiplex.internal.discovery.DigiplexDiscoveryService;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private @Nullable DigiplexBridgeConfiguration config;
    private @Nullable SerialPort serialPort;
    private @Nullable DigiplexReceiverThread receiverThread;
    private @Nullable DigiplexSenderThread senderThread;
    private final BlockingQueue<DigiplexRequest> sendQueue = new LinkedBlockingQueue<>();
    private final SerialPortManager serialPortManager;
    private final Set<DigiplexMessageHandler> handlers = ConcurrentHashMap.newKeySet();

    @Nullable
    private ScheduledFuture<?> reinitializeTask;

    private AtomicLong messagesSent = new AtomicLong(0);
    private AtomicLong responsesReceived = new AtomicLong(0);
    private AtomicLong eventsReceived = new AtomicLong(0);

    public DigiplexBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(DigiplexBridgeConfiguration.class);
        if (config.port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        }

        SerialPortIdentifier portId = serialPortManager.getIdentifier(config.port);
        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No such port: " + config.port);
            return;
        }

        try {
            serialPort = initializeSerialPort(portId);

            InputStream inputStream = serialPort.getInputStream();
            OutputStream outputStream = serialPort.getOutputStream();

            if (inputStream == null || outputStream == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Input/Output stream null");
                return;
            }

            receiverThread = new DigiplexReceiverThread(inputStream);
            senderThread = new DigiplexSenderThread(outputStream);

            registerMessageHandler(new BridgeMessageHandler());

            messagesSent.set(0);
            responsesReceived.set(0);
            eventsReceived.set(0);

            receiverThread.start();
            senderThread.start();

            updateStatus(ThingStatus.ONLINE);
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Port in use: " + config.port);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Communication error: " + e.getMessage());
        }
    }

    @SuppressWarnings("null")
    private @Nullable SerialPort initializeSerialPort(SerialPortIdentifier portId)
            throws PortInUseException, TooManyListenersException, UnsupportedCommOperationException {
        SerialPort serialPort = portId.open(getThing().getUID().toString(), 2000);
        serialPort.setSerialPortParams(config.baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.enableReceiveThreshold(0);
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
            try {
                InputStream inputStream = serialPort.getInputStream();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.debug("Error closing input stream", e);
            }

            try {
                OutputStream outputStream = serialPort.getOutputStream();
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                logger.debug("Error closing output stream", e);
            }

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
        return List.of(DigiplexDiscoveryService.class);
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

        @Override
        public void handleTroubleEvent(TroubleEvent troubleEvent) {
            if (troubleEvent.getAreaNo() == GLOBAL_AREA_NO) {
                String channel = troubleEvent.getType().getBridgeChannel();
                State state = OnOffType.from(troubleEvent.getStatus() == TroubleStatus.TROUBLE_STARTED);
                updateState(channel, state);
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
                    logger.debug("message sent: '{}'", request.getSerialMessage().replace("\r", ""));
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
