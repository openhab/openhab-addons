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
package org.openhab.binding.hue.internal.api.dto.clip1;

import static org.openhab.binding.hue.internal.HueBindingConstants.NORMALIZE_ID_REGEX;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Detailed information about an object on the Hue Bridge
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class FullHueObject extends HueObject {

    private @NonNullByDefault({}) String type;
    private @Nullable String modelid;
    @SerializedName("manufacturername")
    private @NonNullByDefault({}) String manufacturerName;
    @SerializedName("productname")
    private @NonNullByDefault({}) String productName;
    private @Nullable String swversion;
    private @Nullable String uniqueid;

    public FullHueObject() {
        super();
    }

    /**
     * Returns the type of the object.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type of the object.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Returns the model ID of the object.
     *
     * @return model id
     */
    public @Nullable String getModelID() {
        return modelid;
    }

    public @Nullable String getNormalizedModelID() {
        String modelid = this.modelid;
        return modelid != null ? modelid.replaceAll(NORMALIZE_ID_REGEX, "_") : modelid;
    }

    /**
     * Set the model ID of the object.
     */
    public void setModelID(final String modelId) {
        this.modelid = modelId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Returns the software version of the object.
     *
     * @return software version
     */
    public @Nullable String getSoftwareVersion() {
        return swversion;
    }

    /**
     * Returns the unique id of the object. The unique is the MAC address of the device with a unique endpoint id in the
     * form: AA:BB:CC:DD:EE:FF:00:11-XX
     *
     * @return the unique id, can be null for some virtual types like the daylight sensor
     */
    public @Nullable String getUniqueID() {
        return uniqueid;
    }

    /**
     * Sets the unique id of the object.
     */
    public void setUniqueID(final String uniqueid) {
        this.uniqueid = uniqueid;
    }
}
