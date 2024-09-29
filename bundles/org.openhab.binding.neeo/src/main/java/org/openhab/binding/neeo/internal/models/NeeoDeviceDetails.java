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
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing a Neeo Device Details (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoDeviceDetails {

    /** The source name (neeo-deviceadapter or sdk name) */
    @Nullable
    private String sourceName;

    /** The adapter name (name given by source) */
    @Nullable
    private String adapterName;

    /** The NEEO type */
    @Nullable
    private String type;

    /** The manufacture */
    @Nullable
    private String manufacturer;

    /** The name of the device given by source */
    @Nullable
    private String name;

    /** The timings of the device */
    @Nullable
    private NeeoDeviceDetailsTiming timing;

    /** The device capabilities */
    private String @Nullable [] deviceCapabilities;

    /**
     * The device source name
     *
     * @return the device source name
     */
    @Nullable
    public String getSourceName() {
        return sourceName;
    }

    /**
     * The device adapter name (given by the source)
     *
     * @return the adapter name
     */
    @Nullable
    public String getAdapterName() {
        return adapterName;
    }

    /**
     * The NEEO device type
     *
     * @return the NEEO device type
     */
    @Nullable
    public String getType() {
        return type;
    }

    /**
     * The manufacturer of the device
     *
     * @return the manufacturer
     */
    @Nullable
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * The name of the device (given by the source)
     *
     * @return the device name
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * The device timing
     *
     * @return the timings
     */
    @Nullable
    public NeeoDeviceDetailsTiming getTiming() {
        return timing;
    }

    /**
     * The device capabilities
     *
     * @return the capabilities
     */
    public String[] getDeviceCapabilities() {
        final String[] localCapabilities = deviceCapabilities;
        return localCapabilities == null ? new String[0] : localCapabilities;
    }

    @Override
    public String toString() {
        return "NeeoDeviceDetails{" + "sourceName='" + sourceName + '\'' + ", adapterName='" + adapterName + '\''
                + ", type='" + type + '\'' + ", manufacturer='" + manufacturer + '\'' + ", name='" + name + '\''
                + ", timing=" + timing + ", deviceCapabilities=" + Arrays.toString(deviceCapabilities) + '}';
    }
}
