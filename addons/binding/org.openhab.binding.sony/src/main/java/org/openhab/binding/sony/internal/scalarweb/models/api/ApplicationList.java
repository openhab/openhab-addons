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
 * The Class ApplicationList.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ApplicationList {

    /** The title. */
    private final String title;

    /** The uri. */
    private final String uri;

    /** The icon. */
    private final String icon;

    /** The data. */
    private final String data;

    /**
     * Instantiates a new application list.
     *
     * @param title the title
     * @param uri the uri
     * @param icon the icon
     * @param data the data
     */
    public ApplicationList(String title, String uri, String icon, String data) {
        super();
        this.title = title;
        this.uri = uri;
        this.icon = icon;
        this.data = data;
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
     * Gets the uri.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Gets the icon.
     *
     * @return the icon
     */
    public String getIcon() {
        return icon;
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
        return "ApplicationListItem [title=" + title + ", uri=" + uri + ", icon=" + icon + ", data=" + data + "]";
    }
}
