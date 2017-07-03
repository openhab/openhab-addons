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
 * The Class WebAppStatus.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class WebAppStatus {

    /** The active. */
    private final boolean active;

    /** The url. */
    private final String url;

    /**
     * Instantiates a new web app status.
     *
     * @param active the active
     * @param url the url
     */
    public WebAppStatus(boolean active, String url) {
        super();
        this.active = active;
        this.url = url;
    }

    /**
     * Checks if is active.
     *
     * @return true, if is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "WebAppStatus [active=" + active + ", url=" + url + "]";
    }
}
