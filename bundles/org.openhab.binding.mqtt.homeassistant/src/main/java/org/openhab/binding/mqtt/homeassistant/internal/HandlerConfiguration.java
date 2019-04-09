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
package org.openhab.binding.mqtt.homeassistant.internal;

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
    /**
     * hint: cannot be final, or <code>getConfigAs</code> will not work.
     * The MQTT prefix topic
     */
    public String basetopic;
    /**
     * hint: cannot be final, or <code>getConfigAs</code> will not work.
     * The object id. This is comparable to a Homie Device.
     */
    public String objectid;

    public HandlerConfiguration() {
        this("homeassistant", "");
    }

    public HandlerConfiguration(String basetopic, String objectid) {
        super();
        this.basetopic = basetopic;
        this.objectid = objectid;
    }

    /**
     * Add the <code>basetopic</code> and <code>objectid</code> to the properties.
     *
     * @param properties
     * @return the modified properties
     */
    public <T extends Map<String, Object>> T appendToProperties(T properties) {
        properties.put("basetopic", basetopic);
        properties.put("objectid", objectid);
        return properties;
    }
}
