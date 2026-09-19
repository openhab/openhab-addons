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
 * Response body of {@code GET /vodService/v2/detailscreen/{titleId}}. For an episode, {@code title} is the
 * episode's own title and {@code seriesTitle} is the show name; for a movie, {@code title} is the movie
 * title and {@code seriesTitle} is absent.
 *
 * @author Mark - Initial contribution
 */
public class VodDetailDto {

    @SerializedName("id")
    public String id;

    @SerializedName("type")
    public String type;

    @SerializedName("title")
    public String title;

    @SerializedName("seriesTitle")
    public String seriesTitle;

    @SerializedName("season")
    public Integer season;

    @SerializedName("episode")
    public Integer episode;

    public boolean isEpisode() {
        return "EPISODE".equalsIgnoreCase(type);
    }

    @Override
    public String toString() {
        return "VodDetailDto [id=" + id + ", type=" + type + ", title=" + title + ", seriesTitle=" + seriesTitle
                + ", season=" + season + ", episode=" + episode + "]";
    }
}
