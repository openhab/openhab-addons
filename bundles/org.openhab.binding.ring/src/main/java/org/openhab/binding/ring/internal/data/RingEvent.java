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
package org.openhab.binding.ring.internal.data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.json.simple.JSONObject;
import org.openhab.binding.ring.internal.ApiConstants;

/**
 *
 * @author Wim Vissers - Initial contribution
 */

public class RingEvent {

    /**
     * The JSONObject contains the data retrieved from the Ring API,
     * or the data to send to the API.
     */
    private JSONObject jsonObject;
    /**
     * The Doorbot linked to this event
     */
    private Doorbot doorbot;

    /**
     * The JSONObject is retrieved from the Ring API, example:
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
    public RingEvent(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        this.doorbot = new Doorbot((JSONObject) jsonObject.get(ApiConstants.EVENT_DOORBOT));
    }

    /**
     * Get the event id.
     *
     * @return the id.
     */
    @SuppressWarnings("unchecked")
    public String getEventId() {
        return jsonObject.getOrDefault(ApiConstants.EVENT_ID, "?").toString();
    }

    /**
     * Get the date/time created as String.
     *
     * @return the date/time.
     */
    @SuppressWarnings("unchecked")
    public String getCreatedAt() {
        String eventTime = jsonObject.getOrDefault(ApiConstants.EVENT_CREATED_AT, "?").toString();
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
    @SuppressWarnings("unchecked")
    public boolean isAnswered() {
        return jsonObject.getOrDefault(ApiConstants.EVENT_ANSWERED, "false").toString().equalsIgnoreCase("true");
    }

    /**
     * Get the event kind (motion or ding).
     *
     * @return the kind.
     */
    @SuppressWarnings("unchecked")
    public String getKind() {
        return jsonObject.getOrDefault(ApiConstants.EVENT_KIND, "?").toString();
    }

    /**
     * Get the favorite status.
     *
     * @return favorite or not.
     */
    @SuppressWarnings("unchecked")
    public boolean isFavorite() {
        return jsonObject.getOrDefault(ApiConstants.EVENT_FAVORITE, "false").toString().equalsIgnoreCase("true");
    }

    /**
     * Get the snapshot url, if any.
     *
     * @return the snapshot url.
     */
    @SuppressWarnings("unchecked")
    public String getSnapshotUrl() {
        return jsonObject.getOrDefault(ApiConstants.EVENT_SNAPSHOT_URL, "").toString();
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
