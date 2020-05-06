/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal.model;

import java.math.BigDecimal;

import com.google.gson.annotations.SerializedName;

/**
 * embyPlayState - part of the model for the json object received from the server
 *
 * @author Zachary Christiansen - Initial Contribution
 *
 */
public class EmbyNowPlayingItem {

    @SerializedName("Name")
    private String name;
    @SerializedName("OriginalTitle")
    private String originalTitle;
    @SerializedName("Id")
    private String id;
    @SerializedName("RunTimeTicks")
    private BigDecimal runTimeTicks;
    @SerializedName("Overview")
    private String overview;
    @SerializedName("SeasonId")
    private String seasonId;
    @SerializedName("Type")
    private String nowPlayingType;

    String getName() {
        return name;

    }

    String getSeasonId() {
        return this.seasonId;
    }

    String getNowPlayingType() {
        return nowPlayingType;

    }

    String getOriginalTitle() {
        return originalTitle;

    }

    /**
     * @return the media source id of the now playing item
     */
    String getId() {
        return this.id;

    }

    /**
     * @return the total runtime ticks of the currently playing item
     */
    BigDecimal getRunTimeTicks() {
        return runTimeTicks;

    }

    String getOverview() {
        return overview;

    }

}
