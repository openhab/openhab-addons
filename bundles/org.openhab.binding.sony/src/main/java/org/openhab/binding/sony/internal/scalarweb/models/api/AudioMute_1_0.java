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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents whether the audio is muted or not and is used for serialzation
 *
 * Versions:
 * <ol>
 * <li>1.0: {"status":"bool"}</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class AudioMute_1_0 {

    /** The target of the mute */
    private final String target;

    /** Whether muted or not */
    private final boolean status;

    /**
     * Instantiates a new audio mute.
     *
     * @param target the non-null, possibly empty target
     * @param status the status
     */
    public AudioMute_1_0(final String target, final boolean status) {
        Objects.requireNonNull(target, "target cannot be empty");

        this.target = target;
        this.status = status;
    }

    /**
     * Checks if is muted
     *
     * @return true, if muted - false otherwise
     */
    public boolean isOn() {
        return status;
    }

    /**
     * Gets the target of the mute
     *
     * @return the target of the mute
     */
    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "AudioMute_1_0 [Target=" + getTarget() + ", Status=" + status + "]";
    }
}
