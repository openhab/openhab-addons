/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * MQTT topic subscribed to receive availability (online/offline) updates. Must not be used together with
 * availability_topic
 *
 * @author Anton Kharuzhy - Initial contribution
 */
public class Availability {
    @SerializedName("payload_available")
    protected String payloadAvailable = "online";
    @SerializedName("payload_not_available")
    protected String payloadNotAvailable = "offline";
    @SerializedName("value_template")
    protected @Nullable String valueTemplate;
    protected String topic;

    public String getPayloadAvailable() {
        return payloadAvailable;
    }

    public String getPayloadNotAvailable() {
        return payloadNotAvailable;
    }

    public String getTopic() {
        return topic;
    }

    public @Nullable String getValueTemplate() {
        return valueTemplate;
    }
}
