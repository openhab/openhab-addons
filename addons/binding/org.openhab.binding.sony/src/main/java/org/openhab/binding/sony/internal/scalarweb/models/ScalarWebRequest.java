/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebRequest.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebRequest {

    /** The id. */
    private final int id;

    /** The method. */
    private final String method;

    /** The version. */
    private final String version;

    /** The params. */
    private final Object[] params;

    /**
     * Instantiates a new scalar web request.
     *
     * @param id the id
     * @param method the method
     * @param version the version
     */
    public ScalarWebRequest(int id, String method, String version) {
        this(id, method, version, new Object[0]);
    }

    /**
     * Instantiates a new scalar web request.
     *
     * @param id the id
     * @param method the method
     * @param version the version
     * @param params the params
     */
    public ScalarWebRequest(int id, String method, String version, Object... params) {
        this.id = id;
        this.method = method;
        this.version = version;
        this.params = params;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the method.
     *
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the params.
     *
     * @return the params
     */
    public Object[] getParams() {
        return params;
    }
}
