/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.openhab.binding.insteonplm.internal.message.modem.BaseModemMessage;
import org.openhab.binding.insteonplm.internal.message.modem.PureNack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The Port class represents a port, that is a connection to either an Insteon modem either through
 * a serial or USB port, or via an Insteon Hub.
 * It does the initialization of the port, and (via its inner classes IOStreamReader and IOStreamWriter)
 * manages the reading/writing of messages on the Insteon network.
 *
 * The IOStreamReader and IOStreamWriter class combined implement the somewhat tricky flow control protocol.
 * In combination with the MsgFactory class, the incoming data stream is turned into a Msg structure
 * for further processing by the upper layers (MsgListeners).
 *
 * A write queue is maintained to pace the flow of outgoing messages. Sending messages back-to-back
 * can lead to dropped messages.
 *
 *
 * @author Bernd Pfrommer
 * @author Daniel Pfrommer
 * @since 1.5.0
 */

public class Port {
    private static final Logger logger = LoggerFactory.getLogger(Port.class);

    /**
     * The ReplyType is used to keep track of the state of the serial port receiver
     */
    enum ReplyType {
        GOT_ACK,
        WAITING_FOR_ACK,
        GOT_NACK
    }

    private IOStream ioStream = null;
    private IOStreamReader reader = null;
    private IOStreamWriter writer = null;
    private final int readSize = 1024; // read buffer size
    private Thread readThread = null;
    private Thread writeThread = null;
    private boolean running = false;
    private LinkedBlockingQueue<BaseModemMessage> writeQueue = new LinkedBlockingQueue<BaseModemMessage>();
    private List<MessageListener> messageListeners = Lists.newArrayList();
    private DataParser parser;

