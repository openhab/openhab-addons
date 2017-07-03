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
 * The Class DeleteContent.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class DeleteContent {

    /** The uri. */
    private final String uri;

    /**
     * Instantiates a new delete content.
     *
     * @param uri the uri
     */
    public DeleteContent(String uri) {
        super();
        this.uri = uri;
    }

    /**
     * Gets the uri.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeleteContent [uri=" + uri + "]";
    }
}
