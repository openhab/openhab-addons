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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class MapSetReport {
    public String type;
    public int count;
    @SerializedName("mid")
    public String mapId;
    @SerializedName("msid")
    public String mapSetId;
    public List<MapSubSetInfo> subsets;

    public static class MapSubSetInfo {
        @SerializedName("mssid")
        public String id;
    }
}
