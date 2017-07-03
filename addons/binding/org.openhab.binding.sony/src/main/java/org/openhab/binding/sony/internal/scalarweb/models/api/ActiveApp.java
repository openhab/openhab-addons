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
 * The Class ActiveApp.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ActiveApp {

    /** The uri. */
    private final String uri;

    /** The data. */
    private final String data;

    /**
     * Instantiates a new active app.
     *
     * @param uri the uri
     * @param data the data
     */
    public ActiveApp(String uri, String data) {
        this.uri = uri;
        this.data = data;
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
     * Gets the data.
     *
     * @return the data
     */
    public String getData() {
        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ActiveApp [uri=" + uri + ", data=" + data + "]";
    }
}
