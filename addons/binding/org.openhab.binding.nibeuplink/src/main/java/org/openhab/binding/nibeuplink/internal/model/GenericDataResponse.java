/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * data class to map the json status response
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class GenericDataResponse implements DataResponse {

    public static class Value {
        @SerializedName("VariableId")
        private String variableId;
        @SerializedName("CurrentValue")
        private String currentValue;
    }

    @SerializedName("IsOffline")
    private String isOffline;
    @SerializedName("OnlineImage")
    private String onlineImage;
    @SerializedName("Date")
    private String date;
    @SerializedName("FuzzyDate")
    private String fuzzyDate;
    @SerializedName("Values")
    private List<Value> values = new ArrayList<>();

    public String getValue(VVM320Channels key) {
        for (Value value : values) {
            if (value.variableId.equals(key.getId())) {
                return value.currentValue;
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getValues() {
        Map<String, String> valueMap = new HashMap<>();
        for (Value value : values) {
            valueMap.put(value.variableId, value.currentValue);
        }
        return valueMap;
    }

    public String getIsOffline() {
        return isOffline;
    }

    public String getOnlineImage() {
        return onlineImage;
    }

    public String getDate() {
        return date;
    }

    public String getFuzzyDate() {
        return fuzzyDate;
    }

}
