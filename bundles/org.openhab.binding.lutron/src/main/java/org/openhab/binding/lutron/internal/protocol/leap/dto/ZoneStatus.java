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

import org.openhab.binding.lutron.internal.protocol.FanSpeedType;
import org.openhab.binding.lutron.internal.protocol.leap.AbstractMessageBody;

import com.google.gson.annotations.SerializedName;

/**
 * LEAP ZoneStatus Object
 *
 * @author Bob Adair - Initial contribution
 */
public class ZoneStatus extends AbstractMessageBody {
    private static final Pattern ZONE_HREF_PATTERN = Pattern.compile("/zone/([0-9]+)");

    @SerializedName("href")
    public String href = "";
    @SerializedName("Level")
    public int level; // 0-100
    @SerializedName("SwitchedLevel")
    public String switchedLevel = ""; // "On" or "Off"
    @SerializedName("FanSpeed")
    public FanSpeedType fanSpeed;
    @SerializedName("Zone")
    public Href zone = new Href();;
    @SerializedName("StatusAccuracy")
    public String statusAccuracy = ""; // "Good" or ??

    public ZoneStatus() {
    }

    public int getZone() {
        if (zone != null) {
            return hrefNumber(ZONE_HREF_PATTERN, zone.href);
        } else {
            return 0;
        }
    }

    public boolean statusAccuracyGood() {
        return "Good".equals(statusAccuracy);
    }

    public boolean switchedLevelOn() {
        return "On".equals(switchedLevel);
    }
}
