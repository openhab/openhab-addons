/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.semsportal.internal.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * POJO containing the response to the {@link StationListRequest}
 *
 * @author Iwan Bron - Initial contribution
 * @author Julio Vilmar Gesser - Migrated to the SEMS+ API
 */
public class StationListResponse extends BaseResponse {
    @SerializedName("data")
    private StationList stations;

    public List<Station> getStations() {
        return stations == null || stations.dataList == null ? List.of() : stations.dataList;
    }

    private static class StationList {
        private List<Station> dataList;
    }
}
