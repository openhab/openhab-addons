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
package org.openhab.binding.lghorizon.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response body of {@code GET
 * /linearService/v2/replayEvent/{eventId}?returnLinearContent=true&forceLinearResponse=true}.
 * Used to resolve title/episode metadata for linear TV, live-TV rewind (reviewBuffer) and catch-up replay -
 * all three share this identical endpoint and identifier shape.
 *
 * @author Mark - Initial contribution
 */
public class EventDetailDto {

    @SerializedName("eventId")
    public String eventId;

    @SerializedName("channelId")
    public String channelId;

    @SerializedName("title")
    public String title;

    @SerializedName("episodeName")
    public String episodeName;

    @SerializedName("seasonNumber")
    public Integer seasonNumber;

    @SerializedName("episodeNumber")
    public Integer episodeNumber;

    @Override
    public String toString() {
        return "EventDetailDto [eventId=" + eventId + ", channelId=" + channelId + ", title=" + title + ", episodeName="
                + episodeName + ", seasonNumber=" + seasonNumber + ", episodeNumber=" + episodeNumber + "]";
    }
}
