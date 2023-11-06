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
package org.openhab.binding.km200.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The KM200ErrorService representing an error service with its all capabilities
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public class KM200ErrorServiceHandler {

    private final Logger logger = LoggerFactory.getLogger(KM200ErrorServiceHandler.class);

    private Integer activeError = 1;

    /* List for all errors */
    private final List<Map<String, String>> errorMap;

    public KM200ErrorServiceHandler() {
        errorMap = new ArrayList<>();
    }

    /**
     * This function removes all errors from the list
     */
    void removeAllErrors() {
        synchronized (errorMap) {
            errorMap.clear();
        }
    }

    /**
     * This function updates the errors
     */
    public void updateErrors(JsonObject nodeRoot) {
        synchronized (errorMap) {
            /* Update the list of errors */
            removeAllErrors();
            JsonArray sPoints = nodeRoot.get("values").getAsJsonArray();
            for (int i = 0; i < sPoints.size(); i++) {
                JsonObject subJSON = sPoints.get(i).getAsJsonObject();
                Map<String, String> valMap = new HashMap<>();
                Set<Map.Entry<String, JsonElement>> oMap = subJSON.entrySet();
                oMap.forEach(item -> {
                    logger.trace("Set: {} val: {}", item.getKey(), item.getValue().getAsString());
                    valMap.put(item.getKey(), item.getValue().getAsString());
                });
                errorMap.add(valMap);
            }
        }
    }

    /**
     * This function returns the number of errors
     */
    public int getNbrErrors() {
        synchronized (errorMap) {
            return errorMap.size();
        }
    }

    /**
     * This function sets the actual errors
     */
    public void setActiveError(int error) {
        int actError;
        if (error < 1) {
            actError = 1;
        } else if (error > getNbrErrors()) {
            actError = getNbrErrors();
        } else {
            actError = error;
        }
        synchronized (activeError) {
            activeError = actError;
        }
    }

    /**
     * This function returns the selected error
     */
    public int getActiveError() {
        synchronized (activeError) {
            return activeError;
        }
    }

    /**
     * This function returns an error string with all parameters
     */
    public @Nullable String getErrorString() {
        String value = "";
        synchronized (errorMap) {
            int actN = getActiveError();
            if (errorMap.size() < actN || errorMap.isEmpty()) {
                return null;
            }
            /* is the time value existing ("t") then use it on the begin */
            if (errorMap.get(actN - 1).containsKey("t")) {
                value = errorMap.get(actN - 1).get("t");
                for (String para : errorMap.get(actN - 1).keySet()) {
                    if (!"t".equals(para)) {
                        value += " " + para + ":" + errorMap.get(actN - 1).get(para);
                    }
                }
            } else {
                for (String para : errorMap.get(actN - 1).keySet()) {
                    value += para + ":" + errorMap.get(actN - 1).get(para) + " ";
                }
            }
            return value;
        }
    }
}
