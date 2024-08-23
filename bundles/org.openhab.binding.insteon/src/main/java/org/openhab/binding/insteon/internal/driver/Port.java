/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceType;
import org.openhab.binding.insteon.internal.device.DeviceTypeLoader;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.ModemDBBuilder;
import org.openhab.binding.insteon.internal.handler.InsteonDeviceHandler;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.message.MsgFactory;
import org.openhab.binding.insteon.internal.message.MsgListener;
import org.openhab.binding.insteon.internal.utils.Utils;
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

    private IOStream ioStream;
    private String devName;
    private String logName;
    private Modem modem;
    private IOStreamReader reader;
    private IOStreamWriter writer;
    private final int readSize = 1024; // read buffer size
    private @Nullable Thread readThread = null;
    private @Nullable Thread writeThread = null;
    private boolean running = false;
    private boolean modemDBComplete = false;
    private MsgFactory msgFactory = new MsgFactory();
    private Driver driver;
    private ModemDBBuilder mdbb;
    private ArrayList<MsgListener> listeners = new ArrayList<>();
    private LinkedBlockingQueue<Msg> writeQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean disconnected = new AtomicBoolean(false);

    /**
     * Constructor
     *
     * @param devName the name of the port, i.e. '/dev/insteon'
     * @param d The Driver object that manages this port
     */
    public Port(String devName, Driver d, @Nullable SerialPortManager serialPortManager,
            ScheduledExecutorService scheduler) {
        this.devName = devName;
        this.driver = d;
        this.logName = Utils.redactPassword(devName);
        this.modem = new Modem();
        addListener(modem);
        this.ioStream = IOStream.create(serialPortManager, devName);
        this.reader = new IOStreamReader();
        this.writer = new IOStreamWriter();
        this.mdbb = new ModemDBBuilder(this, scheduler);
    }

    public boolean isModem(InsteonAddress a) {
        return modem.getAddress().equals(a);
    }

    public synchronized boolean isModemDBComplete() {
        return (modemDBComplete);
    }

    public boolean isRunning() {
        return running;
    }

    public InsteonAddress getAddress() {
        return modem.getAddress();
    }

    public String getDeviceName() {
        return devName;
    }

    public Driver getDriver() {
        return driver;
    }

    public void addListener(MsgListener l) {
        synchronized (listeners) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }
    }

    public void removeListener(MsgListener l) {
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
        Map<InsteonAddress, ModemDBEntry> dbes = getDriver().lockModemDBEntries();
        for (Entry<InsteonAddress, ModemDBEntry> entry : dbes.entrySet()) {
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
        logger.debug("starting port {}", logName);
        if (running) {
            logger.debug("port {} already running, not started again", logName);
            return;
        }

        writeQueue.clear();
        if (!ioStream.open()) {
            logger.debug("failed to open port {}", logName);
            return;
        }
        ioStream.start();
        readThread = new Thread(reader);
        setParamsAndStart(readThread, "Reader");
        writeThread = new Thread(writer);
        setParamsAndStart(writeThread, "Writer");

        if (!mdbb.isComplete()) {
            modem.initialize();
            mdbb.start(); // start downloading the device list
        }

        running = true;
        disconnected.set(false);
    }

    private void setParamsAndStart(@Nullable Thread thread, String type) {
        if (thread != null) {
            thread.setName("OH-binding-Insteon " + logName + " " + type);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Stops all threads
     */
    public void stop() {
        if (!running) {
            logger.debug("port {} not running, no need to stop it", logName);
            return;
        }

        running = false;
        ioStream.stop();
        ioStream.close();

        Thread readThread = this.readThread;
        if (readThread != null) {
            readThread.interrupt();
        }
        Thread writeThread = this.writeThread;
        if (writeThread != null) {
            writeThread.interrupt();
        }
        logger.debug("waiting for read thread to exit for port {}", logName);
        try {
            if (readThread != null) {
                readThread.join();
            }
        } catch (InterruptedException e) {
            logger.debug("got interrupted waiting for read thread to exit.");
        }
        logger.debug("waiting for write thread to exit for port {}", logName);
        try {
            if (writeThread != null) {
                writeThread.join();
            }
        } catch (InterruptedException e) {
            logger.debug("got interrupted waiting for write thread to exit.");
        }
        this.readThread = null;
        this.writeThread = null;

        logger.debug("all threads for port {} stopped.", logName);
    }

    /**
     * Adds message to the write queue
     *
     * @param m message to be added to the write queue
     * @throws IOException
     */
    public void writeMessage(@Nullable Msg m) throws IOException {
        if (m == null) {
            logger.warn("trying to write null message!");
            throw new IOException("trying to write null message!");
        }
        if (m.getData() == null) {
            logger.warn("trying to write message without data!");
            throw new IOException("trying to write message without data!");
        }
        try {
            writeQueue.add(m);
            logger.trace("enqueued msg: {}", m);
        } catch (IllegalStateException e) {
            logger.warn("cannot write message {}, write queue is full!", m);
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
                logger.warn("port {} disconnected", logName);
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
            logger.debug("starting reader...");
            byte[] buffer = new byte[2 * readSize];
            try {
                for (int len = -1; (len = ioStream.read(buffer, 0, readSize)) > 0;) {
                    msgFactory.addData(buffer, len);
                    processMessages();
                }
            } catch (InterruptedException e) {
                logger.debug("reader thread got interrupted!");
            } catch (IOException e) {
                logger.debug("got an io exception in the reader thread");
                disconnected();
            }
            logger.debug("reader thread exiting!");
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
                    if (!msg.isUnsolicited()) {
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
            ArrayList<MsgListener> tempList = null;
            synchronized (listeners) {
                tempList = (ArrayList<MsgListener>) listeners.clone();
            }
            for (MsgListener l : tempList) {
                l.msg(msg); // deliver msg to listener
            }
        }

        /**
         * Blocking wait for ack or nack from modem.
         * Called by IOStreamWriter for flow control.
         *
         * @return true if retransmission is necessary
         */
        public boolean waitForReply() {
            reply = ReplyType.WAITING_FOR_ACK;
            while (reply == ReplyType.WAITING_FOR_ACK) {
                try {
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
     * @author Bernd Pfrommer - Initial contribution
     */
    class IOStreamWriter implements Runnable {
        private static final int WAIT_TIME = 200; // milliseconds

        @Override
        public void run() {
            logger.debug("starting writer...");
            while (true) {
                try {
                    // this call blocks until the lock on the queue is released
                    logger.trace("writer checking message queue");
                    Msg msg = writeQueue.take();
                    if (msg.getData() == null) {
                        logger.warn("found null message in write queue!");
                    } else {
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
                    logger.debug("got interrupted exception in write thread");
                    break;
                } catch (IOException e) {
                    logger.debug("got an io exception in the write thread");
                    disconnected();
                    break;
                }
            }
            logger.debug("writer thread exiting!");
        }
    }

    /**
     * Class to get info about the modem
     */
    class Modem implements MsgListener {
        private @Nullable InsteonDevice device = null;

        InsteonAddress getAddress() {
            InsteonDevice device = this.device;
            return (device == null) ? new InsteonAddress() : (device.getAddress());
        }

        @Nullable
        InsteonDevice getDevice() {
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
                    InsteonAddress a = new InsteonAddress(msg.getAddress("IMAddress"));
                    DeviceTypeLoader instance = DeviceTypeLoader.instance();
                    if (instance != null) {
                        DeviceType dt = instance.getDeviceType(InsteonDeviceHandler.PLM_PRODUCT_KEY);
                        if (dt == null) {
                            logger.warn("unknown modem product key: {} for modem: {}.",
                                    InsteonDeviceHandler.PLM_PRODUCT_KEY, a);
                        } else {
                            device = InsteonDevice.makeDevice(dt);
                            initDevice(a, device);
                            mdbb.updateModemDB(a, Port.this, null, true);
                        }
                    } else {
                        logger.warn("device type loader instance is null");
                    }
                    // can unsubscribe now
                    removeListener(this);
                }
            } catch (FieldException e) {
                logger.warn("error parsing im info reply field: ", e);
            }
        }

        private void initDevice(InsteonAddress a, @Nullable InsteonDevice device) {
            if (device != null) {
                device.setAddress(a);
                device.setProductKey(InsteonDeviceHandler.PLM_PRODUCT_KEY);
                device.setDriver(driver);
                device.setIsModem(true);
                logger.debug("found modem {} in device_types: {}", a, device.toString());
            } else {
                logger.warn("device is null");
            }
        }

        public void initialize() {
            try {
                Msg m = Msg.makeMessage("GetIMInfo");
                writeMessage(m);
            } catch (IOException e) {
                logger.warn("modem init failed!", e);
            } catch (InvalidMessageTypeException e) {
                logger.warn("invalid message", e);
            }
        }
    }
}
