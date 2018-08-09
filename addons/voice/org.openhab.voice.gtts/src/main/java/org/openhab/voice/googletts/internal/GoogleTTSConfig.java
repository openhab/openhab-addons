/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.googletts.internal;

/**
 * Voice service implementation.
 *
 * @author Gabor Bicskei - Initial contribution
 */
class GoogleTTSConfig {
    /**
     * JSON key to access Google service
     */
    private String serviceAccountKey;

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

    String getServiceAccountKey() {
        return serviceAccountKey;
    }

    void setServiceAccountKey(String serviceAccountKey) {
        this.serviceAccountKey = serviceAccountKey;
    }

    Double getPitch() {
        return pitch;
    }

    void setPitch(Double pitch) {
        this.pitch = pitch;
    }

    Double getSpeakingRate() {
        return speakingRate;
    }

    void setSpeakingRate(Double speakingRate) {
        this.speakingRate = speakingRate;
    }

    Double getVolumeGainDb() {
        return volumeGainDb;
    }

    void setVolumeGainDb(Double volumeGainDb) {
        this.volumeGainDb = volumeGainDb;
    }

    @Override
    public String toString() {
        return "GoogleTTSConfig{" +
                "serviceAccountKey='" + serviceAccountKey + '\'' +
                ", pitch=" + pitch +
                ", speakingRate=" + speakingRate +
                ", volumeGainDb=" + volumeGainDb +
                '}';
    }
}
