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
 * The Class AudioMute.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class AudioMute {

    /** The target. */
    private final String target;

    /** The status. */
    private final boolean status;

    /**
     * Instantiates a new audio mute.
     *
     * @param target the target
     * @param status the status
     */
    public AudioMute(String target, boolean status) {
        this.target = target;
        this.status = status;
    }

    /**
     * Checks if is on.
     *
     * @return true, if is on
     */
    public boolean isOn() {
        return status;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AudioMute [Target=" + getTarget() + ", Status=" + status + "]";
    }
}
