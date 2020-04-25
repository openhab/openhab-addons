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

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.util.HexUtils;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.enocean.internal.EnOceanBindingConstants;
import org.openhab.binding.enocean.internal.EnOceanException;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class EnOceanTransceiver implements SerialPortEventListener {

    public static final int ENOCEAN_MAX_DATA = 65790;

    // Thread management
    protected Future<?> readingTask = null;
    private Future<?> timeOut = null;

    protected Logger logger = LoggerFactory.getLogger(EnOceanTransceiver.class);

    private SerialPortManager serialPortManager;
    private static final int ENOCEAN_DEFAULT_BAUD = 57600;
    protected String path;
    SerialPort serialPort;

    class Request {
        BasePacket RequestPacket;

        Response ResponsePacket;
        ResponseListener<? extends Response> ResponseListener;
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
                    if (currentRequest != null && currentRequest.RequestPacket != null) {
                        synchronized (currentRequest) {
                            logger.debug("Sending data, type {}, payload {}{}",
                                    currentRequest.RequestPacket.getPacketType().name(),
                                    HexUtils.bytesToHex(currentRequest.RequestPacket.getPayload()),
                                    HexUtils.bytesToHex(currentRequest.RequestPacket.getOptionalPayload()));

                            byte[] b = serializePacket(currentRequest.RequestPacket);
                            logger.trace("Sending raw data: {}", HexUtils.bytesToHex(b));
                            outputStream.write(b);
                            outputStream.flush();

                            if (timeOut != null) {
                                timeOut.cancel(true);
                            }

                            // slowdown sending of message to avoid hickups at receivers
                            // Todo tweak sending intervall (250 ist just a first try)
                            timeOut = scheduler.schedule(() -> {
                                try {
                                    sendNext();
                                } catch (IOException e) {
                                    errorListener.ErrorOccured(e);
                                    return;
                                }
                            }, 250, TimeUnit.MILLISECONDS);
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
    Request currentRequest = null;

    protected Map<Long, HashSet<PacketListener>> listeners;
    protected PacketListener teachInListener;

    protected InputStream inputStream;
    protected OutputStream outputStream;

    private byte[] filteredDeviceId;
    TransceiverErrorListener errorListener;

    public EnOceanTransceiver(String path, TransceiverErrorListener errorListener, ScheduledExecutorService scheduler,
            SerialPortManager serialPortManager) {
        requestQueue = new RequestQueue(scheduler);

        listeners = new HashMap<>();
        teachInListener = null;

        this.errorListener = errorListener;
        this.serialPortManager = serialPortManager;
        this.path = path;
    }

    public void Initialize()
            throws UnsupportedCommOperationException, PortInUseException, IOException, TooManyListenersException {
        SerialPortIdentifier id = serialPortManager.getIdentifier(path);
        if (id == null) {
            throw new IOException("Could not find a gateway on given path '" + path + "', "
                    + serialPortManager.getIdentifiers().count() + " ports available.");
        }

        serialPort = id.open(EnOceanBindingConstants.BINDING_ID, 1000);
        serialPort.setSerialPortParams(ENOCEAN_DEFAULT_BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        try {
            serialPort.enableReceiveThreshold(1);
            serialPort.enableReceiveTimeout(100); // In ms. Small values mean faster shutdown but more cpu usage.
        } catch (UnsupportedCommOperationException e) {
            // rfc connections do not allow a ReceiveThreshold
        }

        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();

        logger.info("EnOceanSerialTransceiver initialized");
    }

    public void StartReceiving(ScheduledExecutorService scheduler) {
        if (readingTask == null || readingTask.isCancelled()) {
            readingTask = scheduler.submit(new Runnable() {
                @Override
                public void run() {
                    receivePackets();
                }
            });
        }
    }

    public void ShutDown() {
        logger.debug("shutting down transceiver");
        logger.debug("Interrupt rx Thread");

        if (timeOut != null) {
            timeOut.cancel(true);
        }

        if (readingTask != null) {
            readingTask.cancel(true);
            try {
                inputStream.close();
            } catch (Exception e) {
            }
        }

        readingTask = null;
        timeOut = null;
        listeners.clear();
        teachInListener = null;
        errorListener = null;

        if (outputStream != null) {
            logger.debug("Closing serial output stream");
            IOUtils.closeQuietly(outputStream);
        }
        if (inputStream != null) {
            logger.debug("Closeing serial input stream");
            IOUtils.closeQuietly(inputStream);
        }

        if (serialPort != null) {
            logger.debug("Closing serial port");
            serialPort.close();
        }

        serialPort = null;
        outputStream = null;
        inputStream = null;

        logger.info("Transceiver shutdown");
    }

    private void receivePackets() {
        byte[] buffer = new byte[1];

        while (readingTask != null && !readingTask.isCancelled()) {
            int bytesRead = read(buffer, 1);
            if (bytesRead > 0) {
                processMessage(buffer[0]);
            }
        }
    }

    protected abstract void processMessage(byte firstByte);

    protected int read(byte[] buffer, int length) {
        try {
            return this.inputStream.read(buffer, 0, length);
        } catch (IOException e) {
            return 0;
        }
    }

    protected void informListeners(ERP1Message msg) {
        try {
            byte[] senderId = msg.getSenderId();

            if (senderId != null) {
                if (filteredDeviceId != null && senderId[0] == filteredDeviceId[0] && senderId[1] == filteredDeviceId[1]
                        && senderId[2] == filteredDeviceId[2]) {
                    // filter away own messages which are received through a repeater
                    return;
                }

                if (teachInListener != null) {
                    if (msg.getIsTeachIn() || (msg.getRORG() == RORG.RPS)) {
                        logger.info("Received teach in message from {}", HexUtils.bytesToHex(msg.getSenderId()));
                        teachInListener.packetReceived(msg);
                        return;
                    }
                } else {
                    if (msg.getIsTeachIn()) {
                        logger.info("Discard message because this is a teach-in telegram from {}!",
                                HexUtils.bytesToHex(msg.getSenderId()));
                        return;
                    }
                }

                long s = Long.parseLong(HexUtils.bytesToHex(senderId), 16);
                HashSet<PacketListener> pl = listeners.get(s);
                if (pl != null) {
                    pl.forEach(l -> l.packetReceived(msg));
                }
            }
        } catch (Exception e) {
            logger.error("Exception in informListeners", e);
        }
    }

    protected void handleResponse(Response response) throws IOException {
        if (currentRequest != null) {
            if (currentRequest.ResponseListener != null) {
                currentRequest.ResponsePacket = response;
                try {
                    currentRequest.ResponseListener.handleResponse(response);
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

    public void sendBasePacket(BasePacket packet, ResponseListener<? extends Response> responseCallback)
            throws IOException {
        if (packet == null) {
            return;
        }

        logger.debug("Enqueue new send request with ESP3 type {} {} callback", packet.getPacketType().name(),
                responseCallback == null ? "without" : "with");
        Request r = new Request();
        r.RequestPacket = packet;
        r.ResponseListener = responseCallback;

        requestQueue.enqueRequest(r);
    }

    protected abstract byte[] serializePacket(BasePacket packet) throws EnOceanException;

    public void addPacketListener(PacketListener listener, long senderIdToListenTo) {
        if (listeners.computeIfAbsent(senderIdToListenTo, k -> new HashSet<>()).add(listener)) {
            logger.debug("Listener added: {}", senderIdToListenTo);
        }
    }

    public void removePacketListener(PacketListener listener, long senderIdToListenTo) {
        HashSet<PacketListener> pl = listeners.get(senderIdToListenTo);
        if (pl != null) {
            pl.remove(listener);
            if (pl.isEmpty()) {
                listeners.remove(senderIdToListenTo);
            }
        }
    }

    public void startDiscovery(PacketListener teachInListener) {
        this.teachInListener = teachInListener;
    }

    public void stopDiscovery() {
        this.teachInListener = null;
    }

    public void setFilteredDeviceId(byte[] filteredDeviceId) {
        if (filteredDeviceId != null) {
            System.arraycopy(filteredDeviceId, 0, filteredDeviceId, 0, filteredDeviceId.length);
        }
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
