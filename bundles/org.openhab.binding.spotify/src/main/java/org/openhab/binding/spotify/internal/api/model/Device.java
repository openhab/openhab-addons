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
package org.openhab.binding.spotify.internal.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Spotify Web Api Device data class.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public class Device {

    private String id;
    @SerializedName("is_active")
    private boolean active;
    @SerializedName("is_restricted")
    private boolean restricted;
    private String name;
    private String type;
    private Integer volumePercent;

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Integer getVolumePercent() {
        return volumePercent;
    }
}
