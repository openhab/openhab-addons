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
package org.openhab.binding.insteon.internal.driver;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.message.MsgFactory;
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
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class Port {
    private final Logger logger = LoggerFactory.getLogger(Port.class);

    /**
     * The ReplyType is used to keep track of the state of the serial port receiver
     */
    enum ReplyType {
        GOT_ACK,
        WAITING_FOR_ACK,
        GOT_NACK
    }

    private String name;
    private IOStream ioStream;
    private IOStreamReader reader;
    private IOStreamWriter writer;
    private @Nullable Thread readThread;
    private @Nullable Thread writeThread;
    private MsgFactory msgFactory = new MsgFactory();
    private Set<PortListener> listeners = new CopyOnWriteArraySet<>();
    private LinkedBlockingQueue<Msg> writeQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean connected = new AtomicBoolean(false);

    public Port(InsteonBridgeConfiguration config, ScheduledExecutorService scheduler,
            @Nullable SerialPortManager serialPortManager) {
        this.name = config.getId();
        this.ioStream = IOStream.create(config, scheduler, serialPortManager);
        this.reader = new IOStreamReader();
        this.writer = new IOStreamWriter();
    }

    public String getName() {
        return name;
    }

    public void addListener(PortListener listener) {
        if (listeners.add(listener)) {
            if (logger.isTraceEnabled()) {
                logger.trace("added listener for {}", listener.getClass().getSimpleName());
            }
        }
    }

    public void removeListener(PortListener listener) {
        if (listeners.remove(listener)) {
            if (logger.isTraceEnabled()) {
                logger.trace("removed listener for {}", listener.getClass().getSimpleName());
            }
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

        Thread readThread = new Thread(reader);
        readThread.setName("OH-binding-" + InsteonBindingConstants.BINDING_ID + "-ioStreamReader:" + name);
        readThread.setDaemon(true);
        readThread.start();
        this.readThread = readThread;

        Thread writeThread = new Thread(writer);
        writeThread.setName("OH-binding-" + InsteonBindingConstants.BINDING_ID + "-ioStreamWriter:" + name);
        writeThread.setDaemon(true);
        writeThread.start();
        this.writeThread = writeThread;

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

        if (ioStream.isOpen()) {
            ioStream.close();
        }

        Thread readThread = this.readThread;
        if (readThread != null) {
            try {
                readThread.interrupt();
                readThread.join();
            } catch (InterruptedException e) {
                logger.debug("got interrupted waiting for read thread to exit.");
            }
            this.readThread = null;
        }

        Thread writeThread = this.writeThread;
        if (writeThread != null) {
            try {
                writeThread.interrupt();
                writeThread.join();
            } catch (InterruptedException e) {
                logger.debug("got interrupted waiting for write thread to exit.");
            }
            this.writeThread = null;
        }

        logger.trace("all threads for port {} stopped.", name);
    }

    /**
     * Adds message to the write queue
     *
     * @param msg message to be added to the write queue
     * @throws IOException
     */
    public void writeMessage(@Nullable Msg msg) throws IOException {
        if (msg == null) {
            throw new IOException("trying to write null message!");
        }
        try {
            writeQueue.add(msg);
            if (logger.isTraceEnabled()) {
                logger.trace("enqueued msg ({}): {}", writeQueue.size(), msg);
            }
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
            listeners.forEach(listener -> listener.disconnected());
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
     * to implement flow control (tell the IOStreamWriter that it needs to retransmit,
     * or the reply message has been received correctly).
     */
    private class IOStreamReader implements Runnable {
        private static final int READ_BUFFER_SIZE = 1024;
        private static final int REPLY_TIMEOUT_TIME = 30000; // milliseconds

        private ReplyType replyType = ReplyType.GOT_ACK;
        private Object replyLock = new Object();

        public Object getReplyLock() {
            return replyLock;
        }

        @Override
        public void run() {
            logger.debug("starting reader thread");
            byte[] buffer = new byte[READ_BUFFER_SIZE];
            try {
                while (!Thread.interrupted()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("reader checking for input data");
                    }
                    // this call blocks until input data is available
                    int len = ioStream.read(buffer);
                    if (len > 0) {
                        msgFactory.addData(buffer, len);
                        processMessages();
                    }
                }
            } catch (InterruptedException e) {
                logger.debug("reader thread got interrupted!");
            } catch (IOException e) {
                logger.debug("reader thread got an io exception", e);
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
                        if (logger.isDebugEnabled()) {
                            logger.debug("got msg: {}", msg);
                        }
                        messageReceived(msg);
                        notifyWriter(msg);
                    }
                } catch (IOException e) {
                    // got bad data from modem,
                    // unblock those waiting for ack
                    synchronized (replyLock) {
                        if (replyType == ReplyType.WAITING_FOR_ACK) {
                            logger.debug("got bad data back, must assume message was acked.");
                            replyType = ReplyType.GOT_ACK;
                            replyLock.notify();
                        }
                    }
                }
            }
        }

        private void notifyWriter(Msg msg) {
            synchronized (replyLock) {
                if (replyType == ReplyType.WAITING_FOR_ACK) {
                    if (msg.isEcho()) {
                        replyType = msg.isPureNack() ? ReplyType.GOT_NACK : ReplyType.GOT_ACK;
                        if (logger.isTraceEnabled()) {
                            logger.trace("signaling receipt of ack: {}", replyType == ReplyType.GOT_ACK);
                        }
                        replyLock.notify();
                    }
                }
            }
        }

        /**
         * Blocking wait for ack or nack from modem.
         * Called by IOStreamWriter for flow control.
         *
         * @return true if retransmission is necessary
         * @throws InterruptedException
         */
        public boolean waitForReply() throws InterruptedException {
            if (logger.isTraceEnabled()) {
                logger.trace("waiting for reply ack");
            }
            replyType = ReplyType.WAITING_FOR_ACK;
            // There have been cases observed, in particular for
            // the Hub, where we get no ack or nack back, causing the binding
            // to hang in the wait() below, because unsolicited messages
            // do not trigger a notify(). For this reason we request retransmission
            // if the wait() times out.
            replyLock.wait(REPLY_TIMEOUT_TIME);
            if (replyType == ReplyType.WAITING_FOR_ACK) { // timeout expired without getting ACK or NACK
                if (logger.isTraceEnabled()) {
                    logger.trace("reply ack timeout expired, asking for retransmit!");
                }
                replyType = ReplyType.GOT_NACK;
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("got reply ack: {}", replyType == ReplyType.GOT_ACK);
                }
            }
            return replyType == ReplyType.GOT_NACK;
        }
    }

    /**
     * Writes messages to the port. Flow control is implemented following Insteon
     * documents to avoid overloading the modem.
     */
    private class IOStreamWriter implements Runnable {
        private static final int RETRANSMIT_WAIT_TIME = 200; // milliseconds
        private static final int WRITE_WAIT_TIME = 500; // milliseconds

        @Override
        public void run() {
            logger.debug("starting writer thread");
            try {
                while (!Thread.interrupted()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("writer checking message queue");
                    }
                    // this call blocks until the lock on the queue is released
                    Msg msg = writeQueue.take();
                    if (logger.isDebugEnabled()) {
                        logger.debug("writing: {}", msg);
                    }
                    synchronized (reader.getReplyLock()) {
                        ioStream.write(msg.getData());
                        messageSent(msg);
                        while (reader.waitForReply()) {
                            Thread.sleep(RETRANSMIT_WAIT_TIME);
                            if (logger.isTraceEnabled()) {
                                logger.trace("retransmitting msg: {}", msg);
                            }
                            ioStream.write(msg.getData());
                        }
                    }
                    // limit rate by waiting between writes to transport
                    Thread.sleep(WRITE_WAIT_TIME);
                }
            } catch (InterruptedException e) {
                logger.debug("write thread got interrupted!");
            } catch (IOException e) {
                logger.debug("write thread got an io exception", e);
                disconnected();
            }
            logger.debug("exiting writer thread!");
        }
    }
}
