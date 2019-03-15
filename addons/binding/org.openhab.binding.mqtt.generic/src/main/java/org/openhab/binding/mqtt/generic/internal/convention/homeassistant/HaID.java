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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;

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
    final private String baseTopic;
    final private String component;
    final private String nodeID;
    final private String objectID;

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

    public HaID() {
        this("", "", "", "");
    }

    /**
     * Creates a {@link HaID} by providing all components separately.
     *
     * @param baseTopic The base topic. Usually "homeassistant".
     * @param objectID The object ID
     * @param nodeID The node ID (can be the empty string)
     * @param component The component ID
     */
    private HaID(String baseTopic, String objectID, String nodeID, String component) {
        this.baseTopic = baseTopic;
        this.objectID = objectID;
        this.nodeID = nodeID;
        this.component = component;
    }

    public static HaID fromConfig(String baseTopic, Configuration config) {
        String objectID = (String) config.get("objectid");
        String nodeID = (String) config.getProperties().getOrDefault("nodeid", "");
        String component = (String) config.get("component");
        return new HaID(baseTopic, objectID, nodeID, component);
    }

    public void toConfig(Configuration config) {
        config.put("objectid", objectID);
        config.put("nodeid", nodeID);
        config.put("component", component);
    }

    public HandlerConfiguration toHandlerConfiguration() {
        String objectID = this.objectID;
        if (StringUtils.isNotBlank(nodeID)) {
            objectID = nodeID + "/" + objectID;
        }

        return new HandlerConfiguration(baseTopic, objectID);
    }

    public static HaID fromConfig(HandlerConfiguration config) {
        String baseTopic = config.getBasetopic();
        String objectID = config.getObjectid();
        String nodeID = "";

        if (StringUtils.contains(objectID, '/')) {
            String[] parts = objectID.split("/");

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Bad configuration. objectid must be <objectId> or <nodeId>/<objectId>!");
            }
            nodeID = parts[0];
            objectID = parts[1];
        }
        return new HaID(baseTopic, objectID, nodeID, "+");
    }

    public String getFallbackGroupId() {
        StringBuilder str = new StringBuilder();

        if (StringUtils.isNotBlank(nodeID)) {
            str.append(nodeID).append('_');
        }
        str.append(objectID).append('_').append(component);
        return str.toString();
    }

    public String getComponent() {
        return component;
    }

    public String getObjectID() {
        return objectID;
    }

    public String getTopic(String suffix) {
        StringBuilder str = new StringBuilder();

        str.append(baseTopic).append('/').append(component).append('/');

        if (StringUtils.isNotBlank(nodeID)) {
            str.append(nodeID).append('/');
        }
        str.append(objectID);
        if (StringUtils.isNotBlank(suffix)) {
            str.append('/').append(suffix);
        }

        return str.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + baseTopic.hashCode();
        result = prime * result + component.hashCode();
        result = prime * result + nodeID.hashCode();
        result = prime * result + objectID.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HaID other = (HaID) obj;
        if (!baseTopic.equals(other.baseTopic)) {
            return false;
        }
        if (!component.equals(other.component)) {
            return false;
        }
        if (!nodeID.equals(other.nodeID)) {
            return false;
        }
        if (!objectID.equals(other.objectID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return baseTopic + "/" + component + "/" + nodeID + "/" + objectID;
    }
}
