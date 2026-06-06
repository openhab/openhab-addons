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
package org.openhab.binding.rachio.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioZoneStatus;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;

import com.google.gson.Gson;

/**
 * {@link RachioEventStringDTO} store some relevant information from API events.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioEventStringDTO {
    private @Nullable GenericEvent gEvent;
    private @Nullable ZoneEvent zEvent;
    private Gson gson = new Gson();

    private class GenericEvent {
        final String timestamp;
        final String summary;
        final String topic;
        final String type;
        final String subType;

        public GenericEvent(RachioEventGsonDTO event) {
            timestamp = event.timestamp;
            summary = event.summary;
            topic = event.topic;
            type = event.type;
            subType = event.subType;
        }
    }

    private class ZoneEvent {
        final String timestamp;
        final String summary;
        final String type;
        final String subType;

        final String zoneName;
        final int zoneNumber;
        final String zoneRunState;
        final String scheduleType;
        final String startTime;
        final String endTime;
        final int duration;

        public ZoneEvent(RachioEventGsonDTO event) {
            timestamp = event.timestamp;
            summary = event.summary;
            type = event.type;
            subType = event.subType;

            zoneName = event.zoneName;
            zoneNumber = event.zoneNumber;
            zoneRunState = event.zoneRunState;
            RachioZoneStatus runStatus = event.zoneRunStatus;
            scheduleType = runStatus != null ? runStatus.scheduleType : "";
            startTime = runStatus != null ? runStatus.startTime : "";
            endTime = runStatus != null ? runStatus.endTime : "";
            duration = event.duration;
        }
    }

    public RachioEventStringDTO(RachioEventGsonDTO event) {
        if (event.type.equals("ZONE_STATUS")) {
            zEvent = new ZoneEvent(event);
        } else {
            gEvent = new GenericEvent(event);
        }
    }

    public @Nullable String toJson() {
        return zEvent != null ? gson.toJson(zEvent) : gson.toJson(gEvent);
    }
}
