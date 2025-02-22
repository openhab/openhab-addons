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
package org.openhab.binding.enocean.internal.transceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.TooManyListenersException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.EnOceanBindingConstants;
import org.openhab.binding.enocean.internal.EnOceanException;
import org.openhab.binding.enocean.internal.Helper;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.binding.enocean.internal.messages.BasePacket.ESPPacketType;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.EventMessage;
import org.openhab.binding.enocean.internal.messages.EventMessage.EventMessageType;
import org.openhab.binding.enocean.internal.messages.Response;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class EnOceanTransceiver implements SerialPortEventListener {

    public static final int ENOCEAN_MAX_DATA = 65790;

    // Thread management
    protected @Nullable Future<?> readingTask = null;
    private @Nullable Future<?> timeOutTask = null;

    protected Logger logger = LoggerFactory.getLogger(EnOceanTransceiver.class);

    private @Nullable SerialPortManager serialPortManager;
    private static final int ENOCEAN_DEFAULT_BAUD = 57600;
    protected String path;
    private @Nullable SerialPort serialPort;

    class Request {
        @Nullable
        BasePacket requestPacket;
        @Nullable
        Response responsePacket;
        @Nullable
        ResponseListener<? extends @Nullable Response> responseListener;
    }

    private class RequestQueue {
        private Queue<Request> queue = new LinkedBlockingQueue<>();
        private ScheduledExecutorService scheduler;

        public RequestQueue(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
        }

        public synchronized void enqueRequest(Request request) throws IOException {
            boolean wasEmpty = queue.isEmpty();

            if (queue.offer(request)) {
                if (wasEmpty) {
                    send();
                }
            } else {
                logger.error("Transmit queue overflow. Lost message: {}", request);
            }
        }

        private synchronized void sendNext() throws IOException {
            queue.poll();
            send();
        }

        private synchronized void send() throws IOException {
            if (!queue.isEmpty()) {
                currentRequest = queue.peek();
                try {
                    Request localCurrentRequest = currentRequest;
                    if (localCurrentRequest != null && localCurrentRequest.requestPacket != null) {
                        synchronized (localCurrentRequest) {
                            BasePacket rqPacket = localCurrentRequest.requestPacket;
                            if (currentRequest != null && rqPacket != null) {
                                logger.debug("Sending data, type {}, payload {}{}", rqPacket.getPacketType().name(),
                                        HexUtils.bytesToHex(rqPacket.getPayload()),
                                        HexUtils.bytesToHex(rqPacket.getOptionalPayload()));
                                byte[] b = serializePacket(rqPacket);
                                logger.trace("Sending raw data: {}", HexUtils.bytesToHex(b));
                                OutputStream localOutPutStream = outputStream;
                                if (localOutPutStream != null) {
                                    localOutPutStream.write(b);
                                    localOutPutStream.flush();
                                }
                                Future<?> localTimeOutTask = timeOutTask;
                                if (localTimeOutTask != null) {
                                    localTimeOutTask.cancel(true);
                                }

                                // slowdown sending of message to avoid hickups at receivers
                                // Todo tweak sending intervall (250 ist just a first try)
                                timeOutTask = scheduler.schedule(() -> {
                                    try {
                                        sendNext();
                                    } catch (IOException e) {
                                        logger.trace("Unable to process message", e);
                                        TransceiverErrorListener localListener = errorListener;
                                        if (localListener != null) {
                                            localListener.errorOccurred(e);
                                        }
                                        return;
                                    }
                                }, 250, TimeUnit.MILLISECONDS);
                            }
                        }
                    } else {
                        sendNext();
                    }
                } catch (EnOceanException e) {
                    logger.error("exception while sending data", e);
                }
            }
        }
    }

    RequestQueue requestQueue;
    @Nullable
    Request currentRequest = null;

    protected Map<Long, HashSet<PacketListener>> listeners;
    protected HashSet<EventListener> eventListeners;
    protected @Nullable TeachInListener teachInListener;

    protected @Nullable InputStream inputStream;
    protected @Nullable OutputStream outputStream;

    private byte[] filteredDeviceId = new byte[0];
    @Nullable
    TransceiverErrorListener errorListener;

    public EnOceanTransceiver(String path, TransceiverErrorListener errorListener, ScheduledExecutorService scheduler,
            @Nullable SerialPortManager serialPortManager) {
        requestQueue = new RequestQueue(scheduler);

        listeners = new HashMap<>();
        eventListeners = new HashSet<>();
        teachInListener = null;

        this.errorListener = errorListener;
        this.serialPortManager = serialPortManager;
        this.path = path;
    }

    public void initialize()
            throws UnsupportedCommOperationException, PortInUseException, IOException, TooManyListenersException {
        SerialPortManager localSerialPortManager = serialPortManager;
        if (localSerialPortManager == null) {
            throw new IOException("Could access the SerialPortManager, it was null");
        }
        SerialPortIdentifier id = localSerialPortManager.getIdentifier(path);
        if (id == null) {
            throw new IOException("Could not find a gateway on given path '" + path + "', "
                    + localSerialPortManager.getIdentifiers().count() + " ports available.");
        }

        try {
            serialPort = id.open(EnOceanBindingConstants.BINDING_ID, 1000);
        } catch (PortInUseException e) {
            logger.warn("EnOceanSerialTransceiver not initialized, port allready in use", e);
            return;
        }
        SerialPort localSerialPort = serialPort;
        if (localSerialPort == null) {
            logger.debug("EnOceanSerialTransceiver not initialized, serialPort was null");
            return;
        }
        localSerialPort.setSerialPortParams(ENOCEAN_DEFAULT_BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        try {
            localSerialPort.enableReceiveThreshold(1);
            localSerialPort.enableReceiveTimeout(100); // In ms. Small values mean faster shutdown but more cpu usage.
        } catch (UnsupportedCommOperationException e) {
            // rfc connections do not allow a ReceiveThreshold
            logger.debug("EnOceanSerialTransceiver encountered an UnsupportedCommOperationException while initilizing",
                    e);
        }

        inputStream = localSerialPort.getInputStream();
        outputStream = localSerialPort.getOutputStream();
        logger.info("EnOceanSerialTransceiver initialized");
    }

    public void startReceiving(ScheduledExecutorService scheduler) {
        @Nullable
        Future<?> readingTask = this.readingTask;
        if (readingTask == null || readingTask.isCancelled()) {
            this.readingTask = scheduler.submit(new Runnable() {
                @Override
                public void run() {
                    receivePackets();
                }
            });
        }
        logger.info("EnOceanSerialTransceiver RX thread started");
    }

    public void shutDown() {
        logger.debug("shutting down transceiver");
        logger.debug("Interrupt rx Thread");

        Future<?> timeOutTask = this.timeOutTask;
        if (timeOutTask != null) {
            timeOutTask.cancel(true);
            this.timeOutTask = null;
        }

        Future<?> readingTask = this.readingTask;
        if (readingTask != null) {
            readingTask.cancel(true);

            InputStream localInputStream = inputStream;
            if (localInputStream != null) {
                try {
                    localInputStream.close();
                } catch (IOException e) {
                    logger.debug("IOException occurred while closing the stream", e);
                }
            }
            this.readingTask = null;
        }

        listeners.clear();
        eventListeners.clear();
        teachInListener = null;
        errorListener = null;

        OutputStream localOutputStream = outputStream;
        if (localOutputStream != null) {
            try {
                localOutputStream.close();
            } catch (IOException e) {
                logger.debug("IOException occurred while closing the output stream", e);
            }
        }

        InputStream localInputStream = inputStream;
        if (localInputStream != null) {
            try {
                localInputStream.close();
            } catch (IOException e) {
                logger.debug("IOException occurred while closing the input stream", e);
            }
        }

        SerialPort localSerialPort = serialPort;
        if (localSerialPort != null) {
            logger.debug("Closing the serial port");
            localSerialPort.close();
        }

        serialPort = null;
        outputStream = null;
        inputStream = null;

        logger.info("Transceiver shutdown");
    }

    private void receivePackets() {
        byte[] buffer = new byte[1];

        Future<?> readingTask = this.readingTask;
        while (readingTask != null && !readingTask.isCancelled()) {
            int bytesRead = read(buffer, 1);
            if (bytesRead > 0) {
                processMessage(buffer[0]);
            }
        }
    }

    protected abstract void processMessage(byte firstByte);

    protected int read(byte[] buffer, int length) {
        InputStream localInputStream = inputStream;
        if (localInputStream != null) {
            try {
                return localInputStream.read(buffer, 0, length);
            } catch (IOException e) {
                logger.debug("IOException occurred while reading the input stream", e);
                return 0;
            }
        } else {
            logger.warn("Cannot read from null stream");
            Future<?> readingTask = this.readingTask;
            if (readingTask != null) {
                readingTask.cancel(true);
                this.readingTask = null;
            }
            TransceiverErrorListener localListener = errorListener;
            if (localListener != null) {
                localListener.errorOccurred(new IOException("Cannot read from null stream"));
            }
            return 0;
        }
    }

    protected void informListeners(BasePacket packet) {
        try {
            if (packet.getPacketType() == ESPPacketType.RADIO_ERP1) {
                ERP1Message msg = (ERP1Message) packet;
                byte[] senderId = msg.getSenderId();
                byte[] d = Helper.concatAll(msg.getPayload(), msg.getOptionalPayload());

                logger.debug("{} with RORG {} for {} payload {} received", packet.getPacketType().name(),
                        msg.getRORG().name(), HexUtils.bytesToHex(msg.getSenderId()), HexUtils.bytesToHex(d));

                if (msg.getRORG() != RORG.Unknown) {
                    if (senderId.length > 0) {
                        if (senderId.length > 2 && filteredDeviceId.length > 2 && senderId[0] == filteredDeviceId[0]
                                && senderId[1] == filteredDeviceId[1] && senderId[2] == filteredDeviceId[2]) {
                            // filter away own messages which are received through a repeater
                            return;
                        }

                        if (teachInListener != null && (msg.getIsTeachIn() || msg.getRORG() == RORG.RPS)) {
                            logger.info("Received teach in message from {}", HexUtils.bytesToHex(msg.getSenderId()));

                            TeachInListener localListener = teachInListener;
                            if (localListener != null) {
                                localListener.packetReceived(msg);
                            }
                            return;
                        } else if (teachInListener == null && msg.getIsTeachIn()) {
                            logger.info("Discard message because this is a teach-in telegram from {}!",
                                    HexUtils.bytesToHex(msg.getSenderId()));
                            return;
                        }

                        long s = Long.parseLong(HexUtils.bytesToHex(senderId), 16);
                        synchronized (this) {
                            HashSet<PacketListener> pl = listeners.get(s);
                            if (pl != null) {
                                pl.forEach(l -> l.packetReceived(msg));
                            }
                        }
                    }
                } else {
                    logger.debug("Received unknown RORG");
                }
            } else if (packet.getPacketType() == ESPPacketType.EVENT) {
                EventMessage event = (EventMessage) packet;

                byte[] d = Helper.concatAll(packet.getPayload(), packet.getOptionalPayload());
                logger.debug("{} with type {} payload {} received", ESPPacketType.EVENT.name(),
                        event.getEventMessageType().name(), HexUtils.bytesToHex(d));

                if (event.getEventMessageType() == EventMessageType.SA_CONFIRM_LEARN) {
                    byte[] senderId = event.getPayload(EventMessageType.SA_CONFIRM_LEARN.getDataLength() - 5, 4);

                    if (teachInListener != null) {
                        logger.info("Received smart teach in from {}", HexUtils.bytesToHex(senderId));
                        TeachInListener localListener = teachInListener;
                        if (localListener != null) {
                            localListener.eventReceived(event);
                        }
                        return;
                    } else {
                        logger.info("Discard message because this is a smart teach-in telegram from {}!",
                                HexUtils.bytesToHex(senderId));
                        return;
                    }
                }

                synchronized (this) {
                    eventListeners.forEach(l -> l.eventReceived(event));
                }
            }
        } catch (Exception e) {
            logger.error("Exception in informListeners", e);
        }
    }

    protected void handleResponse(Response response) throws IOException {
        Request localCurrentRequest = currentRequest;
        if (localCurrentRequest != null) {
            ResponseListener<? extends @Nullable Response> listener = localCurrentRequest.responseListener;
            if (listener != null) {
                localCurrentRequest.responsePacket = response;
                try {
                    listener.handleResponse(response);
                } catch (Exception e) {
                    logger.debug("Exception during response handling");
                } finally {
                    logger.trace("Response handled");
                }
            } else {
                logger.trace("Response without listener");
            }
        } else {
            logger.trace("Response without request");
        }
    }

    public void sendBasePacket(@Nullable BasePacket packet,
            @Nullable ResponseListener<? extends @Nullable Response> responseCallback) throws IOException {
        if (packet == null) {
            return;
        }

        logger.debug("Enqueue new send request with ESP3 type {} {} callback", packet.getPacketType().name(),
                responseCallback == null ? "without" : "with");
        Request r = new Request();
        r.requestPacket = packet;
        r.responseListener = responseCallback;

        requestQueue.enqueRequest(r);
    }

    protected abstract byte[] serializePacket(BasePacket packet) throws EnOceanException;

    public synchronized void addPacketListener(PacketListener listener, long senderIdToListenTo) {
        HashSet<PacketListener> lst = listeners.computeIfAbsent(senderIdToListenTo, k -> new HashSet<>());
        if (lst != null && lst.add(listener)) {
            logger.debug("Listener added: {}", senderIdToListenTo);
        }
    }

    public synchronized void removePacketListener(PacketListener listener, long senderIdToListenTo) {
        HashSet<PacketListener> pl = listeners.get(senderIdToListenTo);
        if (pl != null) {
            pl.remove(listener);
            if (pl.isEmpty()) {
                listeners.remove(senderIdToListenTo);
            }
        }
    }

    public synchronized void addEventMessageListener(EventListener listener) {
        eventListeners.add(listener);
    }

    public synchronized void removeEventMessageListener(EventListener listener) {
        eventListeners.remove(listener);
    }

    public void startDiscovery(TeachInListener teachInListener) {
        this.teachInListener = teachInListener;
    }

    public void stopDiscovery() {
        this.teachInListener = null;
    }

    public void setFilteredDeviceId(byte[] filteredDeviceId) {
        System.arraycopy(filteredDeviceId, 0, filteredDeviceId, 0, filteredDeviceId.length);
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            synchronized (this) {
                this.notify();
            }
        }
    }
}
