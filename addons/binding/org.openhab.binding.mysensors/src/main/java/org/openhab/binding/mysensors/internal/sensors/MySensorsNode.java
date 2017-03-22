/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openhab.binding.mysensors.internal.exception.MergeException;
import org.openhab.binding.mysensors.internal.exception.NotInitializedException;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageType;

/**
 * Characteristics of a thing/node are stored here:
 * - List of children
 * - Last update (DateTime) from the node
 * - is the child reachable?
 * - battery percent (if available)
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsNode {

    // Reserved ids
    public static final int MYSENSORS_NODE_ID_RESERVED_GATEWAY_0 = 0;
    public static final int MYSENSORS_NODE_ID_RESERVED_255 = 255;

    private final int nodeId;

    private Optional<MySensorsNodeConfig> nodeConfig;

    private boolean reachable = true;

    private Map<Integer, MySensorsChild> childMap;

    private Date lastUpdate = null;

    private int batteryPercent = 0;

    public MySensorsNode(int nodeId) {
        if (!isValidNodeId(nodeId)) {
            throw new IllegalArgumentException("Invalid node id supplied: " + nodeId);
        }
        this.nodeId = nodeId;
        this.childMap = new HashMap<Integer, MySensorsChild>();
        this.nodeConfig = Optional.empty();
        this.lastUpdate = new Date(0);
    }

    public MySensorsNode(int nodeId, MySensorsNodeConfig config) {
        if (!isValidNodeId(nodeId)) {
            throw new IllegalArgumentException("Invalid node id supplied: " + nodeId);
        }

        if (config == null) {
            throw new IllegalArgumentException("Invalid config supplied for node: " + nodeId);
        }

        this.nodeId = nodeId;
        this.childMap = new HashMap<Integer, MySensorsChild>();
        this.nodeConfig = Optional.of(config);
        this.lastUpdate = new Date(0);
    }

    public Map<Integer, MySensorsChild> getChildMap() {
        return childMap;
    }

    /**
     * Get node ID
     *
     * @return the ID of this node
     */
    public int getNodeId() {
        return nodeId;
    }

    /**
     * Add a child not null child to child to this node
     *
     * @param child to add
     */
    public void addChild(MySensorsChild child) {
        if (child == null) {
            throw new IllegalArgumentException("Null child could't be add");
        }

        synchronized (childMap) {
            childMap.put(child.getChildId(), child);
        }
    }

    /**
     * Get a child from a node
     *
     * @param childId the id of the child to get from this node
     * @return MySensorsChild for the given childId
     */
    public MySensorsChild getChild(int childId) {
        return childMap.get(childId);
    }

    /**
     * Set node reachable status.
     *
     * @param reachable (true=yes,false=no)
     */
    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    /**
     * Check if this node is reachable
     *
     * @return true if this node is reachable
     */
    public boolean isReachable() {
        return reachable;
    }

    /**
     * Get battery percent of this node
     *
     * @return the battery percent
     */
    public int getBatteryPercent() {
        return batteryPercent;
    }

    /**
     * Set battery percent
     *
     * @param batteryPercent that will be set
     */
    public void setBatteryPercent(int batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    /**
     * Get last update
     *
     * @return the last update, 1970-01-01 00:00 means no update received
     */
    public Date getLastUpdate() {
        synchronized (this.lastUpdate) {
            return lastUpdate;
        }
    }

    /**
     * Set last update
     *
     * @param lastUpdate
     */
    public void setLastUpdate(Date lastUpdate) {
        synchronized (this.lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }

    /**
     * Get optional node configuration
     *
     * @return the Optional that could contains {@link MySensorsNodeConfig}
     */
    public Optional<MySensorsNodeConfig> getNodeConfig() {
        return nodeConfig;
    }

    /**
     * Set configuration for node
     *
     * @param nodeConfig is a valid instance of {@link MySensorsNodeConfig}ß
     */
    public void setNodeConfig(MySensorsNodeConfig nodeConfig) {
        this.nodeConfig = Optional.of(nodeConfig);
    }

    /**
     * Merge to node into one.
     *
     * @param node
     *
     * @throws MergeException if try to merge to node with same child/children
     */
    public void merge(Object o) throws MergeException {

        if (o == null || !(o instanceof MySensorsNode)) {
            throw new MergeException("Invalid object to merge");
        }

        MySensorsNode node = (MySensorsNode) o;

        // Merge configurations
        if (node.nodeConfig.isPresent() && !nodeConfig.isPresent()) {
            nodeConfig = node.nodeConfig;
        } else if (node.nodeConfig.isPresent() && nodeConfig.isPresent()) {
            nodeConfig.get().merge(node.nodeConfig.get());
        }

        synchronized (childMap) {
            for (Integer i : node.childMap.keySet()) {
                MySensorsChild child = node.childMap.get(i);
                childMap.merge(i, child, (child1, child2) -> {
                    child1.merge(child2);
                    return child1;
                });
            }

        }
    }

    /**
     * Generate message from a state. This method doesn't update variable itself.
     * No check will be performed on value of state parameter
     *
     * @param childId id of the child the message is generated for.
     * @param subType subtype (humidity, temperature ...) the message is of.
     * @param state the new state that is send to the mysensors network.
     *
     * @return a non-null message ready to be sent if childId/type are available on this node
     *
     * @throws NotInitializedException if state is null
     */
    public MySensorsMessage updateVariableState(int childId, MySensorsMessageSubType subType, String state) throws NotInitializedException{
        MySensorsMessage msg = null;

        if (state == null) {
            throw new NotInitializedException("State is null");
        }

        synchronized (childMap) {
            MySensorsChild child = getChild(childId);
            MySensorsChildConfig childConfig = (child.getChildConfig().isPresent()) ? child.getChildConfig().get()
                    : new MySensorsChildConfig();
            
            MySensorsVariable var = child.getVariable(subType);
            if (var != null) {
                msg = new MySensorsMessage();

                // MySensors
                msg.setNodeId(nodeId);
                msg.setChildId(childId);
                msg.setMsgType(MySensorsMessageType.SET);
                msg.setSubType(subType);
                msg.setAck(childConfig.getRequestAck());
                msg.setMsg(state);
                msg.setRevert(childConfig.getRevertState());
                msg.setSmartSleep(childConfig.getSmartSleep());
            }
        
        }

        return msg;
    }

    /**
     * Check if an integer is a valid node ID
     *
     * @param ID to test
     *
     * @return true if ID is valid
     */
    public static boolean isValidNodeId(int id) {
        return (id >= MYSENSORS_NODE_ID_RESERVED_GATEWAY_0 && id < MYSENSORS_NODE_ID_RESERVED_255);
    }

    /**
     * Check if a node has a valid node ID and node is not null
     *
     * @param node to test
     *
     * @return true if node is valid
     */
    public static boolean isValidNode(MySensorsNode n) {
        return (n != null) && (isValidNodeId(n.nodeId));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((childMap == null) ? 0 : childMap.hashCode());
        result = prime * result + nodeId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MySensorsNode other = (MySensorsNode) obj;
        if (batteryPercent != other.batteryPercent) {
            return false;
        }
        if (childMap == null) {
            if (other.childMap != null) {
                return false;
            }
        } else if (!childMap.equals(other.childMap)) {
            return false;
        }
        if (lastUpdate == null) {
            if (other.lastUpdate != null) {
                return false;
            }
        } else if (!lastUpdate.equals(other.lastUpdate)) {
            return false;
        }
        if (nodeId != other.nodeId) {
            return false;
        }
        if (reachable != other.reachable) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsNode [nodeId=" + nodeId + ", childNumber=" + childMap.size() + ", chidldList=" + childMap
                + "]";
    }

}
