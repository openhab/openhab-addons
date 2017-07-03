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
 * The Class BannerMode.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class BannerMode {

    /** The current value. */
    private final String currentValue;

    /**
     * Instantiates a new banner mode.
     *
     * @param currentValue the current value
     */
    public BannerMode(String currentValue) {
        super();
        this.currentValue = currentValue;
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public String getCurrentValue() {
        return currentValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "BannerMode [currentValue=" + currentValue + "]";
    }
}
