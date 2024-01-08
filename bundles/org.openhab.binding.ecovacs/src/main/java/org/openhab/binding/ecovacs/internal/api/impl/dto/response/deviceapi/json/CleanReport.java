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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json;

import org.openhab.binding.ecovacs.internal.api.model.CleanMode;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class CleanReport {
    @SerializedName("trigger")
    public String trigger; // app, workComplete, ...?
    @SerializedName("state")
    public String state;
    @SerializedName("cleanState")
    public CleanStateReport cleanState;

    public static class CleanStateReport {
        @SerializedName("router")
        public String router; // plan, ...?
        @SerializedName("type")
        public String type;
        @SerializedName("motionState")
        public String motionState;
        @SerializedName("content")
        public String areaDefinition;
    }

    public CleanMode determineCleanMode(Gson gson) {
        final String modeValue;
        if (cleanState != null) {
            if ("working".equals(cleanState.motionState)) {
                modeValue = cleanState.type;
            } else {
                modeValue = cleanState.motionState;
            }
        } else {
            modeValue = state;
        }
        return gson.fromJson(modeValue, CleanMode.class);
    }
}
