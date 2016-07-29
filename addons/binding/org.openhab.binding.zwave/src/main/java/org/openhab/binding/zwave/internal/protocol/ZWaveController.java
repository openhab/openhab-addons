/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiInstanceCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInitializationStateEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkStateEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeStatusEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveTransactionCompletedEvent;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeSerializer;
import org.openhab.binding.zwave.internal.protocol.security.SecurityEncapsulatedSerialMessage;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AddNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignSucReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ControllerSetDefaultMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.DeleteReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.EnableSucMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetControllerCapabilitiesMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetRoutingInfoMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetSucNodeIdMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetVersionMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.IdentifyNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.IsFailedNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.MemoryGetIdMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RemoveFailedNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RemoveNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ReplaceFailedNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNetworkUpdateMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeInfoMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeNeighborUpdateMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SendDataMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiGetCapabilitiesMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiGetInitDataMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiSetTimeoutsMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiSoftResetMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SetSucNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWave controller class. Implements communication with the ZWave controller
 * stick using serial messages.
 *
 * @author Chris Jackson
 * @author Victor Belov
 * @author Brian Crosby
 */
public class ZWaveController {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveController.class);

    private static final int ZWAVE_RESPONSE_TIMEOUT = 5000;
    private static final int INITIAL_TX_QUEUE_SIZE = 128;
    private static final int INITIAL_RX_QUEUE_SIZE = 8;
    private static final long WATCHDOG_TIMER_PERIOD = 10000;

    public static final int TRANSMIT_OPTION_ACK = 0x01;
    public static final int TRANSMIT_OPTION_AUTO_ROUTE = 0x04;
    private static final int TRANSMIT_OPTION_EXPLORE = 0x20;

    private final ConcurrentHashMap<Integer, ZWaveNode> zwaveNodes = new ConcurrentHashMap<Integer, ZWaveNode>();
    private final ArrayList<ZWaveEventListener> zwaveEventListeners = new ArrayList<ZWaveEventListener>();
    private final PriorityBlockingQueue<SerialMessage> sendQueue = new PriorityBlockingQueue<SerialMessage>(
            INITIAL_TX_QUEUE_SIZE, new SerialMessage.SerialMessageComparator(this));
    private final PriorityBlockingQueue<SerialMessage> recvQueue = new PriorityBlockingQueue<SerialMessage>(
            INITIAL_RX_QUEUE_SIZE, new SerialMessage.SerialMessageComparator(this));
    private ZWaveSendThread sendThread;
    private ZWaveInputThread inputThread;

    private final Semaphore sendAllowed = new Semaphore(1);
    private final Semaphore transactionCompleted = new Semaphore(1);
    private volatile SerialMessage lastSentMessage = null;
    private long lastMessageStartTime = 0;
    private long longestResponseTime = 0;
    private int zWaveResponseTimeout = ZWAVE_RESPONSE_TIMEOUT;
    private Timer watchdog;

    private String zWaveVersion = "Unknown";
    private String serialAPIVersion = "Unknown";
    private int homeId = 0;
    private int ownNodeId = 0;
    private int manufactureId = 0;
    private int deviceType = 0;
    private int deviceId = 0;
    private int ZWaveLibraryType = 0;
    private int sentDataPointer = 1;
    private boolean setSUC = false;
    private ZWaveDeviceType controllerType = ZWaveDeviceType.UNKNOWN;
    private int sucID = 0;
    private boolean softReset = false;
    private boolean masterController = true;
    private int secureInclusionMode = 0;
    private Set<SerialMessageClass> apiCapabilities = new HashSet<>();

    private AtomicInteger timeOutCount = new AtomicInteger(0);

    private ZWaveIoHandler ioHandler;

    /**
     * This is required for secure pairing. see {@link ZWaveSecurityCommandClass}
     */
    private ZWaveInclusionEvent lastIncludeSlaveFoundEvent;

    // Constructors
    public ZWaveController(ZWaveIoHandler handler) {
        this(handler, new HashMap<String, String>());
    }

    /**
     * Constructor. Creates a new instance of the ZWave controller class.
     *
     * @param handler the io handler to use for communication with the ZWave controller stick.
     * @param config a map of configuration parameters
     * @throws SerialInterfaceException
     *             when a connection error occurs.
     */
    public ZWaveController(ZWaveIoHandler handler, Map<String, String> config) {
        masterController = "true".equals(config.get("masterController"));
        setSUC = "true".equals(config.get("isSUC"));
        softReset = "true".equals(config.get("softReset"));
        secureInclusionMode = config.containsKey("secureInclusion") ? Integer.parseInt(config.get("secureInclusion"))
                : 0;
        final Integer timeout = config.containsKey("timeout") ? Integer.parseInt(config.get("timeout")) : 0;

        logger.info("Starting ZWave controller");

        if (timeout != null && timeout >= 1500 && timeout <= 10000) {
            zWaveResponseTimeout = timeout;
        }
        logger.info("ZWave timeout is set to {}ms. Soft reset is {}.", zWaveResponseTimeout, softReset);
        // this.watchdog = new Timer(true);
        // this.watchdog.schedule(new WatchDogTimerTask(), WATCHDOG_TIMER_PERIOD, WATCHDOG_TIMER_PERIOD);

        ioHandler = handler;

        // We have a delay in running the initialisation sequence to allow any frames queued in the controller to be
        // received before sending the init sequence. This avoids protocol errors (CAN errors).
        Timer initTimer = new Timer();
        initTimer.schedule(new InitializeDelayTask(), 3000);

        sendThread = new ZWaveSendThread();
        sendThread.start();
        inputThread = new ZWaveInputThread();
        inputThread.start();
    }

    private class InitializeDelayTask extends TimerTask {
        private final Logger logger = LoggerFactory.getLogger(InitializeDelayTask.class);

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            logger.debug("Initialising network");
            initialize();
        }
    }

    // Incoming message handlers

    /**
     * Handles incoming Serial Messages. Serial messages can either be messages
     * that are a response to our own requests, or the stick asking us information.
     *
     * @param incomingMessage
     *            the incoming message to process.
     */
    private void handleIncomingMessage(SerialMessage incomingMessage) {
        logger.debug(incomingMessage.toString());

        try {
            switch (incomingMessage.getMessageType()) {
                case Request:
                    handleIncomingRequestMessage(incomingMessage);
                    break;
                case Response:
                    handleIncomingResponseMessage(incomingMessage);
                    break;
                default:
                    logger.warn("Unsupported incomingMessageType: {}", incomingMessage.getMessageType());
            }
        } catch (ZWaveSerialMessageException e) {
            logger.error("Error processing incoming message: {}", e.getMessage());
        }
    }

    /**
     * Handles an incoming request message. An incoming request message is a
     * message initiated by a node or the controller.
     *
     * @param incomingMessage
     *            the incoming message to process.
     */
    private void handleIncomingRequestMessage(SerialMessage incomingMessage) {
        logger.trace("Incoming Message type = REQUEST");

        ZWaveCommandProcessor processor = ZWaveCommandProcessor.getMessageDispatcher(incomingMessage.getMessageClass());
        if (processor == null) {
            logger.warn(String.format("TODO: Implement processing of Request Message = %s (0x%02X)",
                    incomingMessage.getMessageClass() == null ? "--" : incomingMessage.getMessageClass().getLabel(),
                    incomingMessage.getMessageClassKey()));

            return;
        }

        boolean result;
        try {
            result = processor.handleRequest(this, lastSentMessage, incomingMessage);
            if (processor.isTransactionComplete()) {
                notifyEventListeners(new ZWaveTransactionCompletedEvent(this.lastSentMessage, result));
                transactionCompleted.release();
                logger.trace("Released. Transaction completed permit count -> {}",
                        transactionCompleted.availablePermits());
            }
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Handles a failed SendData request. This can either be because of the
     * stick actively reporting it or because of a time-out of the transaction
     * in the send thread.
     *
     * @param originalMessage
     *            the original message that was sent
     */
    private void handleFailedSendDataRequest(SerialMessage originalMessage) {
        new SendDataMessageClass().handleFailedSendDataRequest(this, originalMessage);
    }

    /**
     * Handles an incoming response message. An incoming response message is a
     * response, based one of our own requests.
     *
     * @param incomingMessage
     *            the response message to process.
     */
    private void handleIncomingResponseMessage(SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.trace("Incoming Message type = RESPONSE");

        ZWaveCommandProcessor processor = ZWaveCommandProcessor.getMessageDispatcher(incomingMessage.getMessageClass());
        if (processor == null) {
            logger.warn(String.format("TODO: Implement processing of Response Message = %s (0x%02X)",
                    incomingMessage.getMessageClass().getLabel(), incomingMessage.getMessageClass().getKey()));

            return;
        }

        boolean result = processor.handleResponse(this, lastSentMessage, incomingMessage);
        if (processor.isTransactionComplete()) {
            notifyEventListeners(new ZWaveTransactionCompletedEvent(this.lastSentMessage, result));
            transactionCompleted.release();
            logger.trace("Released. Transaction completed permit count -> {}", transactionCompleted.availablePermits());
        }

        switch (incomingMessage.getMessageClass()) {
            case GetVersion:
                this.zWaveVersion = ((GetVersionMessageClass) processor).getVersion();
                this.ZWaveLibraryType = ((GetVersionMessageClass) processor).getLibraryType();
                break;
            case MemoryGetId:
                this.ownNodeId = ((MemoryGetIdMessageClass) processor).getNodeId();
                this.homeId = ((MemoryGetIdMessageClass) processor).getHomeId();
                break;
            case GetSucNodeId:
                // Remember the SUC ID
                sucID = ((GetSucNodeIdMessageClass) processor).getSucNodeId();

                // If we want to be the SUC, enable it here
                if (setSUC == true && sucID == 0) {
                    // We want to be SUC
                    enqueue(new EnableSucMessageClass().doRequest(EnableSucMessageClass.SUCType.SERVER));
                    enqueue(new SetSucNodeMessageClass().doRequest(ownNodeId, SetSucNodeMessageClass.SUCType.SERVER));
                } else if (setSUC == false && sucID == ownNodeId) {
                    // We don't want to be SUC, but we currently are!
                    // Disable SERVER functionality, and set the node to 0
                    enqueue(new EnableSucMessageClass().doRequest(EnableSucMessageClass.SUCType.NONE));
                    enqueue(new SetSucNodeMessageClass().doRequest(ownNodeId, SetSucNodeMessageClass.SUCType.NONE));
                }
                enqueue(new GetControllerCapabilitiesMessageClass().doRequest());
                break;
            case GetControllerCapabilities:
                controllerType = ((GetControllerCapabilitiesMessageClass) processor).getDeviceType();
                break;
            case SerialApiGetCapabilities:
                serialAPIVersion = ((SerialApiGetCapabilitiesMessageClass) processor).getSerialAPIVersion();
                manufactureId = ((SerialApiGetCapabilitiesMessageClass) processor).getManufactureId();
                deviceId = ((SerialApiGetCapabilitiesMessageClass) processor).getDeviceId();
                deviceType = ((SerialApiGetCapabilitiesMessageClass) processor).getDeviceType();
                apiCapabilities = ((SerialApiGetCapabilitiesMessageClass) processor).getApiCapabilities();

                enqueue(new SerialApiGetInitDataMessageClass().doRequest());
                break;
            case SerialApiGetInitData:
                // this.isConnected = true;
                for (Integer nodeId : ((SerialApiGetInitDataMessageClass) processor).getNodes()) {
                    addNode(nodeId);
                }

                // Notify the system that we're up and running
                notifyEventListeners(new ZWaveNetworkStateEvent(true));
                break;
            default:
                break;
        }
    }

    // Controller methods

    /**
     * Removes the node, and restarts the initialisation sequence
     *
     * @param nodeId
     */
    public void reinitialiseNode(int nodeId) {
        this.zwaveNodes.remove(nodeId);
        addNode(nodeId);
    }

    /**
     * Add a node to the controller
     *
     * @param nodeId
     *            the node number to add
     */
    private void addNode(int nodeId) {
        ZWaveEvent zEvent = new ZWaveInitializationStateEvent(nodeId, ZWaveNodeInitStage.EMPTYNODE);
        notifyEventListeners(zEvent);

        ioHandler.deviceDiscovered(nodeId);
        new ZWaveInitNodeThread(this, nodeId).start();
    }

    private class ZWaveInitNodeThread extends Thread {
        int nodeId;
        ZWaveController controller;

        ZWaveInitNodeThread(ZWaveController controller, int nodeId) {
            this.nodeId = nodeId;
            this.controller = controller;
        }

        @Override
        public void run() {
            logger.debug("NODE {}: Init node thread start", nodeId);

            // Check if the node exists
            if (zwaveNodes.get(nodeId) != null) {
                logger.warn("NODE {}: Attempting to add node that already exists", nodeId);
                return;
            }

            boolean serializedOk = false;
            ZWaveNode node = null;
            try {
                ZWaveNodeSerializer nodeSerializer = new ZWaveNodeSerializer();
                node = nodeSerializer.DeserializeNode(nodeId);
            } catch (Exception e) {
                logger.error("NODE {}: Restore from config: Error deserialising XML file. {}", nodeId, e.toString());
                node = null;
            }

            // Did the node deserialise ok?
            if (node != null) {
                // Sanity check the data from the file
                if (node.getManufacturer() == Integer.MAX_VALUE || node.getHomeId() != controller.homeId
                        || node.getNodeId() != nodeId) {
                    logger.warn("NODE {}: Restore from config: Error. Data invalid, ignoring config.", nodeId);
                    node = null;
                } else {
                    // The restore was ok, but we have some work to set up the links that aren't
                    // made as the deserialiser doesn't call the constructor
                    serializedOk = true;
                    logger.debug("NODE {}: Restore from config: Ok.", nodeId);
                    node.setRestoredFromConfigfile(controller);

                    // Set the controller and node references for all command classes
                    for (ZWaveCommandClass commandClass : node.getCommandClasses()) {
                        commandClass.setController(controller);
                        commandClass.setNode(node);

                        // Handle event handlers
                        if (commandClass instanceof ZWaveEventListener) {
                            controller.addEventListener((ZWaveEventListener) commandClass);
                        }

                        // If this is the multi-instance class, add all command classes for the endpoints
                        if (commandClass instanceof ZWaveMultiInstanceCommandClass) {
                            for (ZWaveEndpoint endPoint : ((ZWaveMultiInstanceCommandClass) commandClass)
                                    .getEndpoints()) {
                                for (ZWaveCommandClass endpointCommandClass : endPoint.getCommandClasses()) {
                                    endpointCommandClass.setController(controller);
                                    endpointCommandClass.setNode(node);
                                    endpointCommandClass.setEndpoint(endPoint);

                                    // Handle event handlers
                                    if (endpointCommandClass instanceof ZWaveEventListener) {
                                        controller.addEventListener((ZWaveEventListener) endpointCommandClass);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Create a new node if it wasn't deserialised ok
            if (node == null) {
                node = new ZWaveNode(controller.homeId, nodeId, controller);
            }

            if (nodeId == controller.ownNodeId) {
                // This is the controller node.
                // We already know the device type, id, manufacturer so set it here.
                // It won't be set later as we probably won't request the manufacturer specific data
                node.setDeviceId(controller.getDeviceId());
                node.setDeviceType(controller.getDeviceType());
                node.setManufacturer(controller.getManufactureId());
            }

            // Place nodes in the local ZWave Controller
            controller.zwaveNodes.putIfAbsent(nodeId, node);

            // If we loaded from file, then we need to add this to the discovery
            // since we bypass the initial discovery phases
            if (serializedOk == true) {
                ZWaveEvent zEvent = new ZWaveInitializationStateEvent(node.getNodeId(),
                        ZWaveNodeInitStage.DISCOVERY_COMPLETE);
                controller.notifyEventListeners(zEvent);
            }

            // Kick off the initialisation
            node.initialiseNode();

            logger.debug("NODE {}: Init node thread finished", nodeId);
        }
    }

    /**
     * Enqueues a message for sending on the send queue.
     *
     * @param serialMessage
     *            the serial message to enqueue.
     */
    public void enqueue(SerialMessage serialMessage) {
        // Sanity check!
        if (serialMessage == null) {
            return;
        }

        // First try and get the node
        // If we're sending to a node, then this obviously isn't to the controller, and we should
        // queue anything to a battery node (ie a node supporting the WAKEUP class)!
        ZWaveNode node = this.getNode(serialMessage.getMessageNode());
        if (node != null) {
            // Keep track of the number of packets sent to this device
            node.incrementSendCount();

            // Does this message need to be security encapsulated?
            if (node.doesMessageRequireSecurityEncapsulation(serialMessage)) {
                ZWaveSecurityCommandClass securityCommandClass = (ZWaveSecurityCommandClass) node
                        .getCommandClass(CommandClass.SECURITY);
                securityCommandClass.queueMessageForEncapsulationAndTransmission(serialMessage);
                // The above call will call enqueue again with the encapsulated message,
                // so we discard this one without putting it on the queue
                return;
            }

            // If the device isn't listening, queue the message if it supports
            // the wakeup class
            if (!node.isListening() && !node.isFrequentlyListening()) {
                ZWaveWakeUpCommandClass wakeUpCommandClass = (ZWaveWakeUpCommandClass) node
                        .getCommandClass(CommandClass.WAKE_UP);

                // If it's a battery operated device, check if it's awake or
                // place in wake-up queue.
                if (wakeUpCommandClass != null && !wakeUpCommandClass.processOutgoingWakeupMessage(serialMessage)) {
                    return;
                }
            }
        }

        // Add the message to the queue
        this.sendQueue.add(serialMessage);
        logger.debug("Message queued. Queue length = {}. Queue={}", this.sendQueue.size());// , this.sendQueue);
    }

    /**
     * Returns the size of the send queue.
     */
    public int getSendQueueLength() {
        return this.sendQueue.size();
    }

    /**
     * Notify our own event listeners of a ZWave event.
     *
     * @param event
     *            the event to send.
     */
    public void notifyEventListeners(ZWaveEvent event) {
        logger.debug("Notifying event listeners: {}", event.getClass().getSimpleName());
        ArrayList<ZWaveEventListener> copy = new ArrayList<ZWaveEventListener>(zwaveEventListeners);
        for (ZWaveEventListener listener : copy) {
            listener.ZWaveIncomingEvent(event);
        }

        // We also need to handle the inclusion internally within the controller
        if (event instanceof ZWaveInclusionEvent) {
            ZWaveInclusionEvent incEvent = (ZWaveInclusionEvent) event;
            switch (incEvent.getEvent()) {
                case IncludeSlaveFound:
                    // When a device is found we get the IncludeSlaveFound notification.
                    // Here we need to end inclusion.
                    requestAddNodesStop();
                    logger.debug("NODE {}: Including node.", incEvent.getNodeId());

                    // First make sure this isn't an existing node
                    if (getNode(incEvent.getNodeId()) != null) {
                        logger.debug("NODE {}: Newly included node already exists - not initialising.",
                                incEvent.getNodeId());
                        break;
                    }

                    // TODO: This can be removed once the key is added to the security class directly
                    // TODO: a few lines below.
                    lastIncludeSlaveFoundEvent = incEvent;

                    // Create a new node
                    ZWaveNode newNode = new ZWaveNode(homeId, incEvent.getNodeId(), this);

                    // Add the device class
                    ZWaveDeviceClass deviceClass = newNode.getDeviceClass();
                    deviceClass.setBasicDeviceClass(incEvent.getBasic());
                    deviceClass.setGenericDeviceClass(incEvent.getGeneric());
                    deviceClass.setSpecificDeviceClass(incEvent.getSpecific());

                    // If we have the NIF as part of the inclusion, use it
                    // TODO: This code now appears in multiple places - consolidate into the node
                    for (CommandClass commandClass : incEvent.getCommandClasses()) {
                        ZWaveCommandClass zwaveCommandClass = ZWaveCommandClass.getInstance(commandClass.getKey(),
                                newNode, this);
                        if (zwaveCommandClass != null) {
                            logger.debug("NODE {}: Inclusion is adding command class {}.", incEvent.getNodeId(),
                                    commandClass);
                            // TODO: Add the network key to the security class
                            if (commandClass == CommandClass.SECURITY) {
                                // ((ZWaveSecurityCommandClass)zwaveCommandClass).setRealNetworkKey(hexString);
                            }
                            newNode.addCommandClass(zwaveCommandClass);
                        }
                    }

                    // Place nodes in the local ZWave Controller
                    zwaveNodes.putIfAbsent(incEvent.getNodeId(), newNode);
                    break;

                case IncludeDone:
                    // Kick off the initialisation.
                    // Since the node is awake, we jump straight into the initialisation sequence
                    // without some of the initial stages like PING that are designed to detect if
                    // the device is responding.
                    // This is primarily designed to speed up the secure inclusion but is valid for all.
                    // TODO: There's an assumption here that the whole NIF is provided with the inclusion method
                    // -- we might want to keep an eye on this in case it's incorrect!
                    ZWaveNode node = getNode(incEvent.getNodeId());
                    if (node == null) {
                        logger.debug("NODE {}: Newly included node doesn't exist - initialising from start.",
                                incEvent.getNodeId());
                        // Add the node using addNode()
                        // This seems to happen if we exclude the node, then add it back in.
                        // We don't get the IncludeSlaveFound notification, just the IncludeDone notification.
                        addNode(incEvent.getNodeId());
                        break;
                    }

                    // If this node is already initialising, then do nothing.
                    // This might happen if a node is re-added even when we are aware of it
                    if (node.getNodeInitStage() != ZWaveNodeInitStage.EMPTYNODE) {
                        logger.debug("NODE {}: Newly included node already initialising at {}", incEvent.getNodeId(),
                                node.getNodeInitStage());
                        break;
                    }

                    // Start initialisation...
                    // If we just included this through the IncludeSlaveFound, then we'll already know the device class
                    if (node.getDeviceClass().getBasicDeviceClass() != Basic.NOT_KNOWN) {
                        node.initialiseNode(ZWaveNodeInitStage.INCLUSION_START);
                    } else {
                        node.initialiseNode(ZWaveNodeInitStage.EMPTYNODE);
                    }
                    break;

                case ExcludeDone:
                    logger.debug("NODE {}: Excluding node.", incEvent.getNodeId());
                    // Remove the node from the controller
                    if (getNode(incEvent.getNodeId()) == null) {
                        logger.debug("NODE {}: Excluding node that doesn't exist.", incEvent.getNodeId());
                        break;
                    }
                    zwaveNodes.remove(incEvent.getNodeId());

                    // Remove the XML file
                    ZWaveNodeSerializer nodeSerializer = new ZWaveNodeSerializer();
                    nodeSerializer.DeleteNode(event.getNodeId());
                    break;
                default:
                    break;
            }
        } else if (event instanceof ZWaveNetworkEvent) {
            ZWaveNetworkEvent networkEvent = (ZWaveNetworkEvent) event;
            switch (networkEvent.getEvent()) {
                case DeleteNode:
                    if (getNode(networkEvent.getNodeId()) == null) {
                        logger.debug("NODE {}: Deleting a node that doesn't exist.", networkEvent.getNodeId());
                        break;
                    }
                    this.zwaveNodes.remove(networkEvent.getNodeId());

                    // Remove the XML file
                    ZWaveNodeSerializer nodeSerializer = new ZWaveNodeSerializer();
                    nodeSerializer.DeleteNode(event.getNodeId());
                    break;
                default:
                    break;
            }
        } else if (event instanceof ZWaveNodeStatusEvent) {
            ZWaveNodeStatusEvent statusEvent = (ZWaveNodeStatusEvent) event;
            logger.debug("NODE {}: Node Status event - Node is {}", statusEvent.getNodeId(), statusEvent.getState());

            // Get the node
            ZWaveNode node = getNode(event.getNodeId());
            if (node == null) {
                logger.error("NODE {}: Node is unknown!", statusEvent.getNodeId());
                return;
            }

            // Handle node state changes
            switch (statusEvent.getState()) {
                case DEAD:
                    break;
                case FAILED:
                    break;
                case ALIVE:
                    break;
            }
        }
    }

    /**
     * Initializes communication with the ZWave controller stick.
     *
     */
    public void initialize() {
        enqueue(new GetVersionMessageClass().doRequest());
        enqueue(new MemoryGetIdMessageClass().doRequest());
        enqueue(new SerialApiGetCapabilitiesMessageClass().doRequest());
        enqueue(new SerialApiSetTimeoutsMessageClass().doRequest(150, 15));
        enqueue(new GetSucNodeIdMessageClass().doRequest());
    }

    /**
     * Send Identify Node message to the controller.
     *
     * @param nodeId
     *            the nodeId of the node to identify
     *
     */
    public void identifyNode(int nodeId) {
        enqueue(new IdentifyNodeMessageClass().doRequest(nodeId));
    }

    /**
     * Send Request Node info message to the controller.
     *
     * @param nodeId
     *            the nodeId of the node to identify
     *
     */
    public void requestNodeInfo(int nodeId) {
        enqueue(new RequestNodeInfoMessageClass().doRequest(nodeId));
    }

    /**
     * Polls a node for any dynamic information
     *
     * @param node
     *
     */
    /*
     * public void pollNode(ZWaveNode node) {
     * for (ZWaveCommandClass zwaveCommandClass : node.getCommandClasses()) {
     * logger.trace("NODE {}: Inspecting command class {}", node.getNodeId(),
     * zwaveCommandClass.getCommandClass().getLabel());
     * if (zwaveCommandClass instanceof ZWaveCommandClassDynamicState) {
     * logger.debug("NODE {}: Found dynamic state command class {}", node.getNodeId(),
     * zwaveCommandClass.getCommandClass().getLabel());
     * ZWaveCommandClassDynamicState zdds = (ZWaveCommandClassDynamicState) zwaveCommandClass;
     * int instances = zwaveCommandClass.getInstances();
     * if (instances == 1) {
     * Collection<SerialMessage> dynamicQueries = zdds.getDynamicValues(true);
     * for (SerialMessage serialMessage : dynamicQueries) {
     * sendData(serialMessage);
     * }
     * } else {
     * for (int i = 1; i <= instances; i++) {
     * Collection<SerialMessage> dynamicQueries = zdds.getDynamicValues(true);
     * for (SerialMessage serialMessage : dynamicQueries) {
     * sendData(node.encapsulate(serialMessage, zwaveCommandClass, i));
     * }
     * }
     * }
     * } else if (zwaveCommandClass instanceof ZWaveMultiInstanceCommandClass) {
     * ZWaveMultiInstanceCommandClass multiInstanceCommandClass = (ZWaveMultiInstanceCommandClass) zwaveCommandClass;
     * for (ZWaveEndpoint endpoint : multiInstanceCommandClass.getEndpoints()) {
     * for (ZWaveCommandClass endpointCommandClass : endpoint.getCommandClasses()) {
     * logger.trace("NODE {}: Inspecting command class {} for endpoint {}", node.getNodeId(),
     * endpointCommandClass.getCommandClass().getLabel(), endpoint.getEndpointId());
     * if (endpointCommandClass instanceof ZWaveCommandClassDynamicState) {
     * logger.debug("NODE {}: Found dynamic state command class {}", node.getNodeId(),
     * endpointCommandClass.getCommandClass().getLabel());
     * ZWaveCommandClassDynamicState zdds2 = (ZWaveCommandClassDynamicState) endpointCommandClass;
     * Collection<SerialMessage> dynamicQueries = zdds2.getDynamicValues(true);
     * for (SerialMessage serialMessage : dynamicQueries) {
     * sendData(node.encapsulate(serialMessage, endpointCommandClass,
     * endpoint.getEndpointId()));
     * }
     * }
     * }
     * }
     * }
     * }
     * }
     */

    /**
     * Request the node routing information.
     *
     * @param nodeId
     *            The address of the node to update
     *
     */
    public void requestNodeRoutingInfo(int nodeId) {
        this.enqueue(new GetRoutingInfoMessageClass().doRequest(nodeId));
    }

    /**
     * Request the node neighbor list to be updated for the specified node. Once
     * this is complete, the requestNodeRoutingInfo will be called automatically
     * to update the data in the binding.
     *
     * @param nodeId
     *            The address of the node to update
     *
     */
    public void requestNodeNeighborUpdate(int nodeId) {
        enqueue(new RequestNodeNeighborUpdateMessageClass().doRequest(nodeId));
    }

    /**
     * Puts the controller into inclusion mode to add new nodes
     *
     * @param inclusionMode the mode to use for inclusion.
     *            <br>
     *            0=Low Power Inclusion
     *            <br>
     *            1=High Power Inclusion
     *            <br>
     *            2=Network Wide Inclusion
     *
     */
    public void requestAddNodesStart(int inclusionMode) {
        if (exclusion == true || inclusion == true) {
            logger.debug("ZWave exclusion already in progress - aborted");
            return;
        }

        logger.debug("ZWave controller start inclusion - mode {}", inclusionMode);

        // Check if the stick supports NWI - if not, revert to HPI
        if (inclusionMode == 2 && hasApiCapability(SerialMessageClass.ExploreRequestInclusion) == false) {
            inclusionMode = 1;
        }

        boolean highPower;
        boolean networkWide;
        switch (inclusionMode) {
            case 0:
                highPower = false;
                networkWide = false;
                break;
            case 1:
                highPower = true;
                networkWide = false;
                break;
            default:
                highPower = true;
                networkWide = true;
                break;
        }

        enqueue(new AddNodeMessageClass().doRequestStart(highPower, networkWide));
        inclusion = true;
        startInclusionTimer();
    }

    /**
     * Terminates the inclusion mode
     *
     */
    private void requestAddNodesStop() {
        enqueue(new AddNodeMessageClass().doRequestStop());
        logger.debug("ZWave controller end inclusion");
    }

    /**
     * Puts the controller into exclusion mode to remove new nodes
     *
     */
    public void requestRemoveNodesStart() {
        if (exclusion == true || inclusion == true) {
            logger.debug("ZWave exclusion already in progress - aborted");
            return;
        }
        enqueue(new RemoveNodeMessageClass().doRequestStart());
        exclusion = true;
        startInclusionTimer();
        logger.debug("ZWave controller start exclusion");
    }

    /**
     * Requests a network update
     *
     */
    public void requestRequestNetworkUpdate() {
        enqueue(new RequestNetworkUpdateMessageClass().doRequest());
        logger.debug("ZWave controller request network update");
    }

    /**
     * Terminates the exclusion mode
     *
     */
    private void requestRemoveNodesStop() {
        enqueue(new RemoveNodeMessageClass().doRequestStop());
        logger.debug("ZWave controller end exclusion");
    }

    /**
     * Terminates inclusion or exclusion mode - which-ever is running
     *
     */
    public void requestInclusionStop() {
        stopInclusionTimer();
    }

    // The following timer class implements a re-triggerable timer to stop the inclusion
    // mode after 30 seconds.
    private Timer timer = new Timer();
    private TimerTask timerTask = null;
    private boolean inclusion = false;
    private boolean exclusion = false;

    private class InclusionTimerTask extends TimerTask {
        @Override
        public void run() {
            logger.debug("Ending inclusion mode.");
            stopInclusionTimer();
        }
    }

    private synchronized void startInclusionTimer() {
        // Stop any existing timer
        if (timerTask != null) {
            timerTask.cancel();
        }

        // Create the timer task
        timerTask = new InclusionTimerTask();

        // Start the timer for 30 seconds
        timer.schedule(timerTask, 30000);
    }

    /**
     * Stops any pending inclusion/exclusion.
     * Resets flags, and signals to controller.
     */
    private synchronized void stopInclusionTimer() {
        logger.debug("Stopping inclusion timer.");
        if (inclusion) {
            requestAddNodesStop();
        } else if (exclusion) {
            requestRemoveNodesStop();
        } else {
            logger.error("Neither inclusion nor exclusion was active!");
        }

        inclusion = false;
        exclusion = false;

        // Stop the timer
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /**
     * Sends a request to perform a soft reset on the controller. This will just
     * reset the controller - probably similar to a power cycle. It doesn't
     * reinitialise the network, or change the network configuration.
     * <p>
     * <b>NOTE</b>: On some (most!) sticks, this doesn't return a response. Therefore, the number of retries is set to
     * 1. <br>
     * <b>NOTE</b>: On some (most!) ZWave-Plus sticks, this can cause the stick to hang.
     *
     */
    public void requestSoftReset() {
        SerialMessage msg = new SerialApiSoftResetMessageClass().doRequest();
        msg.attempts = 1;
        enqueue(msg);
        logger.debug("ZWave controller soft reset");
    }

    /**
     * Sends a request to perform a hard reset on the controller.
     * This will reset the controller to its default, resetting the network completely
     *
     */
    public void requestHardReset() {
        // Clear the queues
        // If we're resetting, there's no point in queuing messages!
        sendQueue.clear();
        recvQueue.clear();

        SerialMessage msg = new ControllerSetDefaultMessageClass().doRequest();
        msg.attempts = 1;
        enqueue(msg);

        // Clear all the nodes and we'll reinitialise
        zwaveNodes.clear();
        enqueue(new SerialApiGetInitDataMessageClass().doRequest());
        logger.debug("ZWave controller hard reset");
    }

    /**
     * Request if the node is currently marked as failed by the controller.
     *
     * @param nodeId
     *            The address of the node to check
     */
    public void requestIsFailedNode(int nodeId) {
        enqueue(new IsFailedNodeMessageClass().doRequest(nodeId));
    }

    /**
     * Removes a failed node from the network. Note that this won't remove nodes
     * that have not failed.
     *
     * @param nodeId
     *            The address of the node to remove
     */
    public void requestRemoveFailedNode(int nodeId) {
        enqueue(new RemoveFailedNodeMessageClass().doRequest(nodeId));
    }

    /**
     * Marks a node as failed
     *
     * @param nodeId
     *            The address of the node to set failed
     */
    public void requestSetFailedNode(int nodeId) {
        enqueue(new ReplaceFailedNodeMessageClass().doRequest(nodeId));
    }

    /**
     * Delete all return nodes from the specified node. This should be performed
     * before updating the routes
     *
     * @param nodeId
     */
    public void requestDeleteAllReturnRoutes(int nodeId) {
        enqueue(new DeleteReturnRouteMessageClass().doRequest(nodeId));
    }

    /**
     * Request the controller to set the return route between two nodes
     *
     * @param nodeId
     *            Source node
     * @param destinationId
     *            Destination node
     */
    public void requestAssignReturnRoute(int nodeId, int destinationId) {
        enqueue(new AssignReturnRouteMessageClass().doRequest(nodeId, destinationId, getCallbackId()));
    }

    /**
     * Request the controller to set the return route from a node to the
     * controller
     *
     * @param nodeId
     *            Source node
     */
    public void requestAssignSucReturnRoute(int nodeId) {
        enqueue(new AssignSucReturnRouteMessageClass().doRequest(nodeId, getCallbackId()));
    }

    /**
     * Returns the next callback ID
     *
     * @return callback ID
     */
    public int getCallbackId() {
        if (++sentDataPointer > 0xFF) {
            sentDataPointer = 1;
        }
        logger.trace("Callback ID = {}", sentDataPointer);

        return sentDataPointer;
    }

    /**
     * Transmits the SerialMessage to a single ZWave Node. Sets the transmission options as well.
     *
     * @param serialMessage
     *            the Serial message to send.
     */
    public void sendData(SerialMessage serialMessage) {
        if (serialMessage == null) {
            return;
        }
        if (serialMessage.getMessageClass() != SerialMessageClass.SendData) {
            logger.error(String.format("Invalid message class %s (0x%02X) for sendData",
                    serialMessage.getMessageClass().getLabel(), serialMessage.getMessageClass().getKey()));
            return;
        }
        if (serialMessage.getMessageType() != SerialMessageType.Request) {
            logger.error("Only request messages can be sent");
            return;
        }

        // We need to wait on the ACK from the controller before completing the transaction.
        // This is required in case the Application Message is received from the SendData ACK
        serialMessage.setAckRequired();

        // ZWaveSecurityCommandClass needs to set it's own transmit options. Only set them here if not already done
        if (!serialMessage.areTransmitOptionsSet()) {
            serialMessage
                    .setTransmitOptions(TRANSMIT_OPTION_ACK | TRANSMIT_OPTION_AUTO_ROUTE | TRANSMIT_OPTION_EXPLORE);
        }
        serialMessage.setCallbackId(getCallbackId());
        enqueue(serialMessage);
    }

    /**
     * Add a listener for ZWave events to this controller.
     *
     * @param eventListener
     *            the event listener to add.
     */
    public void addEventListener(ZWaveEventListener eventListener) {
        synchronized (zwaveEventListeners) {
            // First, check if this listener is already registered
            if (zwaveEventListeners.contains(eventListener)) {
                logger.debug("Event Listener {} already registered", eventListener);
                return;
            }
            zwaveEventListeners.add(eventListener);
        }
    }

    /**
     * Remove a listener for ZWave events to this controller.
     *
     * @param eventListener
     *            the event listener to remove.
     */
    public void removeEventListener(ZWaveEventListener eventListener) {
        synchronized (zwaveEventListeners) {
            zwaveEventListeners.remove(eventListener);
        }
    }

    /**
     * Gets the API Version of the controller.
     *
     * @return the serialAPIVersion
     */
    public String getSerialAPIVersion() {
        return serialAPIVersion;
    }

    /**
     * Gets the zWave Version of the controller.
     *
     * @return the zWaveVersion
     */
    public String getZWaveVersion() {
        return zWaveVersion;
    }

    /**
     * Gets the Manufacturer ID of the controller.
     *
     * @return the manufactureId
     */
    public int getManufactureId() {
        return manufactureId;
    }

    /**
     * Gets the device type of the controller;
     *
     * @return the deviceType
     */
    public int getDeviceType() {
        return deviceType;
    }

    /**
     * Gets the device ID of the controller.
     *
     * @return the deviceId
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the node ID of the controller.
     *
     * @return the deviceId
     */
    public int getOwnNodeId() {
        return ownNodeId;
    }

    /**
     * Gets the device type of the controller.
     *
     * @return the device type
     */
    public ZWaveDeviceType getControllerType() {
        return controllerType;
    }

    /**
     * Gets the networks SUC controller ID.
     *
     * @return the device id of the SUC, or 0 if none exists
     */
    public int getSucId() {
        return sucID;
    }

    /**
     * Returns true if the binding is the master controller in the network. The
     * master controller simply means that we get notifications.
     *
     * @return true if this is the master
     */
    public boolean isMasterController() {
        return masterController;
    }

    /**
     * Checks if the serial API supports the given capability.
     *
     * @param capability
     *            the capability to check
     * @return true if the controller API support the capability
     */
    public boolean hasApiCapability(SerialMessageClass capability) {
        return apiCapabilities.contains(capability);
    }

    /**
     * Returns the secure inclusion mode
     *
     * @return
     *         0 ENTRY_CONTROL
     *         1 All Devices
     */
    public int getSecureInclusionMode() {
        return secureInclusionMode;
    }

    /**
     * Gets the node object using it's node ID as key. Returns null if the node
     * is not found
     *
     * @param nodeId
     *            the Node ID of the node to get.
     * @return node object
     */
    public ZWaveNode getNode(int nodeId) {
        return this.zwaveNodes.get(nodeId);
    }

    /**
     * Gets the node list
     *
     * @return
     */
    public Collection<ZWaveNode> getNodes() {
        return this.zwaveNodes.values();
    }

    /**
     * Returns the number of Time-Outs while sending.
     *
     * @return the timeoutCount
     */
    public int getTimeOutCount() {
        return timeOutCount.get();
    }

    // Nested classes and enumerations

    /**
     * Input thread. This processes incoming messages - it decouples the receive thread, which responds to messages from
     * the controller, and the actual processing of messages to ensure we respond to the controller in a timely manner
     *
     * @author Chris Jackson
     */
    private class ZWaveInputThread extends Thread {
        ZWaveInputThread() {
            super("ZWaveInputThread");
        }

        /**
         * Run method. Runs the actual receiving process.
         */
        @Override
        public void run() {
            logger.debug("Starting ZWave thread: Input");

            SerialMessage recvMessage;
            while (!interrupted()) {
                try {
                    if (recvQueue.size() == 0) {
                        sendAllowed.release();
                    }
                    recvMessage = recvQueue.take();
                    logger.debug("Receive queue TAKE: Length={}", recvQueue.size());
                    logger.debug("Process Message = {}", SerialMessage.bb2hex(recvMessage.getMessageBuffer()));

                    // logger.debug("Receive ---- do receive");
                    handleIncomingMessage(recvMessage);
                    // logger.debug("Receive ---- try acquire");
                    sendAllowed.tryAcquire();
                    // logger.debug("Receive ---- acquired");
                } catch (InterruptedException e) {
                    logger.error("Exception during ZWave thread: Input 1. {}", e);
                    break;
                } catch (Exception e) {
                    logger.error("Exception during ZWave thread: Input 2. {}", e);
                }
            }

            logger.debug("Stopped ZWave thread: Input");
        }
    }

    /**
     * ZWave controller Send Thread. Takes care of sending all messages. It uses a semaphore to synchronize
     * communication with the receiving thread.
     *
     * @author Jan-Willem Spuij
     * @author Chris Jackson
     */
    private class ZWaveSendThread extends Thread {

        private final Logger logger = LoggerFactory.getLogger(ZWaveSendThread.class);

        ZWaveSendThread() {
            super("ZWaveSendThread");
        }

        /**
         * Run method. Runs the actual sending process.
         */
        @Override
        public void run() {
            logger.debug("Starting ZWave thread: Send");
            try {
                while (!interrupted()) {
                    // To avoid sending lots of frames when we still have input frames to process, we wait here until
                    // we've processed all receive frames
                    if (!sendAllowed.tryAcquire(1, zWaveResponseTimeout, TimeUnit.MILLISECONDS)) {
                        sendAllowed.release();
                        logger.warn("Receive queue TIMEOUT:", recvQueue.size());
                        continue;
                    }
                    sendAllowed.release();

                    // Take the next message from the send queue
                    try {
                        lastSentMessage = sendQueue.take();
                        logger.debug("Took message from queue for sending. Queue length = {}", sendQueue.size());
                    } catch (InterruptedException e) {
                        logger.error("Send thread aborted!!!!!!!! {}", e);
                        break;
                    }

                    // Check we got a message
                    if (lastSentMessage == null) {
                        continue;
                    }

                    // Get the node for this message
                    ZWaveNode node = getNode(lastSentMessage.getMessageNode());

                    // If it's a battery device, it needs to be awake, or we queue the frame until it is.
                    if (node != null && !node.isListening() && !node.isFrequentlyListening()) {
                        ZWaveWakeUpCommandClass wakeUpCommandClass = (ZWaveWakeUpCommandClass) node
                                .getCommandClass(CommandClass.WAKE_UP);

                        // If it's a battery operated device, check if it's awake or place in wake-up queue.
                        if (wakeUpCommandClass != null
                                && !wakeUpCommandClass.processOutgoingWakeupMessage(lastSentMessage)) {
                            continue;
                        }
                    }

                    // A transaction consists of (up to) 4 parts -:
                    // 1) We send a REQUEST to the controller.
                    // 2) The controller sends a RESPONSE almost immediately. This RESPONSE typically tells us that the
                    // message was, or wasn't, added to the sticks queue.
                    // 3) The controller sends a REQUEST once it's received the response from the device. We need to be
                    // aware that there is no synchronization of messages between steps 2 and 3 so we can get other
                    // messages received at step 3 that are not related to our original request.
                    // 4) We ultimately receive the requested message from the device if we're requesting such a
                    // message. Again, other messages can come in during this time.
                    //
                    // A transaction is generally completed at the completion of step 4.
                    // However, for some messages, there may not be a further REQUEST so the transaction is terminated
                    // at step 2. This is handled by the serial message class processor by setting transactionCompleted.
                    //
                    // It seems that some of these steps may occur out of order.
                    // For example, the requested message at step 4 may be received before the REQUEST at step 3. This
                    // can (I guess) occur if the message to the device is received by the device, but the ACK back to
                    // the controller is lost. The device then sends the requested data, and then finally the ACK is
                    // received. We cover this by setting an 'AckPending' flag in the sent message.
                    // This needs to be cleared before the transaction is completed.

                    // Clear the semaphore used to acknowledge the completed transaction.
                    transactionCompleted.drainPermits();

                    // Send the REQUEST message TO the controller
                    ioHandler.sendPacket(lastSentMessage);
                    lastMessageStartTime = System.currentTimeMillis();

                    if (lastSentMessage instanceof SecurityEncapsulatedSerialMessage) {
                        // Now that we've sent the encapsulated version, replace lastSentMessage with the original.
                        // This is required because a resend requires a new nonce to be requested and a new
                        // security encapsulated message to be built
                        ((SecurityEncapsulatedSerialMessage) lastSentMessage).setTransmittedAt();
                        // Take the callbackId from the encapsulated version and copy it to the original message
                        int callbackId = lastSentMessage.getCallbackId();
                        lastSentMessage = ((SecurityEncapsulatedSerialMessage) lastSentMessage)
                                .getMessageBeingEncapsulated();
                        lastSentMessage.setCallbackId(callbackId);
                    }

                    // Now wait for the RESPONSE, or REQUEST message FROM the controller
                    // This will terminate when the transactionCompleted flag gets set
                    // So, this might complete on a RESPONSE if there's an error (or no further REQUEST expected) or it
                    // might complete on a subsequent REQUEST.
                    try {
                        if (!transactionCompleted.tryAcquire(1, zWaveResponseTimeout, TimeUnit.MILLISECONDS)) {
                            timeOutCount.incrementAndGet();
                            // If this is a SendData message, then we need to abort
                            // This should only be sent if we didn't get the initial ACK!!!
                            // So we need to check the ACK flag and only abort if it's not set
                            if (lastSentMessage.getMessageClass() == SerialMessageClass.SendData
                                    && lastSentMessage.isAckPending()) {
                                SerialMessage serialMessage = new SerialMessage(SerialMessageClass.SendDataAbort,
                                        SerialMessageType.Request, SerialMessageClass.SendData,
                                        SerialMessagePriority.Immediate);
                                logger.debug("NODE {}: Sending ABORT Message = {}", lastSentMessage.getMessageNode(),
                                        SerialMessage.bb2hex(serialMessage.getMessageBuffer()));

                                ioHandler.sendPacket(serialMessage);
                            }

                            // Check if we've exceeded the number of retries.
                            // Requeue if we're ok, otherwise discard the message
                            if (--lastSentMessage.attempts >= 0) {
                                logger.error("NODE {}: Timeout while sending message. Requeueing - {} attempts left!",
                                        lastSentMessage.getMessageNode(), lastSentMessage.attempts);
                                if (lastSentMessage.getMessageClass() == SerialMessageClass.SendData) {
                                    handleFailedSendDataRequest(lastSentMessage);
                                } else {
                                    enqueue(lastSentMessage);
                                }
                            } else {
                                logger.warn("NODE {}: Too many retries. Discarding message: {}",
                                        lastSentMessage.getMessageNode(), lastSentMessage.toString());
                            }
                            continue;
                        }
                        long responseTime = System.currentTimeMillis() - lastMessageStartTime;
                        if (responseTime > longestResponseTime) {
                            longestResponseTime = responseTime;
                        }
                        logger.debug("NODE {}: Response processed after {}ms/{}ms.", lastSentMessage.getMessageNode(),
                                responseTime, longestResponseTime);
                        logger.trace("Acquired. Transaction completed permit count -> {}",
                                transactionCompleted.availablePermits());
                    } catch (InterruptedException e) {
                        logger.error("Send thread aborted!!!!!!!! {}", e);
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Exception during ZWave thread: Send", e);
            }
            logger.debug("Stopped ZWave thread: Send");
        }
    }

    /**
     * WatchDogTimerTask class. Acts as a watch dog and checks the serial
     * threads to see whether they are still running.
     *
     * @author Jan-Willem Spuij
     */
    private class WatchDogTimerTask extends TimerTask {

        private final Logger logger = LoggerFactory.getLogger(WatchDogTimerTask.class);

        /**
         * Creates a new instance of the WatchDogTimerTask class.
         *
         * @param serialPortName
         *            the serial port name to reconnect to in case the serial
         *            threads have died.
         */
        public WatchDogTimerTask() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            logger.debug("Watchdog: Checking Serial threads");
            if (
            // (receiveThread != null && !receiveThread.isAlive()) ||
            (sendThread != null && !sendThread.isAlive()) || (inputThread != null && !inputThread.isAlive())) {
                logger.warn("Threads not alive, respawning. SEND({}) INPUT({}).",
                        (sendThread != null && !sendThread.isAlive()), (inputThread != null && !inputThread.isAlive()));
                // disconnect();
                // try {
                // connect(serialPortName);
                // } catch (SerialInterfaceException e) {
                // logger.error("Unable to restart Serial threads: {}", e.getLocalizedMessage());
                // }
            }
        }
    }

    public void incomingPacket(SerialMessage packet) {
        // Add the packet to the receive queue
        recvQueue.add(packet);
    }

    /**
     * This is required by {@link ZWaveSecurityCommandClass} for the secure pairing process.
     * {@link ZWaveSecurityCommandClass} can't use the event handling because the
     * object won't exist when this occurs, so we hold it here so {@link ZWaveSecurityCommandClass}
     * can access it
     *
     * @return
     */
    public ZWaveInclusionEvent getLastIncludeSlaveFoundEvent() {
        return lastIncludeSlaveFoundEvent;
    }
}
