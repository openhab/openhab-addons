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
package org.openhab.binding.lutron.internal.protocol.leap.dto;

import java.util.regex.Pattern;

import org.openhab.binding.lutron.internal.protocol.leap.AbstractMessageBody;

import com.google.gson.annotations.SerializedName;

/**
 * LEAP OccupancyGroupStatus object
 *
 * @author Bob Adair - Initial contribution
 */
public class OccupancyGroupStatus extends AbstractMessageBody {
    public static final Pattern OGROUP_HREF_PATTERN = Pattern.compile("/occupancygroup/([0-9]+)");

    @SerializedName("href")
    public String href;

    @SerializedName("OccupancyGroup")
    public Href occupancyGroup;

    @SerializedName("OccupancyStatus")
    public String occupancyStatus; // Occupied, Unoccupied, or Unknown

    public OccupancyGroupStatus() {
    }

    public int getOccupancyGroup() {
        if (occupancyGroup != null && occupancyGroup.href != null) {
            return hrefNumber(OGROUP_HREF_PATTERN, occupancyGroup.href);
        } else {
            return 0;
        }
    }
}
