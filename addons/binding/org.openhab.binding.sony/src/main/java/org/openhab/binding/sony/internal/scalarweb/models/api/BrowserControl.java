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
 * The Class BrowserControl.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class BrowserControl {

    /** The control. */
    private final String control;

    /**
     * Instantiates a new browser control.
     *
     * @param control the control
     */
    public BrowserControl(String control) {
        super();
        this.control = control;
    }

    /**
     * Gets the control.
     *
     * @return the control
     */
    public String getControl() {
        return control;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "BrowserControl [control=" + control + "]";
    }

}
