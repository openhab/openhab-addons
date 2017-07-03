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
 * The Class AudioSourceScreen.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class AudioSourceScreen {

    /** The screen. */
    private final String screen;

    /**
     * Instantiates a new audio source screen.
     *
     * @param screen the screen
     */
    public AudioSourceScreen(String screen) {
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
        return "AudioSourceScreen [screen=" + screen + "]";
    }
}
