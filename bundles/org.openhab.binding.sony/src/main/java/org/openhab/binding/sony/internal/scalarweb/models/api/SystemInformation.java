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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the system information and is used for deserialization only. Note that there are many more
 * properties to the getSystemInformation call that we do not currently use.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SystemInformation {
    /** The product id */
    private @Nullable String product;

    /** The region */
    private @Nullable String region;

    /** The system language */
    private @Nullable String language;

    /** The model */
    private @Nullable String model;

    /** The serial */
    private @Nullable String serial;

    /** The mac address */
    private @Nullable String macAddr;

    /** The name */
    private @Nullable String name;

    /** The generation */
    private @Nullable String generation;

    /** The area */
    private @Nullable String area;

    /** The cid (not sure) */
    private @Nullable String cid;

    /**
     * Constructor used for deserialization only
     */
    public SystemInformation() {
    }

    /**
     * Gets the product
     *
     * @return the product
     */
    public @Nullable String getProduct() {
        return product;
    }

    /**
     * Gets the region
     *
     * @return the region
     */
    public @Nullable String getRegion() {
        return region;
    }

    /**
     * Gets the language
     *
     * @return the language
     */
    public @Nullable String getLanguage() {
        return language;
    }

    /**
     * Gets the model
     *
     * @return the model
     */
    public @Nullable String getModel() {
        return model;
    }

    /**
     * Gets the serial
     *
     * @return the serial
     */
    public @Nullable String getSerial() {
        return serial;
    }

    /**
     * Gets the mac address
     *
     * @return the mac address
     */
    public @Nullable String getMacAddr() {
        return macAddr;
    }

    /**
     * Gets the name
     *
     * @return the name
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Gets the generation
     *
     * @return the generation
     */
    public @Nullable String getGeneration() {
        return generation;
    }

    /**
     * Gets the area
     *
     * @return the area
     */
    public @Nullable String getArea() {
        return area;
    }

    /**
     * Gets the cid
     *
     * @return the cid
     */
    public @Nullable String getCid() {
        return cid;
    }

    @Override
    public String toString() {
        return "SystemInformation [product=" + product + ", region=" + region + ", language=" + language + ", model="
                + model + ", serial=" + serial + ", macAddr=" + macAddr + ", name=" + name + ", generation="
                + generation + ", area=" + area + ", cid=" + cid + "]";
    }
}
