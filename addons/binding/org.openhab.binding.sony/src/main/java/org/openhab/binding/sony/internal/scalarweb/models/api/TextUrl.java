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
 * The Class TextUrl.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class TextUrl {

    /** The url. */
    private final String url;

    /** The title. */
    private final String title;

    /** The type. */
    private final String type;

    /** The favicon. */
    private final String favicon;

    /**
     * Instantiates a new text url.
     *
     * @param url the url
     */
    public TextUrl(String url) {
        this(url, null, null, null);
    }

    /**
     * Instantiates a new text url.
     *
     * @param url the url
     * @param title the title
     * @param type the type
     * @param favicon the favicon
     */
    public TextUrl(String url, String title, String type, String favicon) {
        super();
        this.url = url;
        this.title = title;
        this.type = type;
        this.favicon = favicon;
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

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the favicon.
     *
     * @return the favicon
     */
    public String getFavicon() {
        return favicon;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TextUrl [url=" + url + ", title=" + title + ", type=" + type + ", favicon=" + favicon + "]";
    }
}
