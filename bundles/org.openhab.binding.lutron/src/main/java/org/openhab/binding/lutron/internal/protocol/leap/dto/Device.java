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
package org.openhab.binding.lutron.internal.protocol.leap.dto;

import java.util.regex.Pattern;

import org.openhab.binding.lutron.internal.protocol.leap.AbstractMessageBody;

import com.google.gson.annotations.SerializedName;

/**
 * LEAP Device Object
 *
 * @author Bob Adair - Initial contribution
 */
public class Device extends AbstractMessageBody {
    public static final Pattern DEVICE_HREF_PATTERN = Pattern.compile("/device/([0-9]+)");
    private static final Pattern ZONE_HREF_PATTERN = Pattern.compile("/zone/([0-9]+)");

    @SerializedName("href")
    public String href;

    @SerializedName("Name")
    public String name;

    @SerializedName("FullyQualifiedName")
    public String[] fullyQualifiedName;

    @SerializedName("Parent")
    public Href parent = new Href();

    @SerializedName("SerialNumber")
    public String serialNumber;

    @SerializedName("ModelNumber")
    public String modelNumber;

    @SerializedName("DeviceType")
    public String deviceType;

    @SerializedName("LocalZones")
    public Href[] localZones;

    @SerializedName("AssociatedArea")
    public Href associatedArea = new Href();

    @SerializedName("OccupancySensors")
    public Href[] occupancySensors;

    @SerializedName("LinkNodes")
    public Href[] linkNodes;

    @SerializedName("DeviceRules")
    public Href[] deviceRules;

    @SerializedName("RepeaterProperties")
    public RepeaterProperties repeaterProperties;

    @SerializedName("FirmwareImage")
    public FirmwareImage firmwareImage;

    @SerializedName("IsThisDevice")
    public boolean isThisDevice;

    public class FirmwareImage {
        @SerializedName("Firmware")
        public Firmware firmware;
        @SerializedName("Installed")
        public ProjectTimestamp installed;
    }

    public class Firmware {
        @SerializedName("DisplayName")
        public String displayName;
    }

    public class RepeaterProperties {
        @SerializedName("IsRepeater")
        public boolean isRepeater;
    }

    public Device() {
    }

    public int getDevice() {
        return hrefNumber(DEVICE_HREF_PATTERN, href);
    }

    /**
     * Returns the zone number of the first zone listed in LocalZones.
     * Currently devices should only have one zone listed.
     */
    public int getZone() {
        if (localZones != null && localZones.length > 0) {
            return hrefNumber(ZONE_HREF_PATTERN, localZones[0].href);
        } else {
            return 0;
        }
    }

    public String getFullyQualifiedName() {
        if (fullyQualifiedName != null && fullyQualifiedName.length > 0) {
            return String.join(" ", fullyQualifiedName);
        } else {
            return "";
        }
    }
}
