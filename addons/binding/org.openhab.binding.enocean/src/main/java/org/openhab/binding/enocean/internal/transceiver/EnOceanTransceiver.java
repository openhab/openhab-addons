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

import org.eclipse.smarthome.core.util.HexUtils;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.enocean.internal.EnOceanException;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.ESP3Packet;
import org.openhab.binding.enocean.internal.messages.ESP3PacketFactory;
import org.openhab.binding.enocean.internal.messages.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class EnOceanTransceiver {

    // Thread management
    private Future<?> readingTask = null;
    private Future<?> timeOut = null;

    private Logger logger = LoggerFactory.getLogger(EnOceanTransceiver.class);

    class Request {
        ESP3Packet RequestPacket;

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

                            byte[] b = currentRequest.RequestPacket.serialize();
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
                    logger.error("exception while sending data {}", e);
                }
            }
        }
    }

    RequestQueue requestQueue;
    Request currentRequest = null;

    protected Map<Long, HashSet<ESP3PacketListener>> listeners;
    protected ESP3PacketListener teachInListener;

    // Input and output streams, must be created by transceiver implementations
    protected InputStream inputStream;
    protected OutputStream outputStream;

    private byte[] filteredDeviceId;
    TransceiverErrorListener errorListener;

    enum ReadingState {
        WaitingForSyncByte,
        ReadingHeader,
        ReadingData
    }

    public EnOceanTransceiver(TransceiverErrorListener errorListener, ScheduledExecutorService scheduler) {

        requestQueue = new RequestQueue(scheduler);
        listeners = new HashMap<Long, HashSet<ESP3PacketListener>>();
        teachInListener = null;
        this.errorListener = errorListener;
    }

    public abstract void Initialize()
            throws UnsupportedCommOperationException, PortInUseException, IOException, TooManyListenersException;

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
    }

    private void receivePackets() {
        byte[] buffer = new byte[1];

        while (readingTask != null && !readingTask.isCancelled()) {

            int bytesRead = read(buffer, 1);
            if (bytesRead > 0) {
                // if byte == sync byte => processMessage
                processMessage(buffer[0]);
            }
        }
    }

    protected abstract int read(byte[] buffer, int length);

    byte[] dataBuffer = new byte[Helper.ENOCEAN_MAX_DATA];
    ReadingState state = ReadingState.WaitingForSyncByte; // we already received sync byte when we get called
    int currentPosition = 0;
    int dataLength = -1;
    int optionalLength = -1;
    byte packetType = -1;

    private void processMessage(byte firstByte) {

        byte[] readingBuffer = new byte[Helper.ENOCEAN_MAX_DATA];
        int bytesRead = -1;
        byte _byte;

        try {

            readingBuffer[0] = firstByte;

            bytesRead = this.inputStream.read(readingBuffer, 1, inputStream.available());
            if (bytesRead == -1) {
                throw new IOException("could not read from inputstream");
            }

            if (readingTask == null || readingTask.isCancelled()) {
                return;
            }

            bytesRead++;
            for (int p = 0; p < bytesRead; p++) {
                _byte = readingBuffer[p];

                switch (state) {
                    case WaitingForSyncByte:
                        if (_byte == Helper.ENOCEAN_SYNC_BYTE) {
                            state = ReadingState.ReadingHeader;
                            logger.trace("Received Sync Byte");
                        }
                        break;
                    case ReadingHeader:
                        if (currentPosition == Helper.ENOCEAN_HEADER_LENGTH) {
                            if (Helper.checkCRC8(dataBuffer, Helper.ENOCEAN_HEADER_LENGTH, _byte)
                                    && ((dataBuffer[0] & 0xFF) << 8) + (dataBuffer[1] & 0xFF)
                                            + (dataBuffer[2] & 0xFF) > 0) {

                                state = ReadingState.ReadingData;

                                dataLength = ((dataBuffer[0] & 0xFF << 8) | (dataBuffer[1] & 0xFF));
                                optionalLength = dataBuffer[2] & 0xFF;
                                packetType = dataBuffer[3];
                                currentPosition = 0;

                                if (packetType == 3) {
                                    logger.trace("Received sub_msg");
                                }

                                logger.trace(">> Received header, data length {} optional length {} packet type {}",
                                        dataLength, optionalLength, packetType);
                            } else {
                                // check if we find a sync byte in current buffer
                                int copyFrom = -1;
                                for (int i = 0; i < Helper.ENOCEAN_HEADER_LENGTH; i++) {
                                    if (dataBuffer[i] == Helper.ENOCEAN_SYNC_BYTE) {
                                        copyFrom = i + 1;
                                        break;
                                    }
                                }

                                if (copyFrom != -1) {
                                    System.arraycopy(dataBuffer, copyFrom, dataBuffer, 0,
                                            Helper.ENOCEAN_HEADER_LENGTH - copyFrom);
                                    state = ReadingState.ReadingHeader;
                                    currentPosition = Helper.ENOCEAN_HEADER_LENGTH - copyFrom;
                                    dataBuffer[currentPosition++] = _byte;
                                } else {
                                    currentPosition = 0;
                                    state = _byte == Helper.ENOCEAN_SYNC_BYTE ? ReadingState.ReadingHeader
                                            : ReadingState.WaitingForSyncByte;
                                }
                                logger.trace("CrC8 header check not successful");
                            }
                        } else {
                            dataBuffer[currentPosition++] = _byte;
                        }
                        break;
                    case ReadingData:
                        if (currentPosition == dataLength + optionalLength) {
                            if (Helper.checkCRC8(dataBuffer, dataLength + optionalLength, _byte)) {
                                state = ReadingState.WaitingForSyncByte;
                                ESP3Packet packet = ESP3PacketFactory.BuildPacket(dataLength, optionalLength,
                                        packetType, dataBuffer);

                                if (packet != null) {
                                    switch (packet.getPacketType()) {
                                        case COMMON_COMMAND:
                                            break;
                                        case EVENT:
                                            break;
                                        case RADIO_ERP1: {
                                            ERP1Message msg = (ERP1Message) packet;

                                            byte[] d = new byte[dataLength + optionalLength];
                                            System.arraycopy(dataBuffer, 0, d, 0, d.length);

                                            logger.debug("{} with RORG {} for {} payload {} received",
                                                    packet.getPacketType().name(), msg.getRORG().name(),
                                                    HexUtils.bytesToHex(msg.getSenderId()), HexUtils.bytesToHex(d));

                                            informListeners(msg);
                                        }
                                            break;
                                        case RADIO_ERP2:
                                            break;
                                        case RADIO_MESSAGE:
                                            break;
                                        case RADIO_SUB_TEL:
                                            break;
                                        case REMOTE_MAN_COMMAND:
                                            break;
                                        case RESPONSE: {
                                            byte[] d = new byte[dataLength + optionalLength];
                                            System.arraycopy(dataBuffer, 0, d, 0, d.length);

                                            logger.debug("{} with code {} payload {} received",
                                                    packet.getPacketType().name(),
                                                    ((Response) packet).getResponseType().name(),
                                                    HexUtils.bytesToHex(d));

                                            if (currentRequest != null) {
                                                if (currentRequest.ResponseListener != null) {
                                                    currentRequest.ResponsePacket = (Response) packet;
                                                    try {
                                                        currentRequest.ResponseListener
                                                                .handleResponse(currentRequest.ResponsePacket);
                                                    } catch (Exception e) {
                                                    }

                                                    logger.trace("Response handled");
                                                } else {
                                                    logger.trace("Response without listener");
                                                }
                                            }
                                        }
                                            break;
                                        case SMART_ACK_COMMAND:
                                            break;
                                        default:
                                            break;
                                    }
                                } else {
                                    logger.trace("Unknown ESP3Packet");
                                    byte[] d = new byte[dataLength + optionalLength];
                                    System.arraycopy(dataBuffer, 0, d, 0, d.length);
                                    logger.trace("{}", HexUtils.bytesToHex(d));
                                }
                            } else {
                                state = _byte == Helper.ENOCEAN_SYNC_BYTE ? ReadingState.ReadingHeader
                                        : ReadingState.WaitingForSyncByte;
                                logger.trace("esp packet malformed");
                            }

                            currentPosition = 0;
                            dataLength = optionalLength = packetType = -1;
                        } else {
                            dataBuffer[currentPosition++] = _byte;
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException ioexception) {
            errorListener.ErrorOccured(ioexception);
            return;
        }
    }

    public void sendESP3Packet(ESP3Packet packet, ResponseListener<? extends Response> responseCallback)
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

    protected void informListeners(ERP1Message msg) {
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
                    teachInListener.espPacketReceived(msg);
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
            HashSet<ESP3PacketListener> pl = listeners.get(s);
            if (pl != null) {
                pl.forEach(l -> l.espPacketReceived(msg));
            }
        }
    }

    public void addPacketListener(ESP3PacketListener listener) {

        if (listeners.computeIfAbsent(listener.getSenderIdToListenTo(), k -> new HashSet<ESP3PacketListener>())
                .add(listener)) {
            logger.debug("Listener added: {}", listener.getSenderIdToListenTo());
        }
    }

    public void removePacketListener(ESP3PacketListener listener) {
        HashSet<ESP3PacketListener> pl = listeners.get(listener.getSenderIdToListenTo());
        if (pl != null) {
            pl.remove(listener);
            if (pl.isEmpty()) {
                listeners.remove(listener.getSenderIdToListenTo());
            }
        }
    }

    public void startDiscovery(ESP3PacketListener teachInListener) {
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
}
