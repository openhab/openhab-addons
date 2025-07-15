/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;

/**
 * MQTT topic subscribed to receive availability (online/offline) updates. Must not be used together with
 * availability_topic
 *
 * @author Anton Kharuzhy - Initial contribution
 * @author Cody Cutrer - Rewrite for Python-based configuration
 */
public class Availability extends AbstractConfiguration {
    public Availability(Map<String, @Nullable Object> config) {
        super(config);
    }

    public String getPayloadAvailable() {
        return getString("payload_available");
    }

    public String getPayloadNotAvailable() {
        return getString("payload_not_available");
    }

    public String getTopic() {
        return getString("topic");
    }

    public @Nullable Value getValueTemplate() {
        return getOptionalValue("value_template");
    }
}
