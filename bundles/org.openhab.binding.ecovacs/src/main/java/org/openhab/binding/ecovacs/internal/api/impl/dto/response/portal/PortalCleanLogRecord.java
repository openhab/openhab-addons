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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import org.openhab.binding.ecovacs.internal.api.model.CleanMode;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class PortalCleanLogRecord {
    @SerializedName("ts")
    public final long timestamp;

    @SerializedName("last")
    public final long duration;

    public final int area;

    public final String id;

    public final String imageUrl;

    public final CleanMode type;

    // more possible fields:
    // aiavoid (int), aitypes (list of something), aiopen (int), aq (int), mapName (string),
    // sceneName (string), triggerMode (int), powerMopType (int), enablePowerMop (int), cornerDeep (int)

    PortalCleanLogRecord(long timestamp, long duration, int area, String id, String imageUrl, CleanMode type) {
        this.timestamp = timestamp;
        this.duration = duration;
        this.area = area;
        this.id = id;
        this.imageUrl = imageUrl;
        this.type = type;
    }
}
