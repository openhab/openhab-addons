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
package org.openhab.binding.sony.internal.ircc.models;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The Class IrccUnrDeviceInfo.
 *
 * <pre>
 * {@code
    <av:X_UNR_DeviceInfo xmlns:av="urn:schemas-sony-com:av">
        <av:X_UNR_Version>1.2</av:X_UNR_Version>
        <av:X_CERS_ActionList_URL>http://192.168.1.100:80/cers/ActionList.xml</av:X_CERS_ActionList_URL>
    </av:X_UNR_DeviceInfo>
  *  }
 * </pre>
 *
 * @author Tim Roberts - Initial contribution
 */

@NonNullByDefault
public class IrccUnrDeviceInfo {

    /** The Constant NOTSPECIFIED. */
    public static final String NOTSPECIFIED = "Not Specified";

    /** The version. */
    @XStreamAlias("X_UNR_Version")
    private final @Nullable String version;

    /** The action list url. */
    @XStreamAlias("X_CERS_ActionList_URL")
    private @Nullable String actionListUrl;

    /**
     * Constructs a blank UNR device info with a {@link #NOTSPECIFIED} version
     */
    public IrccUnrDeviceInfo() {
        version = NOTSPECIFIED;
    }

    /**
     * Gets the action list url.
     *
     * @return the action list url
     */
    public @Nullable String getActionListUrl() {
        return actionListUrl;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public @Nullable String getVersion() {
        return StringUtils.isEmpty(version) ? NOTSPECIFIED : version;
    }
}
