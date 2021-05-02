/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Base Bosch Smart Home Controller service state.
 * 
 * @author Christian Oeing - Initial contribution
 */
public class BoschSHCServiceState {
    /**
     * gson instance to convert a class to json string and back.
     */
    private static final Gson gson = new Gson();

    /**
     * State type. Initialized when first instance is created.
     */
    private static @Nullable String stateType = null;

    @SerializedName("@type")
    private final String type;

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
        return expectedType != null && expectedType.equals(this.type);
    }

    public static <TState extends BoschSHCServiceState> @Nullable TState fromJson(JsonElement json,
            Class<TState> stateClass) {
        var state = gson.fromJson(json, stateClass);
        if (state == null || !state.isValid()) {
            return null;
        }

        return state;
    }
}
