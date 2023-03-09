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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import java.util.List;

import org.openhab.binding.ecovacs.internal.api.model.CleanMode;

import com.google.gson.annotations.SerializedName;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
public class PortalCleanLogsResponse {
    public static class LogRecord {
        @SerializedName("ts")
        public final long timestamp;

        @SerializedName("last")
        public final long duration;

        public final int area;

        public final String id;

        public final String imageUrl;

        public final CleanMode type;

        // more possible fields: aiavoid (int), aitypes (list of something), stopReason (int)

        LogRecord(long timestamp, long duration, int area, String id, String imageUrl, CleanMode type) {
            this.timestamp = timestamp;
            this.duration = duration;
            this.area = area;
            this.id = id;
            this.imageUrl = imageUrl;
            this.type = type;
        }
    }

    @SerializedName("logs")
    public final List<LogRecord> records;

    @SerializedName("ret")
    final String result;

    PortalCleanLogsResponse(String result, List<LogRecord> records) {
        this.result = result;
        this.records = records;
    }

    public boolean wasSuccessful() {
        return "ok".equals(result);
    }
}
