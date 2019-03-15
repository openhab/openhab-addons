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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.internal.handler.HomeAssistantThingHandler;

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
     * The MQTT prefix topic
     */
    private String basetopic = "homeassistant";
    /**
     * The object id. This is comparable to a Homie Device.
     */
    private String objectid = "";

    public HandlerConfiguration() {
    }

    public HandlerConfiguration(String basetopic, String objectid) {
        super();
        this.basetopic = basetopic;
        this.objectid = objectid;
    }

    public String getBasetopic() {
        return basetopic;
    }

    public String getObjectid() {
        return objectid;
    }

    public void toProperties(Map<String, Object> properties) {
        properties.put("basetopic", basetopic);
        properties.put("objectid", objectid);
    }
}
