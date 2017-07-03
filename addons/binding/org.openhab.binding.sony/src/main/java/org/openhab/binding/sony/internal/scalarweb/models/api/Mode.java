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
 * The Class Mode.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class Mode {

    /** The mode. */
    private final String mode;

    /**
     * Instantiates a new mode.
     *
     * @param mode the mode
     */
    public Mode(String mode) {
        super();
        this.mode = mode;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Mode [mode=" + mode + "]";
    }
}
