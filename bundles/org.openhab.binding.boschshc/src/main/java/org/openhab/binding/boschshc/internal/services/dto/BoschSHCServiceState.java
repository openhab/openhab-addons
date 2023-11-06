/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.dto;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.serialization.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Base Bosch Smart Home Controller service state.
 *
 * @author Christian Oeing - Initial contribution
 */
public class BoschSHCServiceState {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * State type. Initialized when instance is created.
     */
    private @Nullable String stateType = null;

    @SerializedName("@type")
    public final String type;

    protected BoschSHCServiceState(String type) {
        this.type = type;

        if (stateType == null) {
            stateType = type;
        }
    }

    public String getType() {
        return type;
    }

    protected boolean isValid() {
        String expectedType = stateType;
        if (expectedType == null || !expectedType.equals(this.type)) {
            var className = this.getClass().getName();
            logger.debug("Expected state type {} for state class {}, received {}", expectedType, className, this.type);
            return false;
        }

        return true;
    }

    public static <TState extends BoschSHCServiceState> @Nullable TState fromJson(String json,
            Class<TState> stateClass) {
        var state = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(json, stateClass);
        if (state == null || !state.isValid()) {
            return null;
        }

        return state;
    }

    public static <TState extends BoschSHCServiceState> @Nullable TState fromJson(JsonElement json,
            Class<TState> stateClass) {
        var state = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(json, stateClass);
        if (state == null || !state.isValid()) {
            return null;
        }

        return state;
    }
}
