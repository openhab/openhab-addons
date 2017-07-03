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
 * The Class ApplicationStatusList.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ApplicationStatusList {

    /** The Text input. */
    public static String TextInput = "textInput";

    /** The Cursor display. */
    public static String CursorDisplay = "cursorDisplay";

    /** The Web browse. */
    public static String WebBrowse = "webBrowse";

    /** The On. */
    public static String On = "on";

    /** The Off. */
    public static String Off = "off";

    /** The name. */
    private final String name;

    /** The status. */
    private final String status;

    /**
     * Instantiates a new application status list.
     *
     * @param name the name
     * @param status the status
     */
    public ApplicationStatusList(String name, String status) {
        super();
        this.name = name;
        this.status = status;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
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
     * Checks if is on.
     *
     * @return true, if is on
     */
    public boolean isOn() {
        return On.equals(status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ApplicationStatusList [name=" + name + ", status=" + status + "]";
    }
}
