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

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used by the get_consumables response
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

public class GetConsumables {
    public int id;

    public Result[] result;

    public class Result {
        @SerializedName("main_brush_work_time")
        public int mainBrushWorkTime;

        @SerializedName("side_brush_work_time")
        public int sideBrushWorkTime;

        @SerializedName("filter_work_time")
        public int filterWorkTime;

        @SerializedName("filter_element_work_time")
        public int filterElementWorkTime;

        @SerializedName("sensor_dirty_time")
        public int sensorDirtyTime;

        @SerializedName("strainer_work_times")
        public int strainerWorkTimes;

        @SerializedName("dust_collection_work_times")
        public int dustCollectionWorkTimes;
    }

    public GetConsumables() {
    }
}
