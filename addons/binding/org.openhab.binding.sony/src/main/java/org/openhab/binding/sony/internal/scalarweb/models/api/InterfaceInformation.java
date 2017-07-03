/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

// TODO: Auto-generated Javadoc
/**
 * The Class InterfaceInformation.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class InterfaceInformation {

    /** The product category. */
    private final String productCategory;

    /** The product name. */
    private final String productName;

    /** The model name. */
    private final String modelName;

    /** The server name. */
    private final String serverName;

    /** The interface version. */
    private final String interfaceVersion;

    /**
     * Instantiates a new interface information.
     *
     * @param productCategory the product category
     * @param productName the product name
     * @param modelName the model name
     * @param serverName the server name
     * @param interfaceVersion the interface version
     */
    public InterfaceInformation(String productCategory, String productName, String modelName, String serverName,
            String interfaceVersion) {
        this.productCategory = productCategory;
        this.productName = productName;
        this.modelName = modelName;
        this.serverName = serverName;
        this.interfaceVersion = interfaceVersion;
    }

    /**
     * Gets the product category.
     *
     * @return the product category
     */
    public String getProductCategory() {
        return productCategory;
    }

    /**
     * Gets the product name.
     *
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Gets the model name.
     *
     * @return the model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Gets the server name.
     *
     * @return the server name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Gets the interface version.
     *
     * @return the interface version
     */
    public String getInterfaceVersion() {
        return interfaceVersion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "InterfaceInformation [productCategory=" + productCategory + ", productName=" + productName
                + ", modelName=" + modelName + ", serverName=" + serverName + ", interfaceVersion=" + interfaceVersion
                + "]";
    }
}
