/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.gtts.internal;

/**
 * Voice service implementation.
 *
 * @author Gabor Bicskei - Initial contribution
 */
class GoogleTTSConfig {
    /**
     * Key file name under $userdata/gtts
     */
    private String serviceKeyFileName;

    /**
     * Pitch
     */
    private Double pitch = 0d;

    /**
     * Speaking Rate
     */
    private Double speakingRate = 1d;

    /**
     * Volume gain
     */
    private Double volumeGainDb = 0d;

    public String getServiceKeyFileName() {
        return serviceKeyFileName;
    }

    public void setServiceKeyFileName(String serviceKeyFileName) {
        this.serviceKeyFileName = serviceKeyFileName;
    }

    public Double getPitch() {
        return pitch;
    }

    public void setPitch(Double pitch) {
        this.pitch = pitch;
    }

    public Double getSpeakingRate() {
        return speakingRate;
    }

    public void setSpeakingRate(Double speakingRate) {
        this.speakingRate = speakingRate;
    }

    public Double getVolumeGainDb() {
        return volumeGainDb;
    }

    public void setVolumeGainDb(Double volumeGainDb) {
        this.volumeGainDb = volumeGainDb;
    }
}
