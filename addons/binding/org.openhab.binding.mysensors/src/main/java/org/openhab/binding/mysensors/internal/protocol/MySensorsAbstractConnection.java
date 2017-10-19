/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.exception.NoAckException;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayConfig;
import org.openhab.binding.mysensors.internal.gateway.MySensorsNetworkSanityChecker;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection of the bridge (via TCP/IP or serial) to the MySensors network.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public abstract class MySensorsAbstractConnection implements Runnable {

    // Used by the reader to request a disconnection if there are too much exception
    private static final int ERROR_COUNT_REQ_DISCONNECT = 5;

    // How often and at which times should the binding retry to send a message if requestAck is true?
    public static final int MYSENSORS_NUMBER_OF_RETRIES = 5;
    public static final int[] MYSENSORS_RETRY_TIMES_IN_MILLISECONDS = { 0, 100, 500, 1000, 2000 };

    // Wait time Arduino reset
    public static final int RESET_TIME_IN_MILLISECONDS = 3000;

    // How long should a Smartsleep message be left in the queue?
    public static final int MYSENSORS_SMARTSLEEP_TIMEOUT_IN_MILLISECONDS = 60 * 60* 6; // 6 hours
    
    // Maximum number of attempts to request for an iVersion Message from the gateway
    public static final int MAX_ATTEMPTS_IVERSION_REQUEST = 5;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    // Connector will check for connection status every CONNECTOR_INTERVAL_CHECK seconds
    public static final int CONNECTOR_INTERVAL_CHECK = 10;

    // Flag set to true while connection is up
    private boolean connected = false;

    // Flag to be set (through available method below)
    private boolean requestDisconnection = false;

    private Object waitingObj = null;

    // I_VERSION response flag
    private boolean iVersionResponse = false;

    // Reader and writer thread
    protected MySensorsWriter mysConWriter = null;
    protected MySensorsReader mysConReader = null;

    protected MySensorsEventRegister myEventRegister = null;

    protected MySensorsGatewayConfig myGatewayConfig = null;

    // Sanity checker
    private MySensorsNetworkSanityChecker netSanityChecker = null;

    // Connection retry done
    private int numOfRetry = 0;

    private int errorCount;

    // Connection status watchdog
    private ScheduledExecutorService watchdogExecutor = null;
    private Future<?> futureWatchdog = null;

    public MySensorsAbstractConnection(MySensorsGatewayConfig myGatewayConfig, MySensorsEventRegister myEventRegister) {
        this.myEventRegister = myEventRegister;
        this.myGatewayConfig = myGatewayConfig;
        this.watchdogExecutor = Executors.newSingleThreadScheduledExecutor();
        this.iVersionResponse = false;
        this.errorCount = 0;
    }

    /**
     * Initialization of the BridgeConnection
     */
    public void initialize() {
        // Launch connection watchdog
        logger.debug("Enabling connection watchdog");
        futureWatchdog = watchdogExecutor.scheduleWithFixedDelay(this, 0, CONNECTOR_INTERVAL_CHECK, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(MySensorsAbstractConnection.class.getName());

        if (requestingDisconnection()) {
            logger.info("Connection request disconnection...");
            requestDisconnection(false);
            disconnect();
        }

        if (!connected) {
            if (connect()) {
                logger.info("Successfully connected to MySensors Bridge.");

                numOfRetry = 0;
            } else {
                logger.error("Failed connecting to bridge...next retry in {} seconds (Retry No.:{})",
                        CONNECTOR_INTERVAL_CHECK, numOfRetry);
                numOfRetry++;
                disconnect();
            }
        } else {
            logger.trace("Bridge is connected, connection skipped");
        }
    }

    /**
     * Startup connection with bridge
     *
     * @return true, if connection established correctly
     */
    private boolean connect() {
        connected = establishConnection();
        myEventRegister.notifyBridgeStatusUpdate(this, isConnected());
        errorCount = 0;
        return connected;
    }

    protected abstract boolean establishConnection();

    /**
     * Shutdown method that allows the correct disconnection with the used bridge
     */
    private void disconnect() {
        if (netSanityChecker != null) {
            netSanityChecker.stop();
            netSanityChecker = null;
        }

        stopConnection();
        connected = false;
        requestDisconnection = false;
        iVersionResponse = false;

        myEventRegister.notifyBridgeStatusUpdate(this, isConnected());
    }

    protected abstract void stopConnection();
    
    /**
     * Stop all threads holding the connection (serial/tcp).
     */
    public void destroy() {
        logger.debug("Destroying connection");

        if (connected) {
            disconnect();
        }

        if (futureWatchdog != null) {
            futureWatchdog.cancel(true);
            futureWatchdog = null;
        }

        if (watchdogExecutor != null) {
            watchdogExecutor.shutdown();
            watchdogExecutor.shutdownNow();
        }
    }

    /**
     * Start thread managing the incoming/outgoing messages. It also have the task to test the connection to gateway by
     * sending a special message (I_VERSION) to it
     *
     * @return true if the gateway test pass successfully
     */
    protected boolean startReaderWriterThread(MySensorsReader reader, MySensorsWriter writer) {
        reader.startReader();
        writer.startWriter();

        if (myGatewayConfig.getStartupCheck()) {
            try {
                int i = 0;
                synchronized (this) {
                    while (!iVersionResponse && i < MAX_ATTEMPTS_IVERSION_REQUEST) {
                        sendMessage(MySensorsMessage.I_VERSION_MESSAGE);
                        waitingObj = this;
                        waitingObj.wait(1000);
                        i++;
                    }
                }
            } catch (Exception e) {
                logger.error("Exception on waiting for I_VERSION message", e);
            }
        } else {
            logger.warn("Skipping I_VERSION connection test, not recommended...");
            iVersionResponse = true;
        }

        if (!iVersionResponse) {
            logger.error(
                    "Cannot start reading/writing thread, probably sync message (I_VERSION) not received. Try set startupCheckEnabled to false");
        }

        return iVersionResponse;
    }

    /**
     * Add a message to the outbound queue. The message will be send automatically. FIFO queue.
     * This method also has the task to populate oldMessage (and keep track thought oldMsgContent map) field on
     * MySensorsMessage
     *
     * @param msg The message that should be send.
     */
    public void sendMessage(MySensorsMessage msg) {
        if (msg.isSmartSleep()) {
            mysConWriter.addMySensorsOutboundSmartSleepMessage(msg);
        } else {
            mysConWriter.addMySensorsOutboundMessage(msg);
        }
    }

    /**
     * Is a connection to the bridge available?
     *
     * @return true, if connection is up and running.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * If the gateway is removed or the binding is stopped the connection to the gateway will be disposed
     * 
     * @return true if the connection should be disposed.
     */
    public boolean requestingDisconnection() {
        return requestDisconnection;
    }

    /**
     * Start the disconnection process.
     *
     * @param flag true if the connection should be stopped.
     */
    public void requestDisconnection(boolean flag) {
        logger.debug("Request disconnection flag setted to: {}", flag);
        requestDisconnection = flag;
    }

    private void handleReaderWriterException() {
        synchronized (this) {
            if (errorCount < ERROR_COUNT_REQ_DISCONNECT) {
                errorCount++;
            } else {
                requestDisconnection(true);
            }
        }
    }

    /**
     * Implements the reader (IP & serial) that receives the messages from the MySensors network.
     *
     * @author Andrea Cioni
     * @author Tim Oberföll
     *
     */
    protected class MySensorsReader implements Runnable {
        private Logger logger = LoggerFactory.getLogger(MySensorsReader.class);

        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private Future<?> future;

        private InputStream inStream;
        private BufferedReader reads;

        private boolean stopReader;

        public MySensorsReader(InputStream inStream) {
            this.inStream = inStream;
            this.reads = new BufferedReader(new InputStreamReader(inStream));
        }

        /**
         * Starts the reader process that will receive the messages from the MySensors network.
         */
        public void startReader() {
            future = executor.submit(this);
        }

        @Override
        public void run() {
            Thread.currentThread().setName(MySensorsReader.class.getName());
            String line = null;

            while (!stopReader) {
                // Is there something to read?
                try {
                    if (!reads.ready()) {
                        Thread.sleep(10);
                        continue;
                    }
                    line = reads.readLine();

                    // We lost connection
                    if (line == null) {
                        logger.warn("Connection to Gateway lost!");
                        requestDisconnection(true);
                        break;
                    }

                    logger.debug("Message from gateway received: {}", line);
                    MySensorsMessage msg = MySensorsMessage.parse(line);

                    if (!msg.isDebugMessage()) {
                        msg.setDirection(MySensorsMessageDirection.INCOMING);

                        // Have we get a I_HEARBEAT_RESPONSE
                        if (msg.isHeartbeatResponseMessage()) {
                            handleSmartSleepMessage(msg);
                        }

                        // Have we get a I_VERSION message?
                        if (msg.isIVersionMessage()) {
                            iVersionMessageReceived(msg.getMsg());
                        }

                        // Is this an ACK message?
                        if (msg.isAck()) {
                            handleAckReceived(msg);
                        }

                        myEventRegister.notifyMessageReceived(msg);
                    }
                } catch (InterruptedException e) {
                    logger.warn("Interrupted MySensorsReader");
                } catch (Exception e) {
                    logger.warn("Exception on reading from connection", e);
                    handleReaderWriterException();
                }
            }
        }

        /**
         * Stops the reader process of the bridge that receives messages from the MySensors network.
         */
        public void stopReader() {
            logger.debug("Stopping Reader thread");

            this.stopReader = true;

            if (future != null) {
                future.cancel(true);
                future = null;
            }

            if (executor != null) {
                executor.shutdown();
                executor.shutdownNow();
                executor = null;
            }

            try {
                if (reads != null) {
                    reads.close();
                    reads = null;
                }

                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
            } catch (IOException e) {
                logger.error("Cannot close reader stream");
            }
        }

        private void iVersionMessageReceived(String msg) {
            if (waitingObj != null) {
                logger.debug("Good,Gateway is up and running! (Ver:{})", msg);
                synchronized (waitingObj) {
                    iVersionResponse = true;
                    waitingObj.notifyAll();
                    waitingObj = null;
                }
            }
        }

        private void handleAckReceived(MySensorsMessage msg) {
            try {
                mysConWriter.confirmAcknowledgeMessage(msg);
            } catch (NoAckException e) {
                logger.warn("Invalid ACK message received: {}", e);
            }
        }

        /**
         * If a heartbeat is received from a node the queue should be checked
         * for pending messages for this node. If a message is pending it has to be send immediately.
         *
         * @param msg The heartbeat message received from a node.
         */
        private void handleSmartSleepMessage(MySensorsMessage msg) {
            mysConWriter.checkPendingSmartSleepMessage(msg.getNodeId());
        }
    }

    /**
     * Implements the writer (IP & serial) that sends messages to the MySensors network.
     *
     * @author Andrea Cioni
     * @author Tim Oberföll
     *
     */
    protected class MySensorsWriter implements Runnable {
        private Logger logger = LoggerFactory.getLogger(MySensorsWriter.class);

        private boolean stopWriting = false; // Stop the thread that sends the messages to the MySensors network

        // Blocking queue wait for message
        private BlockingQueue<MySensorsMessage> outboundMessageQueue = null;

        // Queue for SmartSleep messages
        private Queue<MySensorsMessage> smartSleepMessageQueue = null;

        // Map for acknowledge
        private List<MySensorsMessage> acknowledgeMessages = null;

        private PrintWriter outs = null;
        private OutputStream outStream = null;

        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private Future<?> future = null;

        public MySensorsWriter(OutputStream outStream) {
            this.outStream = outStream;
            this.outboundMessageQueue = new LinkedBlockingQueue<>();
            this.smartSleepMessageQueue = new LinkedList<>();
            this.acknowledgeMessages = new LinkedList<>();
            this.outs = new PrintWriter(outStream);
        }

        /**
         * Start the writer Process that will poll messages from the FIFO outbound queue
         * and send them to the MySensors network.
         */
        public void startWriter() {
            future = executor.submit(this);
        }

        @Override
        public void run() {
            Thread.currentThread().setName(MySensorsWriter.class.getName());
            while (!stopWriting) {
                try {
                    MySensorsMessage msg = pollMySensorsOutboundQueue();
                    synchronized (outboundMessageQueue) {
                        if (msg != null) {
                            if (msg.getNextSend() < System.currentTimeMillis()) {
                                /*
                                 * if we request an ACK we will wait for it and keep the message in the queue (at the
                                 * end) otherwise we remove the message from the queue
                                 */
                                if (msg.isAck()) {
                                    if (!checkForMessageAcknowledgement(msg)) {
                                        msg.setRetries(msg.getRetries() + 1);
                                        if (!(msg.getRetries() > MYSENSORS_NUMBER_OF_RETRIES)) {
                                            msg.setNextSend(System.currentTimeMillis()
                                                    + MYSENSORS_RETRY_TIMES_IN_MILLISECONDS[msg.getRetries() - 1]);
                                            addMySensorsOutboundMessage(msg);
                                        } else {
                                            logger.warn("NO ACK for message: {}",
                                                    MySensorsMessage.generateAPIString(msg));
                                            myEventRegister.notifyAckNotReceived(msg);
                                            continue;
                                        }
                                    } else {
                                        logger.info("ACK received for message: {}",
                                                MySensorsMessage.generateAPIString(msg));
                                        continue;
                                    }
                                }
                                String output = MySensorsMessage.generateAPIString(msg);
                                logger.debug("Sending to MySensors: {}", output.trim());
                                sendMessage(output);
                            } else {
                                addMySensorsOutboundMessage(msg);
                            }
                        } else {
                            logger.warn("Message returned from queue is null");
                        }
                    }
                } catch (InterruptedException e) {
                    logger.warn("Interrupted MySensorsWriter");
                } catch (Exception e) {
                    logger.error("({}) on writing to connection, message: {}", e, getClass(), e.getMessage());
                    handleReaderWriterException();
                }

                try {
                    Thread.sleep(myGatewayConfig.getSendDelay());
                } catch (Exception e) {
                }
            }
        }

        /**
         * Stops the writer process.
         */
        public void stopWriting() {
            logger.debug("Stopping Writer thread");

            this.stopWriting = true;

            if (future != null) {
                future.cancel(true);
                future = null;
            }

            if (executor != null) {
                executor.shutdown();
                executor.shutdownNow();
                executor = null;
            }

            try {
                if (outs != null) {
                    outs.flush();
                    outs.close();
                    outs = null;
                }

                if (outStream != null) {
                    outStream.close();
                    outStream = null;
                }
            } catch (IOException e) {
                logger.error("Cannot close writer stream");
            }
        }

        /**
         * Send a message to the MySensors network.
         *
         * @param output the message/string/line that should be send to the MySensors gateway.
         */
        protected void sendMessage(String output) {
            outs.println(output);
            outs.flush();
        }

        /**
         * Confirm acknowledge for a message from the outbound message queue.
         *
         * @param msg The message that should be acknowledged from the queue.
         * @throws NoAckException 
         */
        private void confirmAcknowledgeMessage(MySensorsMessage msg) throws NoAckException {
            if (msg == null) {
                throw new NoAckException("Invalid ack message to insert");
            }

            synchronized (acknowledgeMessages) {
                acknowledgeMessages.add(msg);
            }
        }

        /**
         * Confirm acknowledge for a message from the outbound message queue.
         * This removes every acknowledge message
         *
         * @param msg The message that should be acknowledged from the queue.
         *
         * @return true if message is confirmed
         */
        private boolean checkForMessageAcknowledgement(MySensorsMessage msg) {
            boolean acknowledgementReceived = false;
            synchronized (acknowledgeMessages) {
                Iterator<MySensorsMessage> iterator = acknowledgeMessages.iterator();
                while (iterator.hasNext()) {
                    MySensorsMessage ackM = iterator.next();
                    if (    ackM.getNodeId() == msg.getNodeId() && 
                            ackM.getChildId() == msg.getChildId() && 
                            ackM.getMsgType() == msg.getMsgType() && 
                            ackM.getSubType() == msg.getSubType() && 
                            ackM.getAck() == msg.getAck() && 
                            ackM.getMsg().equals(msg.getMsg())) {
                        iterator.remove();
                        acknowledgementReceived = true;
                    }
                }
            }
            return acknowledgementReceived;
        }

        /**
         * Store more than one message in the outbound queue.
         *
         * @param msg the message that should be stored in the queue.
         * @param copy the number of copies that should be stored.
         */
        private void addMySensorsOutboundMessage(MySensorsMessage msg) {
            try {
                outboundMessageQueue.put(msg);
            } catch (InterruptedException e) {
                logger.error("Interrupted message while running");
            }
        }

        /**
         * A message to a node that supports smartsleep is not send instantly.
         * The message is send in response to a heartbeat received from this node.
         * Only one message is allowed in the queue. If a new one arrives the old one
         * gets deleted.
         *
         * @param msg the message that should be added to the queue.
         */
        private void addMySensorsOutboundSmartSleepMessage(MySensorsMessage msg) {
            // Only one pending message is allowed in the queue.
            removeSmartSleepMessage(msg.getNodeId(), msg.getChildId());

            synchronized (smartSleepMessageQueue) {
                smartSleepMessageQueue.add(msg);
            }
        }

        /**
         * Get the next message in line from the queue.
         *
         * @return the next message in line.
         * @throws InterruptedException
         */
        private MySensorsMessage pollMySensorsOutboundQueue() throws InterruptedException {
            return outboundMessageQueue.poll(1, TimeUnit.DAYS);
        }

        /**
         * Remove all messages in the smartsleep queue for a corresponding nodeId / childId combination
         *
         * @param nodeId the nodeId which messages should be deleted.
         * @param childId the childId which messages should be deleted.
         */
        private void removeSmartSleepMessage(int nodeId, int childId) {
            synchronized (smartSleepMessageQueue) {
                Iterator<MySensorsMessage> iterator = smartSleepMessageQueue.iterator();
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        MySensorsMessage msgInQueue = iterator.next();
                        if (msgInQueue.getNodeId() == nodeId && msgInQueue.getChildId() == childId) {
                            iterator.remove();
                        } else {
                            logger.debug("Message NOT removed for nodeId: {} and childId: {}.", nodeId, childId);
                        }
                    }
                }
            }
        }

        /**
         * Checks if a message is in the smartsleep queue and adds it to the outbound queues
         *
         * @param nodeId of the messages that should be send immediately
         */
        private void checkPendingSmartSleepMessage(int nodeId) {
            synchronized (smartSleepMessageQueue) {
                Iterator<MySensorsMessage> iterator = smartSleepMessageQueue.iterator();
                while (iterator.hasNext()) {
                    MySensorsMessage msgInQueue = iterator.next();
                    if (msgInQueue.getNodeId() == nodeId) {
                        iterator.remove();
                        addMySensorsOutboundMessage(msgInQueue);
                        logger.debug("Message for nodeId: {} in queue needs to be send immediately!", nodeId);
                    }
                }
            }
        }

        /**
         * Debug print of the smart sleep queue content to logs
         */
        private void printSmartSleepQueue() {
            synchronized (smartSleepMessageQueue) {
                Iterator<MySensorsMessage> iterator = smartSleepMessageQueue.iterator();
                if (iterator != null) {
                    logger.debug("####### START SmartSleep queue #####");
                    int i = 1;
                    while (iterator.hasNext()) {
                        MySensorsMessage msgInQueue = iterator.next();

                        logger.debug("Msg: {}, nodeId: {], childId: {}, nextSend: {}.", i, msgInQueue.getNodeId(),
                                msgInQueue.getChildId(), msgInQueue.getNextSend());
                        i++;
                    }
                    logger.debug("####### END SmartSleep queue #####");
                }
            }
        }
    }
}
