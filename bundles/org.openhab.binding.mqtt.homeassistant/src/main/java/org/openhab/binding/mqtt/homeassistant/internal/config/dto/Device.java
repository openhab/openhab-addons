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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Device configuration
 *
 * @author Jochen Klein - Initial contribution
 */
public class Device extends AbstractConfiguration {
    public Device(Map<String, Object> config) {
        super(config);
    }

    protected @Nullable String swVersion;

    public @Nullable String getId() {
        List<String> identifiers = getIdentifiers();
        return identifiers == null ? null : String.join("_", identifiers);
    }

    public @Nullable String getManufacturer() {
        return getOptionalString("manufacturer");
    }

    public @Nullable String getModel() {
        return getOptionalString("model");
    }

    public @Nullable String getModelId() {
        return getOptionalString("model_id");
    }

    public @Nullable String getName() {
        return getOptionalString("name");
    }

    public @Nullable String getSwVersion() {
        return getOptionalString("sw_version");
    }

    public @Nullable List<String> getIdentifiers() {
        return getOptionalStringList("identifiers");
    }
}
