/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

package org.openhab.binding.homeassistant.internal.config.dto;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.homeassistant.internal.HomeAssistantPythonBridge;

/**
 * Base class for home assistant configurations.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class MqttComponentConfig {
    private final HomeAssistantPythonBridge bridge;
    private final Value value;

    public MqttComponentConfig(HomeAssistantPythonBridge bridge, Value value) {
        this.bridge = bridge;
        this.value = value;
    }

    public String getComponent() {
        return value.getMember("component").asString();
    }

    public String getObjectId() {
        return value.getMember("object_id").asString();
    }

    public @Nullable String getNodeId() {
        Value nodeId = value.getMember("node_id");
        if (nodeId.isNull()) {
            return null;
        }
        return nodeId.asString();
    }

    public Map<String, @Nullable Object> getDiscoveryPayload() {
        return (Map<String, @Nullable Object>) Objects
                .requireNonNull(bridge.toJava(value.getMember("discovery_payload")));
    }

    public boolean isMigrateDiscovery() {
        Value discoveryPayload = value.getMember("discovery_payload");
        if (discoveryPayload.hasMember("migrate_discovery")) {
            Value migrateDiscovery = discoveryPayload.getMember("migrate_discovery");
            if (!migrateDiscovery.isNull()) {
                return migrateDiscovery.asBoolean();
            }
        }
        Object migrateDiscovery = getDiscoveryPayload().get("migrate_discovery");
        return migrateDiscovery instanceof Boolean migrate && migrate;
    }
}
