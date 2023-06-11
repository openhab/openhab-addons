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
package org.openhab.binding.lcn.internal.connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channel;
import java.nio.channels.CompletionHandler;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.openhab.binding.lcn.internal.common.LcnAddrGrp;
import org.openhab.binding.lcn.internal.common.LcnAddrMod;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a configured connection to one LCN-PCHK.
 * It uses a {@link AsynchronousSocketChannel} to connect to LCN-PCHK.
 * Included logic:
 * <ul>
 * <li>Reconnection on connection loss
 * <li>Segment scan (to detect the local segment ID)
 * <li>Acknowledge handling
 * <li>Periodic value requests
 * <li>Caching of runtime data about the underlying LCN bus
 * </ul>
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class Connection {
    private final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static final int BROADCAST_MODULE_ID = 3;
    private static final int BROADCAST_SEGMENT_ID = 3;
    private final ConnectionSettings settings;
    private final ConnectionCallback callback;
    @Nullable
    private AsynchronousSocketChannel channel;
    /** The local segment id. -1 means "unknown". */
    private int localSegId;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private final ByteArrayOutputStream sendBuffer = new ByteArrayOutputStream();
    private final Queue<@Nullable SendData> sendQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<PckQueueItem> offlineSendQueue = new LinkedBlockingQueue<>();
    private final Map<LcnAddr, ModInfo> modData = Collections.synchronizedMap(new HashMap<>());
    private volatile boolean writeInProgress;
    private final ScheduledExecutorService scheduler;
    private final ConnectionStateMachine connectionStateMachine;

    /**
     * Constructs a clean (disconnected) connection with the given settings.
     * This does not start the actual connection process.
     *
     * @param sets the settings to use for the new connection
     * @param callback the callback to the owner
     * @throws IOException
     */
    public Connection(ConnectionSettings sets, ScheduledExecutorService scheduler, ConnectionCallback callback) {
        this.settings = sets;
        this.callback = callback;
        this.scheduler = scheduler;
        this.clearRuntimeData();

        connectionStateMachine = new ConnectionStateMachine(this, scheduler);
    }

    /** Clears all runtime data. */
    void clearRuntimeData() {
        this.channel = null;
        this.localSegId = -1;
        this.readBuffer.clear();
        this.sendQueue.clear();
        this.sendBuffer.reset();
    }

    /**
     * Retrieves the settings for this connection (never changed).
     *
     * @return the settings
     */
    public ConnectionSettings getSettings() {
        return this.settings;
    }

    private boolean isSocketConnected() {
        try {
            AsynchronousSocketChannel localChannel = channel;
            return localChannel != null && localChannel.getRemoteAddress() != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Sets the local segment id.
     *
     * @param localSegId the new local segment id
     */
    public void setLocalSegId(int localSegId) {
        this.localSegId = localSegId;
    }

    /**
     * Called whenever an acknowledge is received.
     *
     * @param addr the source LCN module
     * @param code the LCN internal code (-1 = "positive")
     */
    public void onAck(LcnAddrMod addr, int code) {
        synchronized (modData) {
            if (modData.containsKey(addr)) {
                ModInfo modInfo = modData.get(addr);
                if (modInfo != null) {
                    modInfo.onAck(code, this, this.settings.getTimeout(), System.currentTimeMillis());
                }
            }
        }
    }

    /**
     * Creates and/or returns cached data for the given LCN module.
     *
     * @param addr the module's address
     * @return the data
     */
    public ModInfo updateModuleData(LcnAddrMod addr) {
        return Objects.requireNonNull(modData.computeIfAbsent(addr, ModInfo::new));
    }

    /**
     * Reads and processes input from the underlying channel.
     * Fragmented input is kept in {@link #readBuffer} and will be processed with the next call.
     *
     * @throws IOException if connection was closed or a generic channel error occurred
     */
    void readAndProcess() {
        AsynchronousSocketChannel localChannel = channel;
        if (localChannel != null && isSocketConnected()) {
            localChannel.read(readBuffer, null, new CompletionHandler<@Nullable Integer, @Nullable Void>() {
                @Override
                public void completed(@Nullable Integer transmittedByteCount, @Nullable Void attachment) {
                    synchronized (Connection.this) {
                        if (transmittedByteCount == null || transmittedByteCount == -1) {
                            String msg = "Connection was closed by foreign host.";
                            connectionStateMachine.handleConnectionFailed(new LcnException(msg));
                        } else {
                            // read data chunks from socket and separate frames
                            readBuffer.flip();
                            int aPos = readBuffer.position(); // 0
                            String s = new String(readBuffer.array(), aPos, transmittedByteCount, LcnDefs.LCN_ENCODING);
                            int pos1 = 0, pos2 = s.indexOf(PckGenerator.TERMINATION, pos1);
                            while (pos2 != -1) {
                                String data = s.substring(pos1, pos2);
                                if (logger.isTraceEnabled()) {
                                    logger.trace("Received: '{}'", data);
                                }
                                scheduler.submit(() -> {
                                    connectionStateMachine.onInputReceived(data);
                                    callback.onPckMessageReceived(data);
                                });
                                // Seek position in input array
                                aPos += s.substring(pos1, pos2 + 1).getBytes(LcnDefs.LCN_ENCODING).length;
                                // Next input
                                pos1 = pos2 + 1;
                                pos2 = s.indexOf(PckGenerator.TERMINATION, pos1);
                            }
                            readBuffer.limit(readBuffer.capacity());
                            readBuffer.position(transmittedByteCount - aPos); // Keeps fragments for the next call

                            if (isSocketConnected()) {
                                readAndProcess();
                            }
                        }
                    }
                }

                @Override
                public void failed(@Nullable Throwable e, @Nullable Void attachment) {
                    logger.debug("Lost connection");
                    connectionStateMachine.handleConnectionFailed(e);
                }
            });
        } else {
            connectionStateMachine.handleConnectionFailed(new LcnException("Socket not open"));
        }
    }

    /**
     * Writes all queued data.
     * Will try to write all data at once to reduce overhead.
     */
    public synchronized void triggerWriteToSocket() {
        AsynchronousSocketChannel localChannel = channel;
        if (localChannel == null || !isSocketConnected() || writeInProgress) {
            return;
        }
        sendBuffer.reset();
        SendData item = sendQueue.poll();

        if (item != null) {
            try {
                if (!item.write(sendBuffer, localSegId)) {
                    logger.warn("Data loss: Could not write packet into send buffer");
                }

                writeInProgress = true;
                byte[] data = sendBuffer.toByteArray();
                localChannel.write(ByteBuffer.wrap(data), null,
                        new CompletionHandler<@Nullable Integer, @Nullable Void>() {
                            @Override
                            public void completed(@Nullable Integer result, @Nullable Void attachment) {
                                synchronized (Connection.this) {
                                    if (result != data.length) {
                                        logger.warn("Data loss while writing to channel: {}", settings.getAddress());
                                    } else {
                                        if (logger.isTraceEnabled()) {
                                            logger.trace("Sent: {}",
                                                    new String(data, 0, data.length, LcnDefs.LCN_ENCODING).trim());
                                        }
                                    }

                                    writeInProgress = false;

                                    if (sendQueue.size() > 0) {
                                        /**
                                         * This could lead to stack overflows, since the CompletionHandler may run in
                                         * the same Thread as triggerWriteToSocket() is invoked (see
                                         * {@link AsynchronousChannelGroup}/Threading), but we do not expect as much
                                         * data in one chunk here, that the stack can be filled in a critical way.
                                         */
                                        triggerWriteToSocket();
                                    }
                                }
                            }

                            @Override
                            public void failed(@Nullable Throwable exc, @Nullable Void attachment) {
                                synchronized (Connection.this) {
                                    if (exc != null) {
                                        logger.warn("Writing to channel \"{}\" failed: {}", settings.getAddress(),
                                                exc.getMessage());
                                    }
                                    writeInProgress = false;
                                    connectionStateMachine.handleConnectionFailed(new LcnException("write() failed"));
                                }
                            }
                        });
            } catch (BufferOverflowException | IOException e) {
                logger.warn("Sending failed: {}: {}: {}", item, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * Queues plain text to be sent to LCN-PCHK.
     * Sending will be done the next time {@link #triggerWriteToSocket()} is called.
     *
     * @param plainText the text
     */
    public void queueDirectlyPlainText(String plainText) {
        this.queueAndSend(new SendDataPlainText(plainText));
    }

    /**
     * Queues a PCK command to be sent.
     *
     * @param addr the target LCN address
     * @param wantsAck true to wait for acknowledge on receipt (should be false for group addresses)
     * @param pck the pure PCK command (without address header)
     */
    void queueDirectly(LcnAddr addr, boolean wantsAck, String pck) {
        this.queueDirectly(addr, wantsAck, pck.getBytes(LcnDefs.LCN_ENCODING));
    }

    /**
     * Queues a PCK command for immediate sending, regardless of the Connection state. The PCK command is automatically
     * re-sent if the destination is not a group, an Ack is requested and the module did not answer within the expected
     * time.
     *
     * @param addr the target LCN address
     * @param wantsAck true to wait for acknowledge on receipt (should be false for group addresses)
     * @param data the pure PCK command (without address header)
     */
    void queueDirectly(LcnAddr addr, boolean wantsAck, byte[] data) {
        if (!addr.isGroup() && wantsAck) {
            this.updateModuleData((LcnAddrMod) addr).queuePckCommandWithAck(data, this, this.settings.getTimeout(),
                    System.currentTimeMillis());
        } else {
            this.queueAndSend(new SendDataPck(addr, false, data));
        }
    }

    /**
     * Enqueues a raw PCK command and triggers the socket to start sending, if it does not already. Does not take care
     * of any Acks.
     *
     * @param data raw PCK command
     */
    synchronized void queueAndSend(SendData data) {
        this.sendQueue.add(data);

        triggerWriteToSocket();
    }

    /**
     * Enqueues a PCK command to the offline queue. Data will be sent when the Connection state will enter
     * {@link ConnectionStateConnected}.
     *
     * @param addr LCN module address
     * @param wantsAck true, if the LCN module shall respond with an Ack on successful processing
     * @param data the pure PCK command (without address header)
     */
    void queueOffline(LcnAddr addr, boolean wantsAck, byte[] data) {
        offlineSendQueue.add(new PckQueueItem(addr, wantsAck, data));
    }

    /**
     * Enqueues a PCK command for sending. Takes care of the Connection state and buffers the command for a specific
     * time if the Connection is not ready. If an Ack is requested, the PCK command is automatically
     * re-sent, if the module did not answer in the expected time.
     *
     * @param addr LCN module address
     * @param wantsAck true, if the LCN module shall respond with an Ack on successful processing
     * @param pck the pure PCK command (without address header)
     */
    public void queue(LcnAddr addr, boolean wantsAck, String pck) {
        this.queue(addr, wantsAck, pck.getBytes(LcnDefs.LCN_ENCODING));
    }

    /**
     * Enqueues a PCK command for sending. Takes care of the Connection state and buffers the command for a specific
     * time if the Connection is not ready. If an Ack is requested, the PCK command is automatically
     * re-sent, if the module did not answer in the expected time.
     *
     * @param addr LCN module address
     * @param wantsAck true, if the LCN module shall respond with an Ack on successful processing
     * @param pck the pure PCK command (without address header)
     */
    public void queue(LcnAddr addr, boolean wantsAck, byte[] pck) {
        connectionStateMachine.queue(addr, wantsAck, pck);
    }

    /**
     * Process the offline PCK command queue. Does only send recently enqueued PCK commands, the rest is discarded.
     */
    void sendOfflineQueue() {
        List<PckQueueItem> allItems = new ArrayList<>(offlineSendQueue.size());
        offlineSendQueue.drainTo(allItems);

        allItems.forEach(item -> {
            // only send messages that were enqueued recently, discard older messages
            long timeout = settings.getTimeout();
            if (item.getEnqueued().isAfter(Instant.now().minus(timeout * 4, ChronoUnit.MILLIS))) {
                queueDirectly(item.getAddr(), item.isWantsAck(), item.getData());
            }
        });
    }

    /**
     * Gets the Connection's callback.
     *
     * @return the callback
     */
    public ConnectionCallback getCallback() {
        return callback;
    }

    /**
     * Sets the SocketChannel of this Connection
     *
     * @param channel the new Channel
     */
    public void setSocketChannel(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    /**
     * Gets the SocketChannel of the Connection.
     *
     * @returnthe socket channel
     */
    @Nullable
    public Channel getSocketChannel() {
        return channel;
    }

    /**
     * Gets the local segment ID. When no segments are used, the local segment ID is 0.
     *
     * @return the local segment ID
     */
    public int getLocalSegId() {
        return localSegId;
    }

    /**
     * Runs the periodic updates on all ModInfos.
     */
    public void updateModInfos() {
        synchronized (modData) {
            modData.values().forEach(i -> i.update(this, settings.getTimeout(), System.currentTimeMillis()));
        }
    }

    /**
     * Removes an LCN module from the ModData list.
     *
     * @param addr the module's address to be removed
     */
    public void removeLcnModule(LcnAddr addr) {
        modData.remove(addr);
    }

    /**
     * Invoked when this Connection shall be shut-down finally.
     */
    public void shutdown() {
        connectionStateMachine.shutdownFinally();
    }

    /**
     * Sends a broadcast to all LCN modules with a reuqest to respond with an Ack.
     */
    public void sendModuleDiscoveryCommand() {
        queueAndSend(new SendDataPck(new LcnAddrGrp(BROADCAST_SEGMENT_ID, BROADCAST_MODULE_ID), true,
                PckGenerator.nullCommand().getBytes(LcnDefs.LCN_ENCODING)));
        queueAndSend(new SendDataPck(new LcnAddrGrp(0, BROADCAST_MODULE_ID), true,
                PckGenerator.nullCommand().getBytes(LcnDefs.LCN_ENCODING)));
    }

    /**
     * Requests the serial number and the firmware version of the given LCN module.
     *
     * @param addr module's address
     */
    public void sendSerialNumberRequest(LcnAddrMod addr) {
        queueDirectly(addr, false, PckGenerator.requestSn());
    }

    /**
     * Requests theprogrammed name of the given LCN module.
     *
     * @param addr module's address
     */
    public void sendModuleNameRequest(LcnAddrMod addr) {
        queueDirectly(addr, false, PckGenerator.requestModuleName(0));
        queueDirectly(addr, false, PckGenerator.requestModuleName(1));
    }
}
