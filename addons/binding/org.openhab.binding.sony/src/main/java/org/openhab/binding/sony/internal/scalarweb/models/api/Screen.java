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
 * The Class Screen.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class Screen {

    /** The screen. */
    private final String screen;

    /**
     * Instantiates a new screen.
     *
     * @param screen the screen
     */
    public Screen(String screen) {
        super();
        this.screen = screen;
    }

    /**
     * Gets the screen.
     *
     * @return the screen
     */
    public String getScreen() {
        return screen;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Screen [screen=" + screen + "]";
    }
}
