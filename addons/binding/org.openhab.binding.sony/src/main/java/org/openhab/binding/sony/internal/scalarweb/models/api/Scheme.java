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
 * The Class Scheme.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class Scheme {

    /** The tv. */
    public static String TV = "tv";

    /** The scheme. */
    private final String scheme;

    /**
     * Instantiates a new scheme.
     *
     * @param scheme the scheme
     */
    public Scheme(String scheme) {
        super();
        this.scheme = scheme;
    }

    /**
     * Gets the scheme.
     *
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Scheme [scheme=" + scheme + "]";
    }
}
