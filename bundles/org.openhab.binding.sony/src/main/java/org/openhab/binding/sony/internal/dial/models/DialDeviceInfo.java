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
package org.openhab.binding.sony.internal.dial.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The class representing a DIAL device and it's information. The element being deserialized will typically look like:
 *
 * <pre>
 * {@code
       <av:X_DIALEX_DeviceInfo xmlns:av="urn:schemas-sony-com:av">
         <av:X_DIALEX_AppsListURL>http://192.168.1.12:50202/appslist</av:X_DIALEX_AppsListURL>
         <av:X_DIALEX_DeviceID>B0:00:04:07:DD:7E</av:X_DIALEX_DeviceID>
         <av:X_DIALEX_DeviceType>BDP_DIAL</av:X_DIALEX_DeviceType>
       </av:X_DIALEX_DeviceInfo>
 * }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("X_DIALEX_DeviceInfo")
public class DialDeviceInfo {

    /** The apps list url. */
    @XStreamAlias("X_DIALEX_AppsListURL")
    private final @Nullable String appsListUrl;

    /** The device id. */
    @XStreamAlias("X_DIALEX_DeviceID")
    private final @Nullable String deviceId;

    /** The device type. */
    @XStreamAlias("X_DIALEX_DeviceType")
    private final @Nullable String deviceType;

    /**
     * Private constructor to construct the object - only called from the {@link #withApps(List)}
     *
     * @param appsListUrl the possibly null, possibly empty application list URL
     * @param deviceId the possibly null, possibly empty application device ID
     * @param deviceType the possibly null, possibly empty application device type
     */
    public DialDeviceInfo(final @Nullable String appsListUrl, final @Nullable String deviceId,
            final @Nullable String deviceType) {
        this.appsListUrl = appsListUrl;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
    }

    /**
     * Get's the application list URL
     *
     * @return a possibly null, possibly empty application list URL
     */
    public @Nullable String getAppsListUrl() {
        return appsListUrl;
    }

    /**
     * Gets the device id
     *
     * @return a possibly null, possibly empty device id
     */
    public @Nullable String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the device type
     *
     * @return a possibly null, possibly empty device type
     */
    public @Nullable String getDeviceType() {
        return deviceType;
    }
}
