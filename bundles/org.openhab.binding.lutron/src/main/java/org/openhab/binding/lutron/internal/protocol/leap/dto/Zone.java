/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * LEAP Zone Object
 *
 * @author Peter J Wojciechowski - Initial contribution
 */

public class Zone extends AbstractMessageBody {
    public static final Pattern ZONE_HREF_PATTERN = Pattern.compile("/zone/([0-9]+)");

    @SerializedName("href")
    public String href = "";
    @SerializedName("XID")
    public String xid = "";
    @SerializedName("Name")
    public String name = "";
    @SerializedName("AvailableControlTypes")
    public String[] controlTypes = {};
    @SerializedName("Category")
    public ZoneCategory zoneCategory = new ZoneCategory();
    @SerializedName("AssociatedArea")
    public Href associatedArea = new Href();
    @SerializedName("SortOrder")
    public int sortOrder;
    @SerializedName("ControlType")
    public String controlType = "";

    public int getZone() {
        if (href != null) {
            return hrefNumber(ZONE_HREF_PATTERN, href);
        } else {
            return 0;
        }
    }

    public class ZoneCategory {
        @SerializedName("Type")
        public String type = "";
        @SerializedName("IsLight")
        public boolean isLight = false;
    }
}
