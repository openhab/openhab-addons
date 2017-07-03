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
 * The Class PipSubScreenPosition.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class PipSubScreenPosition {

    /** The position. */
    private final String position;

    /**
     * Instantiates a new pip sub screen position.
     *
     * @param position the position
     */
    public PipSubScreenPosition(String position) {
        super();
        this.position = position;
    }

    /**
     * Gets the position.
     *
     * @return the position
     */
    public String getPosition() {
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PipSubScreenPosition [position=" + position + "]";
    }
}
