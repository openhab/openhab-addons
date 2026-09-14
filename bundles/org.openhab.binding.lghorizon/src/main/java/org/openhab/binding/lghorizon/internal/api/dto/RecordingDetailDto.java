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
 * Response body of {@code GET /recordingService/customers/{customerId}/details/single/{recordingId}} - a
 * single nDVR (network recording) detail.
 * <p>
 * {@code title}'s meaning depends on {@code source}: for a {@code "show"}-sourced recording (a standalone
 * item recorded individually, not part of an ongoing series recording rule), {@code title} itself holds the
 * show/program name; for any other source (an episode recorded as part of a series/season rule), the show
 * name is instead in the separate {@code showTitle} field, and {@code title} is a general/fallback label.
 * See {@link #getShowTitle()}, which applies this distinction. *
 *
 * @author Mark - Initial contribution
 */
public class RecordingDetailDto {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("source")
    public String source;

    @SerializedName("channelId")
    public String channelId;

    @SerializedName("episodeTitle")
    public String episodeTitle;

    @SerializedName("showTitle")
    public String showTitle;

    @SerializedName("seasonNumber")
    public Integer seasonNumber;

    @SerializedName("episodeNumber")
    public Integer episodeNumber;

    /**
     * The show/program name: {@code title} itself for a {@code "show"}-sourced (standalone) recording, otherwise the
     * separate {@code showTitle} field.
     */
    public String getShowTitle() {
        return "show".equalsIgnoreCase(source) ? title : showTitle;
    }

    @Override
    public String toString() {
        return "RecordingDetailDto [id=" + id + ", title=" + title + ", source=" + source + ", channelId=" + channelId
                + ", episodeTitle=" + episodeTitle + ", showTitle=" + showTitle + ", seasonNumber=" + seasonNumber
                + ", episodeNumber=" + episodeNumber + "]";
    }
}
