/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.util.UIDUtils;

/**
 * HomeAssistant MQTT components use a specific MQTT topic layout,
 * starting with a base prefix (usually "homeassistant"),
 * followed by the component id, an optional node id and the object id.
 *
 * This helper class can split up an MQTT topic into such parts.
 * <p>
 * Implementation note: This is an immutable class.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HaID {
    public final String baseTopic;
    public final String component;
    public final String nodeID;
    public final String objectID;

    private final String topic;

    /**
     * Creates a {@link HaID} object for a given HomeAssistant MQTT topic.
     *
     * @param mqttTopic A topic like "homeassistant/binary_sensor/garden/config" or
     *            "homeassistant/binary_sensor/0/garden/config"
     */
    public HaID(String mqttTopic) {
        String[] strings = mqttTopic.split("/");
        if (strings.length < 4 || strings.length > 5) {
            throw new IllegalArgumentException("MQTT topic not a HomeAssistant topic (wrong length)!");
        }
        if (!"config".equals(strings[strings.length - 1])) {
            throw new IllegalArgumentException("MQTT topic not a HomeAssistant topic ('config' missing)!");
        }

        baseTopic = strings[0];
        component = strings[1];

        if (strings.length == 5) {
            nodeID = strings[2];
            objectID = strings[3];
        } else {
            nodeID = "";
            objectID = strings[2];
        }

        this.topic = createTopic(this);
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
        this.topic = createTopic(this);
    }

    private static String createTopic(HaID id) {
        StringBuilder str = new StringBuilder();
        str.append(id.baseTopic).append('/').append(id.component).append('/');
        if (!id.nodeID.isBlank()) {
            str.append(id.nodeID).append('/');
        }
        str.append(id.objectID).append('/');
        return str.toString();
    }

    /**
     * Extract the HaID information from a channel configuration.
     * <p>
     * <code>objectid</code>, <code>nodeid</code>, and <code>component</code> values are fetched from the configuration.
     *
     * @param baseTopic base topic
     * @param config config
     * @return newly created HaID
     */
    public static HaID fromConfig(String baseTopic, Configuration config) {
        String component = (String) config.get("component");
        String nodeID = (String) config.getProperties().getOrDefault("nodeid", "");
        String objectID = (String) config.get("objectid");
        return new HaID(baseTopic, objectID, nodeID, component);
    }

    /**
     * Add the HaID information to a channel configuration.
     * <p>
     * <code>objectid</code>, <code>nodeid</code>, and <code>component</code> values are added to the configuration.
     *
     * @param config config
     * @return the modified configuration
     */
    public Configuration toConfig(Configuration config) {
        config.put("objectid", objectID);
        config.put("nodeid", nodeID);
        config.put("component", component);
        return config;
    }

    /**
     * Extract the HaID information from a thing configuration.
     * <p>
     * <code>basetpoic</code> and <code>objectid</code> are taken from the configuration.
     * The <code>objectid</code> string may be in the form <code>nodeid/objectid</code>.
     * <p>
     * The <code>component</code> component in the resulting HaID will be set to <code>+</code>.
     * This enables the HaID to be used as an mqtt subscription topic.
     *
     * @param config config
     * @return newly created HaID
     */
    public static Collection<HaID> fromConfig(HandlerConfiguration config) {
        Collection<HaID> result = new ArrayList<>();

        for (String topic : config.topics) {
            String[] parts = topic.split("/");

            switch (parts.length) {
                case 2:
                    result.add(new HaID(config.basetopic, parts[1], "", parts[0]));
                    break;
                case 3:
                    result.add(new HaID(config.basetopic, parts[2], parts[1], parts[0]));
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Bad configuration. topic must be <component>/<objectId> or <component>/<nodeId>/<objectId>!");
            }
        }
        return result;
    }

    /**
     * Return the topic to put into the HandlerConfiguration for this component.
     * <p>
     * <code>objectid</code> in the thing configuration will be
     * <code>nodeID/objectID</code> from the HaID, if <code>nodeID</code> is not empty.
     * <p>
     *
     * @return the short topic.
     */
    public String toShortTopic() {
        String objectID = this.objectID;
        if (!nodeID.isBlank()) {
            objectID = nodeID + "/" + objectID;
        }

        return component + "/" + objectID;
    }

    /**
     * The default group id is the unique_id of the component, given in the config-json.
     * If the unique id is not set, then a fallback is constructed from the HaID information.
     *
     * @return group id
     */
    public String getGroupId(@Nullable final String uniqueId) {
        String result = uniqueId;

        // the null test is only here so the compile knows, result is not null afterwards
        if (result == null || result.isBlank()) {
            StringBuilder str = new StringBuilder();

            if (!nodeID.isBlank()) {
                str.append(nodeID).append('_');
            }
            str.append(objectID).append('_').append(component);
            result = str.toString();
        }

        return UIDUtils.encode(result);
    }

    /**
     * Return a topic, which can be used for a mqtt subscription.
     * Defined values for suffix are:
     * <ul>
     * <li>config</li>
     * <li>state</li>
     * </ul>
     *
     * @return fallback group id
     */
    public String getTopic(String suffix) {
        return topic + suffix;
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
        return objectID.equals(other.objectID);
    }

    @Override
    public String toString() {
        return topic;
    }
}
