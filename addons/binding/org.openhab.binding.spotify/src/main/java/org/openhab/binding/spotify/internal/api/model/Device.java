/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
