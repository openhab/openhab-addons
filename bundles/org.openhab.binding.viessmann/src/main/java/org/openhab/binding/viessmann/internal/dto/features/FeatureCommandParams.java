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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link FeatureCommandParams} provides parameters of features command
 *
 * @author Ronny Grun - Initial contribution
 */
public final class FeatureCommandParams {
    public final String type;
    public final boolean required;
    public final Map<String, Object> constraints;

    @JsonCreator
    public FeatureCommandParams(@JsonProperty("type") String type, @JsonProperty("required") boolean required,
            @JsonProperty("constraints") Map<String, Object> constraints) {
        this.type = type;
        this.required = required;
        this.constraints = constraints;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Map<String, Object> getConstraints() {
        return constraints;
    }
}