    /**
     * Constructor
     *
     * @param devName the name of the port, i.e. '/dev/insteon'
     * @param d The Driver object that manages this port
     */
    public Port(IOStream stream) {
        ioStream = stream;
        reader = new IOStreamReader();
        writer = new IOStreamWriter();
        this.parser = new DataParser();
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Adds a message listener to the set, so we know about all incoming messages.
     */
    public void addListener(MessageListener l) {
        synchronized (messageListeners) {
            messageListeners.add(l);
        }
    }

    public void removeListener(MessageListener l) {
        synchronized (messageListeners) {
            messageListeners.remove(l);
        }
    }

    /**
     * Starts threads necessary for reading and writing. Assume the iostream is already open.
     */
    public boolean start() {
        logger.debug("starting port {}", ioStream.toString());
        if (running) {
            logger.debug("port {} already running, not started again", ioStream.toString());
            return true;
        }
        readThread = new Thread(reader);
        writeThread = new Thread(writer);
        readThread.setName(ioStream.toString() + " Reader");
        writeThread.setName(ioStream.toString() + " Writer");
        readThread.start();
        writeThread.start();
        running = true;
        return true;
    }

    /**
     * Stops all threads
     */
    public void stop() {
        if (!running) {
            logger.debug("port {} not running, no need to stop it", ioStream.toString());
            return;
        }
        if (readThread != null) {
            readThread.interrupt();
        }
        if (writeThread != null) {
            writeThread.interrupt();
        }
        logger.error("waiting for read thread to exit for port {}", ioStream.toString());
        try {
            if (readThread != null) {
                readThread.join(1000);
            }
        } catch (InterruptedException e) {
            logger.debug("got interrupted waiting for read thread to exit.");
        }
        logger.error("waiting for write thread to exit for port {}", ioStream.toString());
        try {
            if (writeThread != null && writeThread.isAlive()) {
                writeThread.join(1000);
            }
        } catch (InterruptedException e) {
            logger.debug("got interrupted waiting for write thread to exit.");
        }
        logger.error("all threads for port {} stopped.", ioStream.toString());
        running = false;
        // Close the streams.
        reader = null;
        writer = null;
        synchronized (messageListeners) {
            messageListeners.clear();
        }
    }

    /**
     * Adds message to the write queue
     *
     * @param m message to be added to the write queue
     * @throws IOException
     */
    public void writeMessage(BaseModemMessage m) throws IOException {
        if (m == null) {
            logger.error("trying to write null message!");
            throw new IOException("trying to write null message!");
        }
        if (m.getPayload() == null) {
            logger.error("trying to write message without data!");
            throw new IOException("trying to write message without data!");
        }
        try {
            writeQueue.add(m);
            logger.trace("enqueued msg: {}", m);
        } catch (IllegalStateException e) {
            logger.error("cannot write message {}, write queue is full!", m);
        }

    }

    /**
     * The IOStreamReader uses the MsgFactory to turn the incoming bytes into
     * Msgs for the listeners. It also communicates with the IOStreamWriter
     * to implement flow control (tell the IOStreamWriter that it needs to retransmit,
     * or the reply message has been received correctly).
     *
     * @author Bernd Pfrommer
     */
    class IOStreamReader implements Runnable {

        private ReplyType reply = ReplyType.GOT_ACK;
        private Object replyLock = new Object();
        private boolean dropRandomBytes = false; // set to true for fault injection
        private BaseModemMessage waitingFor = null;

        /**
         * Helper function for implementing synchronization between reader and writer
         *
         * @return reference to the RequesReplyLock
         */
        public Object getRequestReplyLock() {
            return replyLock;
        }

        @Override
        public void run() {
            logger.debug("starting reader...");
            byte[] buffer = new byte[2 * readSize];
            Random rng = new Random();
            try {
                for (int len = -1; (len = ioStream.read(buffer, 0, readSize)) > 0;) {
                    if (dropRandomBytes && rng.nextInt(100) < 20) {
                        len = dropBytes(buffer, len);
                    }
                    parser.addData(buffer, len);
                    processMessages();
                }
                logger.error("Read 0 bytes?");
            } catch (InterruptedException e) {
                logger.debug("reader thread got interrupted!");
            }
            logger.error("reader thread exiting!");
        }

        private void processMessages() {
            // must call processData() until we get a null pointer back
            for (BaseModemMessage m : parser.getAndClearPendingMessages()) {
                if (m != null) {
                    toAllListeners(m);
                    notifyWriter(m);
                } else {
                    // got bad data from modem,
                    // unblock those waiting for ack
                    logger.warn("bad data received");
                    synchronized (getRequestReplyLock()) {
                        if (reply == ReplyType.WAITING_FOR_ACK) {
                            logger.warn("got bad data back, must assume message was acked.");
                            reply = ReplyType.GOT_ACK;
                            getRequestReplyLock().notify();
                        }
                    }
                }
            }
        }

        private void notifyWriter(BaseModemMessage msg) {
            synchronized (getRequestReplyLock()) {
                if (reply == ReplyType.WAITING_FOR_ACK) {
                    if (waitingFor.equals(msg)) {
                        reply = (msg instanceof PureNack ? ReplyType.GOT_NACK : ReplyType.GOT_ACK);
                        logger.trace("signaling receipt of ack: {}", (reply == ReplyType.GOT_ACK));
                        getRequestReplyLock().notify();
                    } else if (msg instanceof PureNack) {
                        reply = ReplyType.GOT_NACK;
                        logger.trace("signaling receipt of pure nack");
                        getRequestReplyLock().notify();
                    } else {
                        logger.trace("got unsolicited message");
                    }
                }
            }
        }

        /**
         * Drops bytes randomly from buffer to simulate errors seen
         * from the InsteonHub using the raw interface
         *
         * @param buffer byte buffer from which to drop bytes
         * @param len original number of valid bytes in buffer
         * @return length of byte buffer after dropping from it
         */
        private int dropBytes(byte[] buffer, int len) {
            final int DROP_RATE = 2; // in percent
            Random rng = new Random();
            ArrayList<Byte> l = new ArrayList<Byte>();
            for (int i = 0; i < len; i++) {
                if (rng.nextInt(100) >= DROP_RATE) {
                    l.add(new Byte(buffer[i]));
                }
            }
            for (int i = 0; i < l.size(); i++) {
                buffer[i] = l.get(i);
            }
            return (l.size());
        }

        private void toAllListeners(BaseModemMessage msg) {
            // When we deliver the message, the recipient
            // may in turn call removeListener() or addListener(),
            // thereby corrupting the very same list we are iterating
            // through. That's why we make a copy of it, and
            // iterate through the copy.
            ArrayList<MessageListener> tempList = null;
            synchronized (messageListeners) {
                tempList = Lists.newArrayList(messageListeners);
            }
            for (MessageListener listener : tempList) {
                listener.processMessage(msg); // deliver msg to listener
            }
        }

        /**
         * Blocking wait for ack or nack from modem.
         * Called by IOStreamWriter for flow control.
         *
         * @return true if retransmission is necessary
         */
        public boolean waitForReply(BaseModemMessage mess, byte data[]) {
            reply = ReplyType.WAITING_FOR_ACK;
            waitingFor = mess;
            while (reply == ReplyType.WAITING_FOR_ACK) {
                try {
                    logger.trace("writer waiting for ack.");
                    // There have been cases observed, in particular for
                    // the Hub, where we get no ack or nack back, causing the binding
                    // to hang in the wait() below, because unsolicited messages
                    // do not trigger a notify(). For this reason we request retransmission
                    // if the wait() times out.
                    getRequestReplyLock().wait(3000); // be patient for 30 sec
                    if (reply == ReplyType.WAITING_FOR_ACK) { // timeout expired without getting ACK or NACK
                        logger.trace("writer timeout expired, asking for retransmit!");
                        reply = ReplyType.GOT_NACK;
                        break;
                    } else {
                        logger.trace("writer got ack: {}", (reply == ReplyType.GOT_ACK));
                    }
                } catch (InterruptedException e) {
                    break; // done for the day...
                }
            }
            return (reply == ReplyType.GOT_NACK);
        }
    }

    /**
     * Writes messages to the port. Flow control is implemented following Insteon
     * documents to avoid over running the modem.
     *
     * @author Bernd Pfrommer
     */
    class IOStreamWriter implements Runnable {
        private static final int WAIT_TIME = 200; // milliseconds

        @Override
        public void run() {
            logger.debug("starting writer...");
            while (true) {
                try {
                    logger.trace("writer checking message queue");
                    // this call blocks until the lock on the queue is released
                    BaseModemMessage msg = writeQueue.take();
                    byte[] payload = msg.getPayload();
                    if (payload == null) {
                        logger.error("found null message in write queue!");
                    } else {
                        byte[] header = new byte[2];
                        header[0] = 0x02;
                        header[1] = (byte) msg.getMessageType().getCommand();
                        logger.error("writing ({}): {}", msg.getQuietTime(), msg);
                        // To debug race conditions during startup (i.e. make the .items
                        // file definitions be available *before* the modem link records,
                        // slow down the modem traffic with the following statement:
                        // Thread.sleep(500);
                        synchronized (reader.getRequestReplyLock()) {
                            ioStream.write(header);
                            ioStream.write(payload);
                            while (reader.waitForReply(msg, payload)) {
                                Thread.sleep(WAIT_TIME);
                                logger.trace("retransmitting msg: {}", msg);
                                ioStream.write(header);
                                ioStream.write(payload);
                            }
                        }
                        // if rate limited, need to sleep now.
                        if (msg.getQuietTime() > 0) {
                            Thread.sleep(msg.getQuietTime());
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("got interrupted exception in write thread");
                    break;
                } catch (Exception e) {
                    logger.error("got exception in write thread:", e);
                }
            }
            logger.error("exiting writer thread!");
        }
    }
}
