/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.internal.event.MySensorsEventType;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection of the bridge (via TCP/IP or serial) to the MySensors network.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public abstract class MySensorsBridgeConnection implements Runnable, MySensorsUpdateListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // Connector will check for connection status every CONNECTOR_INTERVAL_CHECK seconds
    public static final int CONNECTOR_INTERVAL_CHECK = 10;

    // ??
    private boolean pauseWriter = false;

    // Blocking queue wait for message
    private BlockingQueue<MySensorsMessage> outboundMessageQueue = null;
    
    // Queue for SmartSleep messages
    private Queue<MySensorsMessage> smartSleepMessageQueue = null;

    // Flag setted to true while connection is up
    private boolean connected = false;

    // Flag to be set (through available method below)
    private boolean requestDisconnection = false;

    private MySensorsBridgeConnection waitingObj = null;

    // I_VERSION response flag
    private boolean iVersionResponse = false;

    // Check connection on startup flag
    private boolean skipStartupCheck = false;

    // Reader and writer thread
    protected MySensorsWriter mysConWriter = null;
    protected MySensorsReader mysConReader = null;

    // Bridge handler dependency
    private MySensorsBridgeHandler bridgeHandler = null;

    // Sanity checker
    private MySensorsNetworkSanityChecker netSanityChecker = null;

    // Connection retry done
    private int numOfRetry = 0;

    // Update listener
    private List<MySensorsUpdateListener> updateListeners = null;

    // Connection status watchdog
    private ScheduledExecutorService watchdogExecutor = null;
    private Future<?> futureWatchdog = null;

    public MySensorsBridgeConnection(MySensorsBridgeHandler bridgeHandler) {
        this.outboundMessageQueue = new LinkedBlockingQueue<MySensorsMessage>();
        this.smartSleepMessageQueue = new LinkedList<MySensorsMessage>();
        this.bridgeHandler = bridgeHandler;
        this.updateListeners = new ArrayList<>();
        this.watchdogExecutor = Executors.newSingleThreadScheduledExecutor();
        this.iVersionResponse = false;
    }

    /**
     * Initialization of the BridgeConnection
     */
    public void initialize() {
        logger.debug("Set skip check on startup to: {}", bridgeHandler.getBridgeConfiguration().skipStartupCheck);
        skipStartupCheck = bridgeHandler.getBridgeConfiguration().skipStartupCheck;

        // Launch connection watchdog
        logger.debug("Enabling connection watchdog");
        futureWatchdog = watchdogExecutor.scheduleWithFixedDelay(this, 0, CONNECTOR_INTERVAL_CHECK, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(MySensorsBridgeConnection.class.getName());

        if (requestingDisconnection()) {
            logger.info("Connection request disconnection...");
            requestDisconnection(false);
            disconnect();
        }

        if (!connected) {
            if (connect()) {
                logger.info("Successfully connected to MySensors Bridge.");

                numOfRetry = 0;

                // Start discovery service
                MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(bridgeHandler);
                discoveryService.activate();

                if (bridgeHandler.getBridgeConfiguration().enableNetworkSanCheck) {

                    // Start network sanity check
                    netSanityChecker = new MySensorsNetworkSanityChecker(this);
                    netSanityChecker.start();

                } else {
                    logger.warn("Network Sanity Checker thread disabled from bridge configuration");
                }

            } else {
                logger.error("Failed connecting to bridge...next retry in {} seconds (Retry No.:{})",
                        CONNECTOR_INTERVAL_CHECK, numOfRetry);
                numOfRetry++;
                disconnect();
            }

        } else {
            logger.debug("Bridge is connected, connection skipped");
        }

    }

    /**
     * Startup connection with bridge
     *
     * @return true, if connection established correctly
     */
    private boolean connect() {
        connected = _connect();
        broadCastEvent(new MySensorsStatusUpdateEvent(MySensorsEventType.BRIDGE_STATUS_UPDATE, this));
        return connected;
    }

    protected abstract boolean _connect();

    /**
     * Shutdown method that allows the correct disconnection with the used bridge
     */
    private void disconnect() {

        if (netSanityChecker != null) {
            netSanityChecker.stop();
            netSanityChecker = null;
        }

        _disconnect();
        connected = false;
        requestDisconnection = false;
        iVersionResponse = false;

        broadCastEvent(new MySensorsStatusUpdateEvent(MySensorsEventType.BRIDGE_STATUS_UPDATE, this));
    }

    protected abstract void _disconnect();

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

        addEventListener(this);
        if (!skipStartupCheck) {
            try {
                int i = 0;
                synchronized (this) {
                    while (!iVersionResponse && i < 5) {
                        addMySensorsOutboundMessage(MySensorsBindingConstants.I_VERSION_MESSAGE);
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
                    "Cannot start reading/writing thread, probably sync message (I_VERSION) not received. Try set skipStartupCheck to true");
        }

        return iVersionResponse;
    }

    /**
     * Add a message to the outbound queue. The message will be send automatically. FIFO queue.
     *
     * @param msg The message that should be send.
     */
    public void addMySensorsOutboundMessage(MySensorsMessage msg) {
        addMySensorsOutboundMessage(msg, 1);
    }

    /**
     * Store more than one message in the outbound queue.
     *
     * @param msg the message that should be stored in the queue.
     * @param copy the number of copies that should be stored.
     */
    private void addMySensorsOutboundMessage(MySensorsMessage msg, int copy) {
        synchronized (outboundMessageQueue) {
            try {
                for (int i = 0; i < copy; i++) {
                    outboundMessageQueue.put(msg);
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted message while ruuning");
            }
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
    public void addMySensorsOutboundSmartSleepMessage(MySensorsMessage msg) {
    	
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
    public MySensorsMessage pollMySensorsOutboundQueue() throws InterruptedException {
        return outboundMessageQueue.poll(1, TimeUnit.DAYS);
    }

    /**
     * Check if UpdateListener is registered to receive messages from the bridge.
     *
     * @param listener The listener which should be checked.
     * @return true if listener is registered (and should be able to receive messages)
     */
    public boolean isEventListenerRegisterd(MySensorsUpdateListener listener) {
        boolean ret = false;
        synchronized (updateListeners) {
            ret = updateListeners.contains(listener);
        }

        return ret;
    }

    /**
     * Messages received from the bridge/gateway are handed over to the Things via this UpdateListener
     *
     * @param listener An Object, that wants to listen on status updates
     */
    public void addEventListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (!updateListeners.contains(listener)) {
                logger.trace("Adding listener: {}", listener);
                updateListeners.add(listener);
            }
        }
    }

    /**
     * Remove the UpdateListener and stop receiving messages from the bridge.
     *
     * @param listener The Listener that wants to stop to receive messages.
     */
    public void removeEventListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (updateListeners.contains(listener)) {
                logger.trace("Removing listener: {}", listener);
                updateListeners.remove(listener);
            }
        }
    }

    /**
     * Get a list of the event listeners that are currently registered.
     *
     * @return a list of event listeners.
     */
    public List<MySensorsUpdateListener> getEventListeners() {
        return updateListeners;
    }

    /**
     * Broadcast a message to all registered handlers (things).
     *
     * @param event The message that should be forwarded.
     */
    public void broadCastEvent(MySensorsStatusUpdateEvent event) {
        synchronized (updateListeners) {
            for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                logger.trace("Broadcasting event to: {}", mySensorsEventListener);
                mySensorsEventListener.statusUpdateReceived(event);
            }
        }
    }

    /**
     * Remove a message from the outbound message queue.
     *
     * @param msg The message that should be removed from the queue.
     */
    public void removeMySensorsOutboundMessage(MySensorsMessage msg) {

        pauseWriter = true;

        Iterator<MySensorsMessage> iterator = outboundMessageQueue.iterator();
        if (iterator != null) {
            while (iterator.hasNext()) {
                MySensorsMessage msgInQueue = iterator.next();
                if (msgInQueue.getNodeId() == msg.getNodeId() && msgInQueue.getChildId() == msg.getChildId()
                        && msgInQueue.getMsgType() == msg.getMsgType() && msgInQueue.getSubType() == msg.getSubType()
                        && msgInQueue.getAck() == msg.getAck() && msgInQueue.getMsg().equals(msg.getMsg())) {
                    iterator.remove();
                } else {
                    logger.debug("Message NOT removed: {}", msg.getDebugInfo());
                }
            }
        }

        pauseWriter = false;
    }
    
    /**
     * Remove all messages in the smartsleep queue for a corresponding nodeId / childId combination
     * 
     * @param nodeId the nodeId which messages should be deleted.
     * @param childId the childId which messages should be deleted.
     */
    private void removeSmartSleepMessage(int nodeId, int childId) {
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

    /**
     * Status for the writer / message sender.
     *
     * @return true if writer is paused.
     */
    public boolean isWriterPaused() {
        return pauseWriter;
    }

    /**
     * Is a connection to the bridge available?
     *
     * @return true, if connection is up and running.
     */
    public boolean isConnected() {
        return connected;
    }

    private boolean requestingDisconnection() {
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

    /**
     * Wake up main thread that is waiting for confirmation of link up
     */
    private void handleIncomingVersionMessage(String message) {
        iVersionMessageReceived(message);
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

    private void handleIncomingMessageEvent(MySensorsMessage msg) {
        // Is this an ACK message?
        if (msg.getAck() == 1) {
            logger.debug(String.format("ACK received! Node: %d, Child: %d", msg.nodeId, msg.childId));
            removeMySensorsOutboundMessage(msg);
        }

        // Is this an I_CONFIG message?
        if (msg.isIConfigMessage()) {
            answerIConfigMessage(msg);
        }

        // Is this an I_TIME message?
        if (msg.isITimeMessage()) {
            answerITimeMessage(msg);
        }

        // Is this an I_VERSION message?
        if (msg.isIVersionMessage()) {
            handleIncomingVersionMessage(msg.msg);
        }
        
        // Is this an I_HEARTBEAT_RESPONSE
        if (msg.isHeartbeatResponseMessage()) {
        	handleIncomingHeartbeatMessage(msg);
        }
    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        switch (event.getEventType()) {
            case INCOMING_MESSAGE:
                handleIncomingMessageEvent((MySensorsMessage) event.getData());
                break;
            default:
                break;
        }
    }

    /**
     * Answer to I_TIME message for gateway time request from sensor
     *
     * @param msg, the incoming I_TIME message from sensor
     */
    private void answerITimeMessage(MySensorsMessage msg) {
        logger.info("I_TIME request received from {}, answering...", msg.nodeId);

        String time = Long.toString(System.currentTimeMillis() / 1000);
        MySensorsMessage newMsg = new MySensorsMessage(msg.nodeId, msg.childId, MYSENSORS_MSG_TYPE_INTERNAL, 0, false,
                MYSENSORS_SUBTYPE_I_TIME, time);
        addMySensorsOutboundMessage(newMsg);

    }

    /**
     * Answer to I_CONFIG message for imperial/metric request from sensor
     *
     * @param msg, the incoming I_CONFIG message from sensor
     */
    private void answerIConfigMessage(MySensorsMessage msg) {
        boolean imperial = bridgeHandler.getBridgeConfiguration().imperial;
        String iConfig = imperial ? "I" : "M";

        logger.debug("I_CONFIG request received from {}, answering: (is imperial?){}", iConfig, imperial);

        MySensorsMessage newMsg = new MySensorsMessage(msg.nodeId, msg.childId, MYSENSORS_MSG_TYPE_INTERNAL, 0, false,
                MYSENSORS_SUBTYPE_I_CONFIG, iConfig);
        addMySensorsOutboundMessage(newMsg);

    }
    
    /**
     * If a heartbeat is received from a node the queue should be checked
     * for pending messages for this node. If a message is pending it has to be send immediately.
     * 
     * @param msg The heartbeat message received from a node.
     */
    private void handleIncomingHeartbeatMessage(MySensorsMessage msg) {
    	logger.debug("I_HEARTBEAT_RESPONSE received from {}.", msg.getNodeId());
    	checkPendingSmartSleepMessage(msg.getNodeId());
    }
    
    /**
     * Checks if a message is in the smartsleep queue and adds it to the outbound queues
     * 
     * @param nodeId of the messages that should be send immediately
     */
    private void checkPendingSmartSleepMessage(int nodeId) {
    	Iterator<MySensorsMessage> iterator = smartSleepMessageQueue.iterator();
        if (iterator != null) {
        	
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
     * Debug print of the smart sleep queue content to console
     */
    public void printSmartSleepQueue() {
    	pauseWriter = true;
    	
        Iterator<MySensorsMessage> iterator = smartSleepMessageQueue.iterator();
        if (iterator != null) {
        	
        	logger.debug("####### START SmartSleep queue #####");
        	int i = 1;
        	while (iterator.hasNext()) {
                MySensorsMessage msgInQueue = iterator.next();
                
                logger.debug("Msg: {}, nodeId: {], childId: {}, nextSend: {}.", 
                				i, msgInQueue.getNodeId(), msgInQueue.getChildId(), 
                				msgInQueue.getNextSend());
                i++;
            }
        	logger.debug("####### END SmartSleep queue #####");
        }

        pauseWriter = false;
    }

}
