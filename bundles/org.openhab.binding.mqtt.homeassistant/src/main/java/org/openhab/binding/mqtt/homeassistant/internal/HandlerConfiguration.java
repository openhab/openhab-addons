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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.homeassistant.internal.handler.HomeAssistantThingHandler;

/**
 * The {@link HomeAssistantThingHandler} manages Things that are responsible for
 * HomeAssistant MQTT components.
 * This class contains the necessary configuration for such a Thing handler.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HandlerConfiguration {
    public static final String PROPERTY_BASETOPIC = "basetopic";
    public static final String PROPERTY_TOPICS = "topics";
    public static final String DEFAULT_BASETOPIC = "homeassistant";
    /**
     * hint: cannot be final, or <code>getConfigAs</code> will not work.
     * The MQTT prefix topic
     */
    public String basetopic;

    /**
     * hint: cannot be final, or <code>getConfigAs</code> will not work.
     * List of configuration topics.
     * <ul>
     * <li>
     * Each topic is gets the base topic prepended.
     * </li>
     * <li>
     * each topic has:
     * <ol>
     * <li>
     * <code>component</code> (e.g. "switch", "light", ...)
     * </li>
     * <li>
     * <code>node_id</code> (optional)
     * </li>
     * <li>
     * <code>object_id</code> This is only to allow for separate topics for each device
     * </li>
     * <li>
     * "config"
     * </li>
     * </ol>
     * </li>
     * </ul>
     *
     */
    public List<String> topics;

    public HandlerConfiguration() {
        this(DEFAULT_BASETOPIC, Collections.emptyList());
    }

    public HandlerConfiguration(String basetopic, List<String> topics) {
        super();
        this.basetopic = basetopic;
        this.topics = topics;
    }

    /**
     * Add the <code>basetopic</code> and <code>objectid</code> to the properties.
     *
     * @param properties properties
     * @return the modified properties
     */
    public <T extends Map<String, Object>> T appendToProperties(T properties) {
        properties.put(PROPERTY_BASETOPIC, basetopic);
        properties.put(PROPERTY_TOPICS, topics);
        return properties;
    }
}
