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
 * The Class PostalCode.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class PostalCode {

    /** The postal code. */
    public final String postalCode;

    /**
     * Instantiates a new postal code.
     *
     * @param postalCode the postal code
     */
    public PostalCode(String postalCode) {
        super();
        this.postalCode = postalCode;
    }

    /**
     * Gets the postal code.
     *
     * @return the postal code
     */
    public String getPostalCode() {
        return postalCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PostalCode [postalCode=" + postalCode + "]";
    }

}
