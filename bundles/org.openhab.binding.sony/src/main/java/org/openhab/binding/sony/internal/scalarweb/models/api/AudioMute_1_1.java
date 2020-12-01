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

import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents whether the audio is muted or not and is used for serialzation
 *
 * Versions:
 * <ol>
 * <li>1.1: {"output":"string", "mute":"string"}</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class AudioMute_1_1 {

    public static final String MUTEON = "on";
    public static final String MUTEOFF = "off";

    /** The target of the mute */
    private final String output;

    /** Whether muted or not */
    private final String mute;

    /**
     * Instantiates a new audio mute.
     *
     * @param output the non-null, non-empty output
     * @param mute the status
     */
    public AudioMute_1_1(final String output, final boolean muted) {
        this(output, muted ? MUTEON : MUTEOFF);
    }

    /**
     * Instantiates a new audio mute.
     *
     * @param output the non-null, possibly empty output
     * @param mute the mute status
     */
    public AudioMute_1_1(final String output, final String mute) {
        Objects.requireNonNull(output, "output cannot be empty");
        Validate.notEmpty(mute, "mute cannot be empty");

        this.output = output;
        this.mute = mute;
    }

    /**
     * Gets the output of the mute
     *
     * @return the output of the mute
     */
    public String getOutput() {
        return output;
    }

    /**
     * Get's the status of the mute
     *
     * @return the status of the mute
     */
    public String getMute() {
        return mute;
    }

    @Override
    public String toString() {
        return "AudioMute_1_1 [output=" + output + ", mute=" + mute + "]";
    }
}
