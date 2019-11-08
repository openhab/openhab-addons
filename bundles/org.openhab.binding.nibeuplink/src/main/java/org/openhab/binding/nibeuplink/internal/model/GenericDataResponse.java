/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nibeuplink.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * data class to map the json status response
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GenericDataResponse implements DataResponse {
    private final Logger logger = LoggerFactory.getLogger(GenericDataResponse.class);

    public static class Value {
        @SerializedName("VariableId")
        @Nullable
        private String variableId;
        @SerializedName("CurrentValue")
        @Nullable
        private String currentValue;
        @SerializedName("CurrentIntValue")
        @Nullable
        private Long currentIntValue;
        @SerializedName("IsLoading")
        private boolean isLoading;
    }

    @SerializedName("IsOffline")
    @Nullable
    private String isOffline;
    @SerializedName("OnlineImage")
    @Nullable
    private String onlineImage;
    @SerializedName("Date")
    @Nullable
    private String date;
    @SerializedName("FuzzyDate")
    @Nullable
    private String fuzzyDate;
    @SerializedName("Values")
    private List<Value> values = new ArrayList<>();

    @Override
    public Map<String, @Nullable Long> getValues() {
        Map<String, @Nullable Long> valueMap = new HashMap<>();
        for (Value value : values) {
            String id = value.variableId;
            if (!value.isLoading && id != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Channel {} updated to: {} ({})", value.variableId, value.currentIntValue,
                            value.currentValue);
                }
                valueMap.put(id, value.currentIntValue);
            }
        }
        return valueMap;
    }

    public @Nullable String getIsOffline() {
        return isOffline;
    }

    public @Nullable String getOnlineImage() {
        return onlineImage;
    }

    public @Nullable String getDate() {
        return date;
    }

    public @Nullable String getFuzzyDate() {
        return fuzzyDate;
    }

}
