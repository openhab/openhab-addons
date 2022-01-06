/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Voice service implementation.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
class GoogleTTSConfig {
    /**
     * Access to Google Cloud Platform
     */
    public @Nullable String clientId;
    public @Nullable String clientSecret;
    public @Nullable String authcode;

    /**
     * Pitch
     */
    public Double pitch = 0d;

    /**
     * Volume Gain
     */
    public Double volumeGainDb = 0d;

    /**
     * Speaking Rate
     */
    public Double speakingRate = 1d;

    /**
     * Purge cache after configuration changes.
     */
    public Boolean purgeCache = Boolean.FALSE;

    @Override
    public String toString() {
        return "GoogleTTSConfig{pitch=" + pitch + ", speakingRate=" + speakingRate + ", volumeGainDb=" + volumeGainDb
                + ", purgeCache=" + purgeCache + '}';
    }

    String toConfigString() {
        return String.format("pitch=%f,speakingRate=%f,volumeGainDb=%f", pitch, speakingRate, volumeGainDb);
    }
}
