/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.mysensors.internal.event.MySensorsEventType;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ID handling for the MySensors network: Requests for IDs get answered and IDs get stored in a local cache.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsDeviceManager implements MySensorsUpdateListener {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private MySensorsBridgeConnection myCon = null;

    private Map<Integer, MySensorsNode> nodeMap = null;

    public MySensorsDeviceManager(MySensorsBridgeConnection myCon) {
        this.myCon = myCon;
        this.nodeMap = new HashMap<Integer, MySensorsNode>();
    }

    public MySensorsDeviceManager(MySensorsBridgeConnection myCon, Map<Integer, MySensorsNode> nodeMap) {
        this.myCon = myCon;
        if (nodeMap == null) {
            throw new NullPointerException("Cannot create MySensorsDeviceManager null node map passed");
        }
        this.nodeMap = nodeMap;
    }

    public MySensorsDeviceManager(MySensorsBridgeConnection myCon, List<MySensorsNode> nodeList) {
        this.myCon = myCon;
        this.nodeMap = new HashMap<Integer, MySensorsNode>();

        if (nodeList == null) {
            throw new NullPointerException("Cannot create MySensorsDeviceManager null node list passed");
        }

        if (nodeList != null) {
            for (MySensorsNode n : nodeList) {
                if (n != null) {
                    nodeMap.put(n.getNodeId(), n);
                }
            }
        }

    }

    private MySensorsNode getNode(int nodeId) {
        return nodeMap.get(nodeId);
    }

    private void addNode(MySensorsNode node) {
        synchronized (nodeMap) {
            nodeMap.put(node.getNodeId(), node);
        }
    }

    private void addChild(int nodeId, MySensorsChild<?> child) {
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
    public Set<Integer> getGivenIds() {
        return nodeMap.keySet();
    }

    private Integer reserveId() throws NoMoreIdsException {
        int newId = 1;

        // clearNullOnMap();

        Set<Integer> takenIds = getGivenIds();

        synchronized (takenIds) {
            while (newId < MYSENSORS_NODE_ID_RESERVED_255) {
                if (!takenIds.contains(newId)) {
                    nodeMap.put(newId, null);
                    break;
                } else {
                    newId++;
                }
            }
        }

        if (newId == MYSENSORS_NODE_ID_RESERVED_255) {
            throw new NoMoreIdsException();
        }

        return newId;
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

    private void handleIncomingMessageEvent(MySensorsMessage msg) {
        // Are we getting a Request ID Message?
        if (msg.isIdRequestMessage()) {
            answerIDRequest();
            return;
        }

        // Register node if not present
        checkNodeFound(msg);
        // checkChildFound(msg); TODO
    }

    private void checkNodeFound(MySensorsMessage msg) {
        MySensorsNode node = null;
        synchronized (nodeMap) {
            if (msg.nodeId != MYSENSORS_NODE_ID_RESERVED_0 && msg.nodeId != MYSENSORS_NODE_ID_RESERVED_255) {
                if (nodeMap.containsKey(msg.nodeId) && (nodeMap.get(msg.nodeId) == null)
                        || (!nodeMap.containsKey(msg.nodeId))) {
                    logger.debug("Node {} found!", msg.getNodeId());

                    node = new MySensorsNode(msg.nodeId);
                    addNode(node);
                }
            }
        }

        if (node != null) {
            MySensorsStatusUpdateEvent evt = new MySensorsStatusUpdateEvent(MySensorsEventType.NEW_NODE_DISCOVERED,
                    node);
            myCon.broadCastEvent(evt);
        }
    }

    /**
     * Checks if the
     *
     * @param msg
     */
    @SuppressWarnings("unused")
    private void checkChildFound(MySensorsMessage msg) {
        synchronized (nodeMap) {
            if (msg.childId != MYSENSORS_NODE_ID_RESERVED_255 && !nodeMap.containsKey(msg.childId)) {
                logger.debug("New child {} for node {} found!", msg.getChildId(), msg.getNodeId());

                MySensorsChild<?> child = new MySensorsChild<Void>(msg.childId, null);
                addChild(msg.nodeId, child);
            }
        }
    }

    /**
     * Removes null element from map, null element represent reserved, but not used, id for nodes.
     * Null elements will disappear if the sensor accept the ID (so start transmitting information with that ID)
     */
    @SuppressWarnings("unused")
    private void clearNullOnMap() {
        synchronized (nodeMap) {
            Iterator<Integer> iterator = getGivenIds().iterator();
            while (iterator.hasNext()) {
                Integer i = iterator.next();
                if (getNode(i) == null) {
                    nodeMap.remove(i);
                }
            }
        }
    }

    /**
     * If an ID -Request from a sensor is received the controller will send an id to the sensor
     */
    private void answerIDRequest() {
        logger.info("ID Request received");

        int newId = 0;
        try {
            newId = reserveId();
            MySensorsMessage newMsg = new MySensorsMessage(MYSENSORS_NODE_ID_RESERVED_255,
                    MYSENSORS_NODE_ID_RESERVED_255, MYSENSORS_MSG_TYPE_INTERNAL, MYSENSORS_ACK_FALSE, false, 4,
                    newId + "");
            myCon.addMySensorsOutboundMessage(newMsg);
            logger.info("New Node in the MySensors network has requested an ID. ID is: {}", newId);
        } catch (NoMoreIdsException e) {
            logger.error("No more IDs available for this node, try cleaning cache");
        }
    }
}
