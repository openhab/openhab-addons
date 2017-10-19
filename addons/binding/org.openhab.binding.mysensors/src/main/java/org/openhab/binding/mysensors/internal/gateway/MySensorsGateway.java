/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.event.MySensorsNodeUpdateEventType;
import org.openhab.binding.mysensors.internal.exception.MergeException;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.openhab.binding.mysensors.internal.exception.NotInitializedException;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.ip.MySensorsIpConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageAck;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageDirection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageType;
import org.openhab.binding.mysensors.internal.protocol.mqtt.MySensorsMqttConnection;
import org.openhab.binding.mysensors.internal.protocol.serial.MySensorsSerialConnection;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main access point of all the function of MySensors Network, some of there are
 * -ID handling for the MySensors network: Requests for IDs get answered and IDs get stored in a local cache.
 * -Updating sensors variable and status information
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsGateway implements MySensorsGatewayEventListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, MySensorsNode> nodeMap;

    private MySensorsEventRegister myEventRegister;

    private MySensorsAbstractConnection myCon;

    private MySensorsGatewayConfig myConf;

    private MySensorsNetworkSanityChecker myNetSanCheck;

    public MySensorsGateway() {
        nodeMap = new HashMap<>();
        this.myEventRegister = new MySensorsEventRegister();
    }

    public MySensorsGateway(Map<Integer, MySensorsNode> nodeMap) {
        this.nodeMap = nodeMap;
        this.myEventRegister = new MySensorsEventRegister();
    }
    
    public void setMyConf(MySensorsGatewayConfig myConf) {
        this.myConf = myConf;
    }

    /**
     * Build up the gateway following given configuration parameters. Gateway will not start after this method returns.
     * Use startup to do that
     *
     * @param myConf a valid instance of {@link MySensorsGatewayConfig}
     *
     * @return true if setup done correctly
     */
    public boolean setup(MySensorsGatewayConfig myConf) {
        if (myConf != null) {
            if (myCon != null) {
                throw new IllegalStateException("Connection is alredy instantiated");
            }

            setMyConf(myConf);

            switch (myConf.getGatewayType()) {
                case SERIAL:
                    myCon = new MySensorsSerialConnection(myConf, myEventRegister);
                    return true;
                case IP:
                    myCon = new MySensorsIpConnection(myConf, myEventRegister);
                    return true;
                case MQTT:
                    myCon = new MySensorsMqttConnection(myConf, myEventRegister);
                    return true;
            }
        } else {
            logger.error("Invalid configuration supplied: {}", myConf);
        }

        return false;
    }

    /**
     * Startup the gateway
     */
    public void startup() {
        myCon.initialize();

        myEventRegister.addEventListener(this);
        
        if (myConf.getEnableNetworkSanCheck()) {
            myNetSanCheck = new MySensorsNetworkSanityChecker(this, myEventRegister, myCon);
        }
    }

    /**
     * Shutdown the gateway
     */
    public void shutdown() {
        if (myNetSanCheck != null) {
            myNetSanCheck.stop();
        }

        if (myCon != null) {
            myCon.destroy();
            myCon = null;
        }

        myEventRegister.clearAllListeners();
    }

    /**
     * Get node from the gatway
     *
     * @param nodeId the node to retrieve
     *
     * @return node if exist or null instead
     */
    public MySensorsNode getNode(int nodeId) {
        synchronized (nodeMap) {
            return nodeMap.get(nodeId);
        }
    }
    
    public void removeNode(int nodeId) {
        synchronized (nodeMap) {
            nodeMap.remove(nodeId);
        }
    }

    /**
     * Get a child from a node
     *
     * @param nodeId the node of the searched child
     * @param childId the child of a node
     *
     * @return child if exist or null instead
     */
    public MySensorsChild getChild(int nodeId, int childId) {
        MySensorsChild child = null;
        MySensorsNode node = getNode(nodeId);
        if (node != null) {
            child = node.getChild(childId);
        }

        return child;
    }

    /**
     * Get a variable from a child in a node
     *
     * @param nodeId the node of the variable
     *
     * @param childId the child of the variable
     *
     * @param type the variable type (see sub-type of SET/REQ message in API documentation)
     *
     * @return variable if exist or null instead
     */
    public MySensorsVariable getVariable(int nodeId, int childId, MySensorsMessageSubType type) {
        MySensorsVariable variable = null;
        MySensorsChild child = getChild(nodeId, childId);
        if (child != null) {
            variable = child.getVariable(type);
        }

        return variable;
    }

    /**
     * Update variable state. This method <b>not</b> send new updated value to network, use sendMessage for it.
     *
     * @param nodeId node id of sensor
     * @param childId child id of sensor
     * @param subType type of variable to update
     * @param state new state
     *
     * @return a message that should be sent to update variable to desired state
     */
    public MySensorsMessage setVariableState(int nodeId, int childId, MySensorsMessageSubType subType, String state) {
        MySensorsNode node = getNode(nodeId);
        MySensorsMessage msg = null;

        if (node != null) {
            try {
                msg = node.updateVariableState(childId, subType, state);
            } catch (NotInitializedException e) {
                logger.error("State not initialized: {}", e);
            }
        }

        return msg;
    }

    /**
     * Simple method that add node to gateway (only if node is not present previously).
     * This function never fail.
     *
     * @param node the node to add
     */
    public void addNode(MySensorsNode node) {
        synchronized (nodeMap) {
            if (nodeMap.containsKey(node.getNodeId())) {
                logger.warn("Overwriting previous node, it was lost.");
            }
            nodeMap.put(node.getNodeId(), node);
        }
    }

    /**
     * Add node to gateway
     *
     * @param node the node to add
     * @param mergeIfExist if true and node is already present that two nodes will be merged in one
     *
     *
     * @throws MergeException if mergeIfExist is true and nodes has common child/children
     */
    public void addNode(MySensorsNode node, boolean mergeIfExist) throws MergeException {
      synchronized (nodeMap) {
            MySensorsNode exist = null;
            if (mergeIfExist && ((exist = getNode(node.getNodeId())) != null)) {
                logger.debug("Merging child map: {} with: {}", exist.getChildMap(), node.getChildMap());

                exist.merge(node);
      
                logger.trace("Merging result is: {}", exist.getChildMap());
            } else {
                logger.debug("Adding device {}", node.toString());
                addNode(node);
            }
        }
    }

    /**
     * Add child to node
     *
     * @param nodeId the id of the node to add the child
     * @param child the child to add
     *
     * @return true if node is present and child was added successfully
     */
    public void addChild(int nodeId, MySensorsChild child) {
        synchronized (nodeMap) {
            MySensorsNode node = nodeMap.get(nodeId);
            if (node != null) {
                node.addChild(child);
            } else {
                logger.warn("Node {} not found in map", nodeId);
            }
        }
    }

    /**
     * @return a Set of Ids that is already used and known to the binding.
     */
    public List<Integer> getGivenIds() {
        synchronized (nodeMap) {
            return new ArrayList<Integer>(nodeMap.keySet());
        }
    }

    /**
     * Reserve an id for network, mainly for request id messages
     *
     * @return a free id not present in node map
     * @throws NoMoreIdsException if no more ids are available to be reserved
     */
    public Integer reserveId() throws NoMoreIdsException {
        int newId = 1;

        synchronized (nodeMap) {
            List<Integer> takenIds = getGivenIds();
            while (newId < MySensorsNode.MYSENSORS_NODE_ID_RESERVED_255) {
                if (!takenIds.contains(newId)) {
                    addNode(new MySensorsNode(newId));
                    break;
                } else {
                    newId++;
                }
            }
        }

        if (newId == MySensorsNode.MYSENSORS_NODE_ID_RESERVED_255) {
            throw new NoMoreIdsException();
        }

        myEventRegister.notifyNodeIdReserved(newId);

        return newId;
    }

    /**
     * Add a {@link MySensorsGatewayEventListener} event listener to this gateway
     *
     * @param listener
     */
    public void addEventListener(MySensorsGatewayEventListener listener) {
        myEventRegister.addEventListener(listener);
    }

    /**
     *
     * Remove a {@link MySensorsGatewayEventListener} event listener from this gateway
     *
     * @param listener
     */
    public void removeEventListener(MySensorsGatewayEventListener listener) {
        myEventRegister.removeEventListener(listener);
    }

    /**
     * Check if a {@link MySensorsGatewayEventListener} is already registered
     *
     * @param listener
     *
     * @return true if listener is already registered
     */
    public boolean isEventListenerRegisterd(MySensorsGatewayEventListener listener) {
        return myEventRegister.isEventListenerRegisterd(listener);
    }

    /**
     * Send a message through this gateway. If message is of type SET will update variable state of a node/child,
     * this will trigger the update event on the {@link MySensorsEventRegister}
     *
     * @param message to send
     */
    public void sendMessage(MySensorsMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Null message could not be sent over the network");
        }

        try {
            handleOutgoingMessage(message);
        } catch (Exception e) {
            logger.error("Handling outgoing message throw an exception", e);
        }

        myCon.sendMessage(message);
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Exception {
        if (!handleIncomingMessage(message)) {
            handleSpecialMessageEvent(message);
        }
    }

    @Override
    public void ackNotReceived(MySensorsMessage msg) throws Exception {
        if (MySensorsNode.isValidNodeId(msg.getNodeId()) && MySensorsChild.isValidChildId(msg.getChildId())
                && msg.isSetReqMessage()) {
            MySensorsNode node = getNode(msg.getNodeId());
            if (node != null) {
                logger.debug("Node {} found in gateway", msg.getNodeId());

                MySensorsChild child = node.getChild(msg.getChildId());
                if (child != null) {
                    logger.debug("Child {} found in node {}", msg.getChildId(), msg.getNodeId());

                    MySensorsVariable variable = child.getVariable(msg.getSubType());
                    if (variable != null) {
                        if (variable.isRevertible()) {
                            logger.debug("Variable {} found, it will be reverted to last know state",
                                    variable.getClass().getSimpleName());
                            variable.revertValue();
                            myEventRegister.notifyNodeUpdateEvent(node, child, variable,
                                    MySensorsNodeUpdateEventType.REVERT);
                        } else {
                            logger.error("Could not revert variable {}, no previous value is present",
                                    variable.getClass().getSimpleName());
                        }
                    } else {
                        logger.warn("Variable {} not present", msg.getSubType());
                    }
                } else {
                    logger.debug("Child {} not present into node {}", msg.getChildId(), msg.getNodeId());
                }
            }
        }
    }

    @Override
    public void connectionStatusUpdate(MySensorsAbstractConnection connection, boolean connected) throws Exception {
        if (myNetSanCheck != null) {
            if (connected) {
                myNetSanCheck.start();
            } else {
                myNetSanCheck.stop();
            }
        }

        handleBridgeStatusUpdate(connected);
    }

    public MySensorsGatewayConfig getConfiguration() {
        return myConf;
    }

    private void handleBridgeStatusUpdate(boolean connected) {
        synchronized (nodeMap) {
            for (Integer i : nodeMap.keySet()) {
                MySensorsNode node = nodeMap.get(i);
                node.setReachable(connected);
                myEventRegister.notifyNodeReachEvent(node, connected);
            }
        }
    }

    /**
     * Handle the incoming/outgoing message from serial
     *
     * @param msg the incoming/outgoing message
     * @return true if ,and only if:
     *         -the message is propagated to one of the defined node or
     *         -message arrives from a device new device in the network or
     *         -message is REQ type and variable is defined for it
     *
     * @throws Exception
     */
    private boolean handleIncomingMessage(MySensorsMessage msg) throws Exception {
        if (MySensorsNode.isValidNodeId(msg.getNodeId()) && MySensorsChild.isValidChildId(msg.getChildId())) {
            if (msg.getDirection() == MySensorsMessageDirection.INCOMING) {
                updateReachable(msg);
                updateLastUpdateFromMessage(msg);
                
                if(msg.getMsgType() == MySensorsMessageType.INTERNAL)
                    return handleInternalMessage(msg);
                else if(msg.getMsgType() == MySensorsMessageType.SET ||
                        msg.getMsgType() == MySensorsMessageType.REQ)
                    return handleSetReqMessage(msg, true);
                else if(msg.getMsgType() == MySensorsMessageType.PRESENTATION)
                    return handlePresentationMessage(msg);
                else
                    return isNewDevice(msg);
            } else {
                logger.warn("Cannot handle this message, direction MYSENSORS_MSG_DIRECTION_OUTGOING");
            }
        }
        return false;
    }

    private boolean handleOutgoingMessage(MySensorsMessage msg) throws Exception {
        if (MySensorsNode.isValidNodeId(msg.getNodeId()) && MySensorsChild.isValidChildId(msg.getChildId())) {
            if (msg.getDirection() == MySensorsMessageDirection.OUTGOING) {
                if (msg.getMsgType() == MySensorsMessageType.SET) {
                    return handleSetReqMessage(msg, false);
                }
            } else {
                logger.warn("Cannot handle this message, direction MYSENSORS_MSG_DIRECTION_INCOMING");
            }
        }
        return false;
    }

    private void updateReachable(MySensorsMessage msg) {
        MySensorsNode node = getNode(msg.getNodeId());
        if (node != null && !node.isReachable()) {
            logger.info("Node {} available again!", node.getNodeId());
            node.setReachable(true);
            myEventRegister.notifyNodeReachEvent(node, true);
        }
    }

    private boolean isNewDevice(MySensorsMessage msg) {
        MySensorsNode node = getNode(msg.getNodeId());

        if (node == null) {
            logger.debug("Node {} not present, send new node discovered event", msg.getNodeId());

            node = new MySensorsNode(msg.getNodeId());
            
            addNode(node);
            myEventRegister.notifyNewNodeDiscovered(node, null);
            return true;
        }
        return false;
    }

    private boolean handlePresentationMessage(MySensorsMessage msg) {
        boolean insertNode = false;

        MySensorsNode node = getNode(msg.getNodeId());

        MySensorsChild child = getChild(msg.getNodeId(), msg.getChildId());

        logger.debug("Presentation Message received");

        if (child == null) {
            if (node == null) {
                node = new MySensorsNode(msg.getNodeId());
                insertNode = true;
            }

            child = MySensorsChild.fromPresentation(msg.getSubType(), msg.getChildId());
            node.addChild(child);

            if (insertNode) {
                addNode(node);
            }

            myEventRegister.notifyNewNodeDiscovered(node, child);
            return true;
        } else {
            logger.warn("Presented child is alredy present in gateway");
        }

        return false;
    }

    private boolean handleSetReqMessage(MySensorsMessage msg, boolean dispatchUpdate) {
        MySensorsNode node = getNode(msg.getNodeId());
        if (node != null) {
            logger.debug("Node {} found in gateway", msg.getNodeId());

            MySensorsChild child = node.getChild(msg.getChildId());
            if (child != null) {
                logger.debug("Child {} found in node {}", msg.getChildId(), msg.getNodeId());

                MySensorsVariable variable = child.getVariable(msg.getSubType());
                
                if (variable != null) {
                    if (msg.isSetMessage()) {
                        if (node.isReachable()) {
                            logger.trace("Variable {}({}) found in child, pre-update value: {}",
                                    variable.getClass().getSimpleName(), variable.getType(), variable.getValue());
                            variable.setValue(msg);
                            logger.trace("Variable {}({}) found in child, post-update value: {}",
                                    variable.getClass().getSimpleName(), variable.getType(), variable.getValue());

                            if (dispatchUpdate) {
                                myEventRegister.notifyNodeUpdateEvent(node, child, variable,
                                        MySensorsNodeUpdateEventType.UPDATE);
                            }
                        } else {
                            logger.warn("Could not set value to node {} if not reachable", node.getNodeId());
                        }
                    } else {
                        String value = variable.getValue();
                        logger.debug("Request received!");
                        msg.setMsgType(MySensorsMessageType.SET);
                        if(value != null) {
                        msg.setMsg(value);
                        } else {
                            msg.setMsg("0");
                        }
                        myCon.sendMessage(msg);
                    }
                    return true;
                } else {
                    logger.warn("Variable {} not present", msg.getSubType());
                }
            } else {
                logger.debug("Child {} not present in node {}", msg.getChildId(), msg.getNodeId());
            }
        }
        return false;
    }

    private boolean handleInternalMessage(MySensorsMessage msg) {
        MySensorsNode node = getNode(msg.getNodeId());
        if (node != null) {
            if (msg.getSubType() == MySensorsMessageSubType.I_BATTERY_LEVEL) {
                node.setBatteryPercent(Integer.parseInt(msg.getMsg()));
                logger.debug("Battery percent for node {} update to: {}%", node.getNodeId(),
                        node.getBatteryPercent());
                myEventRegister.notifyNodeUpdateEvent(node, null, null, MySensorsNodeUpdateEventType.BATTERY);
                return true;
            }
        }
        return false;
    }

    private void updateLastUpdateFromMessage(MySensorsMessage msg) {
        Date now = new Date();

        // Last update updated only for incoming message
        if (msg != null) {
            MySensorsNode node = getNode(msg.getNodeId());

            if (node != null) {
                node.setLastUpdate(now);
                MySensorsChild child = getChild(msg.getNodeId(), msg.getChildId());

                if (child != null) {
                    child.setLastUpdate(now);

                    MySensorsVariable var = getVariable(msg.getNodeId(), msg.getChildId(), msg.getSubType());

                    if (var != null) {
                        var.setLastUpdate(now);
                    }
                }
            }
        }
    }

    private void handleSpecialMessageEvent(MySensorsMessage msg) {
        // Is this an I_CONFIG message?
        if (msg.isIConfigMessage()) {
            answerIConfigMessage(msg);
        }

        // Is this an I_TIME message?
        if (msg.isITimeMessage()) {
            answerITimeMessage(msg);
        }

        // Requesting ID
        if (msg.isIdRequestMessage()) {
            answerIDRequest();
        }
    }

    /**
     * Answer to I_TIME message for gateway time request from sensor
     *
     * @param msg, the incoming I_TIME message from sensor
     */
    private void answerITimeMessage(MySensorsMessage msg) {
        logger.info("I_TIME request received from {}, answering...", msg.getNodeId());

        String time = Long.toString(System.currentTimeMillis() / 1000);
        MySensorsMessage newMsg = new MySensorsMessage(msg.getNodeId(), msg.getChildId(),
                MySensorsMessageType.INTERNAL, MySensorsMessageAck.FALSE, false, MySensorsMessageSubType.I_TIME,
                time);
        myCon.sendMessage(newMsg);
    }

    /**
     * Answer to I_CONFIG message for imperial/metric request from sensor
     *
     * @param msg, the incoming I_CONFIG message from sensor
     */
    private void answerIConfigMessage(MySensorsMessage msg) {
        boolean imperial = myConf.getImperial();
        String iConfig = imperial ? "I" : "M";

        logger.debug("I_CONFIG request received from {}, answering: (is imperial?){}", iConfig, imperial);

        MySensorsMessage newMsg = new MySensorsMessage(msg.getNodeId(), msg.getChildId(),
                MySensorsMessageType.INTERNAL, MySensorsMessageAck.FALSE, false, MySensorsMessageSubType.I_CONFIG,
                iConfig);
        myCon.sendMessage(newMsg);
    }

    /**
     * If an ID-Request from a sensor is received the controller will send an id to the sensor
     */
    private void answerIDRequest() {
        logger.info("ID Request received");

        try {
            int newId = reserveId();
            logger.info("New Node in the MySensors network has requested an ID. ID is: {}", newId);
            MySensorsMessage newMsg = new MySensorsMessage(MySensorsNode.MYSENSORS_NODE_ID_RESERVED_255,
                    MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255, MySensorsMessageType.INTERNAL,
                    MySensorsMessageAck.FALSE, false, MySensorsMessageSubType.I_ID_RESPONSE, newId + "");
            myCon.sendMessage(newMsg);
        } catch (NoMoreIdsException e) {
            logger.error("No more IDs available for this node, you could try cleaning cache file");
        }
    }
}
