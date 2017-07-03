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
 * The Class AudioVolume.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class AudioVolume {

    /** The target. */
    private final String target;

    /** The volume. */
    private final String volume;

    /**
     * Instantiates a new audio volume.
     *
     * @param target the target
     * @param volume the volume
     */
    public AudioVolume(String target, int volume) {
        this.target = target;
        this.volume = String.valueOf(volume);
    }

    /**
     * Gets the volume.
     *
     * @return the volume
     */
    public int getVolume() {
        return Integer.parseInt(volume);
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
        return "AudioVolume [Target=" + target + ", Volume=" + volume + "]";
    }
}
