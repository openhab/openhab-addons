/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.fox.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link FoxFunctionsConfigAPI} describes body of API structure for Gson deserialization.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
class FoxFunctionsConfigAPI {
    Map<String, String> tasks = new HashMap<String, String>();
    Map<String, String> results = new HashMap<String, String>();
}

/**
 * The {@link FoxFunctionsConfigRoot} describes header of API structure for Gson deserialization.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
class FoxFunctionsConfigRoot {
    @SerializedName("API")
    FoxFunctionsConfigAPI api = new FoxFunctionsConfigAPI();
}

/**
 * The {@link FoxFunctionsConfig} is responsible for Gson deserialization of system API.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
public class FoxFunctionsConfig {
    FoxFunctionsConfigRoot root = new FoxFunctionsConfigRoot();

    @Nullable
    FoxFunctionsConfigRoot parse(String json) {
        if (!json.isEmpty()) {
            try {
                return new Gson().fromJson(json, FoxFunctionsConfigRoot.class);
            } catch (JsonSyntaxException e) {
            }
        }
        return null;
    }

    FoxFunctionsConfig(String json) {
        FoxFunctionsConfigRoot rootFromJson = parse(json);
        if (rootFromJson != null) {
            root = rootFromJson;
        }
    }

    Map<String, String> getTasks() {
        return root.api.tasks;
    }

    Map<String, String> getResults() {
        return root.api.results;
    }
}
