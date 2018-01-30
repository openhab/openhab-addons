/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import org.apache.commons.lang.StringUtils;

/**
 * The model representing an Neeo Device Details (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoDeviceDetails {

    /** The source name (neeo-deviceadapter or sdk name) */
    private final String sourceName;

    /** The adapter name (name given by source) */
    private final String adapterName;

    /** The NEEO type */
    private final String type;

    /** The manufacture */
    private final String manufacturer;

    /** The name of the device given by source */
    private final String name;

    /** The timings of the device */
    private final NeeoDeviceDetailsTiming timing;

    /** The device capabilities */
    private final String[] deviceCapabilities;

    /**
     * Constructs the device details
     *
     * @param sourceName the source name
     * @param adapterName the adapter name
     * @param type the neeo type
     * @param manufacturer the manufacturer
     * @param name the name of the device
     * @param timing the device timings
     * @param deviceCapabilities the device capabilities
     */
    public NeeoDeviceDetails(String sourceName, String adapterName, String type, String manufacturer, String name,
            NeeoDeviceDetailsTiming timing, String[] deviceCapabilities) {
        this.sourceName = sourceName;
        this.adapterName = adapterName;
        this.type = type;
        this.manufacturer = manufacturer;
        this.name = name;
        this.timing = timing;
        this.deviceCapabilities = deviceCapabilities;
    }

    /**
     * The device source name
     *
     * @return the device source name
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * The device adapter name (given by the source)
     *
     * @return the adapter name
     */
    public String getAdapterName() {
        return adapterName;
    }

    /**
     * The NEEO device type
     *
     * @return the NEEO device type
     */
    public String getType() {
        return type;
    }

    /**
     * The manufacturer of the device
     *
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * The name of the device (given by the source)
     *
     * @return the device name
     */
    public String getName() {
        return name;
    }

    /**
     * The device timing
     *
     * @return the timings
     */
    public NeeoDeviceDetailsTiming getTiming() {
        return timing;
    }

    /**
     * The device capabilities
     *
     * @return the capabilities
     */
    public String[] getDeviceCapabilities() {
        return deviceCapabilities;
    }

    @Override
    public String toString() {
        return "NeeoDeviceDetails [sourceName=" + sourceName + ", adapterName=" + adapterName + ", type=" + type
                + ", manufacturer=" + manufacturer + ", name=" + name + ", timing=" + timing + ", deviceCapabilities="
                + StringUtils.join(deviceCapabilities, ',') + "]";
    }

}
