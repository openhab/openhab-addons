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

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents the state of a DIAL device. The DIAL state will include all the devices specified and the URL
 * to access the device
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class DialClient {
    /** The list of {@link DialDeviceInfo} */
    @XStreamImplicit
    private final List<DialDeviceInfo> deviceInfos;

    /** The url to get application state */
    private final URL appUrl;

    /**
     * Constructs the instance from the specified URL and list of {@link DialDeviceInfo}
     *
     * @param appUrl a non-null application URL
     * @param infos a non-null, possibly emply list of {@link DialDeviceInfo}
     */
    public DialClient(final URL appUrl, final List<DialDeviceInfo> infos) {
        Objects.requireNonNull(appUrl, "appUrl cannot be null");
        Objects.requireNonNull(infos, "infos cannot be null");

        this.appUrl = appUrl;
        deviceInfos = Collections.unmodifiableList(infos);
    }

    /**
     * Returns the device application URL
     *
     * @return the non-null device application URL
     */
    public URL getAppUrl() {
        return appUrl;
    }

    /**
     * Checks to see if the state has any services
     *
     * @return true, if successful, false otherwise
     */
    public boolean hasDialService() {
        return !deviceInfos.isEmpty();
    }

    /**
     * Returns the first device ID or null if there are no devices
     *
     * @return the first device ID or null
     */
    public @Nullable String getFirstDeviceId() {
        return deviceInfos.stream().map(e -> e.getDeviceId()).filter(e -> StringUtils.isNotEmpty(e)).findFirst()
                .orElse(null);
    }

    /**
     * Returns the list of device information. Likely only a single device
     * 
     * @return a non-null, possibly empty non-modifiable list of {@link DialDeviceInfo}
     */
    public List<DialDeviceInfo> getDeviceInfos() {
        return deviceInfos;
    }
}
