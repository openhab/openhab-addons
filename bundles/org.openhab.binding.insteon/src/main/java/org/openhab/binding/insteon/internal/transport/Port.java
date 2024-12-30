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
package org.openhab.binding.insteon.internal.transport;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.transport.message.MsgFactory;
import org.openhab.binding.insteon.internal.transport.message.Priority;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Port class represents a port, that is a connection to either an Insteon modem either through
 * a serial or USB port, or via an Insteon Hub.
 * It does the initialization of the port, and (via its inner classes IOStreamReader and IOStreamWriter)
 * manages the reading/writing of messages on the Insteon network.
 *
 * The IOStreamReader and IOStreamWriter class combined implement the somewhat tricky flow control protocol.
 * In combination with the MsgFactory class, the incoming data stream is turned into a Msg structure
 * for further processing by the upper layers (PortListener).
 *
 * A write queue is maintained to pace the flow of outgoing messages. Sending messages back-to-back
 * can lead to dropped messages.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Daniel Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class Port {
    private final Logger logger = LoggerFactory.getLogger(Port.class);

    private String name;
    private ScheduledExecutorService scheduler;
    private IOStream ioStream;
    private IOStreamReader reader = new IOStreamReader();
    private IOStreamWriter writer = new IOStreamWriter();
    private @Nullable ScheduledFuture<?> readJob;
    private @Nullable ScheduledFuture<?> writeJob;
    private Set<PortListener> listeners = new CopyOnWriteArraySet<>();
    private PriorityBlockingQueue<Msg> writeQueue = new PriorityBlockingQueue<>(10,
            Comparator.comparing(Msg::getPriority).thenComparingLong(Msg::getTimestamp));
    private AtomicBoolean connected = new AtomicBoolean(false);

    public Port(InsteonBridgeConfiguration config, HttpClient httpClient, ScheduledExecutorService scheduler,
            SerialPortManager serialPortManager) {
        this.name = config.getId();
        this.scheduler = scheduler;
        this.ioStream = IOStream.create(config, httpClient, scheduler, serialPortManager);
    }

    public String getName() {
        return name;
    }

    public void registerListener(PortListener listener) {
        if (listeners.add(listener)) {
            logger.trace("added listener for {}", listener.getClass().getSimpleName());
        }
    }

    public void unregisterListener(PortListener listener) {
        if (listeners.remove(listener)) {
            logger.trace("removed listener for {}", listener.getClass().getSimpleName());
        }
    }

    /**
     * Starts threads necessary for reading and writing
     *
     * @return true if port is connected, otherwise false
     */
    public boolean start() {
        if (connected.get()) {
            logger.debug("port {} already connected, no need to start it", name);
            return true;
        }

        logger.debug("starting port {}", name);

        writeQueue.clear();

        if (!ioStream.open()) {
            logger.debug("failed to open port {}", name);
            return false;
        }

        readJob = scheduler.schedule(reader, 0, TimeUnit.SECONDS);
        writeJob = scheduler.schedule(writer, 0, TimeUnit.SECONDS);

        connected.set(true);

        logger.trace("all threads for port {} started.", name);

        return true;
    }

    /**
     * Stops all threads
     */
    public void stop() {
        logger.debug("stopping port {}", name);

        connected.set(false);

        ioStream.close();

        ScheduledFuture<?> readJob = this.readJob;
        if (readJob != null) {
            readJob.cancel(true);
            this.readJob = null;
        }

        ScheduledFuture<?> writeJob = this.writeJob;
        if (writeJob != null) {
            writeJob.cancel(true);
            this.writeJob = null;
        }

        logger.trace("all threads for port {} stopped.", name);
    }

    /**
     * Adds message to the write queue
     *
     * @param msg message to be added to the write queue
     */
    public void writeMessage(Msg msg) {
        try {
            writeQueue.add(msg);
            logger.trace("enqueued msg ({}): {}", writeQueue.size(), msg);
        } catch (IllegalStateException e) {
            logger.debug("cannot write message {}, write queue is full!", msg);
        }
    }

    /**
     * Notifies that the port has disconnected
     */
    private void disconnected() {
        if (connected.getAndSet(false)) {
            logger.warn("port {} disconnected", name);
            listeners.forEach(PortListener::disconnected);
        }
    }

    /**
     * Notifies that the port has received a message
     *
     * @param msg the message received
     */
    private void messageReceived(Msg msg) {
        listeners.forEach(listener -> listener.messageReceived(msg));
    }

    /**
     * Notifies that the port has sent a message
     *
     * @param msg the message sent
     */
    private void messageSent(Msg msg) {
        listeners.forEach(listener -> listener.messageSent(msg));
    }

    /**
     * The IOStreamReader uses the MsgFactory to turn the incoming bytes into
     * Msgs for the listeners. It also communicates with the IOStreamWriter
     * to implement flow control.
     */
    private class IOStreamReader implements Runnable {
        private static final int READ_BUFFER_SIZE = 1024;

        private final MsgFactory msgFactory = new MsgFactory();

        @Override
        public void run() {
            logger.debug("starting reader thread");
            byte[] buffer = new byte[READ_BUFFER_SIZE];
            try {
                while (!Thread.interrupted()) {
                    logger.trace("reader checking for input data");
                    // this call blocks until input data is available
                    int len = ioStream.read(buffer);
                    if (len > 0) {
                        msgFactory.addData(buffer, len);
                        processMessages();
                    }
                }
            } catch (InterruptedException e) {
                logger.trace("reader thread got interrupted!");
            } catch (IOException e) {
                logger.trace("reader thread got an io exception", e);
                disconnected();
            }
            logger.debug("exiting reader thread!");
        }

        private void processMessages() {
            // call msgFactory.processData() until it is done processing buffer
            while (!msgFactory.isDone()) {
                try {
                    Msg msg = msgFactory.processData();
                    if (msg != null) {
                        logger.debug("got msg: {}", msg);
                        messageReceived(msg);
                        writer.messageReceived(msg);
                    }
                } catch (IOException e) {
                    // got bad data from modem,
                    // unblock those waiting for ack
                    writer.invalidMessageReceived();
                }
            }
        }
    }

    /**
     * Writes messages to the port. Flow control is implemented following Insteon
     * documents to avoid overloading the modem.
     */
    private class IOStreamWriter implements Runnable {
        private static final int REPLY_TIMEOUT_TIME = 8000; // milliseconds

        private static enum ReplyStatus {
            GOT_ACK,
            GOT_NACK,
            WAITING_FOR_ACK
        }

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition messageReceived = lock.newCondition();
        private final Condition replyReceived = lock.newCondition();
        private ReplyStatus replyStatus = ReplyStatus.GOT_ACK;
        private @Nullable Msg lastMsg;

        @Override
        public void run() {
            logger.debug("starting writer thread");
            try {
                while (!Thread.interrupted()) {
                    logger.trace("writer checking message queue");
                    Msg msg = writeQueue.take();
                    if (msg.isExpired()) {
                        logger.trace("skipping expired message: {}", msg);
                    } else {
                        logger.debug("writing: {}", msg);
                        ioStream.write(msg.getData());
                        messageSent(msg);
                        waitForReply(msg);
                        limitRate(ioStream.getRateLimitTime());
                    }
                }
            } catch (InterruptedException e) {
                logger.trace("writer thread got interrupted!");
            } catch (IOException e) {
                logger.trace("writer thread got an io exception", e);
                disconnected();
            }
            logger.debug("exiting writer thread!");
        }

        private void messageReceived(Msg msg) {
            lock.lock();
            try {
                if (replyStatus == ReplyStatus.WAITING_FOR_ACK) {
                    if (msg.isPureNack() || msg.isReplyOf(lastMsg)) {
                        replyStatus = msg.isPureNack() ? ReplyStatus.GOT_NACK : ReplyStatus.GOT_ACK;
                        logger.trace("signaling receipt of ack: {}", replyStatus == ReplyStatus.GOT_ACK);
                        replyReceived.signal();
                    }
                }
                messageReceived.signal();
            } finally {
                lock.unlock();
            }
        }

        private void invalidMessageReceived() {
            lock.lock();
            try {
                if (replyStatus == ReplyStatus.WAITING_FOR_ACK) {
                    logger.debug("got bad data back, must assume message was acked.");
                    replyStatus = ReplyStatus.GOT_ACK;
                    replyReceived.signal();
                }
                messageReceived.signal();
            } finally {
                lock.unlock();
            }
        }

        private void waitForReply(Msg msg) throws InterruptedException {
            lock.lock();
            try {
                lastMsg = msg;
                replyStatus = ReplyStatus.WAITING_FOR_ACK;
                logger.trace("waiting for reply ack");
                if (replyReceived.await(REPLY_TIMEOUT_TIME, TimeUnit.MILLISECONDS)) {
                    logger.trace("got reply ack: {}", replyStatus == ReplyStatus.GOT_ACK);
                } else {
                    logger.trace("reply ack timeout expired");
                    replyStatus = ReplyStatus.GOT_NACK;
                }
                if (replyStatus == ReplyStatus.GOT_NACK) {
                    logger.trace("retransmitting msg: {}", msg);
                    msg.setPriority(Priority.RETRANSMIT);
                    writeMessage(msg);
                }
            } finally {
                lock.unlock();
            }
        }

        private void limitRate(int time) throws InterruptedException {
            lock.lock();
            try {
                do {
                    logger.trace("writer rate limited for {} msec", time);
                } while (messageReceived.await(time, TimeUnit.MILLISECONDS));
            } finally {
                lock.unlock();
            }
        }
    }
}
