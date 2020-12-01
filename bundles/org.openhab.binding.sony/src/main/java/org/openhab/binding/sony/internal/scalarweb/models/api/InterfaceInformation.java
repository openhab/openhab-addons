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
 * This class represents the request to get interface information and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class InterfaceInformation {

    /** The product category */
    private @Nullable String productCategory;

    /** The product name */
    private @Nullable String productName;

    /** The model name */
    private @Nullable String modelName;

    /** The server name */
    private @Nullable String serverName;

    /** The interface version */
    private @Nullable String interfaceVersion;

    /**
     * Constructor used for deserialization only
     */
    public InterfaceInformation() {
    }

    /**
     * Gets the product category
     *
     * @return the product category
     */
    public @Nullable String getProductCategory() {
        return productCategory;
    }

    /**
     * Gets the product name
     *
     * @return the product name
     */
    public @Nullable String getProductName() {
        return productName;
    }

    /**
     * Gets the model name
     *
     * @return the model name
     */
    public @Nullable String getModelName() {
        return modelName;
    }

    /**
     * Gets the server name
     *
     * @return the server name
     */
    public @Nullable String getServerName() {
        return serverName;
    }

    /**
     * Gets the interface version
     *
     * @return the interface version
     */
    public @Nullable String getInterfaceVersion() {
        return interfaceVersion;
    }

    @Override
    public String toString() {
        return "InterfaceInformation [productCategory=" + productCategory + ", productName=" + productName
                + ", modelName=" + modelName + ", serverName=" + serverName + ", interfaceVersion=" + interfaceVersion
                + "]";
    }
}
