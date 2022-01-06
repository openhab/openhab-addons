/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.siemensrds.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 *
 * Interface to the Plants List of a particular User
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class RdsPlants {

    protected final Logger logger = LoggerFactory.getLogger(RdsPlants.class);

    @SerializedName("items")
    private @Nullable List<PlantInfo> plants;

    private static final Gson GSON = new Gson();

    @SuppressWarnings("null")
    @NonNullByDefault
    public static class PlantInfo {

        @SerializedName("id")
        private @Nullable String plantId;
        @SerializedName("isOnline")
        private boolean online;

        public String getId() throws RdsCloudException {
            String plantId = this.plantId;
            if (plantId != null) {
                return plantId;
            }
            throw new RdsCloudException("plant has no Id");
        }

        public boolean isOnline() {
            return online;
        }
    }

    /*
     * public method: parse JSON, and create a class that encapsulates the data
     */
    public static @Nullable RdsPlants createFromJson(String json) {
        return GSON.fromJson(json, RdsPlants.class);
    }

    /*
     * public method: return the plant list
     */
    public @Nullable List<PlantInfo> getPlants() {
        return plants;
    }
}
