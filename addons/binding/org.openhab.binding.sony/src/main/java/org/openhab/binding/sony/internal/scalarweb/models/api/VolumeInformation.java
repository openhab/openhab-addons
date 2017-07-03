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
 * The Class VolumeInformation.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class VolumeInformation {

    /** The target. */
    private final String target;

    /** The volume. */
    private final int volume;

    /** The mute. */
    private final boolean mute;

    /** The max volume. */
    private final int maxVolume;

    /** The min volume. */
    private final int minVolume;

    /**
     * Instantiates a new volume information.
     *
     * @param target the target
     * @param volume the volume
     * @param mute the mute
     * @param maxVolume the max volume
     * @param minVolume the min volume
     */
    public VolumeInformation(String target, int volume, boolean mute, int maxVolume, int minVolume) {
        this.target = target;
        this.volume = volume;
        this.mute = mute;
        this.maxVolume = maxVolume;
        this.minVolume = minVolume;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * Gets the volume.
     *
     * @return the volume
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Checks if is mute.
     *
     * @return true, if is mute
     */
    public boolean isMute() {
        return mute;
    }

    /**
     * Gets the max volume.
     *
     * @return the max volume
     */
    public int getMaxVolume() {
        return maxVolume;
    }

    /**
     * Gets the min volume.
     *
     * @return the min volume
     */
    public int getMinVolume() {
        return minVolume;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "VolumeInformation [target=" + target + ", volume=" + volume + ", mute=" + mute + ", maxVolume="
                + maxVolume + ", minVolume=" + minVolume + "]";
    }

}
