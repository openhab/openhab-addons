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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.homeassistant.internal.config.ListOrStringDeserializer;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Device configuration
 *
 * @author Jochen Klein - Initial contribution
 */
public class Device {
    @JsonAdapter(ListOrStringDeserializer.class)
    protected @Nullable List<String> identifiers;
    protected @Nullable List<Connection> connections;
    protected @Nullable String manufacturer;
    protected @Nullable String model;
    protected @Nullable String name;

    @SerializedName("sw_version")
    protected @Nullable String swVersion;

    public @Nullable String getId() {
        List<String> identifiers = this.identifiers;
        return identifiers == null ? null : String.join("_", identifiers);
    }

    @Nullable
    public List<Connection> getConnections() {
        return connections;
    }

    @Nullable
    public String getManufacturer() {
        return manufacturer;
    }

    @Nullable
    public String getModel() {
        return model;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getSwVersion() {
        return swVersion;
    }

    @Nullable
    public List<String> getIdentifiers() {
        return identifiers;
    }
}
