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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.system;

import java.time.Instant;
import java.util.List;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectModel;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.types.EventType;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType;

/**
 * Event model for UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Event extends UniFiProtectModel {

    public EventType type;
    public Instant start;
    public Instant end;
    public Integer score;
    public String heatmapId;
    public String cameraId;
    public List<SmartDetectObjectType> smartDetectTypes;
    public List<String> smartDetectEventIds;
    public String thumbnailId;
    public String userId;
    public Instant timestamp;
    public EventMetadata metadata;
    public Instant deletedAt;
    public String deletionType;
    public String category;
    public String subCategory;
    public Boolean isFavorite;
    public List<String> favoriteObjectIds;
    public String partition;
    public String description;

    /**
     * Event metadata containing additional context information
     */
    public static class EventMetadata {
        public String clientPlatform;
        public String reason;
        public String appUpdate;
        public String lightId;
        public String lightName;
        public String type;
        public String sensorId;
        public String sensorName;
        public String sensorType;
        public String doorlockId;
        public String doorlockName;
        public String fromValue;
        public String toValue;
        public String mountType;
        public String status;
        public String alarmType;
        public String deviceId;
        public String mac;
        public List<EventDetectedThumbnail> detectedThumbnails;
    }

    /**
     * Detected thumbnail information (e.g., license plate recognition)
     */
    public static class EventDetectedThumbnail {
        public Instant clockBestWall;
        public String type;
        public List<EventThumbnailGroup> groups;
    }

    /**
     * Group information for detected thumbnails
     */
    public static class EventThumbnailGroup {
        public String id;
        public String name;
        public String matchedName;
        public Integer confidence;
    }
}
