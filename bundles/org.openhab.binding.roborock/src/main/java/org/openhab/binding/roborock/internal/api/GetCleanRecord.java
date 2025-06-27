/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used by the get_clean_record response
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

@NonNullByDefault
public class GetCleanRecord {
    public int id;

    public @NonNullByDefault({}) Result result;

    public class Result {
        public long begin;
        public long end;
        public long duration;
        public int area;
        public int error;
        public int complete;

        @SerializedName("start_type")
        public int startType;

        @SerializedName("clean_type")
        public int cleanType;

        @SerializedName("finish_reason")
        public int finishReason;

        @SerializedName("dust_collection_status")
        public int dustCollectionStatus;

        @SerializedName("avoid_count")
        public int avoidCount;

        @SerializedName("wash_count")
        public int washCount;

        @SerializedName("map_flag")
        public int mapFlag;

        @SerializedName("cleaned_area")
        public int cleanedArea;

        @SerializedName("manual_replenish")
        public int manualReplenish;

        @SerializedName("dirty_replenish")
        public int dirtyReplenish;

        @SerializedName("clean_times")
        public int cleanTimes;
    }

    public GetCleanRecord() {
    }
}
