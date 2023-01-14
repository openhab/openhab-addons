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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractCommunicator} Abstract class with common low-level communication logic. Extended by the
 * communicator classes.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public abstract class AbstractCommunicator implements IParadoxInitialLoginCommunicator {

    protected static final int SOCKET_TIMEOUT = 4000;

    private static final long PACKET_EXPIRATION_TRESHOLD_MILLISECONDS = 2000;

    private final Logger logger = LoggerFactory.getLogger(AbstractCommunicator.class);

    protected ScheduledExecutorService scheduler;
    protected Collection<IDataUpdateListener> listeners;

    protected Socket socket;
    private ISocketTimeOutListener stoListener;

    private final String ipAddress;
    private final int tcpPort;
    private DataOutputStream tx;
    private DataInputStream rx;

    private boolean isOnline;

    public AbstractCommunicator(String ipAddress, int tcpPort, ScheduledExecutorService scheduler)
            throws UnknownHostException, IOException {
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        logger.debug("IP Address={}, TCP Port={}", ipAddress, tcpPort);
        this.scheduler = scheduler;

        initializeSocket();
    }

    protected void initializeSocket() throws IOException, UnknownHostException {
        if (socket != null && !socket.isClosed()) {
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
        setOnline(false);
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
        if (isEncrypted()) {
            request.getRequestPacket().encrypt();
        }
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
            logger.trace("Sending packet with request={}", request);
            byte[] packetBytes = request.getRequestPacket().getBytes();
            ParadoxUtil.printPacket("Tx Packet:", packetBytes);
            tx.write(packetBytes);
            syncQueue.moveRequest();
        } catch (SocketException e) {
            logger.debug("Socket time out occurred. Informing listener. Request={}. Exception=", request, e);
            syncQueue.removeSendRequest();
            stoListener.onSocketTimeOutOccurred(e);
        } catch (IOException e) {
            logger.debug("Error while sending packet with request={}. IOException=", request, e);
            syncQueue.removeSendRequest();
        }
    }

    protected void receivePacket() {
        SyncQueue syncQueue = SyncQueue.getInstance();
        try {
            logger.trace("Found packet to receive in queue...");
            byte[] result = new byte[256];
            int readBytes = rx.read(result);
            if (readBytes > 0 && result[1] > 0 && result[1] + 16 < 256) {
                logger.trace("Successfully read valid packet from Rx");
                IRequest request = syncQueue.poll();
                byte[] bytesData = Arrays.copyOfRange(result, 0, readBytes);
                IResponse response = new Response(request, bytesData, isEncrypted());

                if (response.getPayload() == null || response.getHeader() == null) {
                    handleWrongPacket(result, request);
                }

                IResponseReceiver responseReceiver = request.getResponseReceiver();
                if (responseReceiver != null) {
                    responseReceiver.receiveResponse(response, this);
                }
            } else if (SyncQueue.getInstance().peekReceiveQueue()
                    .isTimeStampExpired(PACKET_EXPIRATION_TRESHOLD_MILLISECONDS)) {
                logger.trace("Unable to receive proper package for {} time. Rescheduling...",
                        PACKET_EXPIRATION_TRESHOLD_MILLISECONDS);
            } else {
                IRequest requestInQueue = syncQueue.poll();
                logger.debug("Error receiving packet after reaching the set timeout of {}ms. Request: {}",
                        PACKET_EXPIRATION_TRESHOLD_MILLISECONDS, requestInQueue);
            }
        } catch (SocketException e) {
            IRequest request = syncQueue.poll();
            logger.debug("Socket time out occurred. Informing listener. Request={}, SocketException=", request, e);
            stoListener.onSocketTimeOutOccurred(e);
        } catch (IOException e) {
            IRequest request = syncQueue.poll();
            logger.debug("Unable to receive package due to IO Exception. Request {}, IOException=", request, e);
        }
    }

    protected void handleWrongPacket(byte[] result, IRequest request) throws IOException {
        logger.trace(
                "Payload or header are null. Probably unexpected package has been read. Need to retry read the same request.");
        rx.read(result);
        ParadoxUtil.printPacket("Flushing packet:", result);
        submitRequest(request);
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

    protected abstract void receiveEpromResponse(IResponse response);

    protected abstract void receiveRamResponse(IResponse response);

    public ISocketTimeOutListener getStoListener() {
        return stoListener;
    }

    @Override
    public void setStoListener(ISocketTimeOutListener stoListener) {
        this.stoListener = stoListener;
    }
}
