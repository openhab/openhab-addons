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
 * The Class DeviceMode.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class DeviceMode {

    /** The is on. */
    private final boolean isOn;

    /**
     * Instantiates a new device mode.
     *
     * @param isOn the is on
     */
    public DeviceMode(boolean isOn) {
        this.isOn = isOn;
    }

    /**
     * Checks if is on.
     *
     * @return true, if is on
     */
    public boolean isOn() {
        return isOn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeviceMode [isOn=" + isOn + "]";
    }
}
