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
 * The Class LedIndicatorStatus.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class LedIndicatorStatus {

    /** The mode. */
    private final String mode;

    /** The status. */
    private final String status;

    /**
     * Instantiates a new led indicator status.
     *
     * @param mode the mode
     * @param status the status
     */
    public LedIndicatorStatus(String mode, String status) {
        super();
        this.mode = mode;
        this.status = status;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LedIndicatorStatus [mode=" + mode + ", status=" + status + "]";
    }
}
