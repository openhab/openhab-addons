/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.internal.convention.homeassistant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;

/**
 * HomeAssistant MQTT components use a specific MQTT topic layout,
 * starting with a base prefix (usually "homeassistant"),
 * followed by the component id, an optional node id and the object id.
 *
 * This helper class can split up an MQTT topic into such parts.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HaID {
    final public String baseTopic;
    final public String component;
    final public String nodeID;
    final public String objectID;

    /**
     * Creates a {@link HaID} object for a given HomeAssistant MQTT topic.
     *
     * @param mqttTopic A topic like "homeassistant/binary_sensor/garden/config" or
     *            "homeassistant/binary_sensor/0/garden/config"
     */
    public HaID(String mqttTopic) {
        String[] strings = mqttTopic.split("/");
        if (strings.length < 3) {
            throw new IllegalArgumentException("MQTT topic not a HomeAssistant topic!");
        }
        if (strings.length >= 5) {
            component = strings[1];
            nodeID = strings[2];
            objectID = strings[3];
        } else {
            component = strings[1];
            nodeID = "";
            objectID = strings[2];
        }
        baseTopic = strings[0];
    }

    /**
     * Creates a {@link HaID} by providing all components separately.
     *
     * @param baseTopic The base topic. Usually "homeassistant".
     * @param objectID The object ID
     * @param nodeID The node ID (can be the empty string)
     * @param component The component ID
     */
    public HaID(String baseTopic, String objectID, String nodeID, String component) {
        this.baseTopic = baseTopic;
        this.objectID = objectID;
        this.nodeID = nodeID;
        this.component = component;
    }

    /**
     * Creates a {@link HaID} by providing a channel UID.
     *
     * @param baseTopic The base topic. Usually "homeassistant".
     * @param channel The channel UID
     */
    public HaID(String baseTopic, ChannelUID channel) {
        String groupId = channel.getGroupId();
        if (groupId == null) {
            throw new IllegalArgumentException("Channel needs a group ID!");
        }
        String[] groupParts = groupId.split("_");
        if (groupParts.length != 2) {
            throw new IllegalArgumentException("Channel needs a group ID with the pattern component_node!");
        }
        this.objectID = channel.getThingUID().getId();
        this.nodeID = groupParts[1];
        this.component = groupParts[0];
        this.baseTopic = baseTopic;
    }

    /**
     * We map the HomeAssistant MQTT topic tree object to an ESH Thing.
     */
    public String getThingID() {
        return objectID;
    }

    /**
     * The channel group type UID consists of all components of this object (object-id + node-id + component-id).
     */
    public String getChannelGroupTypeID() {
        return objectID + "_" + component + nodeID;
    }

    /**
     * A channel type UID consists of all components of this object (object-id + node-id + component-id) and a
     * channel-id on top.
     */
    public ChannelTypeUID getChannelTypeID(String channelID) {
        return new ChannelTypeUID(MqttBindingConstants.BINDING_ID,
                objectID + "_" + component + nodeID + "_" + channelID);
    }

    /**
     * The channel group ID consists of the node-id and the component-id
     */
    public String getChannelGroupID() {
        return component + "_" + nodeID;
    }

    @Override
    public String toString() {
        return baseTopic + "/" + component + "/" + nodeID + "/" + objectID;
    }
}
