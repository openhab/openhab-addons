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
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.insteon.internal.InsteonLegacyBindingConstants;
import org.openhab.binding.insteon.internal.config.InsteonLegacyNetworkConfiguration;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.LegacyDevice;
import org.openhab.binding.insteon.internal.device.LegacyDeviceType;
import org.openhab.binding.insteon.internal.device.LegacyDeviceTypeLoader;
import org.openhab.binding.insteon.internal.device.database.LegacyModemDBBuilder;
import org.openhab.binding.insteon.internal.device.database.LegacyModemDBEntry;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.transport.message.MsgFactory;
import org.openhab.binding.insteon.internal.transport.stream.IOStream;
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
 * for further processing by the upper layers (MsgListeners).
 *
 * A write queue is maintained to pace the flow of outgoing messages. Sending messages back-to-back
 * can lead to dropped messages.
 *
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Daniel Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class LegacyPort {
    private final Logger logger = LoggerFactory.getLogger(LegacyPort.class);

    /**
     * The ReplyType is used to keep track of the state of the serial port receiver
     */
    enum ReplyType {
        GOT_ACK,
        WAITING_FOR_ACK,
        GOT_NACK
    }

    private IOStream ioStream;
    private String name;
    private Modem modem = new Modem();
    private ScheduledExecutorService scheduler;
    private IOStreamReader reader = new IOStreamReader();
    private IOStreamWriter writer = new IOStreamWriter();
    private @Nullable ScheduledFuture<?> readJob;
    private @Nullable ScheduledFuture<?> writeJob;
    private boolean running = false;
    private boolean modemDBComplete = false;
    private MsgFactory msgFactory = new MsgFactory();
    private LegacyDriver driver;
    private LegacyModemDBBuilder mdbb;
    private ArrayList<LegacyPortListener> listeners = new ArrayList<>();
    private LinkedBlockingQueue<Msg> writeQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean disconnected = new AtomicBoolean(false);

    /**
     * Constructor
     *
     * @param config the network bridge config
     * @param driver the driver that manages this port
     * @param httpClient the http client
     * @param scheduler the scheduler
     * @param serialPortManager the serial port manager
     */
    public LegacyPort(InsteonLegacyNetworkConfiguration config, LegacyDriver driver, HttpClient httpClient,
            ScheduledExecutorService scheduler, SerialPortManager serialPortManager) {
        this.name = config.getRedactedPort();
        this.driver = driver;
        this.scheduler = scheduler;
        this.ioStream = IOStream.create(config.parse(), httpClient, scheduler, serialPortManager);
        this.mdbb = new LegacyModemDBBuilder(this, scheduler);
    }

    public boolean isModem(InsteonAddress a) {
        return modem.getAddress().equals(a);
    }

    public synchronized boolean isModemDBComplete() {
        return modemDBComplete;
    }

    public boolean isRunning() {
        return running;
    }

    public InsteonAddress getAddress() {
        return modem.getAddress();
    }

    public String getName() {
        return name;
    }

    public LegacyDriver getDriver() {
        return driver;
    }

    public void addListener(LegacyPortListener l) {
        synchronized (listeners) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }
    }

    public void removeListener(LegacyPortListener l) {
        synchronized (listeners) {
            if (listeners.remove(l)) {
                logger.debug("removed listener from port");
            }
        }
    }

    /**
     * Clear modem database that has been queried so far.
     */
    public void clearModemDB() {
        logger.debug("clearing modem db!");
        Map<InsteonAddress, LegacyModemDBEntry> dbes = getDriver().lockModemDBEntries();
        for (Entry<InsteonAddress, LegacyModemDBEntry> entry : dbes.entrySet()) {
            if (!entry.getValue().isModem()) {
                dbes.remove(entry.getKey());
            }
        }
        getDriver().unlockModemDBEntries();
    }

    /**
     * Starts threads necessary for reading and writing
     */
    public void start() {
        logger.debug("starting port {}", name);
        if (running) {
            logger.debug("port {} already running, not started again", name);
            return;
        }

        writeQueue.clear();
        if (!ioStream.open()) {
            logger.debug("failed to open port {}", name);
            return;
        }

        readJob = scheduler.schedule(reader, 0, TimeUnit.SECONDS);
        writeJob = scheduler.schedule(writer, 0, TimeUnit.SECONDS);

        if (!mdbb.isComplete()) {
            modem.initialize();
            mdbb.start(); // start downloading the device list
        }

        running = true;
        disconnected.set(false);
    }

    /**
     * Stops all threads
     */
    public void stop() {
        if (!running) {
            logger.debug("port {} not running, no need to stop it", name);
            return;
        }

        running = false;

        if (mdbb.isRunning()) {
            mdbb.stop();
        }

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

        logger.debug("all threads for port {} stopped.", name);
    }

    /**
     * Adds message to the write queue
     *
     * @param msg message to be added to the write queue
     * @throws IOException
     */
    public void writeMessage(@Nullable Msg msg) throws IOException {
        if (msg == null) {
            logger.warn("trying to write null message!");
            throw new IOException("trying to write null message!");
        }
        try {
            writeQueue.add(msg);
            logger.trace("enqueued msg: {}", msg);
        } catch (IllegalStateException e) {
            logger.warn("cannot write message {}, write queue is full!", msg);
        }
    }

    /**
     * Gets called by the modem database builder when the modem database is complete
     */
    public void modemDBComplete() {
        synchronized (this) {
            modemDBComplete = true;
        }
        driver.modemDBComplete(this);
    }

    public void disconnected() {
        if (isRunning()) {
            if (!disconnected.getAndSet(true)) {
                logger.warn("port {} disconnected", name);
                driver.disconnected();
            }
        }
    }

    /**
     * The IOStreamReader uses the MsgFactory to turn the incoming bytes into
     * Msgs for the listeners. It also communicates with the IOStreamWriter
     * to implement flow control (tell the IOStreamWriter that it needs to retransmit,
     * or the reply message has been received correctly).
     *
     * @author Bernd Pfrommer - Initial contribution
     */
    class IOStreamReader implements Runnable {
        private static final int READ_BUFFER_SIZE = 1024;

        private ReplyType reply = ReplyType.GOT_ACK;
        private Object replyLock = new Object();

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
            // must call processData() until msgFactory done fully processing buffer
            while (!msgFactory.isDone()) {
                try {
                    Msg msg = msgFactory.processData();
                    if (msg != null) {
                        toAllListeners(msg);
                        notifyWriter(msg);
                    }
                } catch (IOException e) {
                    // got bad data from modem,
                    // unblock those waiting for ack
                    synchronized (getRequestReplyLock()) {
                        if (reply == ReplyType.WAITING_FOR_ACK) {
                            logger.debug("got bad data back, must assume message was acked.");
                            reply = ReplyType.GOT_ACK;
                            getRequestReplyLock().notify();
                        }
                    }
                }
            }
        }

        private void notifyWriter(Msg msg) {
            synchronized (getRequestReplyLock()) {
                if (reply == ReplyType.WAITING_FOR_ACK) {
                    if (msg.isReply()) {
                        reply = (msg.isPureNack() ? ReplyType.GOT_NACK : ReplyType.GOT_ACK);
                        logger.trace("signaling receipt of ack: {}", (reply == ReplyType.GOT_ACK));
                        getRequestReplyLock().notify();
                    } else if (msg.isPureNack()) {
                        reply = ReplyType.GOT_NACK;
                        logger.trace("signaling receipt of pure nack");
                        getRequestReplyLock().notify();
                    } else {
                        logger.trace("got unsolicited message");
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void toAllListeners(Msg msg) {
            // When we deliver the message, the recipient
            // may in turn call removeListener() or addListener(),
            // thereby corrupting the very same list we are iterating
            // through. That's why we make a copy of it, and
            // iterate through the copy.
            ArrayList<LegacyPortListener> tempList = null;
            synchronized (listeners) {
                tempList = (ArrayList<LegacyPortListener>) listeners.clone();
            }
            for (LegacyPortListener listener : tempList) {
                listener.msg(msg); // deliver msg to listener
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
            reply = ReplyType.WAITING_FOR_ACK;
            logger.trace("writer waiting for ack.");
            // There have been cases observed, in particular for
            // the Hub, where we get no ack or nack back, causing the binding
            // to hang in the wait() below, because unsolicited messages
            // do not trigger a notify(). For this reason we request retransmission
            // if the wait() times out.
            getRequestReplyLock().wait(30000); // be patient for 30 msec
            if (reply == ReplyType.WAITING_FOR_ACK) { // timeout expired without getting ACK or NACK
                logger.trace("writer timeout expired, asking for retransmit!");
                reply = ReplyType.GOT_NACK;
            } else {
                logger.trace("writer got ack: {}", (reply == ReplyType.GOT_ACK));
            }
            return reply == ReplyType.GOT_NACK;
        }
    }

    /**
     * Writes messages to the port. Flow control is implemented following Insteon
     * documents to avoid over running the modem.
     *
     * @author Bernd Pfrommer - Initial contribution
     */
    class IOStreamWriter implements Runnable {
        private static final int WAIT_TIME = 200; // milliseconds

        @Override
        public void run() {
            logger.debug("starting writer thread");
            try {
                while (!Thread.interrupted()) {
                    // this call blocks until the lock on the queue is released
                    logger.trace("writer checking message queue");
                    Msg msg = writeQueue.take();
                    logger.debug("writing ({}): {}", msg.getQuietTime(), msg);
                    // To debug race conditions during startup (i.e. make the .items
                    // file definitions be available *before* the modem link records,
                    // slow down the modem traffic with the following statement:
                    // Thread.sleep(500);
                    synchronized (reader.getRequestReplyLock()) {
                        ioStream.write(msg.getData());
                        while (reader.waitForReply()) {
                            Thread.sleep(WAIT_TIME);
                            logger.trace("retransmitting msg: {}", msg);
                            ioStream.write(msg.getData());
                        }
                    }
                    // if rate limited, need to sleep now.
                    if (msg.getQuietTime() > 0) {
                        Thread.sleep(msg.getQuietTime());
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
    }

    /**
     * Class to get info about the modem
     */
    class Modem implements LegacyPortListener {
        private @Nullable LegacyDevice device = null;

        InsteonAddress getAddress() {
            LegacyDevice device = this.device;
            return device == null ? InsteonAddress.UNKNOWN : (InsteonAddress) device.getAddress();
        }

        @Nullable
        LegacyDevice getDevice() {
            return device;
        }

        @Override
        public void msg(Msg msg) {
            try {
                if (msg.isPureNack()) {
                    return;
                }
                if (msg.getByte("Cmd") == 0x60) {
                    // add the modem to the device list
                    InsteonAddress address = msg.getInsteonAddress("IMAddress");
                    LegacyDeviceType deviceType = LegacyDeviceTypeLoader.instance()
                            .getDeviceType(InsteonLegacyBindingConstants.PLM_PRODUCT_KEY);
                    if (deviceType == null) {
                        logger.warn("unknown modem product key: {} for modem: {}.",
                                InsteonLegacyBindingConstants.PLM_PRODUCT_KEY, address);
                    } else {
                        device = LegacyDevice.makeDevice(deviceType);
                        initDevice(address, device);
                        mdbb.updateModemDB(address, LegacyPort.this, null, true);
                    }
                    // can unsubscribe now
                    removeListener(this);
                }
            } catch (FieldException e) {
                logger.warn("error parsing im info reply field: ", e);
            }
        }

        private void initDevice(InsteonAddress a, @Nullable LegacyDevice device) {
            if (device != null) {
                device.setAddress(a);
                device.setProductKey(InsteonLegacyBindingConstants.PLM_PRODUCT_KEY);
                device.setDriver(driver);
                device.setIsModem(true);
                logger.debug("found modem {} in device_types: {}", a, device.toString());
            } else {
                logger.warn("device is null");
            }
        }

        public void initialize() {
            try {
                Msg msg = Msg.makeMessage("GetIMInfo");
                addListener(this);
                writeMessage(msg);
            } catch (IOException e) {
                logger.warn("modem init failed!", e);
            } catch (InvalidMessageTypeException e) {
                logger.warn("invalid message", e);
            }
        }
    }
}
