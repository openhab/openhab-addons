/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.util.concurrent.atomic.AtomicBoolean;

// TODO: Auto-generated Javadoc
/**
 * The Class ContentItem.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ContentItem {

    /** The uri. */
    private final String uri;

    /** The status. */
    private final AtomicBoolean status = new AtomicBoolean(false);

    /**
     * Instantiates a new content item.
     *
     * @param uri the uri
     */
    public ContentItem(String uri) {
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

    /**
     * Checks if is running.
     *
     * @return true, if is running
     */
    public boolean isRunning() {
        return status.get();
    }

    /**
     * Sets the running.
     *
     * @param running the new running
     */
    public void setRunning(boolean running) {
        status.set(running);
    }
}
