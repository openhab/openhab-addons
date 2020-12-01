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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The audio information class used for deserialization only
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class AudioInfo {
    /** The audio channel */
    private @Nullable String channel;

    /** The audio codec */
    private @Nullable String codec;

    /** The audio frequency */
    private @Nullable String frequency;

    /**
     * Constructor used for deserialization only
     */
    public AudioInfo() {
    }

    /**
     * Returns the audio channel
     * 
     * @return the audio channel
     */
    public @Nullable String getChannel() {
        return channel;
    }

    /**
     * Returns the audio codec
     * 
     * @return the audio codec
     */
    public @Nullable String getCodec() {
        return codec;
    }

    /**
     * Returns the audio frequency
     * 
     * @return the audio frequency
     */
    public @Nullable String getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return "AudioInfo [channel=" + channel + ", codec=" + codec + ", frequency=" + frequency + "]";
    }
}
