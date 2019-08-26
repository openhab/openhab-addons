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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.paradoxalarm.internal.communication.messages.HeaderMessageType;
import org.openhab.binding.paradoxalarm.internal.communication.messages.IPPacketPayload;
import org.openhab.binding.paradoxalarm.internal.communication.messages.ParadoxIPPacket;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractCommunicator} Abstract class with common low-level communication logic. Extended by the
 * communicator classes.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public abstract class AbstractCommunicator implements IConnectionHandler {

    protected static final int SOCKET_TIMEOUT = 4000;

    private static final long PACKET_EXPIRATION_TRESHOLD_MILLISECONDS = 2000;

    private final Logger logger = LoggerFactory.getLogger(AbstractCommunicator.class);

    protected ScheduledExecutorService scheduler;
    protected Collection<IDataUpdateListener> listeners;
    protected int retryCounter = 0;
    protected Socket socket;

    private final String ipAddress;
    private final int tcpPort;
    private DataOutputStream tx;
    private DataInputStream rx;

    private boolean isOnline;

    public AbstractCommunicator(String ipAddress, int tcpPort, ScheduledExecutorService scheduler)
            throws UnknownHostException, IOException {
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        this.scheduler = scheduler;

        initializeSocket();
    }

    protected void initializeSocket() throws IOException, UnknownHostException {
        if (socket != null) {
            close();
        }
        socket = new Socket(ipAddress, tcpPort);
        socket.setSoTimeout(SOCKET_TIMEOUT);
        tx = new DataOutputStream(socket.getOutputStream());
        rx = new DataInputStream(socket.getInputStream());
    }

    @Override
    public synchronized void close() {
        logger.info("Stopping communication to Paradox system");
        try {
            tx.close();
            rx.close();
            socket.close();
        } catch (IOException e) {
            logger.warn("IO exception during socket/stream close operation.", e);
        }
    }

    @Override
    public void submitRequest(IRequest request) {
        SyncQueue syncQueue = SyncQueue.getInstance();
        syncQueue.add(request);
        communicateToParadox();
    }

    protected void communicateToParadox() {
        SyncQueue syncQueue = SyncQueue.getInstance();
        synchronized (syncQueue) {
            if (syncQueue.hasPacketToReceive()) {
                receivePacket();
            } else if (syncQueue.hasPacketsToSend()) {
                sendPacket();
            }

            // Recursively check if there are more packets to send in TX queue until it becomes empty
            if (syncQueue.hasPacketsToSend() || syncQueue.hasPacketToReceive()) {
                communicateToParadox();
            }
        }
    }

    protected void sendPacket() {
        SyncQueue syncQueue = SyncQueue.getInstance();
        IRequest request = syncQueue.peekSendQueue();
        try {
            logger.debug("Sending packet with request={}", request);
            byte[] packetBytes = request.getRequestPayload().getBytes();
            ParadoxUtil.printPacket("Tx Packet:", packetBytes);
            tx.write(packetBytes);
            syncQueue.moveRequest();
        } catch (IOException e) {
            syncQueue.removeSendRequest();
            logger.debug("Error while sending packet with request={}. IOException={}. Will discard this request.",
                    request, e.getMessage());
        }
    }

    protected void receivePacket() {
        SyncQueue syncQueue = SyncQueue.getInstance();
        try {
            logger.debug("Found packet to receive in queue...");
            byte[] result = new byte[256];
            int readBytes = rx.read(result);
            if (readBytes > 0 && result[1] > 0 && result[1] + 16 < 256) {
                logger.debug("Successfully read valid packet from Rx");
                retryCounter = 0;
                IRequest request = syncQueue.poll();
                byte[] bytesData = Arrays.copyOfRange(result, 0, result[1] + 16);
                IResponse response = new Response(request, bytesData);
                handleReceivedPacket(response);
            } else if (SyncQueue.getInstance().peekReceiveQueue()
                    .isTimeStampExpired(PACKET_EXPIRATION_TRESHOLD_MILLISECONDS)) {
                logger.debug("Unable to receive proper package for {} time. Rescheduling...", retryCounter);
            } else {
                IRequest requestInQueue = syncQueue.poll();
                logger.debug("Error receiving packet after reaching the set treshold of retries. Request: {}",
                        requestInQueue);
                retryCounter = 0;
            }
        } catch (IOException e) {
            IRequest request = syncQueue.poll();
            retryCounter = 0;
            logger.debug("Unable to receive package due to IO Exception. Request {}. Exception={}", request,
                    e.getMessage());
        }
    }

    protected byte[] parsePacket(IResponse response) throws ParadoxException {
        byte[] payload = response.getPayload();
        return parsePacket((byte) 0x5, payload);
    }

    /**
     * This method reads data from the IP150 module. It can return multiple
     * responses e.g. a live event is combined with another response.
     * The open active TCP/IP stream.
     * A panel command, e.g. 0x5 (read memory
     * An array of an array of the raw bytes received from the TCP/IP
     * stream.
     *
     * @param command (currently it's only 0x5 but other commands can be used for different other areas)
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws ParadoxException
     */
    protected byte[] parsePacket(byte command, byte[] packetResponse) throws ParadoxException {
        // We might enter this too early, meaning the panel has not yet had time to
        // respond to our command. We add a retry counter that will wait and retry.
        final byte finalCommand = command > 0xF ? ParadoxUtil.getHighNibble(command) : command;

        List<byte[]> responses = splitResponsePackets(packetResponse);
        for (byte[] response : responses) {
            // Message too short
            if (response.length < 17) {
                continue;
            }

            // Response command (after header) is not related to reading memory
            if (ParadoxUtil.getHighNibble(response[16]) != finalCommand) {
                continue;
            }

            return Arrays.copyOfRange(response, 22, response.length - 1);
        }
        return null;
    }

    private List<byte[]> splitResponsePackets(byte[] response) throws ParadoxException {
        List<byte[]> packets = new ArrayList<>();
        byte[] responseCopy = Arrays.copyOf(response, response.length);
        int totalLength = responseCopy.length;
        while (responseCopy.length > 0) {
            if (responseCopy.length < 16 || responseCopy[0] != (byte) 0xAA) {
                logger.debug("No 16 byte header found");
            }

            byte[] header = Arrays.copyOfRange(responseCopy, 0, 16);
            byte messageLength = header[1];

            // Remove the header
            responseCopy = Arrays.copyOfRange(responseCopy, 16, totalLength);

            if (responseCopy.length < messageLength) {
                throw new ParadoxException("Unexpected end of data");
            }

            // Check if there's padding bytes (0xEE)
            if (responseCopy.length > messageLength) {
                for (int i = messageLength; i < responseCopy.length; i++) {
                    if (responseCopy[i] == 0xEE) {
                        messageLength++;
                    } else {
                        break;
                    }
                }
            }

            byte[] message = Arrays.copyOfRange(responseCopy, 0, messageLength);

            responseCopy = Arrays.copyOfRange(responseCopy, messageLength, responseCopy.length);

            packets.add(ParadoxUtil.mergeByteArrays(header, message));
        }

        return packets;
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public void setOnline(boolean flag) {
        this.isOnline = flag;
    }

    public DataInputStream getRx() {
        return rx;
    }

    protected ParadoxIPPacket createParadoxIpPacket(IPPacketPayload payload) {
        ParadoxIPPacket readEpromIPPacket = new ParadoxIPPacket(payload)
                .setMessageType(HeaderMessageType.SERIAL_PASSTHRU_REQUEST).setUnknown0((byte) 0x14);
        return readEpromIPPacket;
    }

    protected abstract void handleReceivedPacket(IResponse response);

    protected abstract void receiveEpromResponse(IResponse response) throws ParadoxException;

    protected abstract void receiveRamResponse(IResponse response) throws ParadoxException;
}
