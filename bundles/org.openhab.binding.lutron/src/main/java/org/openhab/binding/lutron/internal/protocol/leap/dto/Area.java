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
package org.openhab.binding.lutron.internal.protocol.leap.dto;

import java.util.regex.Pattern;

import org.openhab.binding.lutron.internal.protocol.leap.AbstractMessageBody;

import com.google.gson.annotations.SerializedName;

/**
 * LEAP Area object
 *
 * @author Bob Adair - Initial contribution
 */
public class Area extends AbstractMessageBody {
    private static final Pattern AREA_HREF_PATTERN = Pattern.compile("/area/([0-9]+)");

    @SerializedName("href")
    public String href;

    @SerializedName("Name")
    public String name;

    @SerializedName("Parent")
    public Href parent;

    // @SerializedName("Category")
    // public Category category;

    @SerializedName("AssociatedDevices")
    public Href[] associatedDevices;

    @SerializedName("AssociatedOccupancyGroups")
    public Href[] associatedOccupancyGroups;

    @SerializedName("LoadShedding")
    public Href loadShedding;

    @SerializedName("OccupancySettings")
    public Href occupancySettings;

    @SerializedName("OccupancySensorSettings")
    public Href occupancySensorSettings;

    @SerializedName("DaylightingGainSettings")
    public Href daylightingGainSettings;

    public Area() {
    }

    public int getArea() {
        return hrefNumber(AREA_HREF_PATTERN, href);
    }
}
