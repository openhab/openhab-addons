/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.config.dto;

/**
 * MQTT topic subscribed to receive availability (online/offline) updates. Must not be used together with
 * availability_topic
 *
 * @author Anton Kharuzhy - Initial contribution
 */
public class Availability {
    protected String payload_available = "online";
    protected String payload_not_available = "offline";
    protected String topic;

    public String getPayload_available() {
        return payload_available;
    }

    public String getPayload_not_available() {
        return payload_not_available;
    }

    public String getTopic() {
        return topic;
    }
}
