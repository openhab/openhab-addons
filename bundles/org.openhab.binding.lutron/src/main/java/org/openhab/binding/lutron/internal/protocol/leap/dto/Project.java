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
 * LEAP Project Object
 *
 * @author Peter Wojciechowski - Initial contribution
 */
public class Project extends AbstractMessageBody {
    @SerializedName("href")
    public String href;

    @SerializedName("Name")
    public String name;

    @SerializedName("ProductType")
    public String productType;

    @SerializedName("MasterDeviceList")
    public MasterDeviceList masterDeviceList;

    @SerializedName("Contacts")
    public Href[] contacts;

    @SerializedName("TimeclockEventRules")
    public Href timeclockEventRules;

    @SerializedName("ProjectModifiedTimestamp")
    public ProjectTimestamp projectModifiedTimestamp;

    public class MasterDeviceList {
        public static final Pattern DEVICE_HREF_PATTERN = Pattern.compile("/device/([0-9]+)");

        public int getDeviceIdFromHref(int deviceIndex) {
            if (devices.length == 0) {
                return 0;
            }

            return hrefNumber(DEVICE_HREF_PATTERN, devices[deviceIndex].href);
        }

        @SerializedName("Devices")
        public Href[] devices;
    }
}
