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
import org.openhab.binding.mqtt.homeassistant.internal.config.ConnectionDeserializer;

import com.google.gson.annotations.JsonAdapter;

/**
 * Connection configuration
 *
 * @author Jochen Klein - Initial contribution
 */
@JsonAdapter(ConnectionDeserializer.class)
public class Connection {
    protected @Nullable String type;
    protected @Nullable String identifier;

    public Connection() {
    }

    public Connection(@Nullable String type, @Nullable String identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    @Nullable
    public String getType() {
        return type;
    }

    @Nullable
    public String getIdentifier() {
        return identifier;
    }
}
