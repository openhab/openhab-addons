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
package org.openhab.binding.viessmann.internal.dto.features;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The {@link FeatureCommandParams} provides parameters of features command
 *
 * @author Ronny Grun - Initial contribution
 */

public enum FeatureCommandParamType {
    BOOLEAN("boolean"),
    NUMBER("number"),
    INTEGER("integer"),
    STRING("string");

    private final String json;

    FeatureCommandParamType(String json) {
        this.json = json;
    }

    @JsonCreator
    public static FeatureCommandParamType fromJson(String value) {
        for (FeatureCommandParamType t : values()) {
            if (t.json.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown param type: " + value);
    }

    @JsonValue
    public String toJson() {
        return json;
    }
}
