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
package org.openhab.binding.dreame.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Credentials and routing information for one mower's Dreame MQTT connection.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public record DreameMqttConfiguration(String host, int port, String clientId, String username, String password,
        String topic) {
    public boolean sameConnection(DreameMqttConfiguration other) {
        return host.equals(other.host) && port == other.port && username.equals(other.username)
                && password.equals(other.password) && topic.equals(other.topic);
    }
}
