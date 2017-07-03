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
 * The Class BrowserBookmark.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class BrowserBookmark {

    /** The url. */
    private final String url;

    /** The title. */
    private final String title;

    /**
     * Instantiates a new browser bookmark.
     *
     * @param url the url
     * @param title the title
     */
    public BrowserBookmark(String url, String title) {
        super();
        this.url = url;
        this.title = title;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "BrowserBookmark [url=" + url + ", title=" + title + "]";
    }

}
