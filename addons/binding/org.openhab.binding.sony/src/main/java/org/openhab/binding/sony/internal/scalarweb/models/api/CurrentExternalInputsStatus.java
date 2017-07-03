/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.HashSet;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class CurrentExternalInputsStatus.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class CurrentExternalInputsStatus {

    /** The uri. */
    private final String uri;

    /** The title. */
    private final String title;

    /** The connection. */
    private final boolean connection;

    /** The label. */
    private final String label;

    /** The icon. */
    private final String icon;

    /** The status. */
    private final String status;

    /** The field names. */
    private final Set<String> fieldNames;

    /**
     * Instantiates a new current external inputs status.
     *
     * @param uri the uri
     * @param title the title
     * @param connection the connection
     * @param label the label
     * @param icon the icon
     * @param status the status
     */
    public CurrentExternalInputsStatus(String uri, String title, boolean connection, String label, String icon,
            String status) {
        super();
        this.uri = uri;
        this.title = title;
        this.connection = connection;
        this.label = label;
        this.icon = icon;
        this.status = status;
        this.fieldNames = new HashSet<String>();
    }

    /**
     * Checks for.
     *
     * @param fieldName the field name
     * @return true, if successful
     */
    private boolean has(String fieldName) {
        return fieldNames.size() == 0 || fieldNames.contains(fieldName);
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
     * Checks for uri.
     *
     * @return true, if successful
     */
    public boolean hasUri() {
        return has("uri");
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
     * Checks for title.
     *
     * @return true, if successful
     */
    public boolean hasTitle() {
        return has("title");
    }

    /**
     * Checks if is connection.
     *
     * @return true, if is connection
     */
    public boolean isConnection() {
        return connection;
    }

    /**
     * Checks for connection.
     *
     * @return true, if successful
     */
    public boolean hasConnection() {
        return has("connection");
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Checks for label.
     *
     * @return true, if successful
     */
    public boolean hasLabel() {
        return has("label");
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
     * Checks for icon.
     *
     * @return true, if successful
     */
    public boolean hasIcon() {
        return has("icon");
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Checks for status.
     *
     * @return true, if successful
     */
    public boolean hasStatus() {
        return has("status");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CurrentExternalInputsStatus [uri=" + uri + ", title=" + title + ", connection=" + connection
                + ", label=" + label + ", icon=" + icon + ", status=" + status + "]";
    }
}
