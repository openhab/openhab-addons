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
 * The Class DeleteProtection.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class DeleteProtection {

    /** The uri. */
    private final String uri;

    /** The is protected. */
    private final boolean isProtected;

    /**
     * Instantiates a new delete protection.
     *
     * @param uri the uri
     * @param isProtected the is protected
     */
    public DeleteProtection(String uri, boolean isProtected) {
        super();
        this.uri = uri;
        this.isProtected = isProtected;
    }

    /**
     * Gets the uri.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Checks if is protected.
     *
     * @return true, if is protected
     */
    public boolean isProtected() {
        return isProtected;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeleteProtection [uri=" + uri + ", isProtected=" + isProtected + "]";
    }

}
