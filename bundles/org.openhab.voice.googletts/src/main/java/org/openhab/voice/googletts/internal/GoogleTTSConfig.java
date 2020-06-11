/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
     * Purge cache after configuration changes.
     */
    private Boolean purgeCache;

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

    Boolean getPurgeCache() {
        return purgeCache;
    }

    void setPurgeCache(Boolean purgeCache) {
        this.purgeCache = purgeCache;
    }

    @Override
    public String toString() {
        return "GoogleTTSConfig{" + "serviceAccountKey='" + serviceAccountKey + '\'' + ", pitch=" + pitch
                + ", speakingRate=" + speakingRate + ", volumeGainDb=" + volumeGainDb + ", purgeCache=" + purgeCache
                + '}';
    }

    String toConfigString() {
        return String.format("pitch=%f,speakingRate=%f,volumeGainDb=%f", pitch, speakingRate, volumeGainDb);
    }
}
