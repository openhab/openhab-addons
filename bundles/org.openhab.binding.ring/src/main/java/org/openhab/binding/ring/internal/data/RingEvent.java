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
package org.openhab.binding.ring.internal.data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.ApiConstants;

import com.google.gson.JsonObject;

/**
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class RingEvent {

    /**
     * The JsonObject contains the data retrieved from the Ring API,
     * or the data to send to the API.
     */
    private JsonObject jsonObject;
    /**
     * The Doorbot linked to this event
     */
    private Doorbot doorbot;

    /**
     * The JsonObject is retrieved from the Ring API, example:
     * {
     * "id": 6514261607488226599,
     * "created_at": "2018-01-23T15:02:03.000Z",
     * "answered": false,
     * "events": [],
     * "kind": "motion",
     * "favorite": false,
     * "snapshot_url": "",
     * "recording": {
     * "status": "ready"
     * },
     * "doorbot": {
     * "id": 5047591,
     * "description": "Front Door"
     * }
     * },
     *
     * @param jsonObject
     */
    public RingEvent(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        this.doorbot = new Doorbot((JsonObject) jsonObject.get(ApiConstants.EVENT_DOORBOT));
    }

    /**
     * Get the event id.
     *
     * @return the id.
     */
    public String getEventId() {
        return jsonObject.get(ApiConstants.EVENT_ID).getAsString();
    }

    /**
     * Get the date/time created as String.
     *
     * @return the date/time.
     */
    public String getCreatedAt() {
        String eventTime = jsonObject.get(ApiConstants.EVENT_CREATED_AT).getAsString();
        ZonedDateTime gmtTime = LocalDateTime
                .parse(eventTime, DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH:mm:ss.SSS'Z'")).atZone(ZoneId.of("GMT"));
        LocalDateTime localTime = gmtTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        return localTime.toString();
    }

    /**
     * Get the answered status.
     *
     * @return true if answered.
     */
    public boolean isAnswered() {
        return jsonObject.get(ApiConstants.EVENT_ANSWERED).getAsString().equalsIgnoreCase("true");
    }

    /**
     * Get the event kind (motion or ding).
     *
     * @return the kind.
     */
    public String getKind() {
        return jsonObject.get(ApiConstants.EVENT_KIND).getAsString();
    }

    /**
     * Get the favorite status.
     *
     * @return favorite or not.
     */
    public boolean isFavorite() {
        return jsonObject.get(ApiConstants.EVENT_FAVORITE).getAsString().equalsIgnoreCase("true");
    }

    /**
     * Get the snapshot url, if any.
     *
     * @return the snapshot url.
     */
    public String getSnapshotUrl() {
        return jsonObject.get(ApiConstants.EVENT_SNAPSHOT_URL).getAsString();
    }

    /**
     * Get the doorbot linked to this event.
     *
     * @return the doorbot.
     */
    public Doorbot getDoorbot() {
        return doorbot;
    }
}
